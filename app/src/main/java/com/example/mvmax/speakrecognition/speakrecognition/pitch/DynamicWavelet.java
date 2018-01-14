package com.example.mvmax.speakrecognition.speakrecognition.pitch;

import java.util.Arrays;

public class DynamicWavelet implements PitchDetector {

    private final PitchDetectionResult result;

    private final float sampleRate;

    private int[] distances;
    private int[] mins;
    private int[] maxs;

    DynamicWavelet(final float sampleRate, final int bufferSize) {
        this.sampleRate = sampleRate;

        distances = new int[bufferSize];
        mins = new int[bufferSize];
        maxs = new int[bufferSize];
        result = new PitchDetectionResult();
    }

    @Override
    public PitchDetectionResult getPitch(float[] audioBuffer) {
        float pitchF = -1.0f;

        int curSamNb = audioBuffer.length;

        int nbMins;
        int nbMaxs;

        if (distances.length == audioBuffer.length) {
            Arrays.fill(distances, 0);
            Arrays.fill(mins, 0);
            Arrays.fill(maxs, 0);
        } else {
            distances = new int[audioBuffer.length];
            mins = new int[audioBuffer.length];
            maxs = new int[audioBuffer.length];
        }

        final double ampltitudeThreshold;
        double theDC = 0.0;

        double maxValue = 0.0;
        double minValue = 0.0;
        for (int i = 0; i < audioBuffer.length; i++) {
            final double sample = audioBuffer[i];
            theDC = theDC + sample;
            maxValue = Math.max(maxValue, sample);
            minValue = Math.min(sample, minValue);
        }
        theDC = theDC / audioBuffer.length;
        maxValue = maxValue - theDC;
        minValue = minValue - theDC;
        final double amplitudeMax = (maxValue > -minValue ? maxValue : -minValue);

        final double maximaThresholdRatio = 0.75;
        ampltitudeThreshold = amplitudeMax * maximaThresholdRatio;

        int curLevel = 0;
        double curModeDistance = -1.;
        int delta;

        search:
        while (true) {
            final double maxF = 3000.;
            delta = (int) (sampleRate / (Math.pow(2, curLevel) * maxF));
            if (curSamNb < 2) {
                break search;
            }

            double dv, previousDV = -1000;

            nbMins = nbMaxs = 0;
            int lastMinIndex = -1000000;
            int lastmaxIndex = -1000000;
            boolean findMax = false;
            boolean findMin = false;
            for (int i = 2; i < curSamNb; i++) {
                final double si = audioBuffer[i] - theDC;
                final double si1 = audioBuffer[i - 1] - theDC;

                if (si1 <= 0 && si > 0) {
                    findMax = true;
                }
                if (si1 >= 0 && si < 0) {
                    findMin = true;
                }

                dv = si - si1;

                if (previousDV > -1000) {
                    if (findMin && previousDV < 0 && dv >= 0) {

                        if (Math.abs(si) >= ampltitudeThreshold) {
                            if (i > lastMinIndex + delta) {
                                mins[nbMins++] = i;
                                lastMinIndex = i;
                                findMin = false;
                            }
                        }
                    }

                    if (findMax && previousDV > 0 && dv <= 0) {
                        if (Math.abs(si) >= ampltitudeThreshold) {
                            if (i > lastmaxIndex + delta) {
                                maxs[nbMaxs++] = i;
                                lastmaxIndex = i;
                                findMax = false;
                            }
                        }
                    }
                }
                previousDV = dv;
            }

            if (nbMins == 0 && nbMaxs == 0) {
                break search;
            }

            int d;
            Arrays.fill(distances, 0);
            for (int i = 0; i < nbMins; i++) {
                final int differenceLevelsN = 3;
                for (int j = 1; j < differenceLevelsN; j++) {
                    if (i + j < nbMins) {
                        d = Math.abs(mins[i] - mins[i + j]);
                        distances[d] = distances[d] + 1;
                    }
                }
            }

            int bestDistance = -1;
            int bestValue = -1;
            for (int i = 0; i < curSamNb; i++) {
                int summed = 0;
                for (int j = -delta; j <= delta; j++) {
                    if (i + j >= 0 && i + j < curSamNb) {
                        summed += distances[i + j];
                    }
                }
                if (summed == bestValue) {
                    if (i == 2 * bestDistance) {
                        bestDistance = i;
                    }

                } else if (summed > bestValue) {
                    bestValue = summed;
                    bestDistance = i;
                }
            }

            double distAvg = 0.0;
            double nbDists = 0;
            for (int j = -delta; j <= delta; j++) {
                if (bestDistance + j >= 0 && bestDistance + j < audioBuffer.length) {
                    final int nbDist = distances[bestDistance + j];
                    if (nbDist > 0) {
                        nbDists += nbDist;
                        distAvg += (bestDistance + j) * nbDist;
                    }
                }
            }

            distAvg /= nbDists;

            if (curModeDistance > -1.) {
                final double similarity = Math.abs(distAvg * 2 - curModeDistance);
                if (similarity <= 2 * delta) {
                    pitchF = (float) (sampleRate / (Math.pow(2, curLevel - 1) * curModeDistance));
                    break search;
                }
            }

            curModeDistance = distAvg;

            curLevel = curLevel + 1;
            final int maxFLWTlevels = 6;
            if (curLevel >= maxFLWTlevels) {
                break search;
            }

            if (curSamNb < 2) {
                break search;
            }

            float[] newAudioBuffer = audioBuffer;
            if (curSamNb == distances.length) {
                newAudioBuffer = new float[curSamNb / 2];
            }
            for (int i = 0; i < curSamNb / 2; i++) {
                newAudioBuffer[i] = (audioBuffer[2 * i] + audioBuffer[2 * i + 1]) / 2.0f;
            }

            audioBuffer = newAudioBuffer;
            curSamNb /= 2;
        }

        result.setPitch(pitchF);
        result.setPitched(-1 != pitchF);
        result.setProbability(-1);

        return result;
    }
}