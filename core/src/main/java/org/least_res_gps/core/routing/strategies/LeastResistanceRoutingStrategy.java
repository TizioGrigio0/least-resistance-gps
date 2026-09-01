package org.least_res_gps.core.routing.strategies;

import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.graph.TrafficObstacle;
import org.least_res_gps.core.util.RoadUtil;

import java.util.List;

public class LeastResistanceRoutingStrategy implements RoutingStrategy {

    @Override
    public double getEdgeCost(Edge edge) {

        // Get the base time to pass the way (at max speed)
        double baseTime = edge.travelTimeSeconds();

        // Calculate the obstacle penalty
        List<TrafficObstacle> obstacleList = edge.destination().getObstacles();
        double obstaclePenalty = 0.0;
        for (TrafficObstacle obstacle : obstacleList) obstaclePenalty += obstacle.getPenaltySeconds();

        // Calculate the road penalty
        double materialMultiplier = edge.roadMaterial().getPenaltyMultiplier();

        // Calculate and return the final cost
        return (baseTime * materialMultiplier) + obstaclePenalty;
    }

    @Override
    public double getHeuristic(Node current, Node target) {
        double distance = RoadUtil.calculateDistance(current.getLat(), current.getLon(), target.getLat(), target.getLon());
        return distance/MAX_MPS;
    }
}
