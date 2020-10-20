package com.openiptv.code.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class TimerActivity extends FragmentActivity {
    private static final int CONTENT_VIEW_ID = 20202020;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        if (savedInstanceState == null) {
            SigninFragment fragment = new SigninFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(CONTENT_VIEW_ID, fragment).commit();


            /*TimerFragment fragment = new TimerFragment();
            fragment.show(getSupportFragmentManager(),null);*/
            //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.add(CONTENT_VIEW_ID, fragment).commit();
        }
    }
}
