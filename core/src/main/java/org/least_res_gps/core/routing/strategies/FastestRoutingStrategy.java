package org.least_res_gps.core.routing.strategies;

import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.util.RoadUtil;

public class FastestRoutingStrategy implements RoutingStrategy {

    @Override
    public double getEdgeCost(Edge edge) {
        return edge.travelTimeSeconds();
    }

    @Override
    public double getHeuristic(Node current, Node target) {
        double distance = RoadUtil.calculateDistance(current.getLat(), current.getLon(), target.getLat(), target.getLon());
        return distance/MAX_MPS;
    }
}
