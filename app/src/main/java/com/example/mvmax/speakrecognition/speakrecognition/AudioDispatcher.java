package com.example.mvmax.speakrecognition.speakrecognition;

import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioFloatConverter;
import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioFormat;
import com.example.mvmax.speakrecognition.speakrecognition.io.TarsosDSPAudioInputStream;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioDispatcher implements Runnable {

    private final TarsosDSPAudioInputStream audioInputStream;

    private float[] audioFloatBuffer;

    private byte[] audioByteBuffer;

    private final List<AudioProcessor> audioProcessors;

    private final TarsosDSPAudioFloatConverter converter;

    private final TarsosDSPAudioFormat format;

    private int floatOverlap, floatStepSize;

    private int byteOverlap, byteStepSize;

    private final long bytesToSkip;

    private long bytesProcessed;

    private final AudioEvent audioEvent;

    private boolean stopped;

    private final boolean zeroPadLastBuffer;

    AudioDispatcher(final TarsosDSPAudioInputStream stream, final int audioBufferSize, final int bufferOverlap) {
        audioProcessors = new CopyOnWriteArrayList<>();
        audioInputStream = stream;

        format = audioInputStream.getFormat();

        setStepSizeAndOverlap(audioBufferSize, bufferOverlap);

        audioEvent = new AudioEvent();
        audioEvent.setFloatBuffer(audioFloatBuffer);

        converter = TarsosDSPAudioFloatConverter.getConverter(format);

        stopped = false;

        bytesToSkip = 0;

        zeroPadLastBuffer = true;
    }

    private void setStepSizeAndOverlap(final int audioBufferSize, final int bufferOverlap) {
        audioFloatBuffer = new float[audioBufferSize];
        floatOverlap = bufferOverlap;
        floatStepSize = audioFloatBuffer.length - floatOverlap;

        audioByteBuffer = new byte[audioFloatBuffer.length * format.getFrameSize()];
        byteOverlap = floatOverlap * format.getFrameSize();
        byteStepSize = floatStepSize * format.getFrameSize();
    }

    void addAudioProcessor(final AudioProcessor audioProcessor) {
        audioProcessors.add(audioProcessor);
    }

    public void run() {

        int bytesRead = 0;

        if (bytesToSkip != 0) {
            skipToStart();
        }

        try {
            bytesRead = readNextAudioBlock();
        } catch (final IOException e) {
            final String message = "Error while reading audio input stream: " + e.getMessage();
            throw new Error(message);
        }

        while (bytesRead != 0 && !stopped) {
            for (final AudioProcessor processor : audioProcessors) {
                if (!processor.process(audioEvent)) {
                    break;
                }
            }

            if (!stopped) {
                bytesProcessed += bytesRead;

                try {
                    bytesRead = readNextAudioBlock();
                } catch (final IOException e) {
                    final String message = "Error while reading audio input stream: " + e.getMessage();
                    throw new Error(message);
                }
            }
        }

        if (!stopped) {
            stop();
        }
    }

    private void skipToStart() {
        long skipped = 0l;
        try {
            skipped = audioInputStream.skip(bytesToSkip);
            if (skipped != bytesToSkip) {
                throw new IOException();
            }
            bytesProcessed += bytesToSkip;
        } catch (final IOException e) {
            final String message = String.format("Did not skip the expected amount of bytes,  %d skipped, %d expected!", skipped, bytesToSkip);
            throw new Error(message);
        }
    }

    void stop() {
        stopped = true;
        for (final AudioProcessor processor : audioProcessors) {
            processor.processingFinished();
        }
        try {
            audioInputStream.close();
        } catch (final IOException e) {

        }
    }

    private int readNextAudioBlock() throws IOException {
        assert floatOverlap < audioFloatBuffer.length;

        final boolean isFirstBuffer = (bytesProcessed == 0 || bytesProcessed == bytesToSkip);

        final int offsetInBytes;

        final int offsetInSamples;

        final int bytesToRead;
        if (isFirstBuffer) {
            bytesToRead = audioByteBuffer.length;
            offsetInBytes = 0;
            offsetInSamples = 0;
        } else {
            bytesToRead = byteStepSize;
            offsetInBytes = byteOverlap;
            offsetInSamples = floatOverlap;
        }

        if (!isFirstBuffer && audioFloatBuffer.length == floatOverlap + floatStepSize) {
            System.arraycopy(audioFloatBuffer, floatStepSize, audioFloatBuffer, 0, floatOverlap);
        }

        int totalBytesRead = 0;

        int bytesRead = 0;

        boolean endOfStream = false;

        while (!stopped && !endOfStream && totalBytesRead < bytesToRead) {
            try {
                bytesRead = audioInputStream.read(audioByteBuffer, offsetInBytes + totalBytesRead, bytesToRead - totalBytesRead);
            } catch (final IndexOutOfBoundsException e) {
                bytesRead = -1;
            }
            if (bytesRead == -1) {
                endOfStream = true;
            } else {
                totalBytesRead += bytesRead;
            }
        }

        if (endOfStream) {
            if (zeroPadLastBuffer) {
                for (int i = offsetInBytes + totalBytesRead; i < audioByteBuffer.length; i++) {
                    audioByteBuffer[i] = 0;
                }
                converter.toFloatArray(audioByteBuffer, offsetInBytes, audioFloatBuffer, offsetInSamples, floatStepSize);
            } else {
                final byte[] audioByteBufferContent = audioByteBuffer;
                audioByteBuffer = new byte[offsetInBytes + totalBytesRead];
                for (int i = 0; i < audioByteBuffer.length; i++) {
                    audioByteBuffer[i] = audioByteBufferContent[i];
                }
                final int totalSamplesRead = totalBytesRead / format.getFrameSize();
                audioFloatBuffer = new float[offsetInSamples + totalBytesRead / format.getFrameSize()];
                converter.toFloatArray(audioByteBuffer, offsetInBytes, audioFloatBuffer, offsetInSamples, totalSamplesRead);

            }
        } else if (bytesToRead == totalBytesRead) {
            if (isFirstBuffer) {
                converter.toFloatArray(audioByteBuffer, 0, audioFloatBuffer, 0, audioFloatBuffer.length);
            } else {
                converter.toFloatArray(audioByteBuffer, offsetInBytes, audioFloatBuffer, offsetInSamples, floatStepSize);
            }
        } else if (!stopped) {
            throw new IOException(String.format("The end of the audio stream has not been reached and the number of bytes read (%d) is not equal "
                    + "to the expected amount of bytes(%d).", totalBytesRead, bytesToRead));
        }

        audioEvent.setFloatBuffer(audioFloatBuffer);

        return totalBytesRead;
    }
}
