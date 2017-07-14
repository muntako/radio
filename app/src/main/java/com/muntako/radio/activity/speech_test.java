package com.muntako.radio.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.muntako.radio.R;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import retrofit2.http.OPTIONS;

import static android.R.attr.path;

/**
 * Created by ADMIN on 17-May-17.
 */

public class speech_test extends AppCompatActivity implements View.OnClickListener, RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String PLAYER_SEARCH = "player";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "radio";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    ArrayList<String> arrayList = new ArrayList<>();

    Button control;

    int currentText = 0;

    String deviceName = android.os.Build.MODEL;
    String deviceMan = android.os.Build.MANUFACTURER;
    int musik_on;

    private String[] testWord = {
            "go back", "previous", "skip", "next", "play", "resume", "pause", "wait", "hold",
            "shut up", "let me hear you", "mute", "quieter", "turn it down", "slow", "volume down", "louder", "turn it up", "volume up",
            "female ", "mustang ", "sonora ", "stop", "speak to me", "pas ", "ras ", "star ", "bens ", "she ",
            "hits ", "quit", "close", "end", "exit", "channel", "random","favorite", "jakarta","oh mighty computer","thank you"};

    final List<String> testText = Arrays.asList(testWord);
    TextView caption, test, resultText;
    MediaPlayer mediaPlayer;
    ToggleButton toggleButton;
    String ujiKata, hasil, nama;
    ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_test);

        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.test);
        captions.put(PLAYER_SEARCH, R.string.say);
        dialog = new ProgressDialog(this);

        control = (Button) findViewById(R.id.control_button);
        toggleButton = (ToggleButton) findViewById(R.id.toggle_music);

        nama = getIntent().getStringExtra("nama");

        Target target = new ViewTarget(R.id.text_test, this);
        new ShowcaseView.Builder(this, true)
                .setTarget(target)
                .setStyle(R.style.ShowcaseView)
                .setContentTitle("Say this word/ keyphrase")
                .setContentText("Click start button, and say text that you see on this field")
                .build();


        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("http://live.masima.co.id:8000/female");
            mediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        caption = (TextView) findViewById(R.id.caption_text);
        resultText = (TextView) findViewById(R.id.result_text);
        test = (TextView) findViewById(R.id.text_test);

        caption.setText("Preparing the recognizer");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(speech_test.this);
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
                    caption.setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
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
        if (text.contains(KEYPHRASE)){
            switchSearch(PLAYER_SEARCH);
        }
        Log.d("text partial", text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            hasil = text;
            resultText.setText(text);
            Log.d("text", text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(KEYPHRASE)) {
            recognizer.startListening(PLAYER_SEARCH, 10000);
        } else {
            recognizer.startListening(searchName, 10000);
        }

        try {
            String caption = getResources().getString(captions.get(searchName));
            ((TextView) findViewById(R.id.caption_text)).setText(caption);
        } catch (Exception e) {
            Log.d("exception", e.getMessage());
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

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);


        File playerGrammar = new File(assetsDir, "player.gram");
        recognizer.addGrammarSearch(PLAYER_SEARCH, playerGrammar);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_button:
                testStart();
                break;
            case R.id.toggle_music:
                toogleMusic();
                break;
        }
    }

    public void toogleMusic() {
        if (toggleButton.isChecked()) {
            mediaPlayer.start();
            musik_on = 1;
        } else {
            mediaPlayer.pause();
            musik_on = 0;
        }
    }

    public void next() {
        arrayList.add(hasil);
        hasil = "";
        if (currentText > 0 && currentText < testText.size()) {
            test.setText(testText.get(currentText));
            ujiKata = testText.get(currentText);
            currentText++;
        } else if (currentText == testText.size()) {
            finishTest();
        }
    }

    public void finishTest() {
        test.setText("test telah selesai");
    }


    public void testStart() {
        switchSearch(PLAYER_SEARCH);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (currentText == 0) {
                    test.setText(testText.get(0));
                    currentText++;
                    ujiKata = testText.get(0);
                    control.setText("Next");
                } else if (currentText == testWord.length - 1) {
                    control.setText("Finish");
                    next();
                } else if (currentText == testWord.length) {
                    control.setText("Retest");
                    processData();
                } else {
                    next();
                }
            }
        });
    }

    public boolean checking(String input, String result) {
        if (input.equalsIgnoreCase(result)) {
            return true;
        }
        return false;
    }

    public void processData() {
        arrayList.add(hasil);
        dialog.setTitle("Loading......");
        dialog.show();
        String URL = "http://perhimak-ui.id/radio/dataRespondent.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        currentText = 0;
                        arrayList.clear();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(speech_test.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("nama", nama);
                for (int i = 0; i < arrayList.size(); i++) {
                    int a = i + 1;
                    params.put("data_" + a, String.valueOf(arrayList.get(i)));
                }
                params.put("deviceName",deviceName);
                params.put("deviceMan",deviceMan);
                params.put("musik_on", String.valueOf(musik_on));

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Warning");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure want to exit and stop this test?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity

                        if (mediaPlayer != null)
                            mediaPlayer.stop();
                        if (recognizer != null) {
                            recognizer.stop();
                            recognizer.shutdown();
                        }
                        startActivity(new Intent(speech_test.this, MainActivity.class));
                        speech_test.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
}
