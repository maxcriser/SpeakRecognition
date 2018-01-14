package com.example.mvmax.speakrecognition.speakrecognition.lift;

class PolynomialInterpolation {

    private static final int numPts = 4;

    private final float[][] fourPointTable;

    private final float[][] twoPointTable;

    private void lagrange(final float x, final int N, final float[] c) {
        float num, denom;

        for (int i = 0; i < N; i++) {
            num = 1;
            denom = 1;
            for (int k = 0; k < N; k++) {
                if (i != k) {
                    num = num * (x - k);
                    denom = denom * (i - k);
                }
            }
            c[i] = num / denom;
        }
    }

    private void fillTable(final int N, final float[][] table) {
        float x;
        final float n = N;
        int i = 0;

        for (x = 0.5f; x < n; x = x + 1.0f) {
            lagrange(x, N, table[i]);
            i++;
        }
    }

    PolynomialInterpolation() {
        fourPointTable = new float[numPts][numPts];

        fillTable(numPts, fourPointTable);

        twoPointTable = new float[2][2];

        fillTable(2, twoPointTable);
    }

    private void getCoef(final float x, final int n, final float[] c) {
        float[][] table = null;

        final int j = (int) x;

        if (n == numPts) {
            table = fourPointTable;
        } else if (n == 2) {
            table = twoPointTable;
            c[2] = 0.0f;
            c[3] = 0.0f;
        }

        if (table != null) {
            for (int i = 0; i < n; i++) {
                c[i] = table[j][i];
            }
        }
    }

    float interpPoint(final float x, final int N, final float[] d) {
        final float[] c = new float[numPts];
        float point = 0;

        int n = numPts;
        if (N < numPts) {
            n = N;
        }

        getCoef(x, n, c);

        if (n == numPts) {
            point = c[0] * d[0] + c[1] * d[1] + c[2] * d[2] + c[3] * d[3];
        } else if (n == 2) {
            point = c[0] * d[0] + c[1] * d[1];
        }

        return point;
    }

}
