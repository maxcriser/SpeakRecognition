package com.example.mvmax.speakrecognition.speakrecognition.io;

public class TarsosDSPAudioFormat {

    private final Encoding encoding;

    private final float sampleRate;

    private final int sampleSizeInBits;

    private final int channels;

    private final int frameSize;

    private final float frameRate;

    private final boolean bigEndian;

    private static final int NOT_SPECIFIED = -1;

    private TarsosDSPAudioFormat(final Encoding encoding, final float sampleRate, final int sampleSizeInBits,
                                 final int channels, final int frameSize, final float frameRate, final boolean bigEndian) {

        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
    }

    public TarsosDSPAudioFormat(final float sampleRate, final int sampleSizeInBits,
                                final int channels, final boolean signed, final boolean bigEndian) {

        this((signed ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED),
                sampleRate,
                sampleSizeInBits,
                channels,
                (channels == NOT_SPECIFIED || sampleSizeInBits == NOT_SPECIFIED) ?
                        NOT_SPECIFIED :
                        ((sampleSizeInBits + 7) / 8) * channels,
                sampleRate,
                bigEndian);
    }

    Encoding getEncoding() {

        return encoding;
    }

    private float getSampleRate() {

        return sampleRate;
    }

    int getSampleSizeInBits() {

        return sampleSizeInBits;
    }

    int getChannels() {

        return channels;
    }

    public int getFrameSize() {

        return frameSize;
    }

    private float getFrameRate() {

        return frameRate;
    }

    boolean isBigEndian() {

        return bigEndian;
    }

    public String toString() {
        String sEncoding = "";
        if (getEncoding() != null) {
            sEncoding = getEncoding() + " ";
        }

        final String sSampleRate;
        if (getSampleRate() == (float) NOT_SPECIFIED) {
            sSampleRate = "unknown sample rate, ";
        } else {
            sSampleRate = "" + getSampleRate() + " Hz, ";
        }

        final String sSampleSizeInBits;
        if (getSampleSizeInBits() == (float) NOT_SPECIFIED) {
            sSampleSizeInBits = "unknown bits per sample, ";
        } else {
            sSampleSizeInBits = "" + getSampleSizeInBits() + " bit, ";
        }

        final String sChannels;
        if (getChannels() == 1) {
            sChannels = "mono, ";
        } else if (getChannels() == 2) {
            sChannels = "stereo, ";
        } else {
            if (getChannels() == NOT_SPECIFIED) {
                sChannels = " unknown number of channels, ";
            } else {
                sChannels = "" + getChannels() + " channels, ";
            }
        }

        final String sFrameSize;
        if (getFrameSize() == (float) NOT_SPECIFIED) {
            sFrameSize = "unknown frame size, ";
        } else {
            sFrameSize = "" + getFrameSize() + " bytes/frame, ";
        }

        String sFrameRate = "";
        if (Math.abs(getSampleRate() - getFrameRate()) > 0.00001) {
            if (getFrameRate() == (float) NOT_SPECIFIED) {
                sFrameRate = "unknown frame rate, ";
            } else {
                sFrameRate = getFrameRate() + " frames/second, ";
            }
        }

        String sEndian = "";
        if ((getEncoding().equals(Encoding.PCM_SIGNED)
                || getEncoding().equals(Encoding.PCM_UNSIGNED))
                && ((getSampleSizeInBits() > 8)
                || (getSampleSizeInBits() == NOT_SPECIFIED))) {
            if (isBigEndian()) {
                sEndian = "big-endian";
            } else {
                sEndian = "little-endian";
            }
        }

        return sEncoding
                + sSampleRate
                + sSampleSizeInBits
                + sChannels
                + sFrameSize
                + sFrameRate
                + sEndian;

    }

    public static class Encoding {

        static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");

        static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");

        private final String name;

        Encoding(final String name) {
            this.name = name;
        }

        public final boolean equals(final Object obj) {
            if (toString() == null) {
                return (obj != null) && (obj.toString() == null);
            }

            return obj instanceof Encoding && toString().equals(obj.toString());
        }

        public final int hashCode() {
            if (toString() == null) {
                return 0;
            }
            return toString().hashCode();
        }

        public final String toString() {
            return name;
        }

    }
}
