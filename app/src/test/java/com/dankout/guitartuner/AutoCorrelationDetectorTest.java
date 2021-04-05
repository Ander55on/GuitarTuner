package com.dankout.guitartuner;

import com.dankout.guitartuner.tuner.AutoCorrelationDetector;
import com.dankout.guitartuner.tuner.FrequencyDetector;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AutoCorrelationDetectorTest {

    private FrequencyDetector detector;

    @Test
    public void testDetectingFrequency() {
        detector = new AutoCorrelationDetector();
        int fMin = 75;
        int fMax = 600;
        int sampleRate = 41000;
        int freq = 400;
        double period = sampleRate / freq;
        short[] data = new short[sampleRate];


        for(int i = 0; i < sampleRate; i++) {
            double angle = freq * Math.PI * (i / period);
            data[i] = (short)(Math.sin(angle) * Short.MAX_VALUE);
        }

       float result = detector.getFundamentalFrequency(data,fMax,fMin,sampleRate);
       assertEquals(400, result, 5);

    }


}
