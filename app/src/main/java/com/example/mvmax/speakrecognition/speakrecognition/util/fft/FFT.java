package com.example.mvmax.speakrecognition.speakrecognition.util.fft;

public class FFT {

    private final FloatFFT fft;
    private final WindowFunction windowFunction;
    private final float[] window;

    public FFT(final int size, final WindowFunction windowFunction) {
        fft = new FloatFFT(size);
        this.windowFunction = windowFunction;
        if (windowFunction == null) {
            window = null;
        } else {
            window = windowFunction.generateCurve(size);
        }
    }

    public void forwardTransform(final float[] data) {
        if (windowFunction != null) {
            for (int i = 0; i < window.length - 1; i++) {
                data[i] = data[i] * window[i];
            }
        }
        fft.realForward(data);
    }

    public float modulus(final float[] data, final int index) {
        final int realIndex = 2 * index;
        final int imgIndex = 2 * index + 1;
        final float modulus = data[realIndex] * data[realIndex] + data[imgIndex] * data[imgIndex];
        return (float) Math.sqrt(modulus);
    }
}
