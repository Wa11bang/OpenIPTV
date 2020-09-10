package com.openiptv.code;

import android.accounts.Account;
import android.os.Bundle;
import android.text.InputType;
import android.text.style.TtsSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionEditText;
import androidx.leanback.widget.GuidedActionsStylist;


import java.util.ArrayList;
import java.util.List;


public class SetupNewAccountFragment extends GuidedStepSupportFragment {

    ArrayList<String> accountDetails;

    final Long USERNAME = 0L;
    final Long PASSWORD = 1L;
    final Long HOSTNAME = 2L;
    final Long PORT = 3L;
    final Long CLIENT = 4L;


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
                .title("")
                .id(USERNAME)
                .description("Enter a Username")
                .editable(true)
                .build();

        GuidedAction passwordForm = new GuidedAction.Builder(getActivity())
                .title("")
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .description("Enter a Password")
                .id(PASSWORD)
                .editable(true)
                .build();

        GuidedAction hostnameForm = new GuidedAction.Builder(getActivity())
                .title("")
                .description("Enter a Hostname")
                .id(HOSTNAME)
                .editable(true)
                .build();

        GuidedAction portForm = new GuidedAction.Builder(getActivity())
                .title("")
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER)
                .description("Enter a Port")
                .id(PORT)
                .editable(true)
                .build();

        GuidedAction clientNameForm = new GuidedAction.Builder(getActivity())
                .title("")
                .description("Enter a Client Name")
                .id(CLIENT)
                .editable(true)
                .build();



        GuidedAction finishButton = new GuidedAction.Builder(getActivity())
                .title("Next")
                .editable(false)
                .build();





        actions.add(usernameForm);
        actions.add(passwordForm);
        actions.add(hostnameForm);
        actions.add(portForm);
        actions.add(clientNameForm);
        actions.add(finishButton);



    }



    //TODO in the Final setup fragment add the account to the database
    // to avoid duplication of accounts by pressing back and then next.
    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction formResults) {
        return super.onGuidedActionEditedAndProceed(formResults);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getTitle().toString().equals("Next")) {



                Bundle newAccountDetails = new Bundle(5);

                newAccountDetails.putString("username", findActionById(USERNAME).getTitle().toString());
                newAccountDetails.putString("password", findActionById(PASSWORD).getTitle().toString());
                newAccountDetails.putString("hostname", findActionById(HOSTNAME).getTitle().toString());
                newAccountDetails.putString("port", findActionById(PORT).getTitle().toString());
                newAccountDetails.putString("clientName", findActionById(CLIENT).getTitle().toString());

                Boolean emptyField = false;

                for (Long i = 0L; i < 5; i++){
                    if (findActionById(i).getTitle().equals("")){
                        emptyField = true;
                        Toast.makeText(getContext(),findActionById(i).getDescription().toString(),Toast.LENGTH_SHORT).show();
                        break;

                    }
                }

                if (emptyField == false ){


                    TVHeadendAccount newAccount = new TVHeadendAccount(newAccountDetails);

                    DatabaseActions databaseActions= new DatabaseActions(getContext());
                    databaseActions.addAccount(newAccount);
                    databaseActions.close();

                    GuidedStepSupportFragment fragment = new EmptyTestFragment();
                    fragment.setArguments(getArguments());
                    add(getFragmentManager(), fragment);
                }



        }

    }
}
