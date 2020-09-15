import android.os.Build;

import com.openiptv.code.epg.Channel;
import com.openiptv.code.htsp.HTSPMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class ChannelTest {
    private static final String TAG = ChannelTest.class.getSimpleName();
    private static final Channel RESULT_CHANNEL = new Channel(177477477, 100, 4, "TestChannel");

    @Test
    public void testChannelContentValueParse() {
        HTSPMessage testChannelInput = new HTSPMessage();
        testChannelInput.put("channelId", 177477477);
        testChannelInput.put("channelName", "TestChannel");
        testChannelInput.put("channelNumber", 100);
        testChannelInput.put("channelNumberMinor", 4);

        Channel channel = new Channel(testChannelInput);

        // ...then the result should be the expected one.
        assertThat(channel.getContentValues()).isEqualTo(RESULT_CHANNEL.getContentValues());
    }

    @Test
    public void testChannelContentValueParseInvalid() {
        HTSPMessage testChannelInput = new HTSPMessage();
        testChannelInput.put("channelId", 177477478);
        testChannelInput.put("channelName", "TestChannel");
        testChannelInput.put("channelNumber", 100);
        testChannelInput.put("channelNumberMinor", 4);

        Channel channel = new Channel(testChannelInput);

        // ...then the result should be the expected one.
        assertThat(channel.getContentValues()).isNotEqualTo(RESULT_CHANNEL.getContentValues());
    }
}
