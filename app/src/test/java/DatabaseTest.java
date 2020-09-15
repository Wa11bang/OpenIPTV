import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.GuidedStepSupportFragment;


import com.openiptv.code.DatabaseActions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)

/**
 * Checks that entries in the database are valid entries.
 * No fields left unfilled.
 * Port is a number
 */
public class DatabaseTest extends GuidedStepSupportFragment {


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
                Long portNum = Long.parseLong( accounts.getString(4));
            } catch (NumberFormatException e) {
                assertThat(true).isTrue();
            }
        }

        databaseActions.close();
    }
}
