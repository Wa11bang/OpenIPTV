package com.openiptv.code.htsp;

import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Set;

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

    private final HTSPMessageDispatcher dispatcher;
    private final Set<Listener> listeners = new ArraySet<>();
    private final int subscriptionId;
    private long startTime = -1;

    private long timeshiftStart = -1;
    private long timeshiftShift = -1;
    private long timeshiftEnd = -1;

    private long channelId;
    private boolean isSubscribed = false;

    /**
     * Constructor for a Subscriber Object
     * @param dispatcher message dispatcher (used for sending and receiving messages),
     */
    public Subscriber(@NonNull HTSPMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        subscriptionId = 1000;
    }

    /**
     * Adds a listener to the list of Listeners.
     * @param listener to add
     */
    public void addSubscriptionListener(Listener listener) {
        if (listeners.contains(listener)) {
            Log.w(TAG, "Attempted to add duplicate subscription listener");
            return;
        }
        listeners.add(listener);
    }

    /**
     * Removes a listener from the list of Listeners.
     * @param listener to remove
     */
    public void removeSubscriptionListener(Listener listener) {
        if (!listeners.contains(listener)) {
            Log.w(TAG, "Attempted to remove non existing subscription listener");
            return;
        }
        listeners.remove(listener);
    }

    /**
     * Returns true if there is a current live subscription
     * @return isSubscribed.
     */
    public boolean getIsSubscribed()
    {
        return isSubscribed;
    }

    /**
     * Returns the current subscription ID
     * @return subscriptionId
     */
    public int getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Starts a new subscription to a given TV Channel (default profile).
     * @param channelId TVH channelId
     * @throws HTSPException if there is no connection made
     */
    public void subscribe(long channelId) throws HTSPException {
        subscribe(channelId, null);
    }

    /**
     * Starts a new subscription to a give TV Channel
     * @param channelId TVH channelId
     * @param profile TVH stream profile
     * @throws HTSPException if there is no connection made
     */
    public void subscribe(long channelId, String profile) throws HTSPException {
        Log.i(TAG, "Requesting subscription to channel " + this.channelId);

        if (!isSubscribed) {
            dispatcher.addMessageListener(this);
        }

        this.channelId = channelId;

        HTSPMessage subscribeRequest = new HTSPMessage();

        subscribeRequest.put("method", "subscribe");
        subscribeRequest.put("subscriptionId", subscriptionId);
        subscribeRequest.put("channelId", channelId);
        subscribeRequest.put("profile", profile);
        subscribeRequest.put("timeshiftPeriod", (60*60));

        dispatcher.sendMessage(subscribeRequest);
        isSubscribed = true;
    }

    /**
     * Sets the speed of the currently subscribed stream.
     * @param speed in TVH Format.
     */
    public void setSpeed(int speed) {
        Log.i(TAG, "Requesting speed " + speed + " for channel " + channelId);

        HTSPMessage subscriptionSpeedRequest = new HTSPMessage();

        subscriptionSpeedRequest.put("method", "subscriptionSpeed");
        subscriptionSpeedRequest.put("subscriptionId", subscriptionId);
        subscriptionSpeedRequest.put("speed", speed);

        try {
            dispatcher.sendMessage(subscriptionSpeedRequest);
        } catch (HTSPException e) {
            // Ignore
        }
    }

    /**
     * Pauses the stream, sets the speed to 0 (0x).
     */
    public void pause() {
        setSpeed(0);
    }

    /**
     * Play the stream, sets the speed back to 100 (1x).
     */
    public void resume() {
        setSpeed(100);
    }

    /**
     * Seek a subscription using HTSPMessage request
     * @param time to seek the stream by
     */
    public void seek(long time) {
        Log.i(TAG, "Requesting skip for channel " + channelId);

        HTSPMessage subscriptionSkipRequest = new HTSPMessage();

        subscriptionSkipRequest.put("method", "subscriptionSkip");
        subscriptionSkipRequest.put("subscriptionId", subscriptionId);
        subscriptionSkipRequest.put("time", time);
        subscriptionSkipRequest.put("absolute", 1);

        try {
            dispatcher.sendMessage(subscriptionSkipRequest);
            if(time > timeshiftShift)
            {
                timeshiftShift = time;
            }
        } catch (HTSPException e) {
            // Ignore
        }
    }

    /**
     * Returns the current shift period from the start time.
     * @return offset from the start in PTS
     */
    public long getTimeshiftOffsetPts() {
        return timeshiftShift * -1;
    }

    /**
     * Returns the starting time in PTS format.
     * @return start time in PTS
     */
    public long getTimeshiftStartPts() {
        return timeshiftStart;
    }

    /**
     * Gets the current Time-shift Starting time
     * @return timeShiftStartTime
     */
    public long getTimeshiftStartTime() {
        long startPts = getTimeshiftStartPts();

        if (startPts == -1 || startTime == -1) {
            return -1;
        }

        return startTime + startPts;
    }

    /**
     * Unsubscribe the current TV service.
     */
    public void unsubscribe() {
        Log.i(TAG, "Requesting unsubscribe from channel " + channelId);
        isSubscribed = false;
        dispatcher.removeMessageListener(this);

        HTSPMessage unsubscribeRequest = new HTSPMessage();

        unsubscribeRequest.put("method", "unsubscribe");
        unsubscribeRequest.put("subscriptionId", subscriptionId);

        try {
            dispatcher.sendMessage(unsubscribeRequest);
        } catch (HTSPException ignored) {
        }
    }

    @Override
    public void onMessage(@NonNull HTSPMessage message) {
        final String method = message.getString("method", null);

        if (SUBSCRIPTION_METHODS.contains(method)) {
            final int subscriptionId = message.getInteger("subscriptionId", FALLBACK_SUBSCRIPTION_ID);

            if (subscriptionId != this.subscriptionId) {
                return;
            }

            switch (method) {
                case "subscriptionStart":
                    for (final Listener listener : listeners) {
                        listener.onSubscriptionStart(message);
                    }
                    // Using 1000 Ms as a buffer to prevent incorrect timestamp.
                    startTime = (System.currentTimeMillis() * 1000) - 1000;
                    break;
                case "subscriptionStatus":
                    for (final Listener listener : listeners) {
                        listener.onSubscriptionStatus(message);
                    }
                    break;
                case "subscriptionStop":
                    for (final Listener listener : listeners) {
                        listener.onSubscriptionStop(message);
                    }
                    break;
                case "timeshiftStatus":
                    timeshiftShift = message.getLong("shift", -1);
                    timeshiftEnd = message.getLong("end", -1);
                    timeshiftStart = message.getLong("start", -1);
                    break;
                case "muxpkt":
                    for (final Listener listener : listeners) {
                        listener.onMuxpkt(message);
                    }
                    break;
            }
        }
    }
}