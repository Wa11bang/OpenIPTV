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
import androidx.leanback.widget.GuidedActionEditText;


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



        GuidedAction usernameForm = new GuidedAction.Builder(getActivity())
                .title("Username")
                .description("Enter a Username")
                .editable(true)
                .build();

        GuidedAction passwordForm = new GuidedAction.Builder(getActivity())
                .title("Password")
                .description("Enter a Password")
                .descriptionEditable(true)
                .editable(false)
                .build();

        GuidedAction hostnameForm = new GuidedAction.Builder(getActivity())
                .title("Hostname")
                .description("Enter a Hostname")
                .editable(true)
                .build();

        GuidedAction portForm = new GuidedAction.Builder(getActivity())
                .title("Port")
                .description("Enter a Port")
                .editable(true)
                .build();

        GuidedAction clientNameForm = new GuidedAction.Builder(getActivity())
                .title("Client Name")
                .description("Enter a Client Name")
                .editable(true)
                .build();

        actions.add(usernameForm);
        actions.add(passwordForm);
        actions.add(hostnameForm);
        actions.add(portForm);
        actions.add(clientNameForm);







    }





}
