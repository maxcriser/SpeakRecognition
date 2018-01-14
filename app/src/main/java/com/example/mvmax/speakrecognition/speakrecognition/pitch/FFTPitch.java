package com.example.mvmax.speakrecognition.speakrecognition.pitch;

public class FFTPitch implements PitchDetector {

    private final PitchDetectionResult result;

    FFTPitch() {
        result = new PitchDetectionResult();
    }

    @Override
    public PitchDetectionResult getPitch(final float[] audioBuffer) {

        return result;
    }

}
