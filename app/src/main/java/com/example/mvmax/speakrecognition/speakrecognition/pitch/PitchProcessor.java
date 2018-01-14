package com.example.mvmax.speakrecognition.speakrecognition.pitch;

import com.example.mvmax.speakrecognition.speakrecognition.AudioEvent;
import com.example.mvmax.speakrecognition.speakrecognition.AudioProcessor;

public class PitchProcessor implements AudioProcessor {

    public enum PitchEstimationAlgorithm {
        /**
         * See {@link Yin} for the implementation. Or see <a href=
         * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
         * >the YIN article</a>.
         */
        YIN,
        /**
         * See {@link McLeodPitchMethod}. It is described in the article "<a
         * href=
         * "http://miracle.otago.ac.nz/postgrads/tartini/papers/A_Smarter_Way_to_Find_Pitch.pdf"
         * >A Smarter Way to Find Pitch</a>".
         */
        MPM,
        /**
         * A YIN implementation with a faster  {@link FastYin} for the implementation. Or see <a href=
         * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf"
         * >the YIN article</a>.
         */
        FFT_YIN,
        /**
         * An implementation of a dynamic wavelet pitch detection algorithm (See
         * {@link DynamicWavelet}), described in a paper by Eric Larson and Ross
         * Maddox <a href= http://online.physics.uiuc
         * .edu/courses/phys498pom/NSF_REU_Reports/2005_reu/Real
         * -Time_Time-Domain_Pitch_Tracking_Using_Wavelets.pdf">"Real-Time
         * Time-Domain Pitch Tracking Using Wavelets</a>
         */
        DYNAMIC_WAVELET,
        /**
         * Returns the frequency of the FFT-bin with most energy.
         */
        FFT_PITCH,
        /**
         * A pitch extractor that extracts the Average Magnitude Difference
         * (AMDF) from an audio buffer. This is a good measure of the Pitch (f0)
         * of a signal.
         */
        AMDF;

        /**
         * Returns a new instance of a pitch detector object based on the provided values.
         *
         * @param sampleRate The sample rate of the audio buffer.
         * @param bufferSize The size (in samples) of the audio buffer.
         * @return A new pitch detector object.
         */
        public PitchDetector getDetector(final float sampleRate, final int bufferSize) {
            final PitchDetector detector;
            if (this == MPM) {
                detector = new McLeodPitchMethod(sampleRate, bufferSize);
            } else if (this == DYNAMIC_WAVELET) {
                detector = new DynamicWavelet(sampleRate, bufferSize);
            } else if (this == FFT_YIN) {
                detector = new FastYin(sampleRate, bufferSize);
            } else if (this == AMDF) {
                detector = new AMDF(sampleRate, bufferSize);
            } else if (this == FFT_PITCH) {
                detector = new FFTPitch();
            } else {
                detector = new Yin(sampleRate, bufferSize);
            }
            return detector;
        }

    }

    private final PitchDetector detector;

    private final PitchDetectionHandler handler;

    public PitchProcessor(final PitchEstimationAlgorithm algorithm, final float sampleRate,
                          final int bufferSize,
                          final PitchDetectionHandler handler) {
        detector = algorithm.getDetector(sampleRate, bufferSize);
        this.handler = handler;
    }

    @Override
    public boolean process(final AudioEvent audioEvent) {
        final float[] audioFloatBuffer = audioEvent.getFloatBuffer();

        final PitchDetectionResult result = detector.getPitch(audioFloatBuffer);

        handler.handlePitch(result, audioEvent);
        return true;
    }

    @Override
    public void processingFinished() {

    }
}