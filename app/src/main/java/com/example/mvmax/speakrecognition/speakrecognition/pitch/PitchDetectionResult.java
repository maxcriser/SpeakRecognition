package com.example.mvmax.speakrecognition.speakrecognition.pitch;

public class PitchDetectionResult {

    private float pitch;
    private float probability;
    private boolean pitched;

    public PitchDetectionResult() {
        pitch = -1;
        probability = -1;
        pitched = false;
    }

    public PitchDetectionResult(final PitchDetectionResult other) {
        this.pitch = other.pitch;
        this.probability = other.probability;
        this.pitched = other.pitched;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public PitchDetectionResult clone() {
        return new PitchDetectionResult(this);
    }

    float getProbability() {
        return probability;
    }

    void setProbability(final float probability) {
        this.probability = probability;
    }

    void setPitched(final boolean pitched) {
        this.pitched = pitched;
    }
}
