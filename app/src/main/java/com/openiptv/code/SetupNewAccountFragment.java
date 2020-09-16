package com.openiptv.code;

import android.accounts.Account;
import android.content.Intent;
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
    SelectedBundle selectedBundle;

    final Long USERNAME = 0L;
    final Long PASSWORD = 1L;
    final Long HOSTNAME = 2L;
    final Long PORT = 3L;
    final Long CLIENT = 4L;


    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getString(R.string.setup_new_account_fragment_title),
                getString(R.string.setup_new_account_fragment_description),
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {


        GuidedAction usernameForm = new GuidedAction.Builder(getActivity())
                .title("")
                .id(USERNAME)
                .description(getString(R.string.setup_new_account_fragment_username))
                .editable(true)
                .build();

        GuidedAction passwordForm = new GuidedAction.Builder(getActivity())
                .title("")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .description(R.string.setup_new_account_fragment_password)
                .id(PASSWORD)
                .editable(true)
                .build();

        GuidedAction hostnameForm = new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.setup_new_account_fragment_hostname)
                .id(HOSTNAME)
                .editable(true)
                .build();

        GuidedAction portForm = new GuidedAction.Builder(getActivity())
                .title("")
                .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER)
                .description(R.string.setup_new_account_fragment_port)
                .id(PORT)
                .editable(true)
                .build();

        GuidedAction clientNameForm = new GuidedAction.Builder(getActivity())
                .title("")
                .description(R.string.setup_new_account_fragment_client)
                .id(CLIENT)
                .editable(true)
                .build();


        GuidedAction finishButton = new GuidedAction.Builder(getActivity())
                .title(R.string.setup_new_account_fragment_next)
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

    public interface SelectedBundle {
        void onBundleSelect(Bundle bundle);
    }

    public void setOnBundleSelected(SelectedBundle selectedBundle) {
        this.selectedBundle = selectedBundle;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getTitle().toString().equals(getString(R.string.setup_new_account_fragment_next))) {

            Bundle newAccountDetails = new Bundle(5);

            Boolean emptyField = false;

            for (Long i = 0L; i < 5; i++) {
                if (findActionById(i).getTitle().equals("")) {
                    emptyField = true;
                    Toast.makeText(getContext(), findActionById(i).getDescription().toString(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if (emptyField == false) {

                newAccountDetails.putString("username", findActionById(USERNAME).getTitle().toString());
                newAccountDetails.putString("password", findActionById(PASSWORD).getTitle().toString());
                newAccountDetails.putString("hostname", findActionById(HOSTNAME).getTitle().toString());
                newAccountDetails.putString("port", findActionById(PORT).getTitle().toString());
                newAccountDetails.putString("clientName", findActionById(CLIENT).getTitle().toString());

                TVHeadendAccount newAccount = new TVHeadendAccount(newAccountDetails);
                addAccountToDatabase(newAccount);

                //TODO Fix, currently not working
                GuidedStepSupportFragment fragment = new SetupActivity.SyncFragment();
                fragment.setArguments(newAccountDetails);
                add(getFragmentManager(), fragment);

            }


        }

    }

    /**
     * Takes a TVHeadend Account and adds it to the database.
     *
     * @param account
     */
    public void addAccountToDatabase(TVHeadendAccount account) {
        DatabaseActions databaseActions = new DatabaseActions(getContext());
        databaseActions.addAccount(account);
        databaseActions.close();
    }
}
