import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import nz.co.theron.iptv.SetupActivity;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DemoTest {
    private static final int FAKE_TEST = 10;
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void readStringFromContext_LocalizedString() {
        // Given a Context object retrieved from Robolectric...
        SetupActivity myObjectUnderTest = new SetupActivity();

        // ...when the string is returned from the object under test...
        int result = myObjectUnderTest.getTest();

        // ...then the result should be the expected one.
        assertThat(result).isEqualTo(FAKE_TEST);
    }
}