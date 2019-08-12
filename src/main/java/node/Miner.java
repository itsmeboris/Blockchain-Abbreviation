package node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static node.Message.MESSAGE_TYPE.*;

public class Miner {
    public String name;
    private int difficulty;
    private List<Node> peers;

    private ServerSocket serverSocket;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    public boolean listening = true;

    public Miner(){
    }

    Miner(final String name, int difficulty, final List<Node> peers){
        this.name = name;
        this.difficulty = difficulty;
        this.peers = peers;
    }

    @Override
    public String toString(){
        return "Node{" +
                "name=" + name +
                ", peers number='" + Integer.toString(peers.size()) + '\'' +
//                ", previousHash='" + previousHash + '\'' +
                '}';
    }

    private Node getNode(int port){
        for(Node a: peers){
            if(a.port == port)
                return a;
        }
        return null;
    }

    public Block createBlock(int port){
        Node n = getNode(port);
        if (n == null)
            return null;
        List<Block> blockchain = n.getBlockchain();
        if (blockchain.isEmpty()) {
            return null;
        }
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        if (previousBlock == null) {
            return null;
        }
        final int index = previousBlock.id + 1;
        final Block block = new Block(index, name, "new Block", previousBlock.hash, difficulty);
        block.mineBlock(difficulty);
        System.out.println(String.format("%s created new block %s", name, block.toString()));
        broadcast(INFO_NEW_BLOCK, block, port);
        return block;
    }

    private void broadcast(Message.MESSAGE_TYPE type, final Block block, int port) {
        peers.forEach(peer -> sendMessage(type, peer.address, peer.port, port, block));
    }

    private void sendMessage(Message.MESSAGE_TYPE type, String host, int port, int myPort, Block... blocks) {
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Node n = getNode(port);
        List<Block> blockchain =  n.getBlockchain();
        try (
                final Socket peer = new Socket(host, port);
                final ObjectOutputStream out = new ObjectOutputStream(peer.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(peer.getInputStream())) {
            Object fromPeer;
            while ((fromPeer = in.readObject()) != null) {
                if (fromPeer instanceof Message) {
                    final Message msg = (Message) fromPeer;
                    System.out.println(String.format("%d received: %s", myPort, msg.toString()));
                    if (READY == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withType(type)
                                .withReceiver(port)
                                .withSender(myPort)
                                .withBlocks(Arrays.asList(blocks)).build());
                    } else if (RSP_ALL_BLOCKS == msg.type) {
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
