package com.openiptv.code;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;

import com.openiptv.code.epg.EPGCaptureTask;
import com.openiptv.code.epg.EPGService;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.epg.EPGService.setSetupComplete;

public class SetupActivity extends FragmentActivity {
    private static final String TAG = SetupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepSupportFragment fragment = new IntroFragment();
        fragment.setArguments(getIntent().getExtras());
        GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    public int getTest()
    {
        return 10;
    }

    public static abstract class BaseGuidedStepFragment extends GuidedStepSupportFragment {
        AccountManager mAccountManager;

        static Account sAccount;

        @Override
        public int onProvideTheme() {
            return R.style.Theme_Setup;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAccountManager = AccountManager.get(getActivity());
        }
    }

    public static class IntroFragment extends GuidedStepSupportFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    "Welcome Title",
                    "A short description of the application",
                    getString(R.string.account_label),
                    ContextCompat.getDrawable(getActivity(), R.drawable.standard));
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .title("Button")
                    .description("Button action")
                    .editable(false)
                    .editInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            // Move onto the next step
            GuidedStepSupportFragment fragment = new SyncFragment();
            fragment.setArguments(getArguments());
            add(getFragmentManager(), fragment);
        }
    }

    public static class SyncFragment extends BaseGuidedStepFragment implements EPGCaptureTask.Listener {
            EPGCaptureTask mEpgSyncTask;
            BaseConnection connection;

            @Override
            public void onSyncComplete() {
                Log.d(TAG, "Initial Sync Completed");

                // Move to the CompletedFragment
                GuidedStepSupportFragment fragment = new CompletedFragment();
                fragment.setArguments(getArguments());
                add(getFragmentManager(), fragment);
            }

            @Override
            public void onStart() {
                super.onStart();
                mEpgSyncTask = new EPGCaptureTask(getActivity().getBaseContext());
                mEpgSyncTask.addSyncListener(this);
            }

            @Override
            public void onStop() {
                mEpgSyncTask = null;

                super.onStop();
            }

            @Override
            public GuidedActionsStylist onCreateActionsStylist() {
                return new GuidedActionsStylist() {
                    @Override
                    public int onProvideItemLayoutId() {
                        return R.layout.setup_progress;
                    }
                };
            }

            @NonNull
            @Override
            public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

                return new GuidanceStylist.Guidance(
                        "title",
                        "body",
                        getString(R.string.account_label),
                        null);
            }

            @Override
            public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
                GuidedAction action = new GuidedAction.Builder(getActivity())
                        .title("Progress")
                        .infoOnly(true)
                        .build();
                actions.add(action);
            }
    }

    public static class AccountFragment extends GuidedStepSupportFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    "View Accounts",
                    "this is accounts",
                    getString(R.string.account_label),
                    ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

            List<GuidedAction> subActions = new ArrayList<GuidedAction>();
            subActions.add(new GuidedAction.Builder(getActivity())
                    .id(1)
                    .title("action2")
                    .description("desc")
                    .build());

            subActions.add(new GuidedAction.Builder(getActivity())
                    .id(1)
                    .title("action3")
                    .description("desc")
                    .build());



            GuidedAction actionWithSubActions = new GuidedAction.Builder(getActivity())
                    .title("Button")
                    .description("Button action")
                    .editable(false)
                    .subActions(subActions)
                    .build();

            GuidedAction action2 = new GuidedAction.Builder(getActivity())
                    .title("Button")
                    .description("Button action")
                    .editable(false)
                    .build();

            actions.add(actionWithSubActions);
            actions.add(action2);
        }

        @Override
        public boolean onSubGuidedActionClicked(GuidedAction action) {
            // Check for which action was clicked, and handle as needed
            if (action.getId() == 1) {
                Toast.makeText(getActivity(), "You clicked action2", Toast.LENGTH_SHORT).show();
            }
            // Return true to collapse the subactions drop-down list, or
            // false to keep the drop-down list expanded.
            return true;
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            // Move onto the next step
            //GuidedStepSupportFragment fragment = new EmptyFragment();
            //fragment.setArguments(getArguments());
            //add(getFragmentManager(), fragment);
        }
    }

    public static class CompletedFragment extends GuidedStepSupportFragment {
        private static final int ACTION_ID_SETTINGS = 1;
        private static final int ACTION_ID_COMPLETE = 2;

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    "Completed",
                    "Complete body",
                    getString(R.string.account_label),
                    null);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_COMPLETE)
                    .title("Complete title")
                    .description("Complete body")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ID_COMPLETE) {

                setSetupComplete(getActivity(), true);

                Log.d(TAG, "Exiting Setup!");
                Intent intent = new Intent(getActivity(), EPGService.class);
                getActivity().startService(intent);

                // Wrap up setup
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            }
        }
    }
}
