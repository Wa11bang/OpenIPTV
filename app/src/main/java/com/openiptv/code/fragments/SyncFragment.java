package com.openiptv.code.fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;

import com.openiptv.code.R;
import com.openiptv.code.SetupActivity;
import com.openiptv.code.epg.EPGCaptureTask;
import com.openiptv.code.htsp.BaseConnection;

import java.util.List;

public class SyncFragment extends BaseGuidedStepFragment implements EPGCaptureTask.Listener {
    EPGCaptureTask mEpgSyncTask;
    BaseConnection connection;
    private static final String TAG = SyncFragment.class.getName();
    private FragmentManager fm;

    public SyncFragment(FragmentManager fragmentManager) {
        this.fm = fragmentManager;
    }

    @Override
    public void onSyncComplete() {
        Log.d(TAG, "Initial Sync Completed");

        // Move to the CompletedFragment
        GuidedStepSupportFragment fragment = new CompletedFragment(getFragmentManager());
        fragment.setArguments(getArguments());
        add(this.fm, fragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        mEpgSyncTask = new EPGCaptureTask(getActivity().getBaseContext());
        mEpgSyncTask.addSyncListener(this);
    }

    @Override
    public void onStop() {
        mEpgSyncTask = null;

        super.onStop();
    }

    @Override
    public GuidedActionsStylist onCreateActionsStylist() {
        return new GuidedActionsStylist() {
            @Override
            public int onProvideItemLayoutId() {
                return R.layout.setup_progress;
            }
        };
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getResources().getString(R.string.SyncFragment_guidance_title),
                getResources().getString(R.string.SyncFragment_guidance_description),
                getString(R.string.account_label),
                null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .title(getResources().getString(R.string.SyncFragment_action_title))
                .infoOnly(true)
                .build();
        actions.add(action);
    }
}