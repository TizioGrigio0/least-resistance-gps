package org.least_res_gps.core.graph;

import java.util.HashMap;
import java.util.Map;

public class Graph {

    private Map<Long, Node> nodeMap = new HashMap<>();

    public void addNode(Node node) {
        nodeMap.put(node.getId(), node);
    }

    public Node getNode(long id) {
        return nodeMap.get(id);
    }

    // For testing
    public int getNodeCount() {
        return nodeMap.size();
    }

    // For testing
    public int getTotalEdgeCount() {
        int total = 0;
        for (Node node : nodeMap.values()) {
            total += node.getEdgeList().size();
        }
        return total;
    }
}
