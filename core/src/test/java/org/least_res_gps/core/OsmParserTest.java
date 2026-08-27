package org.least_res_gps.core;

import org.junit.jupiter.api.Test;
import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Graph;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.parser.OsmParser;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OsmParserTest {

    @Test
    void testParserPopulatesGraph() throws Exception {
        // Get the file
        File osmFile = new File("src/test/resources/giuseppegrazioli.osm");

        // Parse it
        Graph graph = OsmParser.parse(osmFile);

        // Verify results
        assertNotNull(graph, "Parser should return a valid Graph object");
        assertTrue(graph.getNodeCount() > 0, "Graph node count should be greater than zero");

        // Log the total
        System.out.println("Successfully parsed " + graph.getNodeCount() + " nodes!");
    }

    @Test
    void testEdgesAreCreated() throws Exception {
        // Get the file and parse it
        File osmFile = new File("src/test/resources/trento.osm");
        Graph graph = OsmParser.parse(osmFile);

        // Verify the graph contains edges globally
        int totalEdges = graph.getTotalEdgeCount();
        System.out.println("Total Nodes: " + graph.getNodeCount());
        System.out.println("Total Edges: " + totalEdges);

        assertTrue(totalEdges > 0, "Graph should contain at least one edge");

        Node testNode = graph.getNode(889555477);
        for (Edge edge : testNode.getEdgeList()) {
            edge.printInfo();
        }
    }
}