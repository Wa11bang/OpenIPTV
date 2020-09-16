package com.openiptv.code.fragments;

import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.R;
import com.openiptv.code.SetupActivity;

import java.util.List;

public class IntroFragment extends GuidedStepSupportFragment {

    private static final String TAG = IntroFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getResources().getString(R.string.IntroFragment_guidance_title),
                getResources().getString(R.string.IntroFragment_guidance_description),
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.standard));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .title(getResources().getString(R.string.IntroFragment_action_title))
                .description(getResources().getString(R.string.IntroFragment_action_description))
                .editable(false)
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .build();

        actions.add(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        // Move onto the next step
        GuidedStepSupportFragment fragment = new AccountFragment();
        fragment.setArguments(getArguments());
        add(getFragmentManager(), fragment);
    }
}