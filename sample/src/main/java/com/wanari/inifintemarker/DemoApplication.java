package com.wanari.inifintemarker;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public class DemoApplication extends Application {

    private static DemoApplication instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        if (!BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
