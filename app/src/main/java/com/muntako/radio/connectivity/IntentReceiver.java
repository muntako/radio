package com.muntako.radio.connectivity;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.muntako.radio.service.ExoPlayerService;

public class IntentReceiver extends android.content.BroadcastReceiver {
    NotificationManager notificationManager;
    //    RadioMediaPlayerService radioService;
    ExoPlayerService player;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ExoPlayerService.MediaPlayerBinder binder = (ExoPlayerService.MediaPlayerBinder) service;
            player = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onReceive(Context ctx, Intent intent) {

//        Intent myIntent = new Intent(ctx, RadioMediaPlayerService.class);
        Intent myIntent = new Intent(ctx, ExoPlayerService.class);
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            ctx.stopService(myIntent);
        } else if (intent.getAction().equalsIgnoreCase("com.muntako.radio.ACTION_PLAY_PAUSE")) {

        } else if (intent.getAction().equalsIgnoreCase("com.muntako.radio.ACTION_STOP")) {
            try {
                ctx.stopService(myIntent);
            } catch (Exception e) {
            }
        }
    }

}
