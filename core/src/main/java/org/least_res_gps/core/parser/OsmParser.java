package org.least_res_gps.core.parser;

import org.least_res_gps.core.graph.Edge;
import org.least_res_gps.core.graph.Graph;
import org.least_res_gps.core.graph.Node;
import org.least_res_gps.core.graph.RoadType;
import org.least_res_gps.core.util.RoadUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class OsmParser {


    public static Graph parse(File osmFile) throws FileNotFoundException, XMLStreamException {

        // Create factory and reader
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(osmFile));

        Graph graph = new Graph();

        while (reader.hasNext()) {

            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT && "node".equals(reader.getLocalName())) {
                // Extract attributes
                long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));

                // Create a node with those attributes and add it to the graph
                graph.addNode(new Node(id, lat, lon));
            } else if (event == XMLStreamConstants.START_ELEMENT && "way".equals(reader.getLocalName())) { // We reach the tag "way", we finished with the nodes
                // Parse the "ways" after finishing with the nodes
                parseWay(reader, graph);
            }
        }

        reader.close();
        return graph;
    }

    // Parses one "way"
    private static void parseWay(XMLStreamReader reader, Graph graph) throws XMLStreamException {
        // Set the fallback for the attributes of the edge
        List<Long> nodesRefs = new ArrayList<>();
        double distanceMeters = -1;
        int speedLimit = -1;
        RoadType roadType = RoadType.MISSING;
        String name = "Unnamed road";
        boolean oneWay = false;

        // Cycle every edge
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();

                if ("nd".equals(tagName)) {
                    nodesRefs.add(Long.parseLong(reader.getAttributeValue(null, "ref")));
                } else if ("tag".equals(tagName)) {
                    String key = reader.getAttributeValue(null, "k");
                    String value = reader.getAttributeValue(null, "v");

                    switch (key) {
                        case "name" -> name = value;
                        case "highway" -> roadType = RoadType.parseRoadType(value);
                        case "maxspeed" -> speedLimit = parseSpeedLimit(value);
                        case "oneway" -> oneWay = "yes".equalsIgnoreCase(value); // oneway="yes", not oneway="true"
                    }
                } // else if tag closure

            } else if (event == XMLStreamConstants.END_ELEMENT && "way".equals(reader.getLocalName())) {
                break; // Stop reading this way element
            }
        }

        if (roadType == RoadType.MISSING || roadType == RoadType.OTHER) return;

        if (speedLimit == -1) {
            speedLimit = roadType.getDefaultMaxSpeed();
        }

        // Apply the edge at each intersection with other nodes
        for (int i = 1; i < nodesRefs.size(); i++) {

            // Get the starting node
            Node startingNode = graph.getNode(nodesRefs.get(i-1));
            if (startingNode == null) continue; // If the start is out of scope, skip this edge

            // Get the destination node
            Node destinationNode = graph.getNode(nodesRefs.get(i));
            if (destinationNode == null) continue; // If the destination is out of scope, skip this edge

            // Calculate the distance
            distanceMeters = RoadUtil.calculateDistance(startingNode.getLat(), startingNode.getLon(), destinationNode.getLat(), destinationNode.getLon());

            // Get the estimated travel time (s = m/kmh)
            double travelTimeSeconds = distanceMeters * 3.6 / speedLimit; // Travel time seconds

            // Create the edge and add it to the starting node
            Edge edge = new Edge(destinationNode, name, roadType, speedLimit, distanceMeters, travelTimeSeconds);
            startingNode.addEdge(edge);
            if (!oneWay) { // Create the road in the opposite direction if it's not one way
                Edge oppositeEdge = new Edge(startingNode, name, roadType, speedLimit, distanceMeters, travelTimeSeconds);
                destinationNode.addEdge(oppositeEdge);
            }
            //edge.printInfo();
        } // for nodesRefs closure

    } // parseWay closure


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
