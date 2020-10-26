package com.openiptv.code.player;

import android.content.Context;
import android.media.tv.TvTrackInfo;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;

public class ExtendedTrackSelector extends DefaultTrackSelector {
    private static final String TAG = ExtendedTrackSelector.class.getSimpleName();

    private String videoId;
    private String audioId;
    private String subtitleId;

    public ExtendedTrackSelector(Context context)
    {
        super(context);
    }

    public boolean selectTrack(int type, String trackId) {
        switch (type) {
            case TvTrackInfo.TYPE_VIDEO:
                videoId = trackId;
                break;
            case TvTrackInfo.TYPE_AUDIO:
                audioId = trackId;
                break;
            case TvTrackInfo.TYPE_SUBTITLE:
                subtitleId = trackId;
                break;
            default:
                throw new RuntimeException("Invalid track type: " + type);
        }

        invalidate();

        return true;
    }

    @Nullable
    @Override
    protected TrackSelection.Definition selectVideoTrack(TrackGroupArray groups, int[][] formatSupports, int mixedMimeTypeAdaptationSupports, Parameters params, boolean enableAdaptiveTrackSelection) throws ExoPlaybackException {
        Log.d(TAG, "TrackSelector selectVideoTrack");
        if (videoId == null) {
            return super.selectVideoTrack(groups, formatSupports, mixedMimeTypeAdaptationSupports, params, enableAdaptiveTrackSelection);
        } else {
            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupports[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (videoId.equals(format.id)) {
                            return new TrackSelection.Definition(trackGroup, trackIndex);
                        }
                    }
                }
            }

            return null;
        }
    }

    @Nullable
    @Override
    protected Pair<TrackSelection.Definition, AudioTrackScore> selectAudioTrack(TrackGroupArray groups, int[][] formatSupports, int mixedMimeTypeAdaptationSupports, Parameters params, boolean enableAdaptiveTrackSelection) throws ExoPlaybackException {
        Log.d(TAG, "TrackSelector selectAudioTrack");

        if (audioId == null) {
            try {
                return super.selectAudioTrack(groups, formatSupports, mixedMimeTypeAdaptationSupports,params, enableAdaptiveTrackSelection);
            } catch (ExoPlaybackException e) {
                return null;
            }
        } else {
            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupports[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (audioId.equals(format.id)) {
                            TrackSelection.Definition definition = new TrackSelection.Definition(trackGroup, trackIndex);
                            AudioTrackScore audioTrackScore = new AudioTrackScore(format, params, trackIndex);

                            return Pair.create(definition, audioTrackScore);
                        }
                    }
                }
            }

            return null;
        }
    }

    @Nullable
    @Override
    protected Pair<TrackSelection.Definition, TextTrackScore> selectTextTrack(TrackGroupArray groups, int[][] formatSupport, Parameters params, @Nullable String selectedAudioLanguage) throws ExoPlaybackException {
        Log.d(TAG, "TrackSelector selectTextTrack");

        if (subtitleId != null) {

            for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
                TrackGroup trackGroup = groups.get(groupIndex);
                int[] trackFormatSupport = formatSupport[groupIndex];

                for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                    if (isSupported(trackFormatSupport[trackIndex], false)) {
                        Format format = trackGroup.getFormat(trackIndex);

                        if (subtitleId.equals(format.id)) {
                            TrackSelection.Definition definition = new TrackSelection.Definition(trackGroup, trackIndex);
                            TextTrackScore textTrackScore = new TextTrackScore(format, params, trackFormatSupport[trackIndex], selectedAudioLanguage);

                            return Pair.create(definition, textTrackScore);
                        }
                    }
                }
            }

        }
        return null;

    }
}
