package com.wanari.infinitemarker.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.wanari.infinitemarker.R;
import com.wanari.infinitemarker.data.LatLngWrapper;

public class DefaultMarkerRenderer implements IMarkerRenderer {

    private static Bitmap marker;

    @Override
    public void onRenderMarker(Context context, Canvas overlayCanvas, float centerPosX, float centerPosY, LatLngWrapper latLngWrapper) {
        if (marker == null) {
            Bitmap mark = BitmapFactory.decodeResource(context.getResources(), R.drawable.green_marker);
            marker = Bitmap.createScaledBitmap(mark, 20, 20, false);
        }
        overlayCanvas.drawBitmap(marker, centerPosX, centerPosY, null);
    }
}
