/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package com.example.mvmax.speakrecognition.speakrecognition;

import android.media.AudioRecord;

import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioFormat;
import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioInputStream;

import java.io.IOException;

public class AndroidAudioInputStream implements TarsosDSPAudioInputStream {

    private final AudioRecord underlyingStream;
    private final TarsosDSPAudioFormat format;

    public AndroidAudioInputStream(final AudioRecord underlyingStream, final TarsosDSPAudioFormat format) {
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

    @Override
    public long getFrameLength() {
        return -1;
    }

}
