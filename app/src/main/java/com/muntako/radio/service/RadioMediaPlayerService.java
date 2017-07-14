package com.muntako.radio.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.muntako.radio.R;
import com.muntako.radio.activity.MainActivity;
import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;

import java.io.IOException;

public class RadioMediaPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {

    //Variables
    private static final String TAG = RadioMediaPlayerService.class.getSimpleName();

    private final IBinder mMediaPlayerBinder = new MediaPlayerBinder();
    public boolean isPlaying = false;

    final String ACTION_PLAY = Constants.ACTION_PLAY;
    final String ACTION_PAUSE = Constants.ACTION_PAUSE;
    final String ACTION_CLOSE = Constants.ACTION_CLOSE;
    final String ACTION_CLOSE_IF_PAUSED = Constants.ACTION_CLOSE_IF_PAUSED;

    private static final int NOTIFICATION_ID = 4223; // just a number
    private MediaPlayer mMediaPlayer = null;
    private AudioManager mAudioManager = null;


    //Wifi Lock to ensure the wifi does not ge to sleep while we are streaming music.
    private WifiManager.WifiLock mWifiLock;
    String radioName;
    //    String radioUrl = "http://stream-tx1.radioparadise.com:8090/;stream/1";
    String radioUrl = "";
    ImageView imageRadio;
    Channel channel;
    Channel tempChannel;

    public  enum State {
        Stopped,  //Media player is stopped and not prepared to play
        Preparing, // Media player is preparing to play
        Playeng,  // MediaPlayer playback is active.
        // There is a chance that the MP is actually paused here if we do not have audio focus.
        // We stay in this state so we know to resume when we gain audio focus again.
        Paused // Audio Playback is paused
    }

    private State mState = State.Stopped;

    enum AudioFocus {
        NoFocusNoDuck, // service does not have audio focus and cannot duck
        NoFocusCanDuck, // we don't have focus but we can play at low volume ("ducking")
        Focused  // media player has full audio focus
    }

    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mAudioFocus = AudioFocus.Focused;
                // resume playback
                if (mState == State.Playeng) {
                    startMediaPlayer();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                mAudioFocus = AudioFocus.NoFocusNoDuck;
                // Lost focus for an unbounded amount of time: stop playback and release media player
                stopMediaPlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mAudioFocus = AudioFocus.NoFocusNoDuck;
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                processPauseRequest();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mAudioFocus = AudioFocus.NoFocusCanDuck;
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /**
     * Starts the streaming service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
            try {
                tempChannel = (Channel) intent.getSerializableExtra("channel");
            }catch (Exception e){
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


    private void setupAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
    }

    @SuppressLint("WifiManagerLeak")
    private void setupWifiLock() {
        if (mWifiLock == null) {
            mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mediaplayerlock");
        }
    }

    private void setupMediaPlayer(String radioUrl) {
        if (radioUrl == null){
            stopSelf();
        }else {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnInfoListener(this);
                mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(radioUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }
        }
    }


    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }


    /**
     * Checks if there is a data or internet connection before starting the stream.
     * Displays Toast warning if there is no connection
     *
     * @return online status boolean
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    //if the media player is paused or stopped and this method has been triggered then stop the service.
    private void closeIfPaused() {
        if (mState == State.Paused |mState == State.Stopped) {
            close();
        }else {
            mMediaPlayer.pause();
            mState = State.Paused;
            close();
        }
    }

    private void close() {
        if (!(mState == State.Paused | mState == State.Stopped)) {
            mMediaPlayer.pause();
            mState = State.Paused;
        }
        removeNotification();
        stopSelf();
    }

    private void initMediaPlayer() {
        if (channel!=null)
            setupMediaPlayer(channel.getUrlStreamStereo());
        requestResources();
    }

    public Channel getChannel(){
        Channel c = new Channel();
        if (isPlaying()){
            c = this.channel;
        }
        return c;

    }

    /**
     * Check if the media player was initialized and we have audio focus.
     * Without audio focus we do not start the media player.
     * change state and start to prepare async
     */
    private void configAndPrepareMediaPlayer() {
        initMediaPlayer();
        mState = State.Preparing;
        buildNotification(true);
        try {
            mMediaPlayer.prepareAsync();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The media player is prepared check to make sure we are not in the stopped or paused states
     * before starting the media player
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mState != State.Paused && mState != State.Stopped) {
            startMediaPlayer();
        }
    }

    /**
     * Check if the media player is available and start it.
     */
    private void startMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            sendUpdatePlayerIntent();
            mState = State.Playeng;
            buildNotification(false);
        }
    }

    private void sendUpdatePlayerIntent() {
        Log.d(TAG, "updatePlayerIntent");
        Intent updatePlayerIntent = new Intent(MainActivity.UPDATE_PLAYER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updatePlayerIntent);
    }

    /*
        Request audio focus and aquire a wifi lock. Returns true if audio focus was granted.
     */
    private void requestResources() {
        buildNotification(true);
        setupAudioManager();
        setupWifiLock();
        mWifiLock.acquire();
        tryToGetAudioFocus();
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN))
            mAudioFocus = AudioFocus.Focused;

    }

    /**
     * if the Media player is playing then stop it. Change the state and relax the wifi lock and
     * audio focus.
     */
    private void stopMediaPlayer() {
        // Lost focus for an unbounded amount of time: stop playback and release media player
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mState = State.Stopped;
        //relax the resources because we no longer need them.
        relaxResources();
        giveUpAudioFocus();
    }

    private void processPlayRequest() {
        if (mState == State.Paused) {
            requestResources();
            startMediaPlayer();
        } else if (mState == State.Stopped) {
            sendBufferingIntent();
            configAndPrepareMediaPlayer();
        } else {
            stopMediaPlayer();
            sendBufferingIntent();
            configAndPrepareMediaPlayer();
        }
    }

    //send an intent telling any activity listening to this intent that the media player is buffering.
    private void sendBufferingIntent() {
        Intent bufferingPlayerIntent = new Intent(MainActivity.BUFFERING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bufferingPlayerIntent);
    }

    private void processPauseRequest() {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            sendUpdatePlayerIntent();
            mState = State.Paused;
            relaxResources();
            buildNotification(false);
        }
    }

    public State getmState() {
        return mState;
    }

    /**
     * There is no media style notification for operating systems below api 21. So This method builds
     * a simple compat notification that has a play or pause button depending on if the player is
     * paused or played. if foreGroundOrUpdate then the service should go to the foreground. else
     * just update the notification.
     */
    private void buildNotification(boolean startForeground) {
        try {
            radioName = channel.getName();
            radioUrl = channel.getKota();
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), RadioMediaPlayerService.class);
        intent.setAction(ACTION_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(radioName).setContentText(radioUrl)
                .setSmallIcon(R.drawable.ic_radio).setOngoing(true)
                .setContentIntent(getMainContentIntent())
                .setDeleteIntent(pendingIntent);
        if (mState == State.Paused || mState == State.Stopped) {
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

    private PendingIntent getMainContentIntent() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), RadioMediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMediaPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        stopMediaPlayer();
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

    private void removeNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private void giveUpAudioFocus() {
        if ((mAudioFocus == AudioFocus.Focused || mAudioFocus == AudioFocus.NoFocusCanDuck) &&
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this)) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public class MediaPlayerBinder extends Binder {

        public RadioMediaPlayerService getService() {
            return RadioMediaPlayerService.this;
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

}
