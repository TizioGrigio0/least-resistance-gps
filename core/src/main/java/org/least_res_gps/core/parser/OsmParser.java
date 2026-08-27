package org.least_res_gps.core.parser;

import org.least_res_gps.core.exceptions.FileErrorException;
import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Graph;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.graph.RoadType;
import org.least_res_gps.core.util.RoadUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class OsmParser {


    public static Graph parse(File osmFile) throws ParserConfigurationException, FileErrorException {
        // Create factory & builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load the file into an in-memory DOM tree
        Document doc;
        try {
             doc = builder.parse(osmFile);
        } catch (Exception e) {
            throw new FileErrorException(e.getMessage());
        }

        Graph graph = new Graph();

        // N.B: NodeList is from w3c.doc, not least_res_gps.core
        NodeList nodes = doc.getElementsByTagName("node");
        NodeList ways = doc.getElementsByTagName("way");

        // Cycle every node
        for (int i=0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            // Get the main attribute string data
            String idString = element.getAttribute("id");
            String latString = element.getAttribute("lat");
            String lonString = element.getAttribute("lon");

            // Parse the string data into actual data
            long id = Long.parseLong(idString);
            double lat = Double.parseDouble(latString);
            double lon = Double.parseDouble(lonString);

            // Create the new node and add it to the graph
            Node node = new Node(id, lat, lon);
            graph.addNode(node);
        }

        // Cycle every edge
        for (int i=0; i < ways.getLength(); i++) {
            Element element = (Element) ways.item(i);

            // Set the fallback for the attributes of the edge
            double distanceMeters = -1;
            int speedLimit = -1;
            RoadType roadType = RoadType.MISSING;
            String name = "Unnamed road";
            boolean oneWay = false;

            // Get the actual attributes of the edge
            NodeList tags = element.getElementsByTagName("tag");
            for (int j=0; j < tags.getLength(); j++) {
                Element tag = (Element) tags.item(j);
                String key = tag.getAttribute("k");
                String value = tag.getAttribute("v");

                switch (key) {
                    case "name" -> name = value;
                    case "highway" -> roadType = RoadType.parseRoadType(value);
                    case "maxspeed" -> speedLimit = parseSpeedLimit(value);
                    case "oneway" -> oneWay = "yes".equalsIgnoreCase(value); // oneway="yes", not oneway="true"
                }

            }

            if (roadType == RoadType.MISSING || roadType == RoadType.OTHER) continue;

            if (speedLimit == -1) { speedLimit = roadType.getDefaultMaxSpeed(); }

            // Apply the edge at each intersection with other nodes
            NodeList wayNodes = element.getElementsByTagName("nd");
            for (int j=1; j < wayNodes.getLength(); j++) {

                // Get the starting node
                long startingNodeId = Long.parseLong(
                        ((Element)wayNodes.item(j-1))
                                .getAttribute("ref")
                );
                Node startingNode = graph.getNode(startingNodeId);
                if (startingNode == null) continue; // If the start is out of scope, skip this edge

                // Get the destination node
                long destinationNodeId = Long.parseLong(
                        ((Element)wayNodes.item(j))
                                .getAttribute("ref")
                );
                Node destinationNode = graph.getNode(destinationNodeId);
                if (destinationNode == null) continue; // If the destination is out of scope, skip this edge

                // Calculate the distance
                distanceMeters = RoadUtil.calculateDistance(startingNode.getLat(), startingNode.getLon(), destinationNode.getLat(), destinationNode.getLon());

                // Get the estimated travel time (s = m/kmh)
                double travelTimeSeconds = distanceMeters*3.6/speedLimit; // Travel time seconds

                // Create the edge and add it to the starting node
                Edge edge = new Edge(destinationNode, name, roadType, speedLimit, distanceMeters, travelTimeSeconds);
                startingNode.addEdge(edge);
                if (!oneWay) { // Create the road in the opposite direction if it's not one way
                    Edge oppositeEdge = new Edge(startingNode, name, roadType, speedLimit, distanceMeters, travelTimeSeconds);
                    destinationNode.addEdge(oppositeEdge);
                }
                //edge.printInfo();
            }

        }

        return graph;
    }


    // Precompiler regex to parse the speed limit
    private static final java.util.regex.Pattern DIGITS = java.util.regex.Pattern.compile("[^0-9]");

    private static int parseSpeedLimit(String value) {
        if (value == null || value.isBlank()) return -1;

        // Check common values
        String processedValueString = value.toLowerCase().trim();

        switch (processedValueString) {
            case "none" -> { return 130; }
            case "walk", "living_street" -> { return 5; }
            case "signals" -> { return 50; }
        }

        try {
            // Check for imperial units before stripping letters
            boolean isMph = processedValueString.contains("mph");

            // Strip everything except digits
            String numericPart = DIGITS.matcher(processedValueString).replaceAll("");

            // If the numeric part is empty, fallback
            if (numericPart.isEmpty()) { return -1; }

            int speed = Integer.parseInt(numericPart);

            // Convert from mph to km/h if necessary
            return isMph ? (int) Math.round(speed * 1.60934) : speed;

        } catch (NumberFormatException e) { // Fallback if parsing completely fails
            return -1;
        }
    }



}
