package com.wanari.inifintemarker.view;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.wanari.infinitemarker.HugeMapUtil;
import com.wanari.inifintemarker.model.ContentManager;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private HugeMapUtil hugeMapUtil;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            hugeMapUtil = new HugeMapUtil.Builder(getContext())
                    .setMap(googleMap)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ContentManager.getInstance().getRandomPositonsInHungary(500000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(latLngWrappers -> {
                    if (hugeMapUtil != null) {
                        hugeMapUtil.setLatLngWrappers(latLngWrappers);
                    }
                });
    }
}
