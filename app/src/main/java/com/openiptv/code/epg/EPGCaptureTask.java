package com.openiptv.code.epg;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;

import com.openiptv.code.Constants;
import com.openiptv.code.DatabaseActions;
import com.openiptv.code.TVHeadendAccount;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.MessageListener;

import java.util.ArrayList;
import java.util.Set;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.EPG_METHODS;

public class EPGCaptureTask implements MessageListener {
    private static final String TAG = EPGCaptureTask.class.getSimpleName();

    private Context context;
    private BaseConnection connection;
    private Set<Listener> syncListeners;

    public interface Listener {
        void onSyncComplete();
    }

    public EPGCaptureTask(Context context) {
        if (Constants.DEBUG) {
            Log.d(TAG, "Started EPGCaptureTask");
        }

        TVHeadendAccount account = new TVHeadendAccount(DatabaseActions.activeAccount);

        // Create a new BaseConnection
        connection = new BaseConnection(new ConnectionInfo(
                account.getHostname(),
                Integer.parseInt(account.getPort()),
                account.getUsername(),
                account.getPassword(),
                account.getClientName(), "23"));


        // Link ourselves to the BaseConnection to listen for when we have received HTSP Messages.
        // The base connection has an instance of a HTSPMessageDispatcher
        connection.addMessageListener(this);

        // Start HTSP Connection
        connection.start();

        this.context = context;
        syncListeners = new ArraySet<>();
    }

    public void addSyncListener(Listener listener) {
        if (DEBUG) {
            Log.d(TAG, "Added sync listener");
        }
        syncListeners.add(listener);
    }

    public void stop() {
        syncListeners = null;
        connection.stop();
    }

    public void captureChannels(HTSPMessage channelMessage) {
        Channel channel = new Channel(channelMessage);

        Uri channelUri = Channel.getUri(context, channel);

        if (channelUri == null) {
            context.getContentResolver().insert(TvContract.Channels.CONTENT_URI, channel.getContentValues());
        } else {
            if (DEBUG) {
                Log.d(TAG, "Channel already exists");
            }
        }
    }

    public void capturePrograms(HTSPMessage programMessage) {
        Program program = new Program(context, programMessage);
        Uri eventUri = Program.getUri(context, program);

        if (eventUri == null) {
            context.getContentResolver().insert(TvContract.Programs.CONTENT_URI, program.getContentValues());
            if (DEBUG) {
                Log.d(TAG, "Adding program: " + program.getTitle());
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "Updating Program - " + program.getTitle());
            }
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newUpdate(eventUri)
                    .withValues(program.getContentValues())
                    .build());
            try {
                context.getContentResolver().applyBatch(TvContract.AUTHORITY, operations);
            } catch (OperationApplicationException | RemoteException e) {
                Log.d(TAG, "ERROR: " + e.toString()); // Ignored
            }
        }
    }

    public void captureRecordedPrograms(HTSPMessage message) {
        RecordedProgram recordedProgram = new RecordedProgram(context, message);
        Uri recordedProgramUri = RecordedProgram.getUri(context, recordedProgram);

        if (recordedProgramUri == null) {
            context.getContentResolver().insert(TvContract.RecordedPrograms.CONTENT_URI, recordedProgram.getContentValues());
            if (DEBUG) {
                Log.d(TAG, "Adding recorded program: " + recordedProgram.getTitle());
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "Updating Recorded Program - " + recordedProgram.getTitle());
            }
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newUpdate(recordedProgramUri)
                    .withValues(recordedProgram.getContentValues())
                    .build());
            try {
                context.getContentResolver().applyBatch(TvContract.AUTHORITY, operations);
            } catch (OperationApplicationException | RemoteException e) {
                Log.d(TAG, "ERROR: " + e.toString()); // Ignored
            }
        }
    }

    public void initialCompleted() {
        if (DEBUG) {
            Log.d(TAG, "Initial Sync Complete");
        }
        for (Listener l : syncListeners) {
            l.onSyncComplete();
        }
    }

    @Override
    public void onMessage(HTSPMessage message) {
        if (DEBUG) {
            Log.d(TAG, "Received method: " + message.getString("method"));
        }
        if (message.getString("method") != null && EPG_METHODS.contains(message.getString("method"))) {
            switch (message.getString("method")) {
                case "channelAdd":
                case "channelUpdate": {
                    captureChannels(message);
                    break;
                }
                case "eventAdd":
                case "eventUpdate": {
                    capturePrograms(message);
                    break;
                }
                case "dvrEntryAdd":
                case "dvrEntryUpdate": {
                    captureRecordedPrograms(message);
                    break;
                }
                case "initialSyncCompleted": {
                    initialCompleted();
                    break;
                }
            }
        }
    }
}