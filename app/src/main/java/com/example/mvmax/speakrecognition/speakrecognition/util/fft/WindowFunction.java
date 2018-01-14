package com.example.mvmax.speakrecognition.speakrecognition.util.fft;

public abstract class WindowFunction {

    static final float TWO_PI = (float) (2 * Math.PI);
    protected int length;

    WindowFunction() {
    }

    float[] generateCurve(final int length) {
        final float[] samples = new float[length];
        for (int n = 0; n < length; n++) {
            samples[n] = 1f * value(length, n);
        }
        return samples;
    }

    protected abstract float value(int length, int index);
}
