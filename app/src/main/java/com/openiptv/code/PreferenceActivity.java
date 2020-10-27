package com.openiptv.code;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.openiptv.code.fragments.PreferenceFragment;

public class PreferenceActivity extends FragmentActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, new PreferenceFragment())
                .commit();

        /**
         * Listener which performs functions when preferences are changed.
         * @param sharedPreferences Preference that is changed
         * @param key Preferences key
         */
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("STREAM_PROFILE")){
                    System.out.println("Testing for stream profile.");
                }
            }
        };

    }

}
