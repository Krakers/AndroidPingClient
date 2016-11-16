package com.lakomy.tomasz.androidpingclient;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.util.Log;

public class MemoryBoss implements ComponentCallbacks2 {
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(final int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            Log.d("aping", "App is in background");
            PingServerActivity.isInBackground = true;
            PingServerActivity.restartTransmission();
        }
    }
}