package org.least_res_gps.core.routing.strategies;

import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Node;

public interface RoutingStrategy {

    double MAX_MPS = 130 / 3.6;

    public double getEdgeCost(Edge edge);
    public double getHeuristic(Node current, Node target);

}
