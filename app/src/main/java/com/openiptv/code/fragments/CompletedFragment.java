package com.openiptv.code.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;
import com.openiptv.code.epg.EPGService;

import java.util.List;

public class CompletedFragment extends GuidedStepSupportFragment {
    private static final int ACTION_ID_SETTINGS = 1;
    private static final int ACTION_ID_COMPLETE = 2;
    private static final String TAG = CompletedFragment.class.getName();
    private FragmentManager fragmentManager;

    public CompletedFragment(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getResources().getString(R.string.CompletedFragment_guidance_title),
                getResources().getString(R.string.CompletedFragment_guidance_description),
                getString(R.string.account_label),
                null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_COMPLETE)
                .title(getResources().getString(R.string.CompletedFragment_action_title))
                .description(getResources().getString(R.string.CompletedFragment_action_description))
                .editable(false)
                .build();

        actions.add(action);

        GuidedAction test = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_SETTINGS)
                .title("test button")
                .description("test desc")
                .editable(false)
                .build();
        actions.add(test);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == ACTION_ID_COMPLETE) {

            Log.d(TAG, "Exiting Setup!");
            Intent intent = new Intent(getActivity(), EPGService.class);
            getActivity().startService(intent);

            // Wrap up setup
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();

        } else if (action.getId() == ACTION_ID_SETTINGS) {
            GuidedStepSupportFragment sync = new SyncFragment(getFragmentManager());
            sync.setArguments(getArguments());
            add(this.fragmentManager, sync);
        }
    }
}
