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
    private static final String TAG = SettingsFragment.class.getName();



    private PreferenceFragment preferenceFragment;



    @Override
    public void onPreferenceStartInitialScreen() {
        preferenceFragment = buildPreferenceFragment(null);
        startPreferenceFragment(preferenceFragment);
    }



    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        return false;
    }



    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        PreferenceFragment fragment = buildPreferenceFragment(preferenceScreen.getKey());



        startPreferenceFragment(fragment);



        return true;
    }



    private PreferenceFragment buildPreferenceFragment(String root) {
        PreferenceFragment fragment = new CustomLeanbackPreferenceFragment();



        Bundle args = new Bundle();
        args.putString(PreferenceFragment.ARG_PREFERENCE_ROOT, root);
        fragment.setArguments(args);



        return fragment;
    }



    @Override
    public Preference findPreference(CharSequence charSequence) {
        return preferenceFragment.findPreference(charSequence);
    }



    public static class CustomLeanbackPreferenceFragment extends LeanbackPreferenceFragment {



        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            getPreferenceManager().setSharedPreferencesName(Constants.PREFERENCES_NAME);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);



            String root = getArguments().getString(PreferenceFragment.ARG_PREFERENCE_ROOT, null);



            if (root == null) {
                addPreferencesFromResource(R.xml.preferences);
            } else {
                setPreferencesFromResource(R.xml.preferences, root);
            }
        }



        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }
    }
}
