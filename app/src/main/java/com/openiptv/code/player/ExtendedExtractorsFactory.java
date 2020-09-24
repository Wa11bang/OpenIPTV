package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.flac.FlacExtractor;
import com.google.android.exoplayer2.extractor.flv.FlvExtractor;
import com.google.android.exoplayer2.extractor.mkv.MatroskaExtractor;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.ogg.OggExtractor;
import com.google.android.exoplayer2.extractor.ts.Ac3Extractor;
import com.google.android.exoplayer2.extractor.ts.AdtsExtractor;
import com.google.android.exoplayer2.extractor.ts.PsExtractor;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.extractor.wav.WavExtractor;
import com.openiptv.code.Constants;

public class ExtendedExtractorsFactory implements ExtractorsFactory {
    private Context context;
    private @AdtsExtractor.Flags int adtsFlags;

    public ExtendedExtractorsFactory(Context context)
    {
        this.context = context;
    }

    @Override
    public Extractor[] createExtractors() {
        Extractor[] extractors = new Extractor[Constants.NUM_OF_EXTRACTORS];

        extractors[0] = new HTSPSubscriptionDataExtractor(context); // Added to support HTSP Data
        extractors[1] = new Mp4Extractor();
        extractors[2] = new FragmentedMp4Extractor(0);
        extractors[3] = new MatroskaExtractor(0);
        extractors[4] = new Mp3Extractor(0);
        extractors[5] = new AdtsExtractor(adtsFlags | (AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING));
        extractors[6] = new Ac3Extractor();
        extractors[7] = new TsExtractor(0);
        extractors[8] = new FlvExtractor();
        extractors[9] = new OggExtractor();
        extractors[10] = new PsExtractor();
        extractors[11] = new WavExtractor();
        extractors[12] = new FlacExtractor();

        return extractors;
    }
}