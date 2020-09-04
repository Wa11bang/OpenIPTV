package nz.co.theron.iptv;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

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
                    ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
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
            GuidedStepSupportFragment fragment = new AccountFragment();
            fragment.setArguments(getArguments());
            add(getFragmentManager(), fragment);
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
}
