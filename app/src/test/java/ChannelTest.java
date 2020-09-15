import android.content.Context;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.openiptv.code.SetupActivity;
import com.openiptv.code.epg.Channel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static com.openiptv.code.Constants.DEBUG;

@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
public class ChannelTest {
    private static final int FAKE_TEST = 10;
    private static final boolean EXISTS = false;
    private static final String TAG = ChannelTest.class.getSimpleName();

    @Test
    public void addTestChannelToGuide() {
        Context context = RuntimeEnvironment.systemContext;
        Channel channel = new Channel(177477477, 100, 4, "TestChannel");
        context.getContentResolver().insert(TvContract.Channels.CONTENT_URI, channel.getContentValues());

        Uri channelUri = Channel.getUri(context, channel);
        boolean result = false;

        if (channelUri == null) {
            result = false;
            context.getContentResolver().insert(TvContract.Channels.CONTENT_URI, channel.getContentValues());
        }
        else {
            result = true;
            Log.d(TAG, "Channel already exists");
        }

        // ...then the result should be the expected one.
        assertThat(result).isEqualTo(EXISTS);
    }
}
