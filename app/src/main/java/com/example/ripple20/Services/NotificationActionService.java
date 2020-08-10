package com.example.ripple20.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionService extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        context.sendBroadcast(new Intent( "TRACKS_TRACKS")
                .putExtra( "actionname" , intent.getAction()));
    }
}
