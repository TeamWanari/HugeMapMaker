package com.wanari.infinitemarker.renderer;

import com.google.android.gms.maps.model.LatLngBounds;

public interface RenderParams {

    LatLngBounds getDrawingBounds();

    int getOutputHeight();

    int getOutputWidth();

    float getHeightInMeter();

    float getWidthInMeter();
}
