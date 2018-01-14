package com.example.mvmax.speakrecognition.speakrecognition.lift;

public class LineWavelet extends LiftingSchemeBaseWavelet {

    private float new_y(final float y1, final float y2) {
        final float y = 2 * y2 - y1;
        return y;
    }

    protected void predict(final float[] vec, final int N, final int direction) {
        final int half = N >> 1;
        float predictVal;

        for (int i = 0; i < half; i++) {
            final int j = i + half;
            if (i < half - 1) {
                predictVal = (vec[i] + vec[i + 1]) / 2;
            } else if (N == 2) {
                predictVal = vec[0];
            } else {
                predictVal = new_y(vec[i - 1], vec[i]);
            }

            if (direction == forward) {
                vec[j] = vec[j] - predictVal;
            } else if (direction == inverse) {
                vec[j] = vec[j] + predictVal;
            }
        }
    }

    protected void update(final float[] vec, final int N, final int direction) {
        final int half = N >> 1;

        for (int i = 0; i < half; i++) {
            final int j = i + half;
            final float val;

            if (i == 0) {
                val = vec[j] / 2.0f;
            } else {
                val = (vec[j - 1] + vec[j]) / 4.0f;
            }
            if (direction == forward) {
                vec[i] = vec[i] + val;
            } else if (direction == inverse) {
                vec[i] = vec[i] - val;
            }
        }
    }
}
