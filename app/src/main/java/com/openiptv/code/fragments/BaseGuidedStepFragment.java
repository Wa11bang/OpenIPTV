package com.openiptv.code.fragments;

import android.os.Bundle;

import androidx.leanback.app.GuidedStepSupportFragment;

import com.openiptv.code.R;

public abstract class BaseGuidedStepFragment extends GuidedStepSupportFragment {
    @Override
    public int onProvideTheme() {
        return R.style.Theme_Setup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
