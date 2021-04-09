package com.dankout.guitartuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dankout.guitartuner.tuner.StandardGuitarTuner;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    /*Configurations for the Audio recorder*/

    // Amplitude values are recorded every 1/44100 second
    private static final int RECORDING_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    //The pulse code modulation used to convert the analog signal into a digital signal
    //-> bit depth 65536 levels
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //Request code
    private final int PERMISSION_RECORD_AUDIO_REQUEST_CODE = 1;

    private final String CURRENT_STRING = "currentString";

    private int mBufferSize;
    private int mCurrentStringToTune;
    private TextView mFrequencyTextView;
    private HashMap<Integer, View> mStringsAndViewsMap;
    private HashSet<Integer> mInTuneStrings;

    //GraphView
    private LineGraphSeries<DataPoint> mSeries;
    private double mGraphsLastXValue = 0;
    private final int MAX_DATA_POINTS = 100;

    private double noiseLeveldB = 0;
    private final int THRESH_HOLD_dB = 20;

    private AudioRecord mRecorder = null;
    private Guitar guitar;

    //States
    private final int CALIBRATION_STATE = 1;
    private final int SAMPLING_STATE = 2;
    private int currentState = 1;

    //Calibration time in ms
    private final int CALIBRATION_TIME = 1000;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFrequencyTextView = findViewById(R.id.TextView_inTune);

        //GraphView init
        GraphView mGraphView = findViewById(R.id.graphView);
        mSeries = new LineGraphSeries<>();
        mGraphView.addSeries(mSeries);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(40);
        mGraphView.getViewport().setDrawBorder(false);
        mGraphView.setBackgroundColor(Color.BLACK);
        mGraphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);

        mStringsAndViewsMap = new HashMap<>();
        mStringsAndViewsMap.put(Guitar.LOW_E_STRING, (View) findViewById(R.id.E_Low_String_Button));
        mStringsAndViewsMap.put(Guitar.A_STRING, (View) findViewById(R.id.A_String_Button));
        mStringsAndViewsMap.put(Guitar.D_STRING, (View) findViewById(R.id.D_String_Button));
        mStringsAndViewsMap.put(Guitar.G_STRING, (View) findViewById(R.id.G_String_Button));
        mStringsAndViewsMap.put(Guitar.B_STRING, (View) findViewById(R.id.B_String_Button));
        mStringsAndViewsMap.put(Guitar.HIGH_E_STRING, (View) findViewById(R.id.E_High_String_Button));

        setActiveGuitarString(findViewById(R.id.E_Low_String_Button));

        mInTuneStrings = new HashSet<>();

        //Returns the minimum buffer size for the successful creation of an AudioRecord object
        this.mBufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, AUDIO_CHANNEL_CONFIGURATION, AUDIO_FORMAT_ENCODING);
        this.guitar = new Guitar(new StandardGuitarTuner());

        if (mBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Recording parameters are not supported by " +
                    "the hardware or an invalid parameter was passed");
        }
        Log.d(TAG, "size: " + mBufferSize);

        if (savedInstanceState != null) {
            mCurrentStringToTune = savedInstanceState.getInt(CURRENT_STRING);
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
            startTime = System.nanoTime();
            Thread thread = new Thread(this::sendDataToTuner);
            thread.start();

            return true;
        }

        return false;
    }

    private void sendDataToTuner() {
        while (mRecorder != null && mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

            short[] audioData = new short[mBufferSize]; //Since encoding is PCM_16bit we use the short array
            int readSize = mRecorder.read(audioData, 0, mBufferSize);

            //Calibrate the current room noise levels for reference
            long sum = 0;
            for (int i = 0; i < mBufferSize; i++) {
                sum += audioData[i] * audioData[i];
            }

            double mean = sum / (double) readSize;
            double dB = 10 * Math.log10(mean);

            if (currentState == CALIBRATION_STATE) {
                if ((System.nanoTime() - startTime) / Math.pow(10, 6) > CALIBRATION_TIME) {
                    currentState = SAMPLING_STATE;
                }
                noiseLeveldB = Math.max(noiseLeveldB, dB);

            } else {

                if (dB > noiseLeveldB + THRESH_HOLD_dB) {
                    int step = mBufferSize / MAX_DATA_POINTS;

                    for (int i = 0; i < audioData.length; i += mBufferSize / step) {
                        short dataPoint = audioData[i];
                        runOnUiThread(() -> appendDataPoint(dataPoint));
                    }

                    boolean inTune = guitar.tune(audioData, mCurrentStringToTune, RECORDING_SAMPLE_RATE);
                    float freq = guitar.getFrequency();

                    runOnUiThread(() -> updateUI(freq, inTune));
                } else {
                    runOnUiThread(() -> appendDataPoint(0));
                }

            }

        }
    }

    private void appendDataPoint(int dataPoint) {
        mGraphsLastXValue += 1d;
        mSeries.appendData(new DataPoint(mGraphsLastXValue, dataPoint), true, MAX_DATA_POINTS);
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release(); //Releases the native AudioRecord resources
        mRecorder = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENT_STRING, mCurrentStringToTune);
        super.onSaveInstanceState(outState);
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

    private void updateUI(float freq, boolean inTune) {
        if (inTune) {
            mInTuneStrings.add(mCurrentStringToTune);
            mStringsAndViewsMap.get(mCurrentStringToTune).setActivated(true);
        }
        if (mInTuneStrings.contains(mCurrentStringToTune)) {
            mFrequencyTextView.setText("In Tune");
        } else {
            mFrequencyTextView.setText(freq + " HZ");
        }
    }

    public void setActiveGuitarString(View view) {
        deActivateAllStringButtons();

        if (view.getId() == R.id.A_String_Button) {
            mCurrentStringToTune = Guitar.A_STRING;

        } else if (view.getId() == R.id.B_String_Button) {

            mCurrentStringToTune = Guitar.B_STRING;

        } else if (view.getId() == R.id.D_String_Button) {

            mCurrentStringToTune = Guitar.D_STRING;

        } else if (view.getId() == R.id.E_High_String_Button) {

            mCurrentStringToTune = Guitar.HIGH_E_STRING;

        } else if (view.getId() == R.id.E_Low_String_Button) {

            mCurrentStringToTune = Guitar.LOW_E_STRING;

        } else {
            mCurrentStringToTune = Guitar.G_STRING;
        }

        view.setSelected(true);
        //view.setActivated(true);
    }

    private void deActivateAllStringButtons() {
        findViewById(R.id.A_String_Button).setSelected(false);
        findViewById(R.id.B_String_Button).setSelected(false);
        findViewById(R.id.D_String_Button).setSelected(false);
        findViewById(R.id.E_High_String_Button).setSelected(false);
        findViewById(R.id.E_Low_String_Button).setSelected(false);
        findViewById(R.id.G_String_Button).setSelected(false);
    }

}