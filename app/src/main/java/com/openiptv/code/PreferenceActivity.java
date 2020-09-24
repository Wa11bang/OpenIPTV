package com.openiptv.code;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class PreferenceActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, new PreferenceFragment())
                .commit();

    }
}
