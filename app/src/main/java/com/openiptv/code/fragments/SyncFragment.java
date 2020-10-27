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
    private static final String TAG = SetupActivity.class.getName();

    @Override
    public void onSyncComplete() {
        Log.d(TAG, "Initial Sync Completed");

        // Move to the CompletedFragment
        GuidedStepSupportFragment fragment = new CompletedFragment();
        fragment.setArguments(getArguments());
        add(getParentFragmentManager(), fragment);
    }


    @Override
    public void onStart() {
        super.onStart();
        mEpgSyncTask = new EPGCaptureTask(getActivity().getBaseContext());
        mEpgSyncTask.addSyncListener(this);
    }

    @Override
    public void onStop() {
        mEpgSyncTask.stop();
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
                "Syncing with TVHeadend",
                "Please wait...",
                getString(R.string.account_label),
                null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .title("Progress")
                .infoOnly(true)
                .build();
        actions.add(action);
    }
}