package nz.co.theron.iptv;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionEditText;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends FragmentActivity {
    private static final String TAG = SetupActivity.class.getName();

    DatabaseActions mDatabaseActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepSupportFragment fragment = new IntroFragment();
        fragment.setArguments(getIntent().getExtras());
        GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content);




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
                    ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            GuidedAction action = new GuidedAction.Builder(getActivity())
                    .title("Add Account")
                    .description("Click to start")
                    .editable(false)
                    .build();

            actions.add(action);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            // Move onto the next step
            GuidedStepSupportFragment fragment = new SetupFragment();
            fragment.setArguments(getArguments());
            add(getFragmentManager(), fragment);
        }
    }
}
