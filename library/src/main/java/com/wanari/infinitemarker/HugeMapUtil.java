package com.wanari.infinitemarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.data.SimpleLatLng;
import com.wanari.infinitemarker.opengl.GLRenderer;
import com.wanari.infinitemarker.opengl.PixelBuffer;
import com.wanari.infinitemarker.overlay.OverlayCalculationCallback;
import com.wanari.infinitemarker.overlay.OverlayFactory;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by agocs on 2016.01.18..
 */
public class HugeMapUtil implements GoogleMap.OnCameraChangeListener {

    private final Context context;
    private ArrayList<LatLngWrapper> latLngWrappers = new ArrayList<>();
    private GoogleMap mMap = null;
    private GoogleMap.OnCameraChangeListener onCameraChangeListener = null;
    private GroundOverlay actualOverlay = null;
    private LatLng previousCenterPosition = null;
    private LatLng currentCenterPosition = null;
    private Handler renderCheckScheduleHandler = new Handler();
    private Subscription renderSubscription;
    private OverlayCalculationCallback overlayCalculationCallback;

    private PixelBuffer pixelBuffer;
    private GLRenderer glRenderer;

    private HugeMapUtil(Context context, GoogleMap map, ArrayList<LatLngWrapper> latLngWrappers, OverlayCalculationCallback overlayCalculationCallback, GoogleMap.OnCameraChangeListener onCameraChangeListener, int markerDrawable, Bitmap markerBitmap) {
        this.context = context;
        this.mMap = map;
        this.latLngWrappers = latLngWrappers;
        this.overlayCalculationCallback = overlayCalculationCallback;
        this.onCameraChangeListener = onCameraChangeListener;
        this.mMap.setOnCameraChangeListener(this);
        glRenderer = new GLRenderer(context);
        if (markerBitmap == null) {
            glRenderer.setMarkerBitmap(BitmapFactory.decodeResource(context.getResources(), markerDrawable));
        } else {
            glRenderer.setMarkerBitmap(markerBitmap);
        }
    }

    public static class Builder {
        protected Context context;
        protected GoogleMap map;
        protected ArrayList<LatLngWrapper> latLngWrappers = new ArrayList<>();
        protected OverlayCalculationCallback overlayCalculationCallback;
        protected GoogleMap.OnCameraChangeListener onCameraChangeListener = null;
        protected int markerDrawable = R.drawable.green_marker;
        protected Bitmap markerBitmap = null;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMap(GoogleMap map) {
            this.map = map;
            return this;
        }

        public Builder setLatLngData(List<LatLngWrapper> latLngData) {
            this.latLngWrappers.addAll(latLngData);
            return this;
        }

        public Builder setLatLngs(List<LatLng> latLngData) {
            for (LatLng latLng : latLngData) {
                this.latLngWrappers.add(new SimpleLatLng(latLng));
            }
            return this;
        }

        public Builder setOverlayCalculationCallback(OverlayCalculationCallback overlayCalculationCallback) {
            this.overlayCalculationCallback = overlayCalculationCallback;
            return this;
        }

        public Builder setOnCameraChangeListener(GoogleMap.OnCameraChangeListener onCameraChangeListener) {
            this.onCameraChangeListener = onCameraChangeListener;
            return this;
        }

        public Builder setMarkerDrawable(@DrawableRes int markerDrawable) {
            this.markerDrawable = markerDrawable;
            return this;
        }

        public Builder setMarkerBitmap(@NonNull Bitmap markerBitmap) {
            this.markerBitmap = markerBitmap;
            return this;
        }

        public HugeMapUtil build() throws Exception {
            if (context == null) {
                throw new Exception("No context specified for the builder.");
            }
            if (map == null) {
                throw new Exception("Google Maps not ready.");
            }
            return new HugeMapUtil(context, map, latLngWrappers, overlayCalculationCallback, onCameraChangeListener, markerDrawable, markerBitmap);
        }
    }

    public void setMarkerBitmap(@NonNull Bitmap markerBitmap) {
        glRenderer.SetupImage(markerBitmap);
    }

