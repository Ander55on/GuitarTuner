package com.dankout.guitartuner.tuner;

public interface GuitarTuner {
    /**
     * @return current frequency of the string
     */
    float frequency();

    /**
     * @param data audio data
     * @param guitarString to tune
     * @param samplingRate the sampling rate
     * @return true if the string is tuned
     * */
    boolean tune(short[] data, int guitarString, int samplingRate);

}
