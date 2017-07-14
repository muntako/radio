package com.muntako.radio.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Created by ADMIN on 09-May-17.
 */

public class SpeechRecognitionService extends Service implements RecognitionListener {

    private SpeechRecognizer recognizer;

    private static final int SPEECH_REQUEST_CODE = 0;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String PHONE_SEARCH = "phones";
    private static final String CHANNEL_SEARCH = "channels";
    private static final String PLAYER_SEARCH = "player";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "radio";

    private final IBinder PocketSphinxBinder = new PocketSphincBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return PocketSphinxBinder;
    }

    public class PocketSphincBinder extends Binder{
        public SpeechRecognitionService getService(){
            return  SpeechRecognitionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = null;
        if (intent != null) {
            action = intent.getAction();
            try {
                switch (action){
                    case Constants.RECOGNITION_START :
                        runRecognizerSetup();
                        break;
                    case Constants.RECOGNITION_STOP :
                        recognizer.stop();
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplicationContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
//                    ((TextView) findViewById(R.id.caption_text))
//                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

//        String caption = getResources().getString(captions.get(searchName));
//        Toast.makeText(this,caption,Toast.LENGTH_SHORT).show();
//        ((TextView) findViewById(R.id.caption_text)).setText(caption);

    }

    /**
     * Stop intent is sent to the service so the service can determine if it should stop. If the
     * media player is paused when any activity triggers onPause then the service should stop itself.
     */
    private void sendStopIntent() {
        Intent stopIntent = new Intent(this, RadioMediaPlayerService.class);
        stopIntent.setAction(Constants.ACTION_CLOSE);
        startService(stopIntent);
    }

    private void sendPauseIntent(){
        Intent intent = new Intent(this, RadioMediaPlayerService.class);
        intent.setAction(Constants.ACTION_PAUSE);
        startService(intent);
    }

    private void volumeLouder(){
        AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
    }
    private void volumeSlower(){
        AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void volumeMute(){
        AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setStreamMute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
//                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File playerGrammar = new File(assetsDir, "player1.gram");
        recognizer.addGrammarSearch(PLAYER_SEARCH, playerGrammar);

        File channelGrammar = new File(assetsDir, "channels.gram");
        recognizer.addGrammarSearch(CHANNEL_SEARCH, channelGrammar);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            switchSearch(PLAYER_SEARCH);
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }


    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.toLowerCase().contains("stop")) {
                sendStopIntent();
            } else if (text.toLowerCase().contains("paus")) {
                sendPauseIntent();
            } else if (text.toLowerCase().contains("play")) {
//                String radio = text.substring(text.indexOf("putar")+6,text.length());
//                doSearch("mustang");
//                switchSearch(CHANNEL_SEARCH);
            } else if (text.toLowerCase().contains("next")) {
//                doSearch("female");
            } else if (text.toLowerCase().contains("loud")) {
                volumeLouder();
            }else if (text.toLowerCase().contains("slow")) {
                volumeSlower();
            } else if (text.toLowerCase().contains("mute")) {
                volumeMute();
            }else if (text.toLowerCase().contains("radio")) {
                switchSearch(PLAYER_SEARCH);
            }else {
                String toSpeak = " tidak dikenal";
                Toast.makeText(this,text + toSpeak,Toast.LENGTH_SHORT).show();
//                tts.speak(text + " " + toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }

            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}
