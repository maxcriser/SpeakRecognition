package com.example.mvmax.speakrecognition.speakrecognition.lift;

public class Daubechies4Wavelet extends LiftingSchemeBaseWavelet {

    private static final float sqrt3 = (float) Math.sqrt(3);
    private static final float sqrt2 = (float) Math.sqrt(2);

    private void normalize(final float[] S, final int N, final int direction) {
        final int half = N >> 1;

        for (int n = 0; n < half; n++) {
            if (direction == forward) {
                S[n] = ((sqrt3 - 1.0f) / sqrt2) * S[n];
                S[n + half] = ((sqrt3 + 1.0f) / sqrt2) * S[n + half];
            } else if (direction == inverse) {
                S[n] = ((sqrt3 + 1.0f) / sqrt2) * S[n];
                S[n + half] = ((sqrt3 - 1.0f) / sqrt2) * S[n + half];
            } else {
                break;
            }
        }
    }

    protected void predict(final float[] S, final int N, final int direction) {
        final int half = N >> 1;

        if (direction == forward) {
            S[half] = S[half] - (sqrt3 / 4.0f) * S[0]
                    - (((sqrt3 - 2) / 4.0f) * S[half - 1]);
        } else if (direction == inverse) {
            S[half] = S[half] + (sqrt3 / 4.0f) * S[0]
                    + (((sqrt3 - 2) / 4.0f) * S[half - 1]);
        }

        for (int n = 1; n < half; n++) {
            if (direction == forward) {
                S[half + n] = S[half + n] - (sqrt3 / 4.0f) * S[n]
                        - (((sqrt3 - 2) / 4.0f) * S[n - 1]);
            } else if (direction == inverse) {
                S[half + n] = S[half + n] + (sqrt3 / 4.0f) * S[n]
                        + (((sqrt3 - 2) / 4.0f) * S[n - 1]);
            } else {
                break;
            }
        }

    }

    private void updateOne(final float[] S, final int N, final int direction) {
        final int half = N >> 1;

        for (int n = 0; n < half; n++) {
            final float updateVal = sqrt3 * S[half + n];

            if (direction == forward) {
                S[n] = S[n] + updateVal;
            } else if (direction == inverse) {
                S[n] = S[n] - updateVal;
            } else {
                break;
            }
        }
    }

    protected void update(final float[] S, final int N, final int direction) {
        final int half = N >> 1;

        for (int n = 0; n < half - 1; n++) {
            if (direction == forward) {
                S[n] = S[n] - S[half + n + 1];
            } else if (direction == inverse) {
                S[n] = S[n] + S[half + n + 1];
            } else {
                break;
            }
        }

        if (direction == forward) {
            S[half - 1] = S[half - 1] - S[half];
        } else if (direction == inverse) {
            S[half - 1] = S[half - 1] + S[half];
        }
    }

    public void forwardTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = N; n > 1; n = n >> 1) {
            split(vec, n);
            updateOne(vec, n, forward); // update 1
            predict(vec, n, forward);
            update(vec, n, forward); // update 2
            normalize(vec, n, forward);
        }
    }

    public void inverseTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = 2; n <= N; n = n << 1) {
            normalize(vec, n, inverse);
            update(vec, n, inverse);
            predict(vec, n, inverse);
            updateOne(vec, n, inverse);
            merge(vec, n);
        }
    }
}