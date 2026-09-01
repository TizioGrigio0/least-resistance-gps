package org.least_res_gps.core.graph;

public record Edge(
        Node destination,
        String name,
        RoadType roadType,
        int speedLimit,
        double distanceMeters,
        double travelTimeSeconds,
        RoadMaterial roadMaterial
) {
    public void printInfo() {
        System.out.println("EDGE - name:"+name+" - destination:"+destination+" - length:"+distanceMeters+" - limit:"+speedLimit+" - travelTimeSeconds:"+ travelTimeSeconds +" - roadType:"+roadType+" - roadMaterial:"+roadMaterial);
    }
}
