package com.example.mvmax.speakrecognition.speakrecognition.lift;

public class HaarWavelet extends LiftingSchemeBaseWavelet {

    protected void predict(final float[] vec, final int N, final int direction) {
        final int half = N >> 1;

        for (int i = 0; i < half; i++) {
            final float predictVal = vec[i];
            final int j = i + half;

            if (direction == forward) {
                vec[j] = vec[j] - predictVal;
            } else if (direction == inverse) {
                vec[j] = vec[j] + predictVal;
            }
        }
    }

    void forwardTransOne(final float[] vec) {
        final int N = vec.length;

        split(vec, N);
        predict(vec, N, forward);
        update(vec, N, forward);

    }

    protected void update(final float[] vec, final int N, final int direction) {
        final int half = N >> 1;

        for (int i = 0; i < half; i++) {
            final int j = i + half;
            final float updateVal = vec[j] / 2.0f;

            if (direction == forward) {
                vec[i] = vec[i] + updateVal;
            } else if (direction == inverse) {
                vec[i] = vec[i] - updateVal;
            }
        }
    }
}
