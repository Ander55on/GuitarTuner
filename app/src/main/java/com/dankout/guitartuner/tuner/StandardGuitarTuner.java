package com.dankout.guitartuner.tuner;

import com.dankout.guitartuner.Guitar;

import java.util.HashMap;

/**
 * Class used for standard tuning consists of the Notes E2-A2-D3-G3-B3-E4
 */
public class StandardGuitarTuner implements GuitarTuner {

    HashMap<Integer, Float> stringFrequencies;

    public StandardGuitarTuner() {
        stringFrequencies = new HashMap<>();

        //Setting up the frequencies
        stringFrequencies.put(Guitar.LOW_E_STRING, Frequency.E2);
        stringFrequencies.put(Guitar.A_STRING, Frequency.A2);
        stringFrequencies.put(Guitar.D_STRING, Frequency.D3);
        stringFrequencies.put(Guitar.G_STRING, Frequency.G3);
        stringFrequencies.put(Guitar.B_STRING, Frequency.B3);
        stringFrequencies.put(Guitar.HIGH_E_STRING, Frequency.E4);

    }

    @Override
    public float frequency() {
        return 0;
    }


    @Override
    public boolean tune(int guitarString) {
        return false;
    }



}
