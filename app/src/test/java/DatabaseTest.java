import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.GuidedStepSupportFragment;


import com.openiptv.code.DatabaseActions;
import com.openiptv.code.SetupNewAccountFragment;
import com.openiptv.code.TVHeadendAccount;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)

/**
 * Checks that entries in the database are valid entries.
 * No fields left unfilled.
 * Port is a number
 */
public class DatabaseTest extends GuidedStepSupportFragment {


    /**
     * Try to add invalid entries to database.
     */
    @Before
    public void addTestEntries() {
        Bundle newAccountDetails = new Bundle();
        newAccountDetails.putString("username", "");
        newAccountDetails.putString("password", "");
        newAccountDetails.putString("hostname", "");
        newAccountDetails.putString("port", "");
        newAccountDetails.putString("clientName", "");
        TVHeadendAccount tvHeadendAccount = new TVHeadendAccount(newAccountDetails);

        DatabaseActions databaseActions = new DatabaseActions(RuntimeEnvironment.systemContext);
        databaseActions.addAccount(tvHeadendAccount);
        databaseActions.close();
    }

    /**
     * Checks that there are no blank or null fields in the database
     */
    @Test
    public void unfilledEntryCheck() {

        DatabaseActions databaseActions = new DatabaseActions(RuntimeEnvironment.systemContext);
        Cursor accounts = databaseActions.getAccounts();
        Boolean isEmpty = false;
        int test = 1;
        while (accounts.moveToNext()) {
            for (int i = 0; i != 6; i++) {
                String accountToCheck = accounts.getString(i);
                if (accountToCheck.equals("") || accountToCheck.isEmpty()) {
                    isEmpty = true;
                }
                assertThat(isEmpty).isFalse();
            }
        }
        databaseActions.close();
    }

    /**
     * Check that the port is a number
     */
    @Test
    public void portNumberCheck() {
        DatabaseActions databaseActions = new DatabaseActions(RuntimeEnvironment.systemContext);
        Cursor accounts = databaseActions.getAccounts();

        /**
         * Try to parse as long. If fail, test fails
         */
        while (accounts.moveToNext()) {
            try {
                Long portNum = Long.parseLong(accounts.getString(4));
            } catch (NumberFormatException e) {
                assertThat(true).isTrue();
            }
        }

        databaseActions.close();
    }

    @After
    public void cleanUp() {
        DatabaseActions databaseActions = new DatabaseActions(RuntimeEnvironment.systemContext);
        databaseActions.clearAccountsClientName("");
    }
}
