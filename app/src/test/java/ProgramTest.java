import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;

import com.openiptv.code.epg.Program;
import com.openiptv.code.htsp.HTSPMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.openiptv.code.Constants.CHANNEL_ID;
import static com.openiptv.code.Constants.PROGRAM_AGE_RATING;
import static com.openiptv.code.Constants.PROGRAM_DESCRIPTION;
import static com.openiptv.code.Constants.PROGRAM_FINISH_TIME;
import static com.openiptv.code.Constants.PROGRAM_ID;
import static com.openiptv.code.Constants.PROGRAM_IMAGE;
import static com.openiptv.code.Constants.PROGRAM_START_TIME;
import static com.openiptv.code.Constants.PROGRAM_SUMMARY;
import static com.openiptv.code.Constants.PROGRAM_TITLE;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class ProgramTest {
    private static final String TAG = ProgramTest.class.getSimpleName();
    private static final Program RESULT_PROGRAM = new Program(getApplicationContext(),
            100,
            177477477,
            14000000000000L,
            14000003600000L,
            "Test Program",
            "Test Summary",
            "Test Description",
            16,
            null);

    @Test
    public void testProgramContentValueParse() {
        HTSPMessage testProgramInput = new HTSPMessage();
        testProgramInput.put(PROGRAM_ID, 100);
        testProgramInput.put(CHANNEL_ID, 177477477);
        testProgramInput.put(PROGRAM_START_TIME, 14000000000000L);
        testProgramInput.put(PROGRAM_FINISH_TIME, 14000003600000L);
        testProgramInput.put(PROGRAM_TITLE, "Test Program");
        testProgramInput.put(PROGRAM_SUMMARY, "Test Summary");
        testProgramInput.put(PROGRAM_DESCRIPTION, "Test Description");
        testProgramInput.put(PROGRAM_AGE_RATING, 16);
        testProgramInput.put(PROGRAM_IMAGE, null);

        Program program = new Program(getApplicationContext(), testProgramInput);

        // Get Program URI - Check to see if added successfully
        Uri programUri = getApplicationContext().getContentResolver().insert(TvContract.Programs.CONTENT_URI, program.getContentValues());
        assertThat(programUri).isNotNull();

        // ...then the result should be the expected one.
        assertThat(program.getContentValues()).isEqualTo(RESULT_PROGRAM.getContentValues());
    }

    @Test
    public void testProgramContentValueParseInvalid() {
        HTSPMessage testProgramInput = new HTSPMessage();
        testProgramInput.put(PROGRAM_ID, 100);
        testProgramInput.put(CHANNEL_ID, 177477477);
        testProgramInput.put(PROGRAM_START_TIME, 14000000000000L);
        testProgramInput.put(PROGRAM_FINISH_TIME, 14000003600000L);
        testProgramInput.put(PROGRAM_TITLE, "Test Program");
        testProgramInput.put(PROGRAM_SUMMARY, "Test Summary");
        testProgramInput.put(PROGRAM_DESCRIPTION, "Test Description");
        testProgramInput.put(PROGRAM_AGE_RATING, 12);
        testProgramInput.put(PROGRAM_IMAGE, null);

        Program program = new Program(getApplicationContext(), testProgramInput);

        // Get Program URI - Check to see if added successfully
        Uri programUri = getApplicationContext().getContentResolver().insert(TvContract.Programs.CONTENT_URI, program.getContentValues());
        assertThat(programUri).isNotNull();

        // ...then the result should be the expected one.
        assertThat(program.getContentValues()).isNotEqualTo(RESULT_PROGRAM.getContentValues());
    }
}