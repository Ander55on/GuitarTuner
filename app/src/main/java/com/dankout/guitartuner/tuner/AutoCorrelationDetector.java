package com.dankout.guitartuner.tuner;

public class AutoCorrelationDetector implements FrequencyDetector {
    private int tauMin;
    private int tauMax;

    @Override
    public float getFundamentalFrequency(short[] audioData, int fMax, int fMin, int samplingRate) {
        //Expecting the signal to repeat itself and generate the max value somewhere
        //between this two shifts
        tauMin = samplingRate / fMax;
        tauMax = samplingRate / fMin;

        int windowSize = audioData.length;
        float peak = -1;
        float period = 0;

        //Find the strongest peak according to the autocorrelation function -> generates two local max
        //The period corresponds to when this peak occured and the frequency can then be calculated
        //by taking the sampling rate / period

        for(int i = 0; i < windowSize - tauMax; i++) {
            //shift the signal and compare to peak
            for(int j = tauMin; j < tauMax; j++) {

                float temp = audioData[i] * audioData[i + j];

                if(temp > peak) {
                    peak = temp;
                    period = j;
                }
            }
        }

        return samplingRate / period;
    }


}
