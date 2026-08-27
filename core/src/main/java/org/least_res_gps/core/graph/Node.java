package org.least_res_gps.core.graph;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private long id;
    private double lat;
    private double lon;

    private List<Edge> edgeList = new ArrayList<>();

    public long getId() { return id; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public List<Edge> getEdgeList() { return edgeList; }

    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public void addEdge(Edge edge) {
        edgeList.add(edge);
    }

    @Override public String toString() {
        return "Node{"+id+"}";
    }
}
