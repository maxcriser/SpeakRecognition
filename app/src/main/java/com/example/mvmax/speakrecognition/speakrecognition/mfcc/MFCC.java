package com.example.mvmax.speakrecognition.speakrecognition.mfcc;

import com.example.mvmax.speakrecognition.speakrecognition.AudioEvent;
import com.example.mvmax.speakrecognition.speakrecognition.AudioProcessor;
import com.example.mvmax.speakrecognition.speakrecognition.util.fft.FFT;
import com.example.mvmax.speakrecognition.speakrecognition.util.fft.HammingWindow;

public class MFCC implements AudioProcessor {

    private final int amountOfCepstrumCoef;
    private final int amountOfMelFilters;
    private final float lowerFilterFreq;
    private final float upperFilterFreq;

    private float[] mfcc;

    private int[] centerFrequencies;

    private final FFT fft;
    private final int samplesPerFrame;
    private final float sampleRate;

    public MFCC(final int samplesPerFrame, final float sampleRate, final int amountOfCepstrumCoef, final int amountOfMelFilters, final float lowerFilterFreq, final float upperFilterFreq) {
        this.samplesPerFrame = samplesPerFrame;
        this.sampleRate = sampleRate;
        this.amountOfCepstrumCoef = amountOfCepstrumCoef;
        this.amountOfMelFilters = amountOfMelFilters;
        this.fft = new FFT(samplesPerFrame, new HammingWindow());

        this.lowerFilterFreq = Math.max(lowerFilterFreq, 25);
        this.upperFilterFreq = Math.min(upperFilterFreq, sampleRate / 2);
        calculateFilterBanks();
    }

    @Override
    public boolean process(final AudioEvent audioEvent) {
        final float[] audioFloatBuffer = audioEvent.getFloatBuffer().clone();

        final float[] bin = magnitudeSpectrum(audioFloatBuffer);
        final float[] fbank = melFilter(bin, centerFrequencies);
        final float[] f = nonLinearTransformation(fbank);
        mfcc = cepCoefficients(f);

        return true;
    }

    @Override
    public void processingFinished() {

    }

    private float[] magnitudeSpectrum(final float[] frame) {
        final float[] magSpectrum = new float[frame.length];

        fft.forwardTransform(frame);

        for (int k = 0; k < frame.length / 2; k++) {
            magSpectrum[frame.length / 2 + k] = fft.modulus(frame, frame.length / 2 - 1 - k);
            magSpectrum[frame.length / 2 - 1 - k] = magSpectrum[frame.length / 2 + k];
        }

        return magSpectrum;
    }

    private void calculateFilterBanks() {
        centerFrequencies = new int[amountOfMelFilters + 2];

        centerFrequencies[0] = Math.round(lowerFilterFreq / sampleRate * samplesPerFrame);
        centerFrequencies[centerFrequencies.length - 1] = samplesPerFrame / 2;

        final double[] mel = new double[2];
        mel[0] = freqToMel(lowerFilterFreq);
        mel[1] = freqToMel(upperFilterFreq);

        final float factor = (float) ((mel[1] - mel[0]) / (amountOfMelFilters + 1));
        for (int i = 1; i <= amountOfMelFilters; i++) {
            final float fc = (inverseMel(mel[0] + factor * i) / sampleRate) * samplesPerFrame;
            centerFrequencies[i - 1] = Math.round(fc);
        }

    }

    private float[] nonLinearTransformation(final float[] fbank) {
        final float[] f = new float[fbank.length];
        final float FLOOR = -50;

        for (int i = 0; i < fbank.length; i++) {
            f[i] = (float) Math.log(fbank[i]);

            if (f[i] < FLOOR) {
                f[i] = FLOOR;
            }
        }

        return f;
    }

    private float[] melFilter(final float[] bin, final int[] centerFrequencies) {
        final float[] temp = new float[amountOfMelFilters + 2];

        for (int k = 1; k <= amountOfMelFilters; k++) {
            float num1 = 0, num2 = 0;

            float den = (centerFrequencies[k] - centerFrequencies[k - 1] + 1);

            for (int i = centerFrequencies[k - 1]; i <= centerFrequencies[k]; i++) {
                num1 += bin[i] * (i - centerFrequencies[k - 1] + 1);
            }
            num1 /= den;

            den = (centerFrequencies[k + 1] - centerFrequencies[k] + 1);

            for (int i = centerFrequencies[k] + 1; i <= centerFrequencies[k + 1]; i++) {
                num2 += bin[i] * (1 - ((i - centerFrequencies[k]) / den));
            }

            temp[k] = num1 + num2;
        }

        final float[] fbank = new float[amountOfMelFilters];

        for (int i = 0; i < amountOfMelFilters; i++) {
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }

    private float[] cepCoefficients(final float[] f) {
        final float[] cepc = new float[amountOfCepstrumCoef];

        for (int i = 0; i < cepc.length; i++) {
            for (int j = 0; j < f.length; j++) {
                cepc[i] += f[j] * Math.cos(Math.PI * i / f.length * (j + 0.5));
            }
        }

        return cepc;
    }

    private static float freqToMel(final float freq) {
        return 2595 * log10(1 + freq / 700);
    }

    private static float inverseMel(final double x) {
        return (float) (700 * (Math.pow(10, x / 2595) - 1));
    }

    private static float log10(final float value) {
        return (float) (Math.log(value) / Math.log(10));
    }

    public float[] getMFCC() {
        return mfcc.clone();
    }
}