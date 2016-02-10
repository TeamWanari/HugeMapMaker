package com.wanari.infinitemarker.overlay;

/**
 * Created by agocs on 2016.01.21..
 */
public interface OverlayCalculationCallback {

    void onOverlayCalculationStarted();

    void onOverlayCalculationFinished();

    void onOverlayCalculationError(Throwable throwable);

}