    public void setMarkerDrawable(@DrawableRes int markerDrawable) {
        setMarkerBitmap(BitmapFactory.decodeResource(context.getResources(), markerDrawable));
    }

    public void setLatLngs(List<LatLng> latLngs) {
        List<SimpleLatLng> list = new ArrayList<>();
        for (LatLng latLng : latLngs) {
            list.add(new SimpleLatLng(latLng));
        }
        this.latLngWrappers.clear();
        this.latLngWrappers.addAll(list);
        renderMarkers();
    }

    public void setLatLngWrappers(List<LatLngWrapper> latLngWrappers) {
        this.latLngWrappers.clear();
        this.latLngWrappers.addAll(latLngWrappers);
        renderMarkers();
    }

    public void setOnCameraChangeListener(GoogleMap.OnCameraChangeListener onCameraChangeListener) {
        this.onCameraChangeListener = onCameraChangeListener;
    }

    public void setOverlayCalculationCallback(OverlayCalculationCallback overlayCalculationCallback) {
        this.overlayCalculationCallback = overlayCalculationCallback;
    }

    private void checkIfRenderNeeded() {
        if (previousCenterPosition == null || !previousCenterPosition.equals(currentCenterPosition)) {
            previousCenterPosition = currentCenterPosition;
            renderCheckScheduleHandler.postDelayed(scheduledRenderCheck, 300);
        } else {
            renderMarkers();
        }
    }

    private void renderMarkers() {
        if (overlayCalculationCallback != null) {
            overlayCalculationCallback.onOverlayCalculationStarted();
        }

        if (latLngWrappers == null || latLngWrappers.size() == 0) {
            if (overlayCalculationCallback != null) {
                overlayCalculationCallback.onOverlayCalculationFinished(0);
            }
            return;
        }

        if (renderSubscription != null) {
            renderSubscription.unsubscribe();
        }

        renderSubscription = OverlayFactory.prepareRenderParameters(latLngWrappers, mMap.getProjection().getVisibleRegion().latLngBounds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        renderParameters -> {
                            pixelBuffer = new PixelBuffer(renderParameters.getDrawingSize().x, renderParameters.getDrawingSize().y);
                            glRenderer.setVertexList(renderParameters.getVertexList());
                            pixelBuffer.setRenderer(glRenderer);

                            Bitmap overlay = pixelBuffer.getBitmap();

                            LatLngBounds drawingBounds = renderParameters.getDrawingBounds();
                            if (drawingBounds == null) {
                                drawingBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                            }

                            final GroundOverlayOptions options =
                                    new GroundOverlayOptions()
                                            .positionFromBounds(drawingBounds)
                                            .image(BitmapDescriptorFactory.fromBitmap(overlay));

                            overlay.recycle();

                            if (actualOverlay != null) {
                                actualOverlay.remove();
                                actualOverlay = null;
                            }
                            if (overlayCalculationCallback != null) {
                                overlayCalculationCallback.onOverlayCalculationFinished(renderParameters.getMarkerCount());
                            }
                            if (mMap != null) {
                                Log.i("HugeMapUtil", "Overlay added");
                                actualOverlay = mMap.addGroundOverlay(options);
                            }
                        }, throwable -> {
                            throwable.printStackTrace();
                            if (overlayCalculationCallback != null) {
                                overlayCalculationCallback.onOverlayCalculationError(throwable);
                                overlayCalculationCallback.onOverlayCalculationFinished(0);
                            }
                        });

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i("Custom", "onCameraChange tilt: " + cameraPosition.tilt + " bearing: " + cameraPosition.bearing + " zoom: " + cameraPosition.zoom);
        currentCenterPosition = cameraPosition.target;
        renderCheckScheduleHandler.removeCallbacks(scheduledRenderCheck);
        checkIfRenderNeeded();

        if (onCameraChangeListener != null) {
            onCameraChangeListener.onCameraChange(cameraPosition);
        }
    }

    private Runnable scheduledRenderCheck = new Runnable() {
        @Override
        public void run() {
            checkIfRenderNeeded();
        }
    };
}
