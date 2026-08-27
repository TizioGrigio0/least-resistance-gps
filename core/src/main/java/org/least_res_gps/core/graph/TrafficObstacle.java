package org.least_res_gps.core.graph;

public enum TrafficObstacle {

    TRAFFIC_SIGNAL(20.0),
    STOP_SIGN(5.0),
    PEDESTRIAN_CROSSING(10.0),
    SPEED_BUMP(5.0),
    RAILWAY_CROSSING(30.0),
    TOLL_BOOTH(45.0),
    MINI_ROUNDABOUT(5.0),
    ROUNDABOUT(5.0),
    BORDER_CONTROL(90.0),
    BARRIER(Double.POSITIVE_INFINITY),
    SPEED_CAMERA(10.0),
    NONE(0.0);

    private final double penaltySeconds;

    TrafficObstacle(double secondsPenalty) {
        this.penaltySeconds = secondsPenalty;
    }

    public double getPenaltySeconds() {
        return penaltySeconds;
    }

    public static TrafficObstacle parseObstacle(String key, String value) {
        if (key == null || value == null) return NONE;

        String k = key.toLowerCase().trim();
        String v = value.toLowerCase().trim();

        return switch (k) {
            case "highway" -> switch (v) {
                case "traffic_signals" -> TRAFFIC_SIGNAL;
                case "stop" -> STOP_SIGN;
                case "crossing" -> PEDESTRIAN_CROSSING;
                case "mini_roundabout" -> MINI_ROUNDABOUT;
                case "speed_camera" -> SPEED_CAMERA;
                default -> NONE;
            };
            case "traffic_calming" -> SPEED_BUMP; // Matches bump, hump, table, cushion
            case "railway" -> ("level_crossing".equals(v) || "crossing".equals(v)) ? RAILWAY_CROSSING : NONE;
            case "barrier" -> switch (v) {
                case "toll_booth" -> TOLL_BOOTH;
                case "border_control" -> BORDER_CONTROL;
                case "bollard", "block", "wall" -> BARRIER;
                default -> NONE;
            };
            case "junction" -> "roundabout".equals(v) ? ROUNDABOUT : NONE;
            default -> NONE;
        };
    }

}
