package com.example.mvmax.speakrecognition.speakrecognition.pitch;

public final class Yin implements PitchDetector {

    private static final double DEFAULT_THRESHOLD = 0.20;

    private final double threshold;

    private final float sampleRate;

    private final float[] yinBuffer;

    private final PitchDetectionResult result;

    Yin(final float audioSampleRate, final int bufferSize) {
        this(audioSampleRate, bufferSize, DEFAULT_THRESHOLD);
    }

    private Yin(final float audioSampleRate, final int bufferSize, final double yinThreshold) {
        this.sampleRate = audioSampleRate;
        this.threshold = yinThreshold;
        yinBuffer = new float[bufferSize / 2];
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
        int index, tau;
        float delta;
        for (tau = 0; tau < yinBuffer.length; tau++) {
            yinBuffer[tau] = 0;
        }
        for (tau = 1; tau < yinBuffer.length; tau++) {
            for (index = 0; index < yinBuffer.length; index++) {
                delta = audioBuffer[index] - audioBuffer[index + tau];
                yinBuffer[tau] += delta * delta;
            }
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

        if (tau == yinBuffer.length || yinBuffer[tau] >= threshold) {
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
            float s0, s1, s2;
            s0 = yinBuffer[x0];
            s1 = yinBuffer[tauEstimate];
            s2 = yinBuffer[x2];
            betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
        }
        return betterTau;
    }
}
