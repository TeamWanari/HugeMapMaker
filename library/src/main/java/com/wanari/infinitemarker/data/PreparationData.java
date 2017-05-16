package com.wanari.infinitemarker.data;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class PreparationData {
    private List<LatLngWrapper> filteredLatLngs = new ArrayList<>();
    private double south = Double.POSITIVE_INFINITY;
    private double west = Double.POSITIVE_INFINITY;
    private double north = Double.NEGATIVE_INFINITY;
    private double east = Double.NEGATIVE_INFINITY;

    private LatLngBounds bounds;

    public List<LatLngWrapper> getFilteredLatLngs() {
        return filteredLatLngs;
    }

    public void updateLongitude(double longitude) {
        if (longitude > east) {
            east = longitude;
        }
        if (longitude < west) {
            west = longitude;
        }

    }

    public void updateLatitude(double latitude) {
        if (latitude > north) {
            north = latitude;
        }
        if (latitude < south) {
            south = latitude;
        }
    }

    public LatLngBounds getDrawingBounds() {
        if (bounds == null) {
            bounds = new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
        }
        return bounds;
    }
}
