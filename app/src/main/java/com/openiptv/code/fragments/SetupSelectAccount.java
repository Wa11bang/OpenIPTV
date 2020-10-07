package com.openiptv.code.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.R;

import java.util.ArrayList;
import java.util.List;

public class SetupSelectAccount extends GuidedStepSupportFragment {
    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                getString(R.string.setup_select_account_title),
                getString(R.string.setup_select_account_description),
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        DatabaseActions databaseActions = new DatabaseActions(getContext());

        //get the data and append to a list


        Cursor accountList = databaseActions.getAccounts();

        ArrayList<String> accountClientNames = new ArrayList<>();
        while (accountList.moveToNext()) {
            //get the value from the database in client name column
            //then add it to the ArrayList
            accountClientNames.add(accountList.getString(5));
        }

        ArrayList<String> accountId = new ArrayList<>();

        accountList.moveToFirst();
        if (accountList.getCount() != 0) {
            do {
                //get the value from the database in id column
                //then add it to the ArrayList
                accountId.add(accountList.getString(0));
            } while (accountList.moveToNext());
        }

        List<GuidedAction> availableAccounts = new ArrayList<>();
        if (accountClientNames.size() > 0) {

            for (int i = 0; i < accountClientNames.size(); i++) {

                availableAccounts.add(new GuidedAction.Builder(getActivity())
                        .title(accountClientNames.get(i))
                        .id(Long.parseLong(accountId.get(i)))
                        .build());
            }
        } else {
            availableAccounts.add(new GuidedAction.Builder(getActivity())
                    .title(R.string.setup_select_account_no_accounts_message)
                    .editable(false)
                    .build());
        }

        GuidedAction accountSelector = new GuidedAction.Builder(getActivity())
                .title(R.string.setup_select_account_available_accounts)
                .description("")
                .editable(false)
                .subActions(availableAccounts)
                .build();
        actions.add(accountSelector);

        GuidedAction addNewAccount = new GuidedAction.Builder(getActivity())
                .title(R.string.setup_select_account_add_new_account)
                .editable(false)
                .build();

        actions.add(addNewAccount);
        databaseActions.close();

    }

    public void addNewAccountFragmentStarter() {
        GuidedStepSupportFragment fragment = new SetupNewAccountFragment();
        fragment.setArguments(getArguments());
        add(getFragmentManager(), fragment);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        /**
         * If add new account is selected start account setup fragment
         * Else, if the skip button is pressed continue without account
         */
        if (action.getTitle().toString().equals(getString(R.string.setup_select_account_add_new_account))) {
            addNewAccountFragmentStarter();
        } else if (action.getTitle().toString().equals(getString(R.string.setup_select_account_skip))) {


        }
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        if (action.getTitle().equals(getString(R.string.setup_select_account_no_accounts_message))) {
            addNewAccountFragmentStarter();
        } else {
            DatabaseActions databaseActions = new DatabaseActions(getContext());

            Cursor accountSelected = databaseActions.getAccountByID(String.valueOf(action.getId()));
            Bundle accountDetails = new Bundle(6);
            accountSelected.moveToFirst();
            accountDetails.putString("id", accountSelected.getString(0));
            accountDetails.putString("username", accountSelected.getString(1));
            accountDetails.putString("password", accountSelected.getString(2));
            accountDetails.putString("hostname", accountSelected.getString(3));
            accountDetails.putString("port", accountSelected.getString(4));
            accountDetails.putString("clientName", accountSelected.getString(5));

            databaseActions.setActiveAccount(accountSelected.getString(0));

            databaseActions.close();
            GuidedStepSupportFragment fragment = new SyncFragment();
            fragment.setArguments(accountDetails);
            add(getFragmentManager(), fragment);


        }
        return super.onSubGuidedActionClicked(action);
    }
}
