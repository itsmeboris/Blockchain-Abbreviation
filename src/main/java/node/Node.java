package node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static node.Message.MESSAGE_TYPE.*;

public class Node {
    public String name;
    private int difficulty;
    public String address;
    public int port;
    private List<Node> peers;
    private List<Block> blockchain = new ArrayList<>();
    private List<Block> lastBlock = new ArrayList<>();
    private int differ = 0;

    private ServerSocket serverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    public boolean listening = true;

    public Node(){
    }

    Node(final String name, int difficulty, final String address, final int port, final Block genesis, final List<Node> peers){
        this.name = name;
        this.difficulty = difficulty;
        this.address = address;
        this.port = port;
        this.peers = peers;
        addBlock(genesis);
    }

    @Override
    public String toString(){
        return "Node{" +
                "name=" + name +
                ", address=" + address +
                ", port=" + Integer.toString(port) +
                ", peers number='" + Integer.toString(peers.size()) + '\'' +
//                ", previousHash='" + previousHash + '\'' +
                '}';
    }

    public int getPort(){
        return this.port;
    }

    public String getName() { return this.name; }

    public List<Block> getBlockchain(){
        return blockchain;
    }

    public Block createBlock(){
        Block previousBlock;
        int index;
        Block block;
        synchronized (blockchain) {
            if (blockchain.isEmpty()) {
                return null;
            }
            previousBlock = getLatestBlock();
            if (previousBlock == null) {
                return null;
            }
            index = previousBlock.id + 1;
            block = new Block(index, name, "new Block", previousBlock.hash, difficulty);
        }
        //block.mineBlock(difficulty);
        System.out.println(String.format("%s created new block %s", name, block.toString()));
        broadcast(INFO_NEW_BLOCK, Arrays.asList(block));
        return block;
    }

    void addBlock(Block block) {
        if (isBlockValid(block)) {
            blockchain.add(block);
        }
    }

    private boolean isBlockValid(final Block block) {
        if(!block.previousHash.equals("GenesisBlock")) {
            final Block latestBlock = getLatestBlock();
            if (latestBlock == null) {
                return false;
            }
            final int expected = latestBlock.id + 1;
            if (block.id != expected) {
                System.out.println(String.format("Invalid index. Expected: %s Actual: %s", expected, block.id));
                return false;
            }
        }
        synchronized (block) {
            block.addConfirmation(this);
        }
        return true;
    }

    public void abbreviate() {
        System.out.println("node: " + name + " is abbreviating");
        this.differ = 0;
        lastBlock.clear();
        for(Node n: peers){
            if(n.name.equals(name))
                continue;
            if(!(n.getBlockchain().get(0)).equals(blockchain.get(0))){
                System.out.println("node genesis");
                System.out.println(n.getBlockchain().get(0));
                System.out.println("this genesis");
                System.out.println(blockchain.get(0));
                this.differ++;
            }
            lastBlock.add(n.getLatestBlock());
        }
        Collections.sort(lastBlock);
        Block median = lastBlock.get((int)Math.floor(lastBlock.size()/2));
        if(differ > peers.size()/2 || median.id - getLatestBlock().id > 6){
            //System.out.println(differ);
            broadcast(REQ_ALL_BLOCKS, null);
        }
        else{
            int n = peers.size();
            int diff = 0;
            int j = 0;
            List<Block> remove = new ArrayList<>();
            for(int i = blockchain.size() - 1; i >= 0; i--){
                Block b = blockchain.get(i);
                if(b.getConfirmations().size() > (n/2)){
                    for(j = 0; j < i; j++) {
                        diff+= blockchain.get(j).difficulty;
                        remove.add(blockchain.get(j));
                    }
                    b.difficulty = diff;
                    b.previousHash = "GenesisBlock";
                    b.hash = b.calculateHash();
                    synchronized (this) {
                        blockchain.removeAll(remove);
                    }
                    for(i = 1; i < blockchain.size(); i++){
                        blockchain.get(i).hash = blockchain.get(i).calculateHash();
                    }
                    broadcast(ABBREVIATE, blockchain);
                    break;
                }
            }
        }
    }

    private Node findNodeByPort(int port){
        for(Node n: peers){
            if(n.getPort() == port)
                return n;
        }
        return null;
    }

    public void addblocks() {
        int num = (int)Math.floor((Math.random() * 5) + 1);
        int index = getLatestBlock().id;
        for(int i = 0; i < num; i++){
            Block b = new Block(index + 1 + i, "USERBLOCK", "USERBLOCK", getLatestBlock().hash, difficulty);
            b.mineBlock(difficulty);
            addBlock(b);
        }
    }

    public Block getLatestBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }
        return blockchain.get(blockchain.size() - 1);
    }

    void startHost() {
        executor.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(String.format("Server %s started", serverSocket.getLocalPort()));
                listening = true;
                while (listening) {
                    final NodeServerThread thread = new NodeServerThread(Node.this, serverSocket.accept());
                    thread.start();
                }
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Could not listen to port " + port);
            }
        });
        broadcast(REQ_ALL_BLOCKS, null);
    }

    public void signBlock(Block block, Node nodeFrom, Node toSign){
        for(Block b: nodeFrom.getBlockchain()){
            if(b.equals(block)) {
                if (!b.getConfirmations().contains(toSign.getName())) {
                    b.addConfirmation(toSign);
                }
                break;
            }
        }
    }

    void stopHost() {
        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Message.MESSAGE_TYPE type, final List<Block> blocks) {
        synchronized (peers) {
            peers.forEach(peer -> sendMessage(type, peer.address, peer.port, blocks));
        }
    }

    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, List<Block> blocks) {
        try {
            Thread.sleep(300);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        try (
                final Socket peer = new Socket(host, port);
                final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
            Object fromPeer;
            while ((fromPeer = in.readObject()) != null) {
                if (fromPeer instanceof Message) {
                    final Message msg = (Message) fromPeer;
                    System.out.println(String.format("%d received: %s", this.port, msg.toString()));
                    if (READY == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withType(type)
                                .withReceiver(port)
                                .withSender(this.port)
                                .withBlocks(blocks).build());
                    } else if (RSP_ALL_BLOCKS == msg.type) {
                        synchronized (blockchain) {
                            System.out.println(this.blockchain.get(0).difficulty);
                            System.out.println(msg.blocks.get(0).difficulty);
                            if (!msg.blocks.isEmpty()
                                    && this.blockchain.size() <= msg.blocks.size()){
                                blockchain = new ArrayList<>();
                                for(int i = 0; i < msg.blocks.size(); i++) {
                                    Block b = msg.blocks.get(i);
                                    if (isBlockValid(b)) {
                                        int sport = msg.sender;
                                        Node n = findNodeByPort(sport);
                                        addBlock(b);
                                        signBlock(b,n, this);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    else if (ABBREVIATE == msg.type) {
                        synchronized (blockchain) {
                            if (!msg.blocks.isEmpty()
                                    && this.blockchain.get(0).difficulty < msg.blocks.get(0).difficulty) {
                                blockchain = new ArrayList<>();
                                for(Block b: msg.blocks) {
                                    if (isBlockValid(b))
                                        addBlock(b);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unknown host %s %d", host, port));
        } catch (IOException e) {
            System.err.println(String.format("%s couldn't get I/O for the connection to %s. Retrying...%n", port, port));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
