package com.wanari.infinitemarker.rx;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.renderer.RenderParams;

import rx.functions.FuncN;
import timber.log.Timber;

public class LayerMergerFuncN implements FuncN<GroundOverlayOptions> {

    private final GoogleMap map;
    private final RenderParams renderParams;


    public LayerMergerFuncN(GoogleMap map, RenderParams renderParams) {
        this.map = map;
        this.renderParams = renderParams;
    }

    @Override
    public GroundOverlayOptions call(Object... args) {
        Timber.i("Parallel tasks finished.");
        Bitmap output = Bitmap.createBitmap(renderParams.getOutputWidth(), renderParams.getOutputHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        for (Object layer : args) {
            Bitmap layerMap = (Bitmap) layer;
            canvas.drawBitmap(layerMap, 0, 0, null);
            layerMap.recycle();
        }

        LatLngBounds drawingBounds = renderParams.getDrawingBounds();

        if (drawingBounds == null) {
            drawingBounds = map.getProjection().getVisibleRegion().latLngBounds;
        }

        final GroundOverlayOptions options =
                new GroundOverlayOptions()
                        .positionFromBounds(drawingBounds)
                        .image(BitmapDescriptorFactory.fromBitmap(output));

        output.recycle();
        return options;
    }
}