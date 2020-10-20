package com.openiptv.code;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.leanback.preference.BaseLeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.openiptv.code.R;

//LeanbackSettingsFragmentCompat
public class PreferenceFragment extends LeanbackSettingsFragmentCompat {
    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(new DemoFragment());
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller,
                                           PreferenceScreen pref) {
        final Fragment fragment = new DemoFragment();
        final Bundle args = new Bundle(1);
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey());
        fragment.setArguments(args);
        startPreferenceFragment(fragment);
        return true;
    }

    /**
     * The fragment that is embedded in SettingsFragment
     */
    public static class DemoFragment extends LeanbackPreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(Constants.ACCOUNT);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
            // Load the preferences from an XML resource
            if (rootKey == null){
                addPreferencesFromResource(R.xml.preferences);
            } else {
                setPreferencesFromResource(R.xml.preferences, rootKey);
            }
        }
    }
}

