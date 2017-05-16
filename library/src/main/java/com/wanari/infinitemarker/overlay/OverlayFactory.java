package com.wanari.infinitemarker.overlay;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.data.PreparationData;
import com.wanari.infinitemarker.data.RenderParameters;
import com.wanari.infinitemarker.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.annotations.Experimental;
import timber.log.Timber;

import static rx.observables.JoinObservable.from;
import static rx.observables.JoinObservable.when;

public class OverlayFactory {

    public static final int DEFAULT_WIDTH_PX = 600;

    public static Observable<RenderParameters> prepareRenderParameters(@NonNull List<? extends LatLngWrapper> latLngList, LatLngBounds visibleBounds) {
        return Observable.defer(() -> {
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
                distance = PositionUtils.calculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude);
                float posY = calculatedHeight * (distance / heightInMeters);
                glVertexList.add(new PointF(posX, posY));
            }

            renderParameters.setVertexList(glVertexList);
            renderParameters.setDrawingSize(new Point(DEFAULT_WIDTH_PX, calculatedHeight));
            Timber.i("Preparation finished");
            return Observable.just(renderParameters);
        });
    }

    @Experimental
    public static Observable<RenderParameters> prepareRenderParametersReactive(@NonNull List<? extends LatLngWrapper> latLngList, LatLngBounds visibleBounds) {
        return Observable.from(latLngList)
                .filter(latLngWrapper -> visibleBounds.contains(latLngWrapper.getLatLng()))
                .collect(PreparationData::new, (preparationData, latLngWrapper) -> {
                    preparationData.updateLatitude(latLngWrapper.getLatLng().latitude);
                    preparationData.updateLongitude(latLngWrapper.getLatLng().longitude);
                    preparationData.getFilteredLatLngs().add(latLngWrapper);
                })
                .doOnSubscribe(() -> Timber.i("Preparation started"))
                .doOnNext(d -> Timber.i("Filtered pos count: " + d.getFilteredLatLngs().size()))
                .doOnNext(d -> Timber.i("DrawingBounds: " + d.getDrawingBounds().toString()))
                .flatMap(preparationData -> {
                    final RenderParameters renderParameters = new RenderParameters();
                    LatLngBounds drawingBounds = preparationData.getDrawingBounds();
                    renderParameters.setDrawingBounds(preparationData.getDrawingBounds());
                    renderParameters.setMarkerCount(preparationData.getFilteredLatLngs().size());
                    return when(
                            from(PositionUtils.rxCalculateHaversineDistance( //Width in Meters
                                    drawingBounds.northeast.latitude, drawingBounds.northeast.longitude,
                                    drawingBounds.northeast.latitude, drawingBounds.southwest.longitude))
                                    .and(PositionUtils.rxCalculateHaversineDistance( //Height in Meters
                                            drawingBounds.northeast.latitude, drawingBounds.northeast.longitude,
                                            drawingBounds.southwest.latitude, drawingBounds.northeast.longitude))
                                    .then(PointF::new))
                            .toObservable()
                            .doOnNext(d -> Timber.i("Overlay dimensions: " + d.x + "x" + d.y + " meters"))
                            .map(dimensionsInMeter -> {
                                int calculatedHeight = (int) (DEFAULT_WIDTH_PX * dimensionsInMeter.y / dimensionsInMeter.x);
                                renderParameters.setDrawingSize(new Point(DEFAULT_WIDTH_PX, calculatedHeight));
                                renderParameters.setDimensionInMeter(dimensionsInMeter);
                                Timber.i("Calculated canvas dimension: " + DEFAULT_WIDTH_PX + "x" + calculatedHeight);
                                return new Pair<>(renderParameters, preparationData.getFilteredLatLngs());
                            });
                })
                .flatMap(renderParametersListPair -> {
                    RenderParameters renderParameters = renderParametersListPair.first;
                    LatLngBounds drawingBounds = renderParameters.getDrawingBounds();
                    return Observable.from(renderParametersListPair.second)
                            .map(LatLngWrapper::getLatLng)
                            .flatMap(latLng -> when(from(PositionUtils.rxCalculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, drawingBounds.northeast.longitude))
                                    .and(PositionUtils.rxCalculateHaversineDistance(latLng.latitude, drawingBounds.southwest.longitude, latLng.latitude, latLng.longitude))
                                    .and(PositionUtils.rxCalculateHaversineDistance(drawingBounds.northeast.latitude, latLng.longitude, latLng.latitude, latLng.longitude))
                                    .then((lngPartForThisLat, distance, distance2) -> {
                                        float posX = DEFAULT_WIDTH_PX * (distance / lngPartForThisLat);
                                        float posY = renderParameters.getDrawingSize().y * (distance2 / renderParameters.getDimensionInMeter().y);
                                        return new PointF(posX, posY);
                                    }))
                                    .toObservable())
                            .collect(() -> renderParameters, (renderParameters1, pointF) -> renderParameters1.getVertexList().add(pointF));

                })
                .doOnNext(r -> Timber.i("Preparation finished"));
    }
}
