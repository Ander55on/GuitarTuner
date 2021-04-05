package com.dankout.guitartuner.tuner;

public interface FrequencyDetector {

    /**
     * @param audioData The captured audio
     * @param fMax The maximum frequency in the search range
     * @param fMin The minimum frequency in the search range
     * @param samplingRate The sampling rate of the recording
     * */
    public float getFundamentalFrequency(short[] audioData, int fMax, int fMin, int samplingRate);
}
