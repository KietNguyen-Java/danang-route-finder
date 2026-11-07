package com.kietnguyen.routefinder.algo;

public final class Geo {

    private static final double EARTH_RADIUS_M = 6_371_000;

    private Geo() {
    }

    /** Great-circle distance in meters (haversine formula). */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }

    /** Degrees of latitude that cover {@code meters} north-south. */
    public static double latitudeDelta(double meters) {
        return Math.toDegrees(meters / EARTH_RADIUS_M);
    }

    /** Degrees of longitude that cover {@code meters} east-west at the given latitude. */
    public static double longitudeDelta(double meters, double latitude) {
        return Math.toDegrees(meters / (EARTH_RADIUS_M * Math.cos(Math.toRadians(latitude))));
    }
}
