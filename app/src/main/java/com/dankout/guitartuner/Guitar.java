package com.dankout.guitartuner;

import com.dankout.guitartuner.tuner.GuitarTuner;

public class Guitar {

    private GuitarTuner tuner;

    public static final int LOW_E_STRING = 1;
    public static final int A_STRING = 2;
    public static final int D_STRING = 3;
    public static final int G_STRING = 4;
    public static final int B_STRING = 5;
    public static final int HIGH_E_STRING = 6;

    public Guitar(GuitarTuner tuner) {
        this.tuner = tuner;
    }

    public boolean tune(short[] audioData, int guitarString, int samplingRate) {

        return tuner.tune(audioData, guitarString,samplingRate);

    }

    public float getFrequency() {
        return tuner.frequency();
    }


}
