package org.least_res_gps.core.routing;

import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.routing.strategies.RoutingStrategy;

import java.util.*;


public class AStarRouter {

    private final RoutingStrategy routingStrategy;

    public AStarRouter(RoutingStrategy routingStrategy) {
        this.routingStrategy = routingStrategy;
    }

    public Optional<List<Node>> findRoute(Node beginning, Node destination) {

        // Local collections
        PriorityQueue<AStarNode> queue = new PriorityQueue<>();
        Map<Long, Double> bestRouteTime = new HashMap<>();

        // Initialize the start of the path
        AStarNode aBeginning = new AStarNode(beginning, null, 0.0, routingStrategy.getHeuristic(beginning, destination));
        bestRouteTime.put(beginning.getId(), aBeginning.getgTime());
        queue.add(aBeginning);

        // Keep searching until we find the destination, or we searched everything
        AStarNode aFinalNode = null;
        while (!queue.isEmpty()) {
            // Get the node
            AStarNode aNode = queue.poll();

            // If a better route to this node was already found in a precedent check, then skip this
            if (aNode.getgTime() > bestRouteTime.getOrDefault(aNode.getNode().getId(), Double.MAX_VALUE)) {
                continue;
            }

            // Check if we reached the destination
            if (aNode.getNode().equals(destination)) {
                aFinalNode = aNode;
                break;
            }

            // For each connection of the current node, enqueue that connection
            for(Edge e : aNode.getNode().getEdgeList()) {
                double g = aNode.getgTime() + routingStrategy.getEdgeCost(e);
                // If there is a better path to the destination node, skip this
                if (g < bestRouteTime.getOrDefault(e.destination().getId(), Double.MAX_VALUE)) {
                    bestRouteTime.put(e.destination().getId(), g);
                    double h = routingStrategy.getHeuristic(e.destination(), destination);
                    AStarNode newANode = new AStarNode(e.destination(), aNode, g, g+h);
                    queue.add(newANode);
                }
            }
        } // while closure

        // If a result was found
        if (aFinalNode != null) return Optional.of(reconstructPath(aFinalNode));
        // If no path was found
        else return Optional.empty();
    }

    private List<Node> reconstructPath(AStarNode destination) {

        List<Node> path = new ArrayList<>();
        AStarNode current = destination;

        // Rebuild the path from finish to start
        while (current != null) {
            path.add(current.getNode());
            current = current.getParent();
        }

        // Return the reversed version of the path (from start to finish)
        Collections.reverse(path);
        return path;
    }

}
