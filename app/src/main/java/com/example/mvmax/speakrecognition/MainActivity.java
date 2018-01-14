package com.example.mvmax.speakrecognition;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.mvmax.speakrecognition.speakrecognition.RecordingMFCCService;

public class MainActivity extends Activity {

    BroadcastReceiver receiver;
    RecordingMFCCService mService;
    Intent intentBindService;

    boolean isBound;

    Button btnStartRecording;
    Button btnStopRecording;
    TextView txtMessage;
    TextView result;

    boolean mSpeakingSignal;
    private int speakingSeconds;

    private Chronometer mChronometer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChronometer = findViewById(R.id.chronometer);
        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        txtMessage = findViewById(R.id.txtMessage);
        result = findViewById(R.id.result);

        btnStartRecording.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (mService.isDispatcherNull()) {
                    mService.initDispatcher();
                    mService.startMfccExtraction();
                    mService.startPitchDetection();

                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();

                    mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

                        @Override
                        public void onChronometerTick(final Chronometer chronometer) {
                            final long elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();

                            Log.d("thecriser", elapsedMillis + "");

                            if (mSpeakingSignal) {
                                speakingSeconds++;

                                result.setText(speakingSeconds + "");
                            }

                            mSpeakingSignal = false;
                        }
                    });
                } else {
                    intentBindService = new Intent(getApplicationContext(), RecordingMFCCService.class);
                    bindService(intentBindService, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });

        btnStopRecording.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                mChronometer.stop();
                mChronometer.setBase(SystemClock.elapsedRealtime());

                if (isBound) {
                    unbindService(mConnection);
                    isBound = false;
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
                }

                if (!mService.isDispatcherNull()) {
                    mService.stopDispatcher();
                    txtMessage.setText("Recording stopped !");
                }
            }
        });

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(final Context context, final Intent intent) {
                final boolean speakingSignal = intent.getBooleanExtra(RecordingMFCCService.SPEAKING_SIGNAL_MESSAGE, false);

                if (speakingSignal) {
                    mSpeakingSignal = true;

                    txtMessage.setText("Speaking");
                } else {
                    txtMessage.setText("Silent");
                }
            }
        };

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        intentBindService = new Intent(this, RecordingMFCCService.class);
        bindService(intentBindService, mConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(RecordingMFCCService.COPA_RESULT));
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            final RecordingMFCCService.LocalBinder binder = (RecordingMFCCService.LocalBinder) service;
            mService = binder.getService();
            isBound = true;

            txtMessage.setText("Connected");

        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            isBound = false;
            txtMessage.setText("Disconnected");
        }
    };
}