import android.os.Build;

import com.openiptv.code.PreferenceUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class PreferencesTest {
    private static final String TEST_BOOL_KEY = "TEST_BOOL";
    private static final boolean TEST_BOOL_VALUE = true;
    private static final String TEST_STRING_KEY = "TEST_STRING";
    private static final String TEST_STRING_VALUE = "a_test_string_or_preference";
    private static final String TEST_INT_KEY = "TEST_INT";
    private static final int TEST_INT_VALUE = 150;

    private static final boolean EXPECTED_RESULT_1 = true;
    private static final String EXPECTED_RESULT_2 = "a_test_string_or_preference";
    private static final int EXPECTED_RESULT_3 = 150;

    public static PreferenceUtils preferenceUtils = new PreferenceUtils(getApplicationContext());

    @Test
    public void testBoolean() {
        preferenceUtils.setBoolean(TEST_BOOL_KEY, TEST_BOOL_VALUE);
        assertThat(preferenceUtils.getBoolean(TEST_BOOL_KEY)).isEqualTo(EXPECTED_RESULT_1);
    }

    @Test
    public void testString()
    {
        preferenceUtils.setString(TEST_STRING_KEY, TEST_STRING_VALUE);
        assertThat(preferenceUtils.getString(TEST_STRING_KEY)).isEqualTo(EXPECTED_RESULT_2);
    }

    @Test
    public void testInteger()
    {
        preferenceUtils.setInteger(TEST_INT_KEY, TEST_INT_VALUE);
        assertThat(preferenceUtils.getInteger(TEST_INT_KEY)).isEqualTo(EXPECTED_RESULT_3);
    }
}