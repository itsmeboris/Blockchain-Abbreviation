package web;

import node.Node;
import node.NodeManager;
import node.Block;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/node")
public class NodeController {

    private static NodeManager nodeManager = new NodeManager();

    @RequestMapping(method = GET)
    public Node getNode(@RequestParam("name") String name) {
        return nodeManager.getNode(name);
    }

    @RequestMapping(method = DELETE)
    public void deleteNode(@RequestParam("name") String name) {
        nodeManager.deleteNode(name);
    }

    @RequestMapping(method = POST, params = {"name", "port", "rogue"})
    public Node addNode(@RequestParam("name") String name, @RequestParam("port") int port) {
        return nodeManager.addNode(name, port);
    }

    @RequestMapping(path = "all", method = GET)
    public List<Node> getAllNodes() {
        return nodeManager.getAllNodes();
    }

    @RequestMapping(path = "all", method = DELETE)
    public void deleteAllNodes() {
        nodeManager.deleteAllNodes();
    }

    @RequestMapping(path = "miner", method = POST)
    public Block mineBlock(@RequestParam(value = "miner") final String name) {
        System.out.println("miner block");
        return nodeManager.mineBlock(name);
    }
    @RequestMapping(path = "abbreviate", method = POST)
    public void abbreviate(@RequestParam(value = "node") final String name) {
        nodeManager.abbreviate(name);
    }
    @RequestMapping(path = "addblocks", method = POST)
    public void addblocks(@RequestParam(value = "node") final String name) {
        nodeManager.addblocks(name);
    }
    @RequestMapping(method = POST, path = "mine")
    public Block createBlock(@RequestParam(value = "node") final String name) {
        return nodeManager.createBlock(name);
    }
}