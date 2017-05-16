package com.wanari.infinitemarker.data;

import com.google.android.gms.maps.model.LatLng;

public class SimpleLatLng implements LatLngWrapper {

    private LatLng latLng;

    public SimpleLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public LatLng getLatLng() {
        return latLng;
    }
}
