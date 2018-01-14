package com.example.mvmax.speakrecognition.speakrecognition.lift;

final class LiftingSchemeTest {

    public static void main(final String[] args) {

        final float[] vals = {25, 40, 8, 24, 48, 48, 40, 16};

        final HaarWavelet hr = new HaarWavelet();
        final LineWavelet ln = new LineWavelet();
        final Daubechies4Wavelet d = new Daubechies4Wavelet();

        final HaarWithPolynomialInterpolationWavelet hrpy = new HaarWithPolynomialInterpolationWavelet();
        final PolynomialWavelets py = new PolynomialWavelets();

        hr.forwardTrans(vals);
        hr.inverseTrans(vals);
        d.forwardTrans(vals);
        d.inverseTrans(vals);
        ln.forwardTrans(vals);
        ln.inverseTrans(vals);
        hrpy.forwardTrans(vals);
        hrpy.inverseTrans(vals);
        py.forwardTrans(vals);
        py.inverseTrans(vals);

        final float[] t = {56, 40, 8, 24, 48, 48, 40, 16};
        hr.forwardTransOne(t);

        final float[] signal = {56, 40, 8, 24, 48, 48, 40, 16};
        dwtHaar(signal);
    }

    private static void dwtHaar(final float[] signal) {
        final float[] s = new float[signal.length];
        final float[] d = new float[signal.length];
        for (int i = 0; i < signal.length / 2; i++) {
            s[i] = (signal[2 * i] + signal[2 * i + 1]) / 2.0f;
            d[i] = signal[2 * i] - s[i];
        }
    }
}