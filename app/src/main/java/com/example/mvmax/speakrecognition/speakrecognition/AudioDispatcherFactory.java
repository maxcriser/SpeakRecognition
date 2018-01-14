package com.example.mvmax.speakrecognition.speakrecognition;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioFormat;
import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioInputStream;

final class AudioDispatcherFactory {

    static AudioDispatcher fromDefaultMicrophone() {
        final int minAudioBufferSize = AudioRecord.getMinBufferSize(16000,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT);
        final int minAudioBufferSizeInSamples = minAudioBufferSize / 2;
        if (minAudioBufferSizeInSamples <= 1024) {
            final AudioRecord audioInputStream = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, 16000,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    1024 * 2);

            final TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(16000, 16, 1, true, false);

            final TarsosDSPAudioInputStream audioStream = new AndroidAudioInputStream(audioInputStream, format);
            audioInputStream.startRecording();
            return new AudioDispatcher(audioStream, 1024, 0);
        } else {
            throw new IllegalArgumentException("Buffer size too small should be at least " + (minAudioBufferSize * 2));
        }
    }

}
