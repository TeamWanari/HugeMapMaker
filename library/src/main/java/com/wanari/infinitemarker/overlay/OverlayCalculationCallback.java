package com.wanari.infinitemarker.overlay;

/**
 * Created by agocs on 2016.01.21..
 */
public interface OverlayCalculationCallback {

    void onOverlayCalculationStarted();

    void onOverlayCalculationFinished(int shownMarkerCount);

    void onOverlayCalculationError(Throwable throwable);

}
