package com.example.mvmax.speakrecognition.speakrecognition;

import android.media.AudioRecord;

import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioFormat;
import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioInputStream;

import java.io.IOException;

public class AndroidAudioInputStream implements TarsosDSPAudioInputStream {

    private final AudioRecord underlyingStream;
    private final TarsosDSPAudioFormat format;

    AndroidAudioInputStream(final AudioRecord underlyingStream, final TarsosDSPAudioFormat format) {
        this.underlyingStream = underlyingStream;
        this.format = format;
    }

    @Override
    public long skip(final long bytesToSkip) throws IOException {
        throw new IOException("Can not skip in audio stream");
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return underlyingStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        underlyingStream.stop();
        underlyingStream.release();
    }

    @Override
    public TarsosDSPAudioFormat getFormat() {
        return format;
    }
}
