package com.openiptv.code.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.R;
import com.openiptv.code.ParentControlPassword;

import java.util.Date;
import java.util.List;

public class SetParentControlPassword extends GuidedStepSupportFragment {
    final long PASSWORD = 0L;
    final long CONFIRM_PASSWORD = 1L;
    final long NEXT = 1L;
    /**
     * this bundle contain account information such as client name, port....
     * <p>
     * this bundle is passed from SetupNewAccountFragment or SetupSelectAccountFragment
     * <p>
     * the meaning of this is if a user want to use parent control function and want to set up a
     * password for it then the link the password with account
     */
    Bundle accountInfor;
    String username;
    String password;

    public SetParentControlPassword(Bundle accountInfor) {
        this.accountInfor = accountInfor;
    }

    public SetParentControlPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

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
        if (this.password == null && username == null) {
            if (action.getId() == NEXT) {
                ParentControlPassword password = new ParentControlPassword(
                        findActionById(PASSWORD).getTitle().toString(),
                        findActionById(CONFIRM_PASSWORD).getTitle().toString());

                password.checkPassword();

                if (password.getPass() == false) {
                    Toast.makeText(getContext(), "Passwords are not identical or has space", Toast.LENGTH_SHORT).show();
                } else {
                    this.accountInfor.putString("parentControl", findActionById(PASSWORD).getTitle().toString());

                    DatabaseActions dbAction = new DatabaseActions(getContext());

                    boolean result = dbAction.addParentControlPasswordToDB(this.accountInfor);

                    /**
                     * this commented code are for testing purpose, see if the parent control password saved
                     * in the database
                     * */
               /* Cursor search = dbAction.getAccountByClientName(accountInfor.getString("clientName"));

                while (search.moveToNext())
                {
                    String text = search.getString(search.getColumnIndex("parent"));
                    if (text.length()!=0)
                    {
                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                    }
                }*/


                    dbAction.close();
                    if (result == true) {
                        GuidedStepSupportFragment fragment = new SyncFragment();
                        fragment.setArguments(getArguments());
                        add(getParentFragmentManager(), fragment);
                    } else {
                        Toast.makeText(getContext(), "Database action failure", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (username != null && password != null) {
            if (action.getId() == NEXT) {
                ParentControlPassword password = new ParentControlPassword(
                        findActionById(PASSWORD).getTitle().toString(),
                        findActionById(CONFIRM_PASSWORD).getTitle().toString());
                password.checkPassword();
                if (password.getPass() == false) {
                    Toast.makeText(getContext(), "Passwords are not identical or has space", Toast.LENGTH_SHORT).show();
                } else {
                    this.accountInfor = new Bundle();

                    this.accountInfor.putString("username", this.username);
                    this.accountInfor.putString("password", this.password);
                    this.accountInfor.putString("parentControl", findActionById(PASSWORD).getTitle().toString());

                    DatabaseActions dbAction = new DatabaseActions(getContext());

                    boolean result = dbAction.addParentControlPasswordToDB(this.accountInfor);

                    dbAction.close();

                    if (result == true) {
                        Toast.makeText(getContext(),"Parent Control password update successfully!", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Database action failure, from preference sides", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
