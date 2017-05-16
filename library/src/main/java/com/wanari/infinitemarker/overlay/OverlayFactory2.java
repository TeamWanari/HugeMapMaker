package com.wanari.infinitemarker.overlay;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.renderer.DefaultMarkerRenderer;
import com.wanari.infinitemarker.renderer.IMarkerRenderer;
import com.wanari.infinitemarker.renderer.RenderParams;
import com.wanari.infinitemarker.rx.VisibleBoundFilter;
import com.wanari.infinitemarker.utils.PositionUtils;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by agocs on 2016.01.20..
 */

@Deprecated
public class OverlayFactory2 implements RenderParams {

    public static final String TAG = "OverlayFactory";
    public static final int DEFAULT_WIDTH_PX = 600;
    public static final int PARALLEL_DRAWING_TASK = 4;

    double south = Double.POSITIVE_INFINITY;
    double west = Double.POSITIVE_INFINITY;
    double north = Double.NEGATIVE_INFINITY;
    double east = Double.NEGATIVE_INFINITY;

    float widthInMeters;
    float heightInMeters;

    private IMarkerRenderer markerRenderer = new DefaultMarkerRenderer();
    private Context context;

    private LatLngBounds drawingBounds;
    private int calculatedHeight;

    private int shownMarkerCount = 0;

    private List<PointF> points;

    public OverlayFactory2(Context context) {
        this.context = context;
    }

    public Observable<OverlayFactory2> generateMapOverlay(@NonNull GoogleMap map, @NonNull List<LatLngWrapper> latLngList) {
        Log.i(TAG, "Unfiltered pos count: " + latLngList.size());
        return filterInBoundLatLng(latLngList, map.getProjection().getVisibleRegion().latLngBounds)
                .doOnNext(new Action1<List<LatLngWrapper>>() {
                    @Override
                    public void call(List<LatLngWrapper> latLngList) {
                        Timber.i("Filtered pos count: " + latLngList.size());
                        drawingBounds = new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
                        Timber.i("DrawingBounds: " + drawingBounds.toString());
                        widthInMeters = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, drawingBounds.northeast.longitude, drawingBounds.northeast.latitude, drawingBounds.southwest.longitude);
                        heightInMeters = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, drawingBounds.northeast.longitude, drawingBounds.southwest.latitude, drawingBounds.northeast.longitude);
                        Timber.i("Overlay dimensions: " + widthInMeters + "x" + heightInMeters + " meters");
                        double imageWidthHeightRatio = widthInMeters / heightInMeters;
                        calculatedHeight = (int) (DEFAULT_WIDTH_PX / imageWidthHeightRatio);
                        Timber.i("Calculated canvas dimension: " + DEFAULT_WIDTH_PX + "x" + calculatedHeight);
                    }
                }).flatMap(new Func1<List<LatLngWrapper>, Observable<List<PointF>>>() {
                    @Override
                    public Observable<List<PointF>> call(List<LatLngWrapper> latLngWrappers) {
                        return Observable.from(latLngWrappers)
                                .map(latLngWrapper -> {
                                    final LatLng latLng = latLngWrapper.getLatLng();
                                    float lngPartForThisLat = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, drawingBounds.northeast.longitude);
                                    float distance = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, latLng.longitude);
                                    float posX = DEFAULT_WIDTH_PX * (distance / lngPartForThisLat);
                                    //float latPartForThisLng = calculateHaversineDistance(drawingBounds.southwest.latitude, latLng.longitude, drawingBounds.northeast.latitude, latLng.longitude);
                                    distance = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
                                    float posY = calculatedHeight * (distance / heightInMeters);
                                    return new PointF(posX, posY);
                                }).toList();
                    }
                })
                .map(new Func1<List<PointF>, OverlayFactory2>() {
                    @Override
                    public OverlayFactory2 call(List<PointF> pointFs) {
                        points = pointFs;
                        return OverlayFactory2.this;
                    }
                });
    }

    private Observable<List<LatLngWrapper>> filterInBoundLatLng(List<LatLngWrapper> latLngList, LatLngBounds bounds) {
        return Observable.from(latLngList)
                .filter(new VisibleBoundFilter(bounds))
                .doOnNext(latLngWrapper -> {
                    final LatLng latLng = latLngWrapper.getLatLng();
                    shownMarkerCount++;
                    if (latLng.longitude > east) {
                        east = latLng.longitude;
                    }
                    if (latLng.longitude < west) {
                        west = latLng.longitude;
                    }

                    if (latLng.latitude > north) {
                        north = latLng.latitude;
                    }
                    if (latLng.latitude < south) {
                        south = latLng.latitude;
                    }
                })
                .toList();
    }

    @Override
    public LatLngBounds getDrawingBounds() {
        return drawingBounds;
    }

    @Override
    public int getOutputHeight() {
        return calculatedHeight;
    }

    @Override
    public int getOutputWidth() {
        return DEFAULT_WIDTH_PX;
    }

    @Override
    public float getHeightInMeter() {
        return heightInMeters;
    }

    @Override
    public float getWidthInMeter() {
        return widthInMeters;
    }

    public int getShownMarkerCount() {
        return shownMarkerCount;
    }

    public List<PointF> getPoints() {
        return points;
    }
}
