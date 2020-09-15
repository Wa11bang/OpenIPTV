package com.openiptv.code.htsp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.openiptv.code.Constants.FALLBACK_SUBSCRIPTION_ID;
import static com.openiptv.code.Constants.SUBSCRIPTION_METHODS;

public class Subscriber implements MessageListener {
    private static final String TAG = Subscriber.class.getSimpleName();

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

    public boolean getIsSubscribed()
    {
        return mIsSubscribed;
    }

    public int getSubscriptionId() {
        return mSubscriptionId;
    }

    public void subscribe(long channelId) throws HTSPNotConnectedException {
        subscribe(channelId, null);
    }

    public void subscribe(long channelId, String profile) throws HTSPNotConnectedException {
        Log.i(TAG, "Requesting subscription to channel " + mChannelId);

        if (!mIsSubscribed) {
            mDispatcher.addMessageListener(this);
        }

        mChannelId = channelId;

        HTSPMessage subscribeRequest = new HTSPMessage();

        subscribeRequest.put("method", "subscribe");
        subscribeRequest.put("subscriptionId", mSubscriptionId);
        subscribeRequest.put("channelId", channelId);
        subscribeRequest.put("profile", profile);

        mDispatcher.sendMessage(subscribeRequest);
        mIsSubscribed = true;
    }

    public void skip(long time) {
        Log.i(TAG, "Requesting skip for channel " + mChannelId);

        HTSPMessage subscriptionSkipRequest = new HTSPMessage();

        subscriptionSkipRequest.put("method", "subscriptionSkip");
        subscriptionSkipRequest.put("subscriptionId", mSubscriptionId);
        subscriptionSkipRequest.put("time", time);
        subscriptionSkipRequest.put("absolute", 1);

        try {
            mDispatcher.sendMessage(subscriptionSkipRequest);
        } catch (HTSPNotConnectedException e) {
            // Ignore
        }
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

        if (SUBSCRIPTION_METHODS.contains(method)) {
            final int subscriptionId = message.getInteger("subscriptionId", FALLBACK_SUBSCRIPTION_ID);

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