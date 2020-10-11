package com.openiptv.code.manage.account;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.R;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.manage.account.EditAccount.accountToEdit;

public class ManageAccountActivity extends FragmentActivity {
    private static final String TAG = ManageAccountActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepSupportFragment fragment = new AccountOptions();
        fragment.setArguments(getIntent().getExtras());
        GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    public static abstract class BaseGuidedStepFragment extends GuidedStepSupportFragment {
        @Override
        public int onProvideTheme() {
            return R.style.Theme_Setup;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static class AccountOptions extends GuidedStepSupportFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    getString(R.string.manage_account_title),
                    getString(R.string.manage_account_description),
                    getString(R.string.account_label),
                    ContextCompat.getDrawable(getActivity(), R.drawable.standard));
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

        @Override
        public void onGuidedActionClicked(GuidedAction action) {

        }

        @Override
        public boolean onSubGuidedActionClicked(GuidedAction action) {
            if (action.getTitle().equals(getString(R.string.setup_select_account_no_accounts_message))) {
                //TODO start new account activity
            } else {
                DatabaseActions databaseActions = new DatabaseActions(getContext());

                Cursor accountSelected = databaseActions.getAccountByID(String.valueOf(action.getId()));

                accountSelected.moveToFirst();


                // If trying to edit active account
                if (DatabaseActions.activeAccount.getString("id").equals(accountSelected.getString(0))) {
                    Toast.makeText(getContext(), "Cannot edit an active account", Toast.LENGTH_SHORT).show();
                } else {
                    accountToEdit = accountSelected;

                    GuidedStepSupportFragment fragment = new EditAccount();
                    add(getFragmentManager(), fragment);
                }
                databaseActions.close();
            }
            return super.onSubGuidedActionClicked(action);
        }
    }
}
