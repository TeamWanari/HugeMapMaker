package com.wanari.inifintemarker.model;

import com.google.android.gms.maps.model.LatLng;
import com.wanari.infinitemarker.data.LatLngWrapper;
import com.wanari.infinitemarker.data.SimpleLatLng;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class ContentManager {

    private static ContentManager instance;

    public static ContentManager getInstance() {
        if (instance == null) {
            instance = new ContentManager();
        }
        return instance;
    }

    public Observable<List<LatLngWrapper>> getRandomPositonsInHungary(int count) {
        double deltaLat = Constants.northEast.latitude - Constants.southWest.latitude;
        double deltaLng = Constants.northEast.longitude - Constants.southWest.longitude;
        return Observable.range(0, count)
                .map(new Func1<Integer, LatLngWrapper>() {
                    @Override
                    public LatLngWrapper call(Integer integer) {
                        return new SimpleLatLng(new LatLng(Constants.southWest.latitude + Math.random() * deltaLat,
                                Constants.southWest.longitude + Math.random() * deltaLng));
                    }
                }).toList();
    }

}
