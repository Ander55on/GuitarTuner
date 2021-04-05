package com.dankout.guitartuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dankout.guitartuner.tuner.StandardGuitarTuner;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    /*Configurations for the Audio recorder*/

    // Amplitude values are recorded every 1/44100 second
    private static final int RECORDING_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    //The pulse code modulation used to convert the analog signal into a digital signal
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //Request codes
    private final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 1;

    private int mBufferSize;
    private int mCurrentStringToTune;
    private TextView currentStringTextView;
    private TextView inTuneTextView;

    private AudioRecord mRecorder = null;
    private Guitar guitar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentStringTextView = findViewById(R.id.TextView_CurrentString);
        inTuneTextView = findViewById(R.id.TextView_inTune);

        //Returns the minimum buffer size for the successful creation of an AudioRecord object
        this.mBufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING);
        this.guitar = new Guitar(new StandardGuitarTuner());

        if (mBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Recording parameters are not supported by " +
                    "the hardware or an invalid parameter was passed");
        }

    }

    public void record(View view) {

        if (mRecorder == null || mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {

            if (startRecording()) {
                view.setActivated(true);
            }

        } else {
            stopRecording();
            view.setActivated(false);
        }
    }

    private boolean startRecording() {

        if (hasRecordAudioPermission()) {
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDING_SAMPLE_RATE,
                    AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING, mBufferSize);

            //Something went wrong when initializing the AudioRecord
            if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                return false;
            }

            mRecorder.startRecording();
            Thread thread = new Thread(this::sendDataToTuner);
            thread.start();

            return true;
        }

        return false;
    }

    private void sendDataToTuner() {
        while(mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

            //Since encoding is PCM_16bit we use the short array
            short[] audioData = new short[mBufferSize];
            mRecorder.read(audioData,0, mBufferSize);
            String inTune = guitar.tune(audioData, mCurrentStringToTune, RECORDING_SAMPLE_RATE);
            float freq = guitar.getFrequency();

            runOnUiThread(() -> updateUI(inTune,freq));

        }
    }

    private void stopRecording() {
        mRecorder.stop();
        //Releases the native AudioRecord resources
        mRecorder.release();
        mRecorder = null;
    }

    private boolean hasRecordAudioPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO_REQUEST_CODE);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //TODO handle denied request of using RECORD_AUDIO
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void updateUI(String text, float freq) {
        inTuneTextView.setText(text + " " + freq);
    }

    public void setActiveGuitarString(View view) {

       if(view.getId() == R.id.A_String_Button) {

           mCurrentStringToTune = Guitar.A_STRING;
           currentStringTextView.setText(R.string.a_string);

       } else if(view.getId() == R.id.B_String_Button){

           mCurrentStringToTune = Guitar.B_STRING;
           currentStringTextView.setText(R.string.b_String);

        } else if(view.getId() == R.id.D_String_Button) {

           mCurrentStringToTune = Guitar.D_STRING;
           currentStringTextView.setText(R.string.d_string);

       } else if(view.getId() == R.id.E_High_String_Button) {

           mCurrentStringToTune = Guitar.HIGH_E_STRING;
           currentStringTextView.setText(R.string.e_String);

       } else if(view.getId() == R.id.E_Low_String_Button) {

           mCurrentStringToTune = Guitar.LOW_E_STRING;
           currentStringTextView.setText(R.string.e_String);

       } else {
           mCurrentStringToTune = Guitar.G_STRING;
           currentStringTextView.setText(R.string.g_String);
       }

    }
}