package com.wanari.inifintemarker.view;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.wanari.infinitemarker.HugeMapUtil;
import com.wanari.infinitemarker.overlay.OverlayCalculationCallback;
import com.wanari.inifintemarker.model.Constants;
import com.wanari.inifintemarker.model.ContentManager;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, OverlayCalculationCallback {

    private HugeMapUtil hugeMapUtil;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            hugeMapUtil = new HugeMapUtil.Builder(getActivity())
                    .setOverlayCalculationCallback(this)
                    .setMap(googleMap)
                    .setMarkerDrawable(com.wanari.infinitemarker.R.drawable.marker_blue)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getMarkerPositions();
    }

    private void getMarkerPositions() {
        ContentManager.getInstance().getRandomPositonsInHungary(Constants.GENERATED_MARKER_COUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(latLngWrappers -> {
                    if (hugeMapUtil != null) {
                        hugeMapUtil.setLatLngWrappers(latLngWrappers);
                    }
                });
    }

    @Override
    public void onOverlayCalculationStarted() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoader();
        }
    }

    @Override
    public void onOverlayCalculationFinished(int shownMarkerCount) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideLoader();
            ((MainActivity) getActivity()).updateShownMarkerCount(shownMarkerCount);
        }
    }

    @Override
    public void onOverlayCalculationError(Throwable throwable) {
        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
        throwable.printStackTrace();
    }
}
