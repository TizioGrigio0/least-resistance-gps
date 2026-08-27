package org.least_res_gps.core.routing;

import org.least_res_gps.core.graph.Node;

public class AStarNode implements Comparable<AStarNode> {

    private final Node node;
    private final Node parent;
    private final double gTime; // Time to reach this node from the start
    private final double fTime; // Estimated time to reach the target from the start (gTime + hTime)
    // hTime is the estimated time needed from this node to the target (basically distance/maxSpeed in a straight line from this to the destination)

    public AStarNode(Node node, Node parent, double gTime, double fTime) {
        this.node = node;
        this.parent = parent;
        this.gTime = gTime;
        this.fTime = fTime;
    }

    public Node getNode() {return node; }
    public Node getParent() { return parent; }
    public double getgTime() { return gTime; }
    public double getfTime() { return fTime; }

    @Override
    public int compareTo(AStarNode o) {
        return Double.compare(this.fTime, o.fTime);
    }
}
