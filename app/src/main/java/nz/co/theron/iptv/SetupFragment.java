package nz.co.theron.iptv;

import android.os.Bundle;
import android.text.style.TtsSpan;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;


import java.util.List;

public class SetupFragment extends GuidedStepSupportFragment {

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                "Create an Account",
                "Enter TVHeadend Credentials",
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {



        GuidedAction action = new GuidedAction.Builder(getActivity())
                .title("Create Account")
                .description("Click to start")
                .editable(false)
                .build();

        actions.add(action);
    




    }



}
