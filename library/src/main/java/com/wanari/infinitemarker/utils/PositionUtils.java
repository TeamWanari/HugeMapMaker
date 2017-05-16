package com.wanari.infinitemarker.utils;

import rx.Observable;

public class PositionUtils {

    private static final double p = 0.017453292519943295f;      // Math.PI / 180
    private static final double earthRad2 = 12742000f;         // 2 * R; R = 6371 km

    public static float calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double lat1p = lat1 * p;
        final double lat2p = lat2 * p;

        double a = 0.5 - Math.cos(lat2p - lat1p) * 0.5f + Math.cos(lat1p) * Math.cos(lat2p) * (1 - Math.cos((lon2 - lon1) * p)) * 0.5f;
        return (float) (earthRad2 * Math.asin(Math.sqrt(a)));
    }

    public static Observable<Float> rxCalculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        return Observable.fromCallable(() -> calculateHaversineDistance(lat1, lon1, lat2, lon2));
    }
}
