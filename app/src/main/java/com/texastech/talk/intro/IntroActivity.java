package com.texastech.talk.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.texastech.talk.MainActivity;
import com.texastech.talk.R;

public class IntroActivity extends AppIntro2 {

    public static final String LAUNCHED_APP_BEFORE = "IntroActivity.LaunchedAppBefore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SlideHostFragment.newInstance(R.layout.layout_intro_slide1));
        addSlide(SlideHostFragment.newInstance(R.layout.layout_intro_slide2));
        addSlide(SlideHostFragment.newInstance(R.layout.layout_intro_slide3));
        addSlide(SlideHostFragment.newInstance(R.layout.layout_intro_slide4));
        addSlide(SlideHostFragment.newInstance(R.layout.layout_intro_slide6));

        showSkipButton(false);
        showStatusBar(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {

        super.onDonePressed(currentFragment);

        setLaunchedAppBefore();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.QUERY_MOOD_PARAMETER, true);
        finish();
        startActivity(intent);
    }

    void setLaunchedAppBefore() {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(LAUNCHED_APP_BEFORE, true);
        editor.apply();
    }
}