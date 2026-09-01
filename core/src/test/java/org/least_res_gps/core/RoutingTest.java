package org.least_res_gps.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Graph;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.parser.OsmParser;
import org.least_res_gps.core.routing.AStarRouter;
import org.least_res_gps.core.routing.strategies.FastestRoutingStrategy;
import org.least_res_gps.core.routing.strategies.LeastResistanceRoutingStrategy;
import org.least_res_gps.core.routing.strategies.ShortestRoutingStrategy;
import org.least_res_gps.core.routing.strategies.RoutingStrategy;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Routing Tests")
class RoutingTest {

    private static final long START_NODE_ID = 889555477L;
    private static final String TARGET_ROAD_NAME = "Strada per Maranza";

    private Graph graph;

    @BeforeEach
    void setUp() throws Exception {
        File osmFile = new File("src/test/resources/trento.osm");
        graph = OsmParser.parse(osmFile);
    }

    @Test
    @DisplayName("Should print the route ways in travel order")
    void shouldPrintRouteWaysInOrder() {
        Node startNode = graph.getNode(START_NODE_ID);
        assertNotNull(startNode, "Start node should exist in the map");
        assertFalse(startNode.getEdgeList().isEmpty(), "Start node should have outgoing roads");

        Node destinationNode = findFarthestReachableNode(startNode);
        assertNotNull(destinationNode, "A destination node should be found");
        assertNotEquals(startNode.getId(), destinationNode.getId(), "Destination should differ from start");

        AStarRouter router = new AStarRouter(new LeastResistanceRoutingStrategy());
        Optional<List<Node>> route = router.findRoute(startNode, destinationNode);

        assertTrue(route.isPresent(), "Route should exist between the selected nodes");

        List<Node> path = route.orElseThrow();
        assertTrue(path.size() >= 2, "Route should contain at least one edge");

        double totalSeconds = 0.0;

        System.out.printf("%nRoute from %d to %d:%n", startNode.getId(), destinationNode.getId());
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            Edge edge = findEdge(current, next);
            assertNotNull(edge, "Each consecutive pair in the path should be connected");

            double segmentSeconds = edge.travelTimeSeconds();
            totalSeconds += segmentSeconds;

            System.out.printf(
                    "%d. %s | %d -> %d | segment %.2f s | total %.2f s%n",
                    i + 1,
                    edge.name(),
                    current.getId(),
                    next.getId(),
                    segmentSeconds,
                    totalSeconds
            );
        }

