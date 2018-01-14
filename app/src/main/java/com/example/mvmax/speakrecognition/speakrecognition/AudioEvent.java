package com.example.mvmax.speakrecognition.speakrecognition;

public class AudioEvent {

    private float[] floatBuffer;

    AudioEvent() {
    }

    void setFloatBuffer(final float[] floatBuffer) {
        this.floatBuffer = floatBuffer;
    }

    public float[] getFloatBuffer() {
        return floatBuffer;
    }
}
