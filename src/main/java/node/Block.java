package node;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Block implements Serializable, Comparable {
    private static final long serialVersionUID = 1L;

    public int id;
    public int difficulty;
    public String Miner;
    public String hash;
    public int nonce;
    public String previousHash;
    public String data; //our data will be a simple message.
    public Long timeStamp; //as number of milliseconds since 1/1/1970.
    private List<String> confirmations;

    public Block() {
    }

    public Block(Block b) {
        this.nonce = b.nonce;
        this.id = b.id;
        this.Miner = b.Miner;
        this.data = b.data;
        this.previousHash = b.previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.difficulty = b.difficulty;
        this.hash = calculateHash();
        this.confirmations = new ArrayList<>(b.confirmations);
    }

    //Block Constructor.
    public Block(String data,String previousHash, int difficulty) {
        this.nonce = 0;
        this.id = 0;
        this.Miner = "";
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.difficulty = difficulty;
        this.hash = calculateHash();
        this.confirmations = new ArrayList<>();
    }

    public Block(int id, String minerId, String data,String previousHash, int difficulty) {
        this.nonce = 0;
        this.id = id;
        this.Miner = minerId;
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.difficulty = difficulty;
        this.hash = calculateHash();
        this.confirmations = new ArrayList<>();
    }

    public String calculateHash() {
        String calculateHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Miner +
                        Integer.toString(nonce) +
                        data
        );
        return calculateHash;
    }

    public List<String> getConfirmations(){
        return confirmations;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + timeStamp.hashCode();
        result = 31 * result + hash.hashCode();
        result = 31 * result + previousHash.hashCode();
        result = 31 * result + Miner.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o){
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        final Block block = (Block) o;
        return id == block.id
                && Miner.equals(block.Miner)
                && hash.equals(block.hash)
                && nonce == block.nonce
                && previousHash.equals(block.previousHash);
//                && data.equals(block.data)
//                && difficulty == block.difficulty
//                && timeStamp == block.timeStamp;
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
    }

    public void addConfirmation(Node n){
        if (!Objects.equals(calculateHash(), hash)) {
           mineBlock(difficulty);
        }
        if(!confirmations.contains(n.getName())) {
            confirmations.add(n.getName());
            System.out.println("Node: " + n.name + " is confirmed");
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + Integer.toString(id) +
                ", timestamp=" + timeStamp +
                ", creator=" + Miner +
                ", difficulty=" + Integer.toString(difficulty) + '\'' +
//                ", previousHash='" + previousHash + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        Block b  = (Block)o;
        return this.id - b.id;
    }
}