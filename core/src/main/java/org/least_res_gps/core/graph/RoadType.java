package org.least_res_gps.core.graph;

import java.util.ArrayList;
import java.util.List;

public enum RoadType {

    MISSING("", 50),            // Used when the data doesn't have a "highway" tag

    MOTORWAY("motorway", 130),
    TRUNK("trunk", 110),
    PRIMARY("primary", 90),
    SECONDARY("secondary", 90),
    TERTIARY("tertiary", 50),
    UNCLASSIFIED("unclassified", 50),
    RESIDENTIAL("residential", 50),
    SERVICE("service", 30),

    OTHER("", 50);               // Used when the data has a "highway" tag, but it's not in this list

    private final String osmIdentifier;
    private final int maxSpeed; // kmh

    RoadType(String identifier, int maxSpeed) {
        this.osmIdentifier = identifier;
        this.maxSpeed = maxSpeed;
    }

    public static RoadType parseRoadType(String highwayString) {

        for (RoadType roadType : RoadType.values()) {
            if (roadType.osmIdentifier.equalsIgnoreCase(highwayString))
                return roadType;
        }

        return OTHER;
    }

    public int getDefaultMaxSpeed() {
        return maxSpeed;
    }
}
