/*
 * This Unit Test focuses on Connecting to a TVHeadEnd Server - part
 * of 'log into my server using a setup wizard' (US)
 *
 * Created by: Waldo Theron
 * Date: 14/09/20
 *
 * Tests:
 *  -   Correct user account details
 *  -   Incorrect user account details
 *  -   Incorrect TVHeadEnd host details
 */

import android.os.Build;
import android.util.Log;

import com.openiptv.code.htsp.Authenticator;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static com.openiptv.code.htsp.Authenticator.State.AUTHENTICATED;
import static com.openiptv.code.htsp.Authenticator.State.FAILED;
import static com.openiptv.code.htsp.Authenticator.State.UNAUTHORISED;
import static org.awaitility.Awaitility.await;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class ConnectionTests {
    private static final String DEV_ACCOUNT = "development";
    private static final String DEV_PASSWORD = "development";
    private static final String DEV_ACCOUNT_I = "development_i";
    private static final String DEV_PASSWORD_I = "development_i";
    private static final String DEV_HOST = "tv.theron.co.nz";
    private static final int DEV_PORT = 9982;
    private static final int DEV_PORT_I = 9985;

    private static final Authenticator.State EXPECTED_RESULT_1 = AUTHENTICATED;
    private static final Authenticator.State EXPECTED_RESULT_2 = UNAUTHORISED;
    private static final Authenticator.State EXPECTED_RESULT_3 = FAILED;

    /**
     * Tests whether given account details are correct for login.
     *
     * Expected Result: AUTHENTICATED
     */
    @Test
    public void testCorrectAuthDetails() {
        BaseConnection connection = new BaseConnection(new ConnectionInfo(DEV_HOST, DEV_PORT, DEV_ACCOUNT, DEV_PASSWORD, "test", "23"));
        connection.start();

        await().until(() -> connection.getAuthenticator().getState() != null);

        Log.d("Connection Test", "State: " + connection.getAuthenticator().getState());

        assertThat(connection.getAuthenticator().getState()).isEqualTo(EXPECTED_RESULT_1);
    }

    /**
     * Tests whether given account details are incorrect for login.
     *
     * Expected Result: UNAUTHORISED
     */
    @Test
    public void testIncorrectAuthDetails() {
        BaseConnection connection = new BaseConnection(new ConnectionInfo(DEV_HOST, DEV_PORT, DEV_ACCOUNT_I, DEV_PASSWORD_I, "test", "23"));
        connection.start();

        await().until(() -> connection.getAuthenticator().getState() != null);

        Log.d("Connection Test", "State: " + connection.getAuthenticator().getState());

        assertThat(connection.getAuthenticator().getState()).isEqualTo(EXPECTED_RESULT_2);
    }

    /**
     * Tests whether given host details are incorrect.
     *
     * Expected Result: FAILED
     */
    @Test
    public void testIncorrectHostDetails() {
        BaseConnection connection = new BaseConnection(new ConnectionInfo(DEV_HOST, DEV_PORT_I, DEV_ACCOUNT, DEV_PASSWORD, "test", "23"));
        connection.start();

        await().until(() -> connection.getAuthenticator().getState() != null);

        Log.d("Connection Test", "State: " + connection.getAuthenticator().getState());

        assertThat(connection.getAuthenticator().getState()).isEqualTo(EXPECTED_RESULT_3);
    }
}
