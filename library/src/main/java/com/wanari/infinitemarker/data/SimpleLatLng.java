package com.wanari.infinitemarker.data;

import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;

public class SimpleLatLng implements LatLngWrapper {

    private LatLng latLng;

    public SimpleLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @NotNull
    @Override
    public LatLng getLatLng() {
        return latLng;
    }
}