        System.out.printf("Total travel time: %.2f s%n", totalSeconds);
        assertTrue(totalSeconds > 0.0, "Total travel time should be positive");
    }

    @Test
    @DisplayName("Should compare all routing strategies on Strada per Maranza")
    void shouldCompareAllRoutingStrategies() {
        Node startNode = graph.getNode(START_NODE_ID);
        assertNotNull(startNode, "Start node should exist in the map");

        Node destinationNode = findFirstReachableNodeOnRoad(startNode, TARGET_ROAD_NAME);
        assertNotNull(destinationNode, "Destination node on the target road should be reachable");
        assertNotEquals(startNode.getId(), destinationNode.getId(), "Destination should differ from start");

        List<StrategyCase> strategies = List.of(
                new StrategyCase("Fastest", new FastestRoutingStrategy()),
                new StrategyCase("Shortest", new ShortestRoutingStrategy()),
                new StrategyCase("LeastResistance", new LeastResistanceRoutingStrategy())
        );

        RouteMetrics fastestMetrics = null;
        List<Long> fastestRouteIds = null;

        System.out.printf("%nComparing routes from %d to %d (%s):%n", startNode.getId(), destinationNode.getId(), TARGET_ROAD_NAME);
        for (StrategyCase strategyCase : strategies) {
            AStarRouter router = new AStarRouter(strategyCase.strategy());
            Optional<List<Node>> route = router.findRoute(startNode, destinationNode);

            assertTrue(route.isPresent(), strategyCase.name() + " should find a route");

            List<Node> path = route.orElseThrow();
            RouteMetrics metrics = analyzeRoute(path);

            System.out.printf("%n=== %s ===%n", strategyCase.name());
            printRoute(path, metrics);

            if ("Fastest".equals(strategyCase.name())) {
                fastestMetrics = metrics;
                fastestRouteIds = path.stream().map(Node::getId).toList();
            }

            if (fastestMetrics != null) {
                double actualTimeDelta = metrics.baseTravelSeconds() - fastestMetrics.baseTravelSeconds();
                double resistanceDelta = metrics.leastResistanceSeconds() - fastestMetrics.leastResistanceSeconds();
                double distanceDelta = metrics.distanceMeters() - fastestMetrics.distanceMeters();
                System.out.printf("Delta vs fastest path actual travel time: %.2f s%n", actualTimeDelta);
                System.out.printf("Delta vs fastest path resistance score: %.2f s%n", resistanceDelta);
                System.out.printf("Delta vs fastest path distance: %.2f m%n", distanceDelta);
                System.out.printf("Same route as fastest: %s%n", fastestRouteIds.equals(path.stream().map(Node::getId).toList()));
            }
        }
    }

    private Node findFarthestReachableNode(Node startNode) {
        Queue<Node> queue = new ArrayDeque<>();
        Map<Long, Integer> depthByNodeId = new HashMap<>();
        Node farthestNode = startNode;
        int farthestDepth = 0;

        queue.add(startNode);
        depthByNodeId.put(startNode.getId(), 0);

        while (!queue.isEmpty()) {
            Node current = queue.remove();
            int currentDepth = depthByNodeId.get(current.getId());

            if (currentDepth > farthestDepth || (currentDepth == farthestDepth && current.getId() < farthestNode.getId())) {
                farthestNode = current;
                farthestDepth = currentDepth;
            }

            List<Edge> edges = new ArrayList<>(current.getEdgeList());
            edges.sort(Comparator.comparingLong(edge -> edge.destination().getId()));

            for (Edge edge : edges) {
                Node destination = edge.destination();
                if (depthByNodeId.containsKey(destination.getId())) {
                    continue;
                }

                depthByNodeId.put(destination.getId(), currentDepth + 1);
                queue.add(destination);
            }
        }

        return farthestNode;
    }

    private Edge findEdge(Node current, Node next) {
        for (Edge edge : current.getEdgeList()) {
            if (edge.destination().getId() == next.getId()) {
                return edge;
            }
        }
        return null;
    }

    private Node findFirstReachableNodeOnRoad(Node startNode, String roadName) {
        Deque<Node> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();

        queue.add(startNode);
        visited.add(startNode.getId());

        while (!queue.isEmpty()) {
            Node current = queue.removeFirst();

            List<Edge> edges = new ArrayList<>(current.getEdgeList());
            edges.sort(Comparator.comparingLong(edge -> edge.destination().getId()));

            for (Edge edge : edges) {
                if (roadName.equalsIgnoreCase(edge.name())) {
                    return edge.destination();
                }

                Node destination = edge.destination();
                if (visited.add(destination.getId())) {
                    queue.addLast(destination);
                }
            }
        }

        return null;
    }

    private RouteMetrics analyzeRoute(List<Node> path) {
        double distanceMeters = 0.0;
        double baseTravelSeconds = 0.0;
        double obstaclePenaltySeconds = 0.0;
        double roadConditionPenaltySeconds = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            Edge edge = findEdge(current, next);
            assertNotNull(edge, "Each consecutive pair in the path should be connected");

            distanceMeters += edge.distanceMeters();
            baseTravelSeconds += edge.travelTimeSeconds();
            roadConditionPenaltySeconds += edge.travelTimeSeconds() * (edge.roadMaterial().getPenaltyMultiplier() - 1.0);

            for (var obstacle : next.getObstacles()) {
                obstaclePenaltySeconds += obstacle.getPenaltySeconds();
            }
        }

        return new RouteMetrics(distanceMeters, baseTravelSeconds, obstaclePenaltySeconds, roadConditionPenaltySeconds);
    }

    private void printRoute(List<Node> path, RouteMetrics metrics) {
        List<RouteSegment> compressedSegments = compressRoute(path);
        double cumulativeSeconds = 0.0;

        for (int i = 0; i < compressedSegments.size(); i++) {
            RouteSegment segment = compressedSegments.get(i);
            cumulativeSeconds += segment.baseTravelSeconds() + segment.obstaclePenaltySeconds() + segment.roadConditionPenaltySeconds();

            System.out.printf(
                    "%d. %s | %s -> %s | %d edges | dist %.1f m | base %.2f s | obstacles %.2f s | road %.2f s | total %.2f s%n",
                    i + 1,
                    segment.name(),
                    segment.startNodeId(),
                    segment.endNodeId(),
                    segment.edgeCount(),
                    segment.distanceMeters(),
                    segment.baseTravelSeconds(),
                    segment.obstaclePenaltySeconds(),
                    segment.roadConditionPenaltySeconds(),
                    cumulativeSeconds
            );
        }

        printObstacleRecap(path);

        System.out.printf("Total distance: %.2f m%n", metrics.distanceMeters());
        System.out.printf("Base travel time: %.2f s%n", metrics.baseTravelSeconds());
        System.out.printf("Obstacle time loss: %.2f s%n", metrics.obstaclePenaltySeconds());
        System.out.printf("Road condition time loss: %.2f s%n", metrics.roadConditionPenaltySeconds());
        System.out.printf("Least-resistance equivalent: %.2f s%n", metrics.leastResistanceSeconds());
        System.out.printf("Distance lower bound at 130 km/h: %.2f s%n", metrics.distanceLowerBoundSeconds());
    }

    private void printObstacleRecap(List<Node> path) {
        Map<String, Integer> obstacleCounts = new TreeMap<>();
        Map<String, Double> obstacleSeconds = new TreeMap<>();

        for (int i = 1; i < path.size(); i++) {
            Node node = path.get(i);
            for (var obstacle : node.getObstacles()) {
                String name = obstacle.name();
                obstacleCounts.merge(name, 1, Integer::sum);
                obstacleSeconds.merge(name, obstacle.getPenaltySeconds(), Double::sum);
            }
        }

        System.out.printf("Obstacle recap:%n");
        if (obstacleCounts.isEmpty()) {
            System.out.println("  (none)");
            return;
        }

        for (String name : obstacleCounts.keySet()) {
            System.out.printf(
                    "  - %s x%d | %.2f s%n",
                    name,
                    obstacleCounts.get(name),
                    obstacleSeconds.get(name)
            );
        }
    }

    private List<RouteSegment> compressRoute(List<Node> path) {
        List<RouteSegment> segments = new ArrayList<>();

        RouteSegmentBuilder current = null;
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            Edge edge = findEdge(from, to);
            assertNotNull(edge, "Each consecutive pair in the path should be connected");

            if (current == null || !Objects.equals(current.name, edge.name())) {
                if (current != null) {
                    segments.add(current.build());
                }
                current = new RouteSegmentBuilder(edge.name(), from.getId());
            }

            current.addEdge(edge, to);
        }

        if (current != null) {
            segments.add(current.build());
        }

        return segments;
    }

    private record StrategyCase(String name, RoutingStrategy strategy) {}

    private record RouteSegment(
            String name,
            long startNodeId,
            long endNodeId,
            int edgeCount,
            double distanceMeters,
            double baseTravelSeconds,
            double obstaclePenaltySeconds,
            double roadConditionPenaltySeconds
    ) {}

    private static final class RouteSegmentBuilder {
        private final String name;
        private final long startNodeId;
        private long endNodeId;
        private int edgeCount;
        private double distanceMeters;
        private double baseTravelSeconds;
        private double obstaclePenaltySeconds;
        private double roadConditionPenaltySeconds;

        private RouteSegmentBuilder(String name, long startNodeId) {
            this.name = name;
            this.startNodeId = startNodeId;
        }

        private void addEdge(Edge edge, Node endNode) {
            edgeCount++;
            endNodeId = endNode.getId();
            distanceMeters += edge.distanceMeters();
            baseTravelSeconds += edge.travelTimeSeconds();
            roadConditionPenaltySeconds += edge.travelTimeSeconds() * (edge.roadMaterial().getPenaltyMultiplier() - 1.0);
            for (var obstacle : endNode.getObstacles()) {
                obstaclePenaltySeconds += obstacle.getPenaltySeconds();
            }
        }

        private RouteSegment build() {
            return new RouteSegment(
                    name,
                    startNodeId,
                    endNodeId,
                    edgeCount,
                    distanceMeters,
                    baseTravelSeconds,
                    obstaclePenaltySeconds,
                    roadConditionPenaltySeconds
            );
        }
    }

    private record RouteMetrics(
            double distanceMeters,
            double baseTravelSeconds,
            double obstaclePenaltySeconds,
            double roadConditionPenaltySeconds
    ) {
        double leastResistanceSeconds() {
            return baseTravelSeconds + obstaclePenaltySeconds + roadConditionPenaltySeconds;
        }

        double distanceLowerBoundSeconds() {
            return distanceMeters / RoutingStrategy.MAX_MPS;
        }
    }
}
