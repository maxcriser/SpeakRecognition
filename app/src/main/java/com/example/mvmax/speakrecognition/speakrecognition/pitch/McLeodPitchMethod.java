package com.example.mvmax.speakrecognition.speakrecognition.pitch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class McLeodPitchMethod implements PitchDetector {

    private static final double DEFAULT_CUTOFF = 0.97;
    private static final double SMALL_CUTOFF = 0.5;
    private static final double LOWER_PITCH_CUTOFF = 80.0; // Hz
    private final double cutoff;
    private final float sampleRate;
    private final float[] nsdf;
    private float turningPointX, turningPointY;
    private final Collection<Integer> maxPositions = new ArrayList<>();
    private final List<Float> periodEstimates = new ArrayList<>();
    private final List<Float> ampEstimates = new ArrayList<>();
    private final PitchDetectionResult result;

    McLeodPitchMethod(final float audioSampleRate, final int audioBufferSize) {
        this(audioSampleRate, audioBufferSize, DEFAULT_CUTOFF);
    }

    private McLeodPitchMethod(final float audioSampleRate, final int audioBufferSize, final double cutoffMPM) {
        this.sampleRate = audioSampleRate;
        nsdf = new float[audioBufferSize];
        this.cutoff = cutoffMPM;
        result = new PitchDetectionResult();
    }

    private void normalizedSquareDifference(final float[] audioBuffer) {
        for (int tau = 0; tau < audioBuffer.length; tau++) {
            float acf = 0;
            float divisorM = 0;
            for (int i = 0; i < audioBuffer.length - tau; i++) {
                acf += audioBuffer[i] * audioBuffer[i + tau];
                divisorM += audioBuffer[i] * audioBuffer[i] + audioBuffer[i + tau] * audioBuffer[i + tau];
            }
            nsdf[tau] = 2 * acf / divisorM;
        }
    }

    public PitchDetectionResult getPitch(final float[] audioBuffer) {
        final float pitch;

        maxPositions.clear();
        periodEstimates.clear();
        ampEstimates.clear();

        normalizedSquareDifference(audioBuffer);
        peakPicking();

        double highestAmplitude = Double.NEGATIVE_INFINITY;

        for (final Integer tau : maxPositions) {
            highestAmplitude = Math.max(highestAmplitude, nsdf[tau]);

            if (nsdf[tau] > SMALL_CUTOFF) {
                parabolicInterpolation(tau);
                ampEstimates.add(turningPointY);
                periodEstimates.add(turningPointX);
                highestAmplitude = Math.max(highestAmplitude, turningPointY);
            }
        }

        if (periodEstimates.isEmpty()) {
            pitch = -1;
        } else {
            final double actualCutoff = cutoff * highestAmplitude;

            int periodIndex = 0;
            for (int i = 0; i < ampEstimates.size(); i++) {
                if (ampEstimates.get(i) >= actualCutoff) {
                    periodIndex = i;
                    break;
                }
            }

            final double period = periodEstimates.get(periodIndex);
            final float pitchEstimate = (float) (sampleRate / period);
            if (pitchEstimate > LOWER_PITCH_CUTOFF) {
                pitch = pitchEstimate;
            } else {
                pitch = -1;
            }

        }
        result.setProbability((float) highestAmplitude);
        result.setPitch(pitch);
        result.setPitched(pitch != -1);

        return result;
    }

    private void parabolicInterpolation(final int tau) {
        final float nsdfa = nsdf[tau - 1];
        final float nsdfb = nsdf[tau];
        final float nsdfc = nsdf[tau + 1];
        final float bValue = tau;
        final float bottom = nsdfc + nsdfa - 2 * nsdfb;
        if (bottom == 0.0) {
            turningPointX = bValue;
            turningPointY = nsdfb;
        } else {
            final float delta = nsdfa - nsdfc;
            turningPointX = bValue + delta / (2 * bottom);
            turningPointY = nsdfb - delta * delta / (8 * bottom);
        }
    }

    private void peakPicking() {

        int pos = 0;
        int curMaxPos = 0;

        while (pos < (nsdf.length - 1) / 3 && nsdf[pos] > 0) {
            pos++;
        }

        while (pos < nsdf.length - 1 && nsdf[pos] <= 0.0) {
            pos++;
        }

        if (pos == 0) {
            pos = 1;
        }

        while (pos < nsdf.length - 1) {
            assert nsdf[pos] >= 0;
            if (nsdf[pos] > nsdf[pos - 1] && nsdf[pos] >= nsdf[pos + 1]) {
                if (curMaxPos == 0) {
                    curMaxPos = pos;
                } else if (nsdf[pos] > nsdf[curMaxPos]) {
                    curMaxPos = pos;
                }
            }
            pos++;
            if (pos < nsdf.length - 1 && nsdf[pos] <= 0) {
                if (curMaxPos > 0) {
                    maxPositions.add(curMaxPos);
                    curMaxPos = 0;
                }
                while (pos < nsdf.length - 1 && nsdf[pos] <= 0.0f) {
                    pos++;
                }
            }
        }
        if (curMaxPos > 0) {
            maxPositions.add(curMaxPos);
        }
    }
}
