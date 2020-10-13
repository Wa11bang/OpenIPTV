package com.openiptv.code.fragments;

import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;

import java.util.List;

public class ChangeParentControlPassword extends GuidedStepSupportFragment {

    final static long USERNAME = 1L;
    final static long PASSWORD = 2L;
    final static long NEXT = 3L;

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
        GuidedAction username = new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.change_parent_control_username_des)
                .editable(true)
                .id(USERNAME)
                .build();
        GuidedAction password = new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.change_parent_control_password_des)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editable(true)
                .id(PASSWORD)
                .build();
        GuidedAction next = new GuidedAction.Builder(getActivity())
                .title(R.string.change_parent_control_next)
                .editable(false)
                .id(NEXT)
                .build();
        actions.add(username);
        actions.add(password);
        actions.add(next);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == NEXT)
        {

        }
    }
}
