package com.dankout.guitartuner.tuner;

import android.util.Log;

import com.dankout.guitartuner.Guitar;

import java.util.HashMap;

/**
 * Class used for standard tuning consists of the Notes E2-A2-D3-G3-B3-E4
 */
public class StandardGuitarTuner implements GuitarTuner {
    private final String TAG = Guitar.class.getSimpleName();

    HashMap<Integer, Float> stringFrequencies;
    private float currentFreq;
    private FrequencyDetector detector;
    private int fMax = 600;
    private int fMin = 70;

    public StandardGuitarTuner() {
        stringFrequencies = new HashMap<>();
        currentFreq = 0;
        detector = new AutoCorrelationDetector();

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
        return this.currentFreq;
    }


    @Override
    public boolean tune(short[] audioData, int guitarString, int samplingRate) {
        currentFreq = detector.getFundamentalFrequency(audioData, fMax, fMin, samplingRate);
        float wantedFrequency = stringFrequencies.get(guitarString);

        if (wantedFrequency - 5f < currentFreq && currentFreq < wantedFrequency + 5f) {
            return true;
        }

        return false;
    }
}
