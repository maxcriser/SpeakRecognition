package com.example.mvmax.speakrecognition.speakrecognition.io;

import java.io.IOException;

public interface TarsosDSPAudioInputStream {

    long skip(long bytesToSkip) throws IOException;

    int read(byte[] b, int off, int len) throws IOException;

    void close() throws IOException;

    TarsosDSPAudioFormat getFormat();
}
