package com.example.mvmax.speakrecognition.speakrecognition.lift;

public class HaarWithPolynomialInterpolationWavelet extends HaarWavelet {

    private static final int numPts = 4;
    private final PolynomialInterpolation fourPt;

    HaarWithPolynomialInterpolationWavelet() {
        fourPt = new PolynomialInterpolation();
    }

    private void fill(final float[] vec, final float[] d, final int N, final int start) {
        int n = numPts;
        if (n > N) {
            n = N;
        }
        final int end = start + n;
        int j = 0;

        for (int i = start; i < end; i++) {
            d[j] = vec[i];
            j++;
        }
    }

    private void interp(final float[] vec, final int N, final int direction) {
        final int half = N >> 1;
        final float[] d = new float[numPts];

        for (int i = 0; i < half; i++) {
            final float predictVal;

            if (i == 0) {
                if (half == 1) {
                    predictVal = vec[0];
                } else {
                    fill(vec, d, N, 0);
                    predictVal = fourPt.interpPoint(0.5f, half, d);
                }
            } else if (i == 1) {
                predictVal = fourPt.interpPoint(1.5f, half, d);
            } else if (i == half - 2) {
                predictVal = fourPt.interpPoint(2.5f, half, d);
            } else if (i == half - 1) {
                predictVal = fourPt.interpPoint(3.5f, half, d);
            } else {
                fill(vec, d, N, i - 1);
                predictVal = fourPt.interpPoint(1.5f, half, d);
            }

            final int j = i + half;
            if (direction == forward) {
                vec[j] = vec[j] - predictVal;
            } else if (direction == inverse) {
                vec[j] = vec[j] + predictVal;
            }
        }
    }

    public void forwardTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = N; n > 1; n = n >> 1) {
            split(vec, n);
            predict(vec, n, forward);
            update(vec, n, forward);
            interp(vec, n, forward);
        }
    }

    public void inverseTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = 2; n <= N; n = n << 1) {
            interp(vec, n, inverse);
            update(vec, n, inverse);
            predict(vec, n, inverse);
            merge(vec, n);
        }
    }
}
