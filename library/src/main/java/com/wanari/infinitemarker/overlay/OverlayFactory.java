package com.wanari.infinitemarker.overlay;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Size;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.data.RenderParameters;
import com.wanari.infinitemarker.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class OverlayFactory {

    public static final int DEFAULT_WIDTH_PX = 600;

    public static Observable<RenderParameters> prepareRenderParameters(@NonNull List<? extends LatLngWrapper> latLngList, LatLngBounds visibleBounds) {
        return Observable.create(new Observable.OnSubscribe<RenderParameters>() {
            @Override
            public void call(Subscriber<? super RenderParameters> subscriber) {
                Timber.i("Preparation started");
                double south = Double.POSITIVE_INFINITY;
                double west = Double.POSITIVE_INFINITY;
                double north = Double.NEGATIVE_INFINITY;
                double east = Double.NEGATIVE_INFINITY;

                RenderParameters renderParameters = new RenderParameters();
                List<PointF> glVertexList = new ArrayList<>();
                List<LatLngWrapper> filteredLatLngs = new ArrayList<>();

                for (LatLngWrapper latLngWrapper : latLngList) {
                    LatLng latLng = latLngWrapper.getLatLng();
                    if (visibleBounds.contains(latLng)) {
                        //Drawing bounds
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

                        filteredLatLngs.add(latLngWrapper);
                    }
                }

                Timber.i("Filtered pos count: " + filteredLatLngs.size());
                renderParameters.setMarkerCount(filteredLatLngs.size());
                LatLngBounds drawingBounds = new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
                renderParameters.setDrawingBounds(drawingBounds);
                Timber.i("DrawingBounds: " + drawingBounds.toString());
                float widthInMeters = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, drawingBounds.northeast.longitude, drawingBounds.northeast.latitude, drawingBounds.southwest.longitude);
                float heightInMeters = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, drawingBounds.northeast.longitude, drawingBounds.southwest.latitude, drawingBounds.northeast.longitude);
                Timber.i("Overlay dimensions: " + widthInMeters + "x" + heightInMeters + " meters");
                double imageWidthHeightRatio = widthInMeters / heightInMeters;
                int calculatedHeight = (int) (DEFAULT_WIDTH_PX / imageWidthHeightRatio);
                Timber.i("Calculated canvas dimension: " + DEFAULT_WIDTH_PX + "x" + calculatedHeight);

                for (LatLngWrapper latLngWrapper : filteredLatLngs) {
                    final LatLng latLng = latLngWrapper.getLatLng();
                    float lngPartForThisLat = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, drawingBounds.northeast.longitude);
                    float distance = PositionUtils.calculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, latLng.longitude);
                    float posX = DEFAULT_WIDTH_PX * (distance / lngPartForThisLat);
                    //float latPartForThisLng = calculateHaversineDistance(drawingBounds.southwest.latitude, latLng.longitude, drawingBounds.northeast.latitude, latLng.longitude);
                    distance = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
                    float posY = calculatedHeight * (distance / heightInMeters);
                    glVertexList.add(new PointF(posX, posY));
                }

                renderParameters.setVertexList(glVertexList);
                renderParameters.setDrawingSize(new Point(DEFAULT_WIDTH_PX, calculatedHeight));
                subscriber.onNext(renderParameters);
                Timber.i("Preparation finished");
                subscriber.onCompleted();
            }
        });
    }
}
