package com.muntako.radio.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.muntako.radio.DatabaseHelper;
import com.muntako.radio.Fragment.FavoritFragment;
import com.muntako.radio.Fragment.MainFragment;
import com.muntako.radio.Fragment.NowPlayingFragment;
import com.muntako.radio.R;
import com.muntako.radio.adapter.GridAdapter;
import com.muntako.radio.model.Channel;
import com.muntako.radio.model.Constants;
import com.muntako.radio.service.ExoPlayerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NowPlayingFragment.GetMainService, RecognitionListener, SearchView.OnQueryTextListener {

    private ArrayList<Channel> Channels = new ArrayList<>();
    Boolean isMute = false;
    MenuItem mSearchAction;
    private Channel channel;
    private NowPlayingFragment playingFragment;
    SearchView searchView;
    GridAdapter gridAdapter;
    int indexChannel = 0;
    int current_volume ;

    SharedPreferences prefs = null;

    private static final int NOTIFICATION_ID = 4223; // just a number

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String UPDATE_PLAYER = TAG + ".update_player";
    public static final String BUFFERING = TAG + ".buffering_player";
    public static final String ERROR = TAG + ".player_error";

    private static String[] previous = {"back", "previous"};
    private static String[] next = {"skip", "next", "forward"};
    private static String[] play = {"play", "resume"};
    private static String[] pause = {"pause", "wait", "hold"};
    private static String[] mute = {"shut up", "let me hear you", "mute"};
    private static String[] quieter = {"quieter", "turn it down", "slow", "volume down"};
    private static String[] louder = {"louder", "turn it up", "volume up"};
    private static String[] channel_list = {"female", "mustang", "sonora", "pas", "ras", "bens", "star", "she", "hits", "thomson"};
    private static String[] polite = {"oh mighty", "please", "ok"};
    private static String[] quit = {"quit", "end", "close", "exit"};

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String PHONE_SEARCH = "phones";
    private static final String CHANNEL_SEARCH = "channels";
    private static final String PLAYER_SEARCH = "player";
    private static final String OPTION = "option";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "okay radio";

    private SpeechRecognizer recognizer;

//    RadioMediaPlayerService radioService;
    ExoPlayerService radioService;

    private boolean mBound;


    TextToSpeech tts;
    boolean mIsActive;
    boolean isPausedByCommand;
    private DatabaseHelper databaseHelper;
//    private RecognitionProgressView recognitionProgressView;


    private HashMap<String, Integer> captions;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ExoPlayerService.MediaPlayerBinder binder = (ExoPlayerService.MediaPlayerBinder) service;
            radioService = binder.getService();
            if (radioService.isPlaying()) {
                channel = radioService.getChannel();
                showPlaybackControls(channel);
                updateMediaPlayerToggle();
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    /**
     * this broadcast receiver is receiving intents from the media player service and calling the
     * appropriate methods to update the media player bar.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UPDATE_PLAYER)) {
                updateMediaPlayerToggle();
            } else if (intent.getAction().equals(BUFFERING)) {
                showMediaPlayerBuffering();
            }else if (intent.getAction().equals(ERROR)){
                showErrorPlayer();
            }
        }
    };

    private void showMediaPlayerBuffering() {
        NowPlayingFragment nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.now_playing_fragment);
        if (nowPlayingFragment != null) {
            nowPlayingFragment.showBuffering(true);
        }
    }

    private void updateMediaPlayerToggle() {
        NowPlayingFragment nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.now_playing_fragment);
        if (nowPlayingFragment != null) {
            nowPlayingFragment.updateToggle();
        }
    }

    private void showErrorPlayer(){
        tts.speak("I am Sorry, Player Error",TextToSpeech.QUEUE_FLUSH,null);
        hidePlaybackControls();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsActive = true;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);

        prefs = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);

        databaseHelper = new DatabaseHelper(this);
        gridAdapter = new GridAdapter(this);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    try {
                        tts.setLanguage(Locale.US);
                    } catch (Exception e) {
                        tts.setLanguage(Locale.US);
                        e.printStackTrace();
                    }
                }
            }
        });

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(PLAYER_SEARCH, R.string.phone_caption);

        captions.put(OPTION, R.string.option);

        runRecognizerSetup();

        //Allow hardware audio buttons to control volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Channels = new ArrayList<>();

        try {
            readJsonStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //setting drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //setting navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            replaceFragment(new MainFragment());
        }
        playingFragment = (NowPlayingFragment) getFragmentManager()
                .findFragmentById(R.id.now_playing_fragment);
        if (playingFragment == null) {
            Toast.makeText(this, "Mising fragment with id 'controls'. Cannot continue.", Toast.LENGTH_SHORT).show();
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }
        hidePlaybackControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;
        registerBroadcastReceiver();
        Intent mediaIntent = new Intent(this, ExoPlayerService.class);
        startService(mediaIntent);
        if (radioService == null) {
            bindService(mediaIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (radioService != null) {
            unbindService(mServiceConnection);
            radioService = null;
        }
        unRegisterBroadcastReceiver();
        mBound = false;
        mIsActive = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            int hasCameraPermission = checkSelfPermission(Manifest.permission.);
//
//            List<String> permissions = new ArrayList<String>();
//
//            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(Manifest.permission.CAMERA);
//
//            }
//            if (!permissions.isEmpty()) {
//                requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
//            }
//        }
    }



    public void showPlaybackControls(final Channel c) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mIsActive) {
                    playingFragment.setmNowPlaying(c);
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                            .show(playingFragment)
                            .commit();
                }
            }
        });

    }

    protected void hidePlaybackControls() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mIsActive)
                    getFragmentManager().beginTransaction()
                            .hide(playingFragment)
                            .commit();
            }
        });
    }

    /**
     * Register the broadcast receiver te receive intents from the media player service.
     */
    private void registerBroadcastReceiver() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter updateIntentFilter = new IntentFilter();
        updateIntentFilter.addAction(UPDATE_PLAYER);
        updateIntentFilter.addAction(BUFFERING);
        updateIntentFilter.addAction(ERROR);
        broadcastManager.registerReceiver(broadcastReceiver, updateIntentFilter);
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Stop intent is sent to the service so the service can determine if it should stop. If the
     * media player is paused when any activity triggers onPause then the service should stop itself.
     */
    private void sendStopIntent() {
        removeNotification();
        Intent stopIntent = new Intent(this, ExoPlayerService.class);
        hidePlaybackControls();
        stopIntent.setAction(Constants.ACTION_CLOSE);
        startService(stopIntent);
    }


    private void sendPauseIntent() {
        Intent intent = new Intent(this, ExoPlayerService.class);
        intent.setAction(Constants.ACTION_PAUSE);
        startService(intent);
    }

    private boolean sendReplayAfterPaused(){
        if (radioService != null && radioService.getmState() == ExoPlayerService.State.Paused) {
            Intent mediaPlayerIntent = new Intent(MainActivity.this, ExoPlayerService.class);
            mediaPlayerIntent.setAction(Constants.ACTION_PLAY);
            startService(mediaPlayerIntent);
            return true;
        }
        return false;
    }

    private void volumeLouder() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
    }

    private void volumeSlower() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
    }

    //    @TargetApi(Build.VERSION_CODES.M)
    private void volumeMute() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (isMute) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current_volume, AudioManager.FLAG_PLAY_SOUND);
            isMute = false;
        } else {
            current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
            isMute = true;
        }
    }

    private void next() {
        if (indexChannel < (gridAdapter.getChannels().size()-1)) {
            playChannel(gridAdapter.getChannels().get(indexChannel + 1));
            indexChannel++;
        } else {
            playChannel(gridAdapter.getChannels().get(0));
            indexChannel = 0;
        }
    }

    private void previous() {
        if (indexChannel > 0) {
            playChannel(gridAdapter.getChannels().get(indexChannel - 1));
            indexChannel--;
        } else {
            playChannel(gridAdapter.getChannels().get(gridAdapter.getChannels().size() - 1));
            indexChannel = gridAdapter.getChannels().size() - 1;
        }
    }

    private void playRandom() {
        Random rand = new Random();
        if (gridAdapter.getChannels().size() != 0) {
            int n = rand.nextInt(gridAdapter.getChannels().size());
            playChannel(gridAdapter.getChannels().get(n));
            showPlaybackControls(gridAdapter.getChannels().get(n));
        }
    }

    public int getIndexChannel() {
        return indexChannel;
    }

    public void setIndexChannel(int indexChannel) {
        this.indexChannel = indexChannel;
    }

    public ArrayList<Channel> getChannels() {
        return Channels;
    }


    public void readJsonStream() throws IOException {

        AssetManager manager = getApplicationContext().getAssets();
        InputStream input = manager.open("allradios.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        com.muntako.radio.model.Channels tes1 = gson.fromJson(sb.toString(), com.muntako.radio.model.Channels.class);
        List<Channel> channelFromJson = tes1.getChannels();

        if (databaseHelper.getAllChannel().size() == 0) {
            databaseHelper.CreateRadio(channelFromJson);
            Channels = (ArrayList<Channel>) databaseHelper.getAllChannel();
        }
    }

    public void doSearch(String text) {
        boolean ada = false;
        for (Channel c : gridAdapter.getChannels()) {
            if (c.getName().toLowerCase().contains(text)) {
                playChannel(c);
                ada = true;
            }
        }
        if (!ada) {
            for (int i = 0; i < channel_list.length; i++) {
                if (text.contains(channel_list[i])) {
                    text = channel_list[i];
                    for (Channel c : gridAdapter.getChannels()) {
                        if (c.getName().toLowerCase().contains(text)) {
                            playChannel(c);
                        }else {
                            tts.speak("Radio not found",TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                }
            }
        }
    }

    public void playChannel(Channel c) {
        Intent intent = new Intent(this,
                ExoPlayerService.class);
        stopService(intent);
        intent.setAction(Constants.ACTION_PLAY);
        intent.putExtra("channel", (Serializable) c);
        intent.putExtra("name", c.getName());
        intent.putExtra("gambar", c.getPathlogo());
        startService(intent);
        showPlaybackControls(c);
    }

    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            replaceFragment(new MainFragment());
        } else if (id == R.id.nav_favorit) {
            replaceFragment(new FavoritFragment());
        }
//        else if (id == R.id.nav_record) {
//            //open test
//            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//            if (drawer.isDrawerOpen(GravityCompat.START)) {
//                drawer.closeDrawer(GravityCompat.START);
//            }
//            if(recognizer !=null){
//                recognizer.stop();
//                recognizer.shutdown();
//            }
//            sendPauseIntent();
//            sendStopIntent();
//            startActivity(new Intent(this, info.class));
//            finish();
//        }
        else if (id == R.id.nav_exit) {
            showExitDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showExitDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Warning");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                        exit();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    public void exit() {
        if (recognizer != null) {
            recognizer.stop();
            recognizer.shutdown();
        }
        removeNotification();
        sendStopIntent();
        tts.speak("Thank you", TextToSpeech.QUEUE_FLUSH, null);
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                R.animator.slide_in_from_left, R.animator.slide_out_to_right).replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public ExoPlayerService getMainService() {
        return radioService;
    }

    public Channel getChannel() {
        return getMainService().getChannel();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
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
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

//         If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KEYPHRASE))
            recognizer.startListening(PLAYER_SEARCH);
        else
            recognizer.startListening(searchName, 5000);

        try {
            String caption = getResources().getString(captions.get(searchName));
            ((TextView) findViewById(R.id.caption_text)).setText(caption);
            Log.d("caption", caption);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        //TODO add recognizer
//        recognitionProgressView.setSpeechRecognizer(recognizer);
//        recognitionProgressView.setRecognitionListener(this);
//        recognitionProgressView.play();


        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File playerGrammar = new File(assetsDir, "player.gram");
        recognizer.addGrammarSearch(PLAYER_SEARCH, playerGrammar);

        File optionGrammar = new File(assetsDir, "option.gram");
        recognizer.addGrammarSearch(OPTION, optionGrammar);
//
//        File channelGrammar = new File(assetsDir, "channels.gram");
//        recognizer.addGrammarSearch(CHANNEL_SEARCH, channelGrammar);

        // Phonetic search
//        File phoneticModel = new File(assetsDir, "en-phone.dmp");
//        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
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
        Log.d("hasil partial", text);
        if (text.equals(KEYPHRASE)) {
            tts.speak("Yes", TextToSpeech.QUEUE_FLUSH, null);
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

        if (hypothesis == null) {
            Log.d("hasil", "null");
            return;
        }
        String text = hypothesis.getHypstr().toLowerCase();
        Log.d("hasil", text);
        if (text.contains(KEYPHRASE)) {
            sendPauseIntent();
            isPausedByCommand = true;
        } else if (text.contains("jakarta")) {
//            search("jakarta");
        }else if (text.contains("stop")) {
            sendStopIntent();
        } else if (text.contains("random")) {
            playRandom();
        } else if (stringContainsItemFromList(text, pause)) {
            sendPauseIntent();
        } else if (stringContainsItemFromList(text, channel_list)) {
            doSearch(text);
        } else if (stringContainsItemFromList(text, louder)) {
            volumeLouder();
        } else if (stringContainsItemFromList(text, quieter)) {
            volumeSlower();
        } else if (stringContainsItemFromList(text, mute)) {
            volumeMute();
        } else if (stringContainsItemFromList(text, next)) {
            next();
        } else if (stringContainsItemFromList(text, previous)) {
            previous();
        } else if (stringContainsItemFromList(text, play)) {
            if (!sendReplayAfterPaused()){
                playRandom();
            }
        } else if (stringContainsItemFromList(text, quit)) {
            tts.speak("Are you sure want to quit", TextToSpeech.QUEUE_FLUSH, null);
            switchSearch(OPTION);
        } else if (text.contains("yes")) {
            exit();
        } else if (text.contains("no")) {
            tts.speak("OK",TextToSpeech.QUEUE_FLUSH,null);
        } else {
            if (stringContainsItemFromList(text, polite)) {
                switchSearch(KEYPHRASE);
            } else {
                tts.speak(text + "sorry, command not found", TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        if (isPausedByCommand){
            sendReplayAfterPaused();
            isPausedByCommand = false;
        }

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (inputStr.contains(items[i]))
                return true;
        }
        return false;
    }

    public GridAdapter getGridAdapter() {
        return gridAdapter;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        gridAdapter.getFilter().filter(newText);
        return true;
    }

    public void search(String kataKunci){
        gridAdapter.getFilter().filter(kataKunci);
    }
}
