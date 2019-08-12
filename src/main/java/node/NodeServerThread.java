package node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static node.Message.MESSAGE_TYPE.*;

public class NodeServerThread extends Thread{

    private final Node node;
    private Socket client;

    public NodeServerThread(final Node node, final Socket client){
        super(node.name + System.currentTimeMillis());
        this.node = node;
        this.client = client;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            Message message = new Message.MessageBuilder().withSender(node.getPort()).withType(READY).build();
            out.writeObject(message);
            Object fromClient;
            while ((fromClient = in.readObject()) != null) {
                if (fromClient instanceof Message) {
                    final Message msg = (Message) fromClient;
                    System.out.println(String.format("%d received: %s", node.getPort(), fromClient.toString()));
                    if (INFO_NEW_BLOCK == msg.type) {
                        if (msg.blocks.isEmpty() || msg.blocks.size() > 1) {
                            System.err.println("Invalid block received: " + msg.blocks);
                        }
                        synchronized (node) {
                            node.addBlock(msg.blocks.get(0));
                        }
                        break;
                    } else if (REQ_ALL_BLOCKS == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withSender(node.getPort())
                                .withType(RSP_ALL_BLOCKS)
                                .withBlocks(node.getBlockchain())
                                .build());
                        break;
                    }
                    else if (ABBREVIATE == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withSender(client.getPort())
                                .withType(ABBREVIATE)
                                .withBlocks(msg.blocks)
                                .build());
                        break;
                    }
                }
            }
            client.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
