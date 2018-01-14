package com.example.mvmax.speakrecognition.speakrecognition.pitch;

import com.example.mvmax.speakrecognition.speakrecognition.AudioEvent;

public interface PitchDetectionHandler {

    void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent);
}
