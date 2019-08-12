package node;

import java.util.ArrayList;
import java.util.List;

public class NodeManager {

    private int difficulty;
    private List<Node> nodes;
    private List<Miner> miners;
    private static Block root;

    int numOfMiners = 5;

    public NodeManager(){
        difficulty = 1;
        nodes = new ArrayList<>();
        miners = new ArrayList<>();
        root = new Block(0, "ROOT", "GenesisBlock", "GenesisBlock", 0);
        root.mineBlock(difficulty);
        for(int i  = 0; i < numOfMiners; i++){
            miners.add(new Miner("Miner" + (i+1), difficulty, nodes));
        }
        Node n = addNode("ROOT", 3000);
        System.out.println("-------------------------------\nGenesis Block Created\n-------------------------------");
    }

    public Node addNode(String name, int port) {
        Node a = new Node(name, difficulty, "localhost", port, root, nodes);
        a.startHost();
        nodes.add(a);
        return a;
    }

    public Node getNode(String name) {
        for (Node a : nodes) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public Miner getMiner(String name) {
        for (Miner a : miners) {
            if (a.name.equals(name)) {
                return a;
            }
        }
        return null;
    }

    public List<Node> getAllNodes() {
        return nodes;
    }

    public void deleteNode(String name) {
        final Node a = getNode(name);
        if (a != null) {
            a.stopHost();
            nodes.remove(a);
        }
    }

    public void abbreviate(String name){
        final Node node = getNode(name);
        if (node != null){
            node.abbreviate();
        }
    }

    public void addblocks(String name){
        final Node node = getNode(name);
        if (node != null){
            node.addblocks();
        }
    }

    public List<Block> getNodeBlockchain(String name) {
        final Node node = getNode(name);
        if (node != null) {
            return node.getBlockchain();
        }
        return null;
    }

    public void deleteAllNodes() {
        for (Node a : nodes) {
            a.stopHost();
        }
        nodes.clear();
    }

    public Block createBlock(final String name) {
        final Node node = getNode(name);
        if (node != null) {
            return node.createBlock();
        }
        return null;
    }

    public Block mineBlock(final String name) {
        final Miner miner = getMiner(name);
        if (miner != null) {
            int index = (int)(Math.random() * nodes.size());
            return miner.createBlock(nodes.get(index).getPort());
        }
        return null;
    }
}
