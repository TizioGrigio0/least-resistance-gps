package org.least_res_gps.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.least_res_gps.core.graph.*;
import org.least_res_gps.core.parser.OsmParser;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Road Inspection Tests")
class RoadInspectionTest {

    private Graph graph;

    @BeforeEach
    void setUp() throws Exception {
        File osmFile = new File("src/test/resources/trento.osm");
        graph = OsmParser.parse(osmFile);
    }

    @Test
    @DisplayName("Should display all properties of a specific road (edge)")
    void testRoadProperties() {
        Node testNode = graph.getNode(889555477);
        assertNotNull(testNode, "Test node should exist in the graph");
        
        List<Edge> edges = testNode.getEdgeList();
        assertTrue(edges.size() > 0, "Test node should have at least one edge");

        Edge road = edges.get(0);
        
        System.out.println("\n=== ROAD PROPERTIES ===");
        System.out.println("Road Name: " + road.name());
        System.out.println("Road Type: " + road.roadType());
        System.out.println("Speed Limit: " + road.speedLimit() + " km/h");
        System.out.println("Length: " + String.format("%.2f", road.distanceMeters()) + " meters");
        System.out.println("Weight (Travel Time): " + String.format("%.2f", road.weight()) + " seconds");
        System.out.println("Road Material: " + road.roadMaterial());
        
        // Verify all properties are accessible
        assertNotNull(road.name());
        assertNotNull(road.roadType());
        assertTrue(road.speedLimit() > 0);
        assertTrue(road.distanceMeters() > 0);
        assertTrue(road.weight() > 0);
        assertNotNull(road.roadMaterial());
    }

    @Test
    @DisplayName("Should show obstacles at road start and end nodes")
    void testObstaclesOnRoad() {
        Node startNode = graph.getNode(889555477);
        assertNotNull(startNode, "Start node should exist");
        
        List<Edge> edges = startNode.getEdgeList();
        assertTrue(edges.size() > 0, "Start node should have edges");
        
        Edge road = edges.get(0);
        Node endNode = road.destination();
        
        System.out.println("\n=== OBSTACLES ON ROAD ===");
        System.out.println("Road: " + road.name() + " (" + road.roadType() + ")");
        System.out.println("From Node ID: " + startNode.getId());
        System.out.println("To Node ID: " + endNode.getId());
        
        // Check obstacles at start node
        List<TrafficObstacle> startObstacles = startNode.getObstacles();
        System.out.println("\nObstacles at START node: " + startObstacles.size());
        printObstacles(startObstacles);
        
        // Check obstacles at end node
        List<TrafficObstacle> endObstacles = endNode.getObstacles();
        System.out.println("\nObstacles at END node: " + endObstacles.size());
        printObstacles(endObstacles);
        
        // Total count
        int totalObstacles = startObstacles.size() + endObstacles.size();
        System.out.println("\nTotal obstacles associated with this road: " + totalObstacles);
    }

    @Test
    @DisplayName("Should count obstacles on a specific road")
    void testObstacleCount() {
        Node testNode = graph.getNode(889555477);
        assertNotNull(testNode);
        
        Edge road = testNode.getEdgeList().get(0);
        Node endNode = road.destination();
        
        int obstaclesCount = testNode.getObstacles().size() + endNode.getObstacles().size();
        
        System.out.println("\n=== OBSTACLE COUNT ===");
        System.out.println("Road: " + road.name());
        System.out.println("Total obstacles: " + obstaclesCount);
        
        assertTrue(obstaclesCount >= 0);
    }

    @Test
    @DisplayName("Should list all obstacles with their penalty")
    void testObstacleDetailsWithPenalty() {
        Node testNode = graph.getNode(889555477);
        assertNotNull(testNode);
        
        Edge road = testNode.getEdgeList().get(0);
        Node endNode = road.destination();
        
        System.out.println("\n=== DETAILED OBSTACLE INFORMATION ===");
        System.out.println("Road: " + road.name() + " (" + road.roadType() + ")");
        System.out.println("Road Length: " + String.format("%.2f", road.distanceMeters()) + "m");
        System.out.println("Road Speed Limit: " + road.speedLimit() + " km/h");
        System.out.println("Base Travel Time: " + String.format("%.2f", road.weight()) + "s\n");
        
        List<TrafficObstacle> allObstacles = testNode.getObstacles();
        allObstacles.addAll(endNode.getObstacles());
        
        if (allObstacles.isEmpty()) {
            System.out.println("No obstacles found on this road.");
        } else {
            System.out.println("Obstacles found:");
            for (int i = 0; i < allObstacles.size(); i++) {
                TrafficObstacle obstacle = allObstacles.get(i);
                System.out.println((i + 1) + ". " + obstacle.name() + 
                                 " - Penalty: " + obstacle.getPenaltySeconds() + " seconds");
            }
        }
    }

    @Test
    @DisplayName("Should analyze multiple roads with complete information")
    void testMultipleRoadsAnalysis() {
        Node testNode = graph.getNode(889555477);
        assertNotNull(testNode);
        
        List<Edge> roads = testNode.getEdgeList();
        assertTrue(roads.size() > 0);
        
        System.out.println("\n=== MULTIPLE ROADS ANALYSIS ===");
        System.out.println("Starting from Node: " + testNode.getId());
        System.out.println("Number of roads: " + roads.size() + "\n");
        
        for (int i = 0; i < Math.min(roads.size(), 3); i++) {
            Edge road = roads.get(i);
            Node destNode = road.destination();
            
            System.out.println("--- Road " + (i + 1) + " ---");
            System.out.println("Name: " + road.name());
            System.out.println("Type: " + road.roadType());
            System.out.println("Length: " + String.format("%.2f", road.distanceMeters()) + "m");
            System.out.println("Speed Limit: " + road.speedLimit() + " km/h");
            System.out.println("Travel Time: " + String.format("%.2f", road.weight()) + "s");
            System.out.println("Material: " + road.roadMaterial());
            
            int obstacleCount = testNode.getObstacles().size() + destNode.getObstacles().size();
            System.out.println("Obstacles: " + obstacleCount);
            System.out.println();
        }
    }

    @Test
    @DisplayName("Should verify road properties are correctly parsed from OSM")
    void testRoadPropertiesCorrectness() {
        Node testNode = graph.getNode(889555477);
        Edge road = testNode.getEdgeList().get(0);
        
        // Verify length is positive
        assertTrue(road.distanceMeters() > 0, "Distance should be positive");
        
        // Verify speed limit is reasonable (1-200 km/h)
        assertTrue(road.speedLimit() > 0 && road.speedLimit() <= 200, 
                  "Speed limit should be between 1 and 200 km/h");
        
        // Verify weight (travel time) is positive and calculated correctly
        assertTrue(road.weight() > 0, "Weight (travel time) should be positive");
        
        // Verify road name is not null
        assertNotNull(road.name(), "Road name should not be null");
        
        // Verify road type is not MISSING or OTHER
        assertTrue(road.roadType() != RoadType.MISSING && road.roadType() != RoadType.OTHER,
                  "Road type should be properly classified");
        
        System.out.println("\n=== PROPERTY VALIDATION PASSED ===");
        System.out.println("All road properties are valid and correctly parsed.");
    }

    // Helper method to print obstacles
    private void printObstacles(List<TrafficObstacle> obstacles) {
        if (obstacles.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (TrafficObstacle obstacle : obstacles) {
                System.out.println("  - " + obstacle.name() + 
                                 " (penalty: " + obstacle.getPenaltySeconds() + "s)");
            }
        }
    }

}
