package com.example.mvmax.speakrecognition.speakrecognition.pitch;

public interface PitchDetector {

    PitchDetectionResult getPitch(final float[] audioBuffer);

}
