package com.wanari.infinitemarker.rx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.opengl.GLRenderer;
import com.wanari.infinitemarker.opengl.PixelBuffer;
import com.wanari.infinitemarker.renderer.RenderParams;
import com.wanari.infinitemarker.renderer.IMarkerRenderer;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

public class ParallelRendererFunc1 implements Func1<List<LatLngWrapper>, Observable<List<PointF>>> {

    private final Context context;
    private final GoogleMap map;
    private final int parallelTaskCount;
    private final IMarkerRenderer markerRenderer;
    private final RenderParams renderParams;

    private PixelBuffer pixelBuffer;


    public ParallelRendererFunc1(Context context, GoogleMap map, int parallelTaskCount, IMarkerRenderer markerRenderer, RenderParams renderParams) {
        this.context = context;
        this.map = map;
        this.parallelTaskCount = parallelTaskCount;
        this.markerRenderer = markerRenderer;
        this.renderParams = renderParams;
        pixelBuffer = new PixelBuffer(renderParams.getOutputWidth(), renderParams.getOutputHeight());
    }

    @Override
    public Observable<List<PointF>> call(List<LatLngWrapper> latLngWrappers) {
        return Observable.from(latLngWrappers)
                .map(latLngWrapper -> {
                    final LatLng latLng = latLngWrapper.getLatLng();
                    LatLngBounds drawingBounds = renderParams.getDrawingBounds();
                    float lngPartForThisLat = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, drawingBounds.northeast.longitude);
                    float distance = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, latLng.longitude);
                    float posX = renderParams.getOutputWidth() * (distance / lngPartForThisLat);
                    //float latPartForThisLng = calculateHaversineDistance(drawingBounds.southwest.latitude, latLng.longitude, drawingBounds.northeast.latitude, latLng.longitude);
                    distance = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
                    float posY = renderParams.getOutputHeight() * (distance / renderParams.getHeightInMeter());
                    return new PointF(posX, posY);
                })
                .toList();


//        Timber.i("Starting parallel tasks");
//        List<Observable<Bitmap>> subTasks = new ArrayList<>();
//        for (int i = 0; i < parallelTaskCount; i++) {
//            subTasks.add(drawPositionsToBitmap(latLngWrappers.subList(i * latLngWrappers.size() / parallelTaskCount, (i + 1) * latLngWrappers.size() / parallelTaskCount)));
//        }
//        return Observable.zip(subTasks, new LayerMergerFuncN(map, renderParams));
    }

    private Observable<Bitmap> drawPositionsToBitmap(List<LatLngWrapper> latLngWrappers) {
        Bitmap drawingLayer = Bitmap.createBitmap(renderParams.getOutputWidth(), renderParams.getOutputHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(drawingLayer);
        return Observable.from(latLngWrappers)
                .doOnNext(latLngWrapper -> {
                    final LatLng latLng = latLngWrapper.getLatLng();
                    LatLngBounds drawingBounds = renderParams.getDrawingBounds();
                    float lngPartForThisLat = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, drawingBounds.northeast.longitude);
                    float distance = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, latLng.longitude);
                    float posX = canvas.getWidth() * (distance / lngPartForThisLat);
                    //float latPartForThisLng = calculateHaversineDistance(drawingBounds.southwest.latitude, latLng.longitude, drawingBounds.northeast.latitude, latLng.longitude);
                    distance = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
                    float posY = canvas.getHeight() * (distance / renderParams.getHeightInMeter());

                    if (markerRenderer != null) {
                        markerRenderer.onRenderMarker(context, canvas, posX, posY, latLngWrapper);
                    }
                })
                .toList()
                .map(latLngList1 -> drawingLayer);
    }
}