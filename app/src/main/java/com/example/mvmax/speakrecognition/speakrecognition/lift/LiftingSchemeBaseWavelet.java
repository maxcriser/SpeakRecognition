package com.example.mvmax.speakrecognition.speakrecognition.lift;

public abstract class LiftingSchemeBaseWavelet {

    final int forward = 1;

    final int inverse = 2;

    void split(final float[] vec, final int N) {

        int start = 1;
        int end = N - 1;

        while (start < end) {
            for (int i = start; i < end; i = i + 2) {
                final float tmp = vec[i];
                vec[i] = vec[i + 1];
                vec[i + 1] = tmp;
            }
            start = start + 1;
            end = end - 1;
        }
    }

    void merge(final float[] vec, final int N) {
        final int half = N >> 1;
        int start = half - 1;
        int end = half;

        while (start > 0) {
            for (int i = start; i < end; i = i + 2) {
                final float tmp = vec[i];
                vec[i] = vec[i + 1];
                vec[i + 1] = tmp;
            }
            start = start - 1;
            end = end + 1;
        }
    }

    protected abstract void predict(float[] vec, int N, int direction);

    protected abstract void update(float[] vec, int N, int direction);

    public void forwardTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = N; n > 1; n = n >> 1) {
            split(vec, n);
            predict(vec, n, forward);
            update(vec, n, forward);
        }
    }

    public void inverseTrans(final float[] vec) {
        final int N = vec.length;

        for (int n = 2; n <= N; n = n << 1) {
            update(vec, n, inverse);
            predict(vec, n, inverse);
            merge(vec, n);
        }
    }

}
