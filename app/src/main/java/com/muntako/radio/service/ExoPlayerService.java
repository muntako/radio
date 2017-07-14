package com.muntako.radio.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.muntako.radio.R;
import com.muntako.radio.activity.MainActivity;
import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;

import se.greenbird.exoplayershoutcast.Metadata;
import se.greenbird.exoplayershoutcast.ShoutcastMetadataListener;

public class ExoPlayerService extends Service implements ShoutcastMetadataListener{
    final String ACTION_PLAY = Constants.ACTION_PLAY;
    final String ACTION_PAUSE = Constants.ACTION_PAUSE;
    final String ACTION_CLOSE = Constants.ACTION_CLOSE;
    final String ACTION_CLOSE_IF_PAUSED = Constants.ACTION_CLOSE_IF_PAUSED;


    private static final int NOTIFICATION_ID = 4223; // just a number

    @Override
    public void onMetadataReceived(Metadata data) {
        Log.i(TAG, "Metadata Received");
        Log.i(TAG, "Artist: " + data.getArtist());
        Log.i(TAG, "Song: " + data.getSong());
        Log.i(TAG, "Show: " + data.getShow());
    }

    public enum State {
        Stopped,
        Playing,
        Paused
    }

    private State mState = State.Stopped;

    private static final String TAG = "ExoPlayerService";

    private SimpleExoPlayer player;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DataSource.Factory mediaDataSourceFactory;
    private Handler mainHandler;

    Channel channel;
    Channel tempChannel;
    private final IBinder mMediaPlayerBinder = new MediaPlayerBinder();
    //Wifi Lock to ensure the wifi does not ge to sleep while we are streaming music.
    private WifiManager.WifiLock mWifiLock;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
            try {
                tempChannel = (Channel) intent.getSerializableExtra("channel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tempChannel != null)
            channel = tempChannel;

        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    processPlayRequest();
                    break;
                case ACTION_PAUSE:
                    processPauseRequest();
                    break;
                case ACTION_CLOSE_IF_PAUSED:
                    closeIfPaused();
                    break;
                case ACTION_CLOSE:
                    close();
                    break;
            }
        }
        return START_STICKY; //do not restart service if it is killed.
    }

    protected void preparePlayer(String url) {

        mediaDataSourceFactory = buildDataSourceFactory(true);

        mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        Uri uri = Uri.parse(url);

//        Default Media Source
        MediaSource mediaSource = buildMediaSource(uri, "mp3");
        player.prepare(mediaSource);

        player.setPlayWhenReady(true);
        sendUpdatePlayerIntent();
        mState = State.Playing;
        buildNotification(true);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.d(TAG, "onTimelineChanged: ");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.d(TAG, "onTracksChanged: " + trackGroups.length);
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onLoadingChanged: " + isLoading);
                sendBufferingIntent();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: " + playWhenReady);
                sendUpdatePlayerIntent();
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: ", error);
                sendErrorIntent();
                close();
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.d(TAG, "onPositionDiscontinuity: true");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerDemo"), bandwidthMeter);
    }

    @SuppressLint("WifiManagerLeak")
    private void setupWifiLock() {
        if (mWifiLock == null) {
            mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mediaplayerlock");
        }
    }
    //give up wifi lock if it is held and stop the service from being a foreground service.
    private void relaxResources() {

        //Release the WifiLock resource
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }


        // stop service from being a foreground service. Passing true removes the notification as well.
        stopForeground(true);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMediaPlayerBinder;
    }

    @Override
    public void onDestroy() {
        stopMediaPlayer();
    }

    public class MediaPlayerBinder extends Binder {

        public ExoPlayerService getService() {
            return ExoPlayerService.this;
        }
    }

    /**
     * if the Media player is playing then stop it.
     */
    private void stopMediaPlayer() {
        if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
        mState = State.Stopped;
        removeNotification();
    }

    private void processPlayRequest() {
        buildNotification(false);
        if (player != null) {
            player.release();
        }
        setupWifiLock();
        mWifiLock.acquire();
        sendBufferingIntent();
        preparePlayer(channel.getUrlStreamStereo());
    }

    private void processPauseRequest() {
        if (player!=null) {
            player.setPlayWhenReady(false);
            mState = State.Paused;
        }
        buildNotification(false);
        relaxResources();
    }

    private void close() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
        }
        relaxResources();
        removeNotification();
    }

    private void closeIfPaused() {
        player.setPlayWhenReady(false);
        mState = State.Stopped;
        close();
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isPlaying() {
        if (player == null) {
            return false;
        }
        return player.getPlayWhenReady();
    }

    public State getmState() {
        return mState;
    }

    //send an intent telling any activity listening to this intent that the media player is buffering.
    private void sendBufferingIntent() {
        Intent bufferingPlayerIntent = new Intent(MainActivity.BUFFERING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bufferingPlayerIntent);
    }

    private void sendUpdatePlayerIntent() {
        Log.d(TAG, "updatePlayerIntent");
        Intent updatePlayerIntent = new Intent(MainActivity.UPDATE_PLAYER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updatePlayerIntent);
    }

    private void sendErrorIntent(){
        Intent errorPlayerIntent = new Intent(MainActivity.ERROR);
        LocalBroadcastManager.getInstance(this).sendBroadcast(errorPlayerIntent);
    }

    /**
     * There is no media style notification for operating systems below api 21. So This method builds
     * a simple compat notification that has a play or pause button depending on if the player is
     * paused or played. if foreGroundOrUpdate then the service should go to the foreground. else
     * just update the notification.
     */
    private void buildNotification(boolean startForeground) {
        String radioName = null;
        String radioUrl = null;
        try {
            radioName = channel.getName();
            radioUrl = channel.getKota();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), ExoPlayerService.class);
        intent.setAction(ACTION_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(radioName).setContentText(radioUrl)
                .setSmallIcon(R.drawable.ic_radio).setOngoing(true)
                .setContentIntent(getMainContentIntent())
                .setDeleteIntent(pendingIntent);
        if (mState == ExoPlayerService.State.Paused || mState == ExoPlayerService.State.Stopped) {
            builder.addAction(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        } else {
            builder.addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }
        builder.addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", ACTION_CLOSE));

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (startForeground)
            startForeground(NOTIFICATION_ID, builder.build());
        else
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private PendingIntent getMainContentIntent() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), ExoPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }


}