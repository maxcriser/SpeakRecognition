package com.example.mvmax.speakrecognition.speakrecognition.pitch;

import com.example.mvmax.speakrecognition.speakrecognition.util.fft.FloatFFT;

public final class FastYin implements PitchDetector {

    private static final double DEFAULT_THRESHOLD = 0.20;

    private final double threshold;

    private final float sampleRate;

    private final float[] yinBuffer;

    private final PitchDetectionResult result;

    private final float[] audioBufferFFT;

    private final float[] kernel;

    private final float[] yinStyleACF;

    private final FloatFFT fft;

    FastYin(final float audioSampleRate, final int bufferSize) {
        this(audioSampleRate, bufferSize, DEFAULT_THRESHOLD);
    }

    private FastYin(final float audioSampleRate, final int bufferSize, final double yinThreshold) {
        this.sampleRate = audioSampleRate;
        this.threshold = yinThreshold;
        yinBuffer = new float[bufferSize / 2];
        audioBufferFFT = new float[2 * bufferSize];
        kernel = new float[2 * bufferSize];
        yinStyleACF = new float[2 * bufferSize];
        fft = new FloatFFT(bufferSize);
        result = new PitchDetectionResult();
    }

    public PitchDetectionResult getPitch(final float[] audioBuffer) {

        final int tauEstimate;
        final float pitchInHertz;

        difference(audioBuffer);

        cumulativeMeanNormalizedDifference();

        tauEstimate = absoluteThreshold();

        if (tauEstimate != -1) {
            final float betterTau = parabolicInterpolation(tauEstimate);

            pitchInHertz = sampleRate / betterTau;
        } else {
            pitchInHertz = -1;
        }

        result.setPitch(pitchInHertz);

        return result;
    }

    private void difference(final float[] audioBuffer) {
        final float[] powerTerms = new float[yinBuffer.length];
        for (int j = 0; j < yinBuffer.length; ++j) {
            powerTerms[0] += audioBuffer[j] * audioBuffer[j];
        }
        for (int tau = 1; tau < yinBuffer.length; ++tau) {
            powerTerms[tau] = powerTerms[tau - 1] - audioBuffer[tau - 1] * audioBuffer[tau - 1] + audioBuffer[tau + yinBuffer.length] * audioBuffer[tau + yinBuffer.length];
        }

        for (int j = 0; j < audioBuffer.length; ++j) {
            audioBufferFFT[2 * j] = audioBuffer[j];
            audioBufferFFT[2 * j + 1] = 0;
        }
        fft.complexForward(audioBufferFFT);

        for (int j = 0; j < yinBuffer.length; ++j) {
            kernel[2 * j] = audioBuffer[(yinBuffer.length - 1) - j];
            kernel[2 * j + 1] = 0;
            kernel[2 * j + audioBuffer.length] = 0;
            kernel[2 * j + audioBuffer.length + 1] = 0;
        }
        fft.complexForward(kernel);

        for (int j = 0; j < audioBuffer.length; ++j) {
            yinStyleACF[2 * j] = audioBufferFFT[2 * j] * kernel[2 * j] - audioBufferFFT[2 * j + 1] * kernel[2 * j + 1]; // real
            yinStyleACF[2 * j + 1] = audioBufferFFT[2 * j + 1] * kernel[2 * j] + audioBufferFFT[2 * j] * kernel[2 * j + 1]; // imaginary
        }
        fft.complexInverse(yinStyleACF);

        for (int j = 0; j < yinBuffer.length; ++j) {
            yinBuffer[j] = powerTerms[0] + powerTerms[j] - 2 * yinStyleACF[2 * (yinBuffer.length - 1 + j)];
        }
    }

    private void cumulativeMeanNormalizedDifference() {
        int tau;
        yinBuffer[0] = 1;
        float runningSum = 0;
        for (tau = 1; tau < yinBuffer.length; tau++) {
            runningSum += yinBuffer[tau];
            yinBuffer[tau] *= tau / runningSum;
        }
    }

    private int absoluteThreshold() {
        int tau;
        for (tau = 2; tau < yinBuffer.length; tau++) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < yinBuffer.length && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++;
                }

                result.setProbability(1 - yinBuffer[tau]);
                break;
            }
        }

        if (tau == yinBuffer.length || yinBuffer[tau] >= threshold || result.getProbability() > 1.0) {
            tau = -1;
            result.setProbability(0);
            result.setPitched(false);
        } else {
            result.setPitched(true);
        }

        return tau;
    }

    private float parabolicInterpolation(final int tauEstimate) {
        final float betterTau;
        final int x0;
        final int x2;

        if (tauEstimate < 1) {
            x0 = tauEstimate;
        } else {
            x0 = tauEstimate - 1;
        }
        if (tauEstimate + 1 < yinBuffer.length) {
            x2 = tauEstimate + 1;
        } else {
            x2 = tauEstimate;
        }
        if (x0 == tauEstimate) {
            if (yinBuffer[tauEstimate] <= yinBuffer[x2]) {
                betterTau = tauEstimate;
            } else {
                betterTau = x2;
            }
        } else if (x2 == tauEstimate) {
            if (yinBuffer[tauEstimate] <= yinBuffer[x0]) {
                betterTau = tauEstimate;
            } else {
                betterTau = x0;
            }
        } else {
            final float s0;
            float s1;
            final float s2;
            s0 = yinBuffer[x0];
            s1 = yinBuffer[tauEstimate];
            s2 = yinBuffer[x2];
            betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
        }
        return betterTau;
    }
}
