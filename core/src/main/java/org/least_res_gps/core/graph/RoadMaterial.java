package org.least_res_gps.core.graph;

public enum RoadMaterial {

    FLAT(1.0),
    BUMPY(1.3),
    BASICALLY_NO_ROAD(2.0);

    private final double penaltyMultiplier;

    RoadMaterial(double penalityMultiplier) {
        this.penaltyMultiplier = penalityMultiplier;
    }

    public double getPenaltyMultiplier() {
        return penaltyMultiplier;
    }

    public static RoadMaterial parseMaterial(String surfaceString) {
        return switch (surfaceString) {
            case "asphalt", "paved", "concrete", "paving_stones" -> FLAT;
            case "cobblestone", "sett", "uncompacted", "fine_gravel" -> BUMPY;
            case "unpaved", "gravel", "dirt", "earth", "ground", "mud", "sand" -> BASICALLY_NO_ROAD;
            default -> FLAT;
        };
    }

}
