package com.wanari.infinitemarker.renderer;

import android.content.Context;
import android.graphics.Canvas;

import com.wanari.infinitemarker.data.LatLngWrapper;

public interface IMarkerRenderer {

    void onRenderMarker(Context context, Canvas overlayCanvas, float centerPosX, float centerPosY, LatLngWrapper latLngWrapper);

}
