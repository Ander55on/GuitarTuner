package com.dankout.guitartuner.soundMeter;

import android.media.AudioRecord;

public class SoundMeter {

    /**
     * @param audioData the array to which the recorded audio data is written
     * @param bufferSize The size of the buffer
     * @param readSize The number of shorts that was read
     * @return amplitude converted to decibel
     */
    public double decibelReading(short audioData[], int bufferSize, int readSize) {

        long sum = 0;
        for (int i = 0; i < bufferSize; i++) {
            sum += audioData[i] * audioData[i];
        }

        double mean = sum / (double) readSize;
        double dB = 10 * Math.log10(mean);
        return dB;
    }
}
