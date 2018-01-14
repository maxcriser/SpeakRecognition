package com.example.mvmax.speakrecognition.speakrecognition.util.fft;

public class HammingWindow extends WindowFunction {

    public HammingWindow() {
    }

    protected float value(final int length, final int index) {
        return 0.54f - 0.46f * (float) Math.cos(TWO_PI * index / (length - 1));
    }
}

