package com.muntako.radio.activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.muntako.radio.R;

/**
 * Created by akhmadmuntako on 20/09/2016.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread logotimer = new Thread() {
            public void run() {
                try {
                    int logotimer = 0;
                    while (logotimer < 3000) {
                        sleep(50);
                        logotimer = logotimer + 100;
                    }
                    //TODO edit next activity

                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };
        logotimer.start();
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
