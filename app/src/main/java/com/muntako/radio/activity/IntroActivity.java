//package com.muntako.radio.activity;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//
//import com.github.paolorotolo.appintro.AppIntro;
//import com.github.paolorotolo.appintro.AppIntroFragment;
//
///**
// * Created by ADMIN on 26-May-17.
// */
//
//public class IntroActivity extends AppIntro {
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        addSlide(AppIntroFragment.newInstance("Welcome!", "This is a demo of the AppIntro library.", R.drawable.ic_slide1, Color.parseColor("#1976D2")));
//        addSlide(AppIntroFragment.newInstance("Clean App Intros", "This library offers developers the ability to add clean app intros at the start of their apps.", R.drawable.ic_slide2, Color.parseColor("#1976D2")));
//        addSlide(AppIntroFragment.newInstance("Simple, yet Customizable", "The library offers a lot of customization, while keeping it simple for those that like simple.", R.drawable.ic_slide3, Color.parseColor("#1976D2")));
//        addSlide(AppIntroFragment.newInstance("Explore", "Feel free to explore the rest of the library demo!", R.drawable.ic_slide4, Color.parseColor("#1976D2")));
//
//
//        // OPTIONAL METHODS
//        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));
//
//        // Hide Skip/Done button.
//        showSkipButton(false);
//        setProgressButtonEnabled(false);
//
//        // Turn vibration on and set intensity.
//        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
//        setVibrate(true);
//        setVibrateIntensity(30);
//    }
//
//    @Override
//    public void onSkipPressed(Fragment currentFragment) {
//        super.onSkipPressed(currentFragment);
//        // Do something when users tap on Skip button.
//    }
//
//    @Override
//    public void onDonePressed(Fragment currentFragment) {
//        super.onDonePressed(currentFragment);
//        // Do something when users tap on Done button.
//    }
//
//    @Override
//    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
//        super.onSlideChanged(oldFragment, newFragment);
//        // Do something when the slide changes.
//    }
//}
