package com.example.ripple20.Services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;


public class TimerService extends Service {
    public int onStartCommand(Intent intent, int flags, int startId) {

       SharedPreferences preferencesTimer = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String time =  preferencesTimer.getString("TIME_SECONDS" , "0");
        return START_STICKY ;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
