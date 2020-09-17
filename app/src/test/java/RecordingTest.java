import android.os.Build;

import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.HTSPNotConnectedException;
import com.openiptv.code.htsp.MessageListener;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class RecordingTest {
    private static final String DEV_ACCOUNT = "development";
    private static final String DEV_PASSWORD = "development";
    private static final String DEV_HOST = "tv.theron.co.nz";
    private static final int DEV_PORT = 9982;

    private static final int TVNZ_1_CHANNEL_ID = 1997018292;
    private static final int RECORDING_OFFSET = 2000;

    private static final boolean EXPECTED_RESULT_1 = true;
    private static final boolean EXPECTED_RESULT_2 = true;
    private static final boolean EXPECTED_RESULT_3 = false;
    private static final boolean EXPECTED_RESULT_4 = false;

    private static BaseConnection connection;
    private static int recordingId = 0;

    /*
        https://tvheadend.org/projects/tvheadend/wiki/Htsp

        addDvrEntry (Added in version 4)

        Create a new DVR entry. Either eventId or channelId, start and stop must be specified.

        Request message fields:

        eventId            u32   optional   Event ID (Optional since version 5).
        channelId          u32   optional   Channel ID (Added in version 5)
        start              s64   optional   Time to start recording (Added in version 5)
        stop               s64   optional   Time to stop recording (Added in version 5)
        retention          u32   optional   Retention time in days (Added in version 13)
        creator            str   optional   Name of the event creator (Added in version 5, obsoleted in version 18 - applications are not allowed to change credential)
        priority           u32   optional   Recording priority (Added in version 5)
        startExtra         s64   optional   Pre-recording buffer in minutes (Added in version 5)
        stopExtra          s64   optional   Post-recording buffer in minutes (Added in version 5)
        title              str   optional   Recording title, if no eventId (Added in version 6)
        subtitle           str   optional   Recording subtitle, if no eventId (Added in version 20)
        description        str   optional   Recording description, if no eventId (Added in version 5)
        configName         str   optional   DVR configuration name or UUID
        enabled            u32   optional   Enabled flag (Added in version 23).

        Reply message fields:

        success            u32   required   1 if entry was added, 0 otherwise
        id                 u32   optional   ID of created DVR entry
        error              str   optional   English clear text of error message
     */

    /**
     * Initialise ONE connection instance to be used across test instances.
     */
    @BeforeClass
    public static void setUp()
    {
        connection = new BaseConnection(new ConnectionInfo(DEV_HOST, DEV_PORT, DEV_ACCOUNT, DEV_PASSWORD, "test", "23"));
        connection.start();

        // Wait till we are authenticated
        await().until(() -> connection.getAuthenticator().getState() != null);
    }

    @Test
    public void addRecordingToServer() {
        HTSPMessage addRecordingMessage = new HTSPMessage();

        int uniqueSequenceNumber = new Random().nextInt(50) + 1;
        final boolean[] result = new boolean[2];

        addRecordingMessage.put("method", "addDvrEntry");
        addRecordingMessage.put("channelId", TVNZ_1_CHANNEL_ID);
        addRecordingMessage.put("start", System.currentTimeMillis()/1000 + RECORDING_OFFSET);
        addRecordingMessage.put("stop", System.currentTimeMillis()/1000 + (RECORDING_OFFSET*2));
        addRecordingMessage.put("title", "Add Recording Unit Test");

        // Used to quickly filter through incoming messages
        addRecordingMessage.put("seq", uniqueSequenceNumber);


        MessageListener listener = message -> {
            if(message.containsKey("seq") && message.getInteger("seq") == uniqueSequenceNumber)
            {
                System.out.println("Received Response - SEQ: " + uniqueSequenceNumber);

                // Successfully updated value
                result[0] = true;

                // Set result
                result[1] = message.getInteger("success") == 1;

                // Set ID used for deletion
                recordingId = message.getInteger("id");
                System.out.println("Setting recording ID to " + recordingId);
            }
        };

        connection.addMessageListener(listener);

        try {
            connection.getHTSPMessageDispatcher().sendMessage(addRecordingMessage);
        } catch (HTSPNotConnectedException e) {
            // Ignore - We will always be connecting in the case of this test
        }

        // Wait till have received the response message
        await().until(() -> result[0]);

        // ...then the result should be the expected one.
        assertThat(result[1]).isEqualTo(EXPECTED_RESULT_1);
    }

    @Test
    public void removeRecordingToServer() {
        HTSPMessage removeRecordingMessage = new HTSPMessage();

        int uniqueSequenceNumber = new Random().nextInt(50) + 1;
        final boolean[] result = new boolean[2];

        System.out.println("Got recording ID " + recordingId);

        removeRecordingMessage.put("method", "deleteDvrEntry");
        removeRecordingMessage.put("id", recordingId);

        // Used to quickly filter through incoming messages
        removeRecordingMessage.put("seq", uniqueSequenceNumber);


        MessageListener listener = message -> {
            if(message.containsKey("seq") && message.getInteger("seq") == uniqueSequenceNumber)
            {
                System.out.println("Received Response - SEQ: " + uniqueSequenceNumber);

                // Successfully updated value
                result[0] = true;

                // Set result
                result[1] = message.getInteger("success") == 1;
            }
        };

        connection.addMessageListener(listener);

        try {
            connection.getHTSPMessageDispatcher().sendMessage(removeRecordingMessage);
        } catch (HTSPNotConnectedException e) {
            // Ignore - We will always be connecting in the case of this test
        }

        // Wait till have received the response message
        await().until(() -> result[0]);

        // ...then the result should be the expected one.
        assertThat(result[1]).isEqualTo(EXPECTED_RESULT_2);
    }

    @Test
    public void addRecordingToServerInvalid() {
        HTSPMessage addRecordingMessage = new HTSPMessage();

        int uniqueSequenceNumber = new Random().nextInt(50) + 1;
        final boolean[] result = new boolean[2];

        addRecordingMessage.put("method", "addDvrEntry");
        addRecordingMessage.put("channelId", 50);
        addRecordingMessage.put("start", System.currentTimeMillis()/1000 + RECORDING_OFFSET);
        addRecordingMessage.put("stop", System.currentTimeMillis()/1000 + (RECORDING_OFFSET*2));
        addRecordingMessage.put("title", "Add Recording Unit Test - Invalid");

        // Used to quickly filter through incoming messages
        addRecordingMessage.put("seq", uniqueSequenceNumber);


        MessageListener listener = message -> {
            if(message.containsKey("seq") && message.getInteger("seq") == uniqueSequenceNumber)
            {
                System.out.println("Received Response - SEQ: " + uniqueSequenceNumber);

                // Successfully updated value
                result[0] = true;

                // Set result
                result[1] = message.getInteger("success") == 1;
            }
        };

        connection.addMessageListener(listener);

        try {
            connection.getHTSPMessageDispatcher().sendMessage(addRecordingMessage);
        } catch (HTSPNotConnectedException e) {
            // Ignore - We will always be connecting in the case of this test
        }

        // Wait till have received the response message
        await().until(() -> result[0]);

        // ...then the result should be the expected one.
        assertThat(result[1]).isEqualTo(EXPECTED_RESULT_3);
    }

    @Test
    public void removeRecordingFromServerInvalid() {
        HTSPMessage removeRecordingMessage = new HTSPMessage();

        int uniqueSequenceNumber = new Random().nextInt(50) + 1;
        final boolean[] result = new boolean[2];

        removeRecordingMessage.put("method", "deleteDvrEntry");
        removeRecordingMessage.put("id", 50);

        // Used to quickly filter through incoming messages
        removeRecordingMessage.put("seq", uniqueSequenceNumber);


        MessageListener listener = message -> {
            if(message.containsKey("seq") && message.getInteger("seq") == uniqueSequenceNumber)
            {
                System.out.println("Received Response - SEQ: " + uniqueSequenceNumber);

                // Successfully updated value
                result[0] = true;

                // Set result
                result[1] = message.getInteger("success") == 1;
            }
        };

        connection.addMessageListener(listener);

        try {
            connection.getHTSPMessageDispatcher().sendMessage(removeRecordingMessage);
        } catch (HTSPNotConnectedException e) {
            // Ignore - We will always be connecting in the case of this test
        }

        // Wait till have received the response message
        await().until(() -> result[0]);

        // ...then the result should be the expected one.
        assertThat(result[1]).isEqualTo(EXPECTED_RESULT_4);
    }
}
