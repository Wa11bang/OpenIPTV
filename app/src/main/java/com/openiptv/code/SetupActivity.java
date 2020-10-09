package com.openiptv.code;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;

import com.openiptv.code.fragments.IntroFragment;

public class SetupActivity extends FragmentActivity {
    private static final String TAG = SetupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepSupportFragment fragment = new IntroFragment();
        fragment.setArguments(getIntent().getExtras());
        GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    /*public static abstract class BaseGuidedStepFragment extends GuidedStepSupportFragment {
        @Override
        public int onProvideTheme() {
            return R.style.Theme_Setup;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }*/

    /*public static class IntroFragment extends GuidedStepSupportFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    getString(R.string.setup_activity_welcome),
                    getString(R.string.setup_activity_description),
                    getString(R.string.account_label),
                    ContextCompat.getDrawable(getActivity(), R.drawable.standard));
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .title(getString(R.string.setup_activity_select_account))
                    .description(getString(R.string.setup_activity_select_account_description))
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            // Move onto the next step
            GuidedStepSupportFragment fragment = new SetupSelectAccount();
            fragment.setArguments(getArguments());
            add(getParentFragmentManager(), fragment);
        }
    }*/

    /*public static class SyncFragment extends BaseGuidedStepFragment implements EPGCaptureTask.Listener {
        EPGCaptureTask mEpgSyncTask;

        @Override
        public void onSyncComplete() {
            Log.d(TAG, "Initial Sync Completed");

            // Move to the CompletedFragment
            GuidedStepSupportFragment fragment = new CompletedFragment();
            fragment.setArguments(getArguments());
            add(getParentFragmentManager(), fragment);
        }


        @Override
        public void onStart() {
            super.onStart();
            mEpgSyncTask = new EPGCaptureTask(getActivity().getBaseContext());
            mEpgSyncTask.addSyncListener(this);
        }

        @Override
        public void onStop() {
            mEpgSyncTask.stop();
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
                    "Syncing with TVHeadend",
                    "Please wait...",
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
    }*/

    /*public static class CompletedFragment extends GuidedStepSupportFragment {
        private static final int ACTION_ID_SETTINGS = 1;
        private static final int ACTION_ID_COMPLETE = 2;

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

            return new GuidanceStylist.Guidance(
                    "Completed",
                    "All content for your account has been synced. Channels, Programs and Recordings are all linked to your TVHeadend Server",
                    getString(R.string.account_label),
                    null);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .id(ACTION_ID_COMPLETE)
                    .title("Finish")
                    .description("Exit Setup Wizard")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ID_COMPLETE) {

                PreferenceUtils preferenceUtils = new PreferenceUtils(getActivity());
                preferenceUtils.setBoolean(PREFERENCE_SETUP_COMPLETE, true);

                Log.d(TAG, "Exiting Setup!");
                Intent intent = new Intent(getActivity(), EPGService.class);
                getActivity().startService(intent);

                // Wrap up setup
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            }
        }
    }*/
}
