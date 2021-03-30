package com.dankout.guitartuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    //Configurations for the Audio recorder
    private static final int RECORDING_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //Request codes
    private final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 1;

    private int mBufferSize;
    private AudioRecord mRecorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Returns the minimum buffer size for the successful creation of an AudioRecord object
        this.mBufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING);

        if (mBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Recording parameters are not supported by the hardware or an invalid parameter was passed");
        }

    }

    public void record(View view) {

        Drawable background = null;

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

            if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                return false;
            }

            mRecorder.startRecording();

            return true;
        }

        return false;
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
}