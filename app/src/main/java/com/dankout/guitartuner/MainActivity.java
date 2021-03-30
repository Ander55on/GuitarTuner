package com.dankout.guitartuner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    //Configurations for the Audio recorder
    private static final int RECORDING_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSize;
    private AudioRecord mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Returns the minimum buffer size for the successful creation of an AudioRecord object
        this.mBufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING);

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,RECORDING_SAMPLE_RATE,
                AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING, mBufferSize);
    }

    public void record(View view) {

       Drawable background = null;

       if(mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

       } else {
           view.setActivated(true);
       }



    }
}