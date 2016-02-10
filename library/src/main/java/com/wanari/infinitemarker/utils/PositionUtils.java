package com.wanari.infinitemarker.utils;

public class PositionUtils {

    public static float calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double p = 0.017453292519943295;      // Math.PI / 180
        final double earthRad2 = 12742000f;         // 2 * R; R = 6371 km
        final double lat1p = lat1 * p;
        final double lat2p = lat2 * p;

        double a = 0.5 - Math.cos(lat2p - lat1p) / 2 +
                Math.cos(lat1p) * Math.cos(lat2p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return (float) (earthRad2 * Math.asin(Math.sqrt(a)));
    }
}
