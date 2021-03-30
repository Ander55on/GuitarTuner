package com.dankout.guitartuner.tuner;

public interface GuitarTuner {
    /**
     * @return current frequency of the string
     */
    float frequency();

    /**
     * @param guitarString to tune
     * @return true if the string is tuned
     * */
    boolean tune(int guitarString);

}
