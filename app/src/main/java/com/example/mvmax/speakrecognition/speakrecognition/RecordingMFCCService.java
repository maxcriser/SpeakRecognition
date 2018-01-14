package com.example.mvmax.speakrecognition.speakrecognition;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.mvmax.speakrecognition.speakrecognition.mfcc.MFCC;
import com.example.mvmax.speakrecognition.speakrecognition.pitch.PitchDetectionHandler;
import com.example.mvmax.speakrecognition.speakrecognition.pitch.PitchDetectionResult;
import com.example.mvmax.speakrecognition.speakrecognition.pitch.PitchProcessor;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordingMFCCService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private String TAG = "MFCCService";

    private AudioDispatcher dispatcher;
    final double endTime = 20.0;

    static int mfccIndex;
    ArrayList<float[]> mfccList;

    LocalBroadcastManager broadcaster;
    static final public String COPA_RESULT = "com.example.tarsosaudioproject.RecordingMFCCService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "UINotification";

    Handler handler;
    String uiMessage = "";

    //MFCC attributes
    final int samplesPerFrame = 512;
    final int sampleRate = 16000;
    final int amountOfCepstrumCoef = 19; //actually 18 but energy column would be discarded
    int amountOfMelFilters = 30;
    float lowerFilterFreq = 133.3334f;
    float upperFilterFreq = ((float) sampleRate) / 2f;

    public RecordingMFCCService() {
        Log.d(TAG, "constructor done");

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate called");

        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    public class LocalBinder extends Binder {

        public RecordingMFCCService getService() {
            Log.d(TAG, "getService done");

            return RecordingMFCCService.this;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind done");

        return mBinder;
    }

    public void initDispatcher() {
        Log.d(TAG, "initDispatcher done");
        mfccList = new ArrayList<>();

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(16000, 1024, 0); //(22050,1024,0);
    }

    public boolean isDispatcherNull() {
        return dispatcher == null;
    }

    public void stopDispatcher() {
        dispatcher.stop();
        dispatcher = null;
    }

    public void startMfccExtraction() {
        final MFCC mfccObj = new MFCC(samplesPerFrame, sampleRate, amountOfCepstrumCoef, amountOfMelFilters, lowerFilterFreq, upperFilterFreq); //(1024,22050);

        dispatcher.addAudioProcessor(mfccObj);
        dispatcher.addAudioProcessor(new AudioProcessor() {

            @Override
            public void processingFinished() {

            }

            @Override
            public boolean process(final AudioEvent audioEvent) {
                float[] mfccOutput = mfccObj.getMFCC();
                mfccOutput = Arrays.copyOfRange(mfccOutput, 1, mfccOutput.length);

                mfccList.add(mfccOutput);
                Log.i("MFCC", String.valueOf(Arrays.toString(mfccOutput)));

                return true;
            }
        });

        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    public ArrayList<float[]> getMfccList() {
        return mfccList;
    }

    public void startPitchDetection() {
        Log.d(TAG, "startPitchDetection");

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 16000, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(final PitchDetectionResult pitchDetectionResult, final AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();

                if (pitchInHz == -1) {
                    sendResult("Silent");
                } else {
                    sendResult("Speaking");
                }
            }
        }));
    }

    public void sendResult(final String message) {
        final Intent intent = new Intent(COPA_RESULT);

        if (message != null) {
            intent.putExtra(COPA_MESSAGE, message);
        }

        broadcaster.sendBroadcast(intent);
    }
}