package com.openiptv.code;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

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
                if (key.equals()){

                }
            }

        };

    }

}
