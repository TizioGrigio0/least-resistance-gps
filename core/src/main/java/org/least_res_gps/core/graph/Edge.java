package org.least_res_gps.core.graph;

public record Edge(
        Node destination,
        String name,
        RoadType roadType,
        int speedLimit,
        double distanceMeters,
        double weight,
        RoadMaterial roadMaterial
) {
    public void printInfo() {
        System.out.println("EDGE - name:"+name+" - destination:"+destination+" - length:"+distanceMeters+" - limit:"+speedLimit+" - weight:"+weight+" - roadType:"+roadType+" - roadMaterial:"+roadMaterial);
    }
}
