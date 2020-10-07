package com.openiptv.code.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;

import java.util.List;

public class IntroFragment extends GuidedStepSupportFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getString(R.string.setup_activity_welcome),
                getString(R.string.setup_activity_description),
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.standard));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .title(getString(R.string.setup_activity_select_account))
                .description(getString(R.string.setup_activity_select_account_description))
                .editable(false)
                .build();

        actions.add(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        // Move onto the next step
        GuidedStepSupportFragment fragment = new SetupSelectAccount();
        fragment.setArguments(getArguments());
        add(getFragmentManager(), fragment);
    }
}