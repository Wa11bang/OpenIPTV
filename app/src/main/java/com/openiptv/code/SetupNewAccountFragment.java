package com.openiptv.code;

import android.content.ContentResolver;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.openiptv.code.htsp.Authenticator;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.PREFERENCE_SETUP_COMPLETE;

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
                .editInputType(InputType.TYPE_CLASS_NUMBER)
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
                if (findActionById(i).getTitle().toString().equals("")) {
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
                if (addAccountToDatabase(newAccount)) {


                    DatabaseActions databaseActions = new DatabaseActions(getContext());


                    String accountId = databaseActions.getActiveAccount();
                    databaseActions.setActiveAccount(accountId);

                    databaseActions.close();
                    GuidedStepSupportFragment fragment = new SetupActivity.SyncFragment();
                    fragment.setArguments(newAccountDetails);
                    add(getParentFragmentManager(), fragment);
                }
                else{
                    Log.d("AddAccount", "Error, adding account. Check field is not empty");
                }
            }
        }
    }

    /**
     * Takes a TVHeadend Account and adds it to the database.
     *
     * @param account
     */
    private static Authenticator.State state;
    public boolean addAccountToDatabase(TVHeadendAccount account) {
        // Check if user login is successful


        BaseConnection connection = new BaseConnection(new ConnectionInfo(account.getHostname(), Integer.parseInt(account.getPort()), account.getUsername(), account.getPassword(), account.getClientName(), "23"));

        Authenticator.Listener listener = new Authenticator.Listener() {

            @Override
            public void onAuthenticated(Authenticator.State inState) {
                state = inState;
            }
        };

        connection.getAuthenticator().addListener(listener);
        connection.start();

        long timeoutTime = System.currentTimeMillis() + (20*100);
        while(state == null)
        {
            if(timeoutTime < System.currentTimeMillis())
            {
                state = Authenticator.State.FAILED;
            }
            Log.v("BW", "Waiting for Server Response");
        }

        /*
         * Delete all existing content for account
         */
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceUtils preferenceUtils = new PreferenceUtils(getActivity());

        if(preferenceUtils.getBoolean(PREFERENCE_SETUP_COMPLETE))
        {
            // TODO: Make sure logo directories for the channels get deleted as well.
            getActivity().getContentResolver().delete(TvContract.Channels.CONTENT_URI, null, null);
            getActivity().getContentResolver().delete(TvContract.Programs.CONTENT_URI, null, null);
            getActivity().getContentResolver().delete(TvContract.RecordedPrograms.CONTENT_URI, null, null);
        }

        if(state == Authenticator.State.AUTHENTICATED)
        {
            DatabaseActions databaseActions = new DatabaseActions(getContext());
            boolean status = databaseActions.addAccount(account);
            databaseActions.close();

            connection.getAuthenticator().removeListener(listener);
            connection.stop();
            state = null;
            return status;
        }

        Toast.makeText(getContext(), "Server Response: " + state.name(), Toast.LENGTH_SHORT).show();

        connection.getAuthenticator().removeListener(listener);
        connection.stop();
        state = null;

        return false;
    }
}
