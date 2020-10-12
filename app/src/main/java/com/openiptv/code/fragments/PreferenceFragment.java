package com.openiptv.code.fragments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
        final Bundle args = pref.getExtras();
        final Fragment f = getChildFragmentManager().getFragmentFactory().instantiate(
                requireActivity().getClassLoader(), pref.getFragment());
        f.setArguments(args);
        f.setTargetFragment(caller, 0);
        if (f instanceof PreferenceFragmentCompat
                || f instanceof PreferenceDialogFragmentCompat) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
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
            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("changePassword"))
            {
                Toast.makeText(getContext(),"Your clicked the change password", Toast.LENGTH_SHORT).show();

            }
            if (preference.getKey().equals("timer"))
            {

            }


            return super.onPreferenceTreeClick(preference);
        }
    }
}

