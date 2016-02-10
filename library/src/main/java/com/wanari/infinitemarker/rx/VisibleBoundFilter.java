package com.wanari.infinitemarker.rx;

import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.data.LatLngWrapper;

import rx.functions.Func1;

public class VisibleBoundFilter implements Func1<LatLngWrapper, Boolean> {

    final LatLngBounds bounds;

    public VisibleBoundFilter(LatLngBounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public Boolean call(LatLngWrapper latLngWrapper) {
        return bounds.contains(latLngWrapper.getLatLng());
    }
}