package com.openiptv.code.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;

import java.util.List;

/**
 * this fragment is used to set up the parent control
 */
public class ParentControlFragment extends GuidedStepSupportFragment {
    final long NEXT = 0L;
    final long SKIP = 1L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getString(R.string.parent_control_title),
                getString(R.string.parent_control_des),
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.standard));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        /**
         * setPassword is hit and will lead program to a new fragment
         *
         * skip will lead program proceed which is the SyncFragment
         * */
        GuidedAction setPassword = new GuidedAction.Builder(getActivity())
                .title(R.string.parent_control_set_password_title)
                .description(R.string.parent_control_set_password_des)
                .id(NEXT)
                .editable(false)
                .build();
        GuidedAction skip = new GuidedAction.Builder(getActivity())
                .title(R.string.parent_control_skip)
                .id(SKIP)
                .editable(false)
                .build();
        actions.add(setPassword);
        actions.add(skip);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == SKIP) {
            GuidedStepSupportFragment fragment = new SyncFragment();
            fragment.setArguments(getArguments());
            add(getParentFragmentManager(), fragment);
        } else if (action.getId() == NEXT) {
            GuidedStepSupportFragment fragment = new SetParentControlPassword();
            fragment.setArguments(getArguments());
            add(getParentFragmentManager(), fragment);
        }
    }
}
