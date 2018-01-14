package com.example.mvmax.speakrecognition.speakrecognition;

public interface AudioProcessor {

    boolean process(AudioEvent audioEvent);

    void processingFinished();
}
