package com.openiptv.code;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;

import java.util.Arrays;
import java.util.Stack;


/**
 * Fragment that provides user settings preferences.
 */

public class SettingsFragment extends LeanbackSettingsFragment implements DialogPreference.TargetFragment {
    private static final Stack<Fragment> fragments = new Stack<Fragment>();


    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.preferences, null));
    }


    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        return false;
    }


    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.preferences, preferenceScreen.getKey());
        startPreferenceFragment(frag);
        return true;
    }


    @Override
    public Preference findPreference(CharSequence prefKey) {
        return ((PreferenceFragment) fragments.peek()).findPreference(prefKey);
    }


    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("preferenceResource", preferenceResId);
        args.putString("root", root);
        fragment.setArguments(args);
        return fragment;
    }

    public static class PrefFragment extends LeanbackPreferenceFragment {


        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString("root", null);
            int prefResId = getArguments().getInt("preferenceResource");
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }
        }


        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String[] keys = {"prefs_wifi_connect_wps", "prefs_date", "prefs_time",
                    "prefs_date_time_use_timezone", "app_banner_sample_app", "pref_force_stop",
                    "pref_uninstall", "pref_more_info"};
            if (Arrays.asList(keys).contains(preference.getKey())) {
                Toast.makeText(getActivity(), "Implement your own action handler.", Toast.LENGTH_SHORT).show();
                return true;
            }
            return super.onPreferenceTreeClick(preference);
        }


        @Override
        public void onAttach(Context context) {
            fragments.push(this);
            super.onAttach(context);
        }


        @Override
        public void onDetach() {
            fragments.pop();
            super.onDetach();
        }
    }
}
