package com.openiptv.code.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;
import com.openiptv.code.ParentControlPassword;

import java.util.List;

public class SetParentControlPassword extends GuidedStepSupportFragment {
    final long PASSWORD = 0L;
    final long CONFIRM_PASSWORD = 1L;
    final long NEXT = 1L;

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
        actions.add(new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.parent_control_password)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editable(true)
                .id(PASSWORD)
                .build());
        actions.add(new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.parent_control_confirm_password)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editable(true)
                .id(CONFIRM_PASSWORD)
                .build());
        actions.add(new GuidedAction.Builder(getActivity())
                .title(R.string.parent_control_next)
                .editable(false)
                .id(NEXT)
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == NEXT) {
            ParentControlPassword password = new ParentControlPassword(
                    findActionById(PASSWORD).getTitle().toString(),
                    findActionById(CONFIRM_PASSWORD).getTitle().toString());

            password.checkPassword();

            if (password.getPass() == false) {
                Toast.makeText(getContext(), "Passwords are not identical or has space", Toast.LENGTH_SHORT).show();
            } else {
                GuidedStepSupportFragment fragment = new SyncFragment();
                fragment.setArguments(getArguments());
                add(getParentFragmentManager(), fragment);
            }
        }
    }
}
