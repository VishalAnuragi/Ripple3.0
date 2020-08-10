package com.example.ripple20.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class OnClearFromRecentService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }
}
