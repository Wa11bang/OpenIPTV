package com.openiptv.code.htsp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Subscriber implements MessageListener {
    private static final String TAG = Subscriber.class.getSimpleName();

    private static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final int INVALID_START_TIME = -1;
    private static final int STATS_INTERVAL = 10000;
    private static final int DEFAULT_TIMESHIFT_PERIOD = 0;

    // Copy of TvInputManager.TIME_SHIFT_INVALID_TIME, available on M+ Only.
    public static final long INVALID_TIMESHIFT_TIME = -9223372036854775808L;

    private static final Set<String> HANDLED_METHODS = new HashSet<>(Arrays.asList(new String[]{
            "subscriptionStart", "subscriptionStatus", "subscriptionStop",
            "queueStatus", "signalStatus", "timeshiftStatus", "muxpkt",
            "subscriptionSkip", "subscriptionSpeed",
            // "subscriptionGrace"
    }));

    /**
     * A listener for Subscription events
     */
    public interface Listener {
        void onSubscriptionStart(@NonNull HTSPMessage message);
        void onSubscriptionStatus(@NonNull HTSPMessage message);
        void onSubscriptionStop(@NonNull HTSPMessage message);
        void onMuxpkt(@NonNull HTSPMessage message);
    }

    private final HTSPMessageDispatcher mDispatcher;
    private final Set<Listener> mListeners = new CopyOnWriteArraySet<>();
    private final int mSubscriptionId;


    private long mChannelId;

    private boolean mIsSubscribed = false;

    public Subscriber(@NonNull HTSPMessageDispatcher dispatcher) {
        mDispatcher = dispatcher;

        mSubscriptionId = 1000;
    }

    public void addSubscriptionListener(Listener listener) {
        if (mListeners.contains(listener)) {
            Log.w(TAG, "Attempted to add duplicate subscription listener");
            return;
        }
        mListeners.add(listener);
    }

    public void removeSubscriptionListener(Listener listener) {
        if (!mListeners.contains(listener)) {
            Log.w(TAG, "Attempted to remove non existing subscription listener");
            return;
        }
        mListeners.remove(listener);
    }

    public int getSubscriptionId() {
        return mSubscriptionId;
    }

    public void subscribe(long channelId) throws HTSPNotConnectedException {
        subscribe(channelId, null, DEFAULT_TIMESHIFT_PERIOD);
    }

    public void subscribe(long channelId, String profile, int timeshiftPeriod) throws HTSPNotConnectedException {
        Log.i(TAG, "Requesting subscription to channel " + mChannelId);

        if (!mIsSubscribed) {
            mDispatcher.addMessageListener(this);
        }

        mChannelId = channelId;

        HTSPMessage subscribeRequest = new HTSPMessage();

        subscribeRequest.put("method", "subscribe");
        subscribeRequest.put("subscriptionId", mSubscriptionId);
        subscribeRequest.put("channelId", channelId);
        subscribeRequest.put("timeshiftPeriod", timeshiftPeriod);

        mDispatcher.sendMessage(subscribeRequest);
        mIsSubscribed = true;
    }

    public void unsubscribe() {
        Log.i(TAG, "Requesting unsubscription from channel " + mChannelId);
        mIsSubscribed = false;
        mDispatcher.removeMessageListener(this);

        HTSPMessage unsubscribeRequest = new HTSPMessage();

        unsubscribeRequest.put("method", "unsubscribe");
        unsubscribeRequest.put("subscriptionId", mSubscriptionId);

        try {
            mDispatcher.sendMessage(unsubscribeRequest);
        } catch (HTSPNotConnectedException ignored) {
        }
    }

    @Override
    public void onMessage(@NonNull HTSPMessage message) {
        final String method = message.getString("method", null);

        if (HANDLED_METHODS.contains(method)) {
            final int subscriptionId = message.getInteger("subscriptionId", INVALID_SUBSCRIPTION_ID);

            if (subscriptionId != mSubscriptionId) {
                return;
            }

            switch (method) {
                case "subscriptionStart":
                    for (final Listener listener : mListeners) {
                        listener.onSubscriptionStart(message);
                    }
                    break;
                case "subscriptionStatus":
                    for (final Listener listener : mListeners) {
                        listener.onSubscriptionStatus(message);
                    }
                    break;
                case "subscriptionStop":
                    for (final Listener listener : mListeners) {
                        listener.onSubscriptionStop(message);
                    }
                    break;
                case "muxpkt":
                    for (final Listener listener : mListeners) {
                        listener.onMuxpkt(message);
                    }
                    break;
            }
        }
    }

}