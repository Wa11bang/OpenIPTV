import android.os.Build;

import com.openiptv.code.SetupActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class ProgramTest {
    private static final int FAKE_TEST = 10;

    @Test
    public void addTestProgramToGuide() {
        // Given a Context object retrieved from Robolectric...
        SetupActivity myObjectUnderTest = new SetupActivity();

        // ...when the string is returned from the object under test...
        int result = myObjectUnderTest.getTest();

        // ...then the result should be the expected one.
        assertThat(result).isEqualTo(FAKE_TEST);
    }
}
