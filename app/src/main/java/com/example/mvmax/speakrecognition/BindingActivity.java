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
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.mvmax.speakrecognition.speakrecognition.RecordingMFCCService;

public class BindingActivity extends Activity {

    BroadcastReceiver receiver;
    RecordingMFCCService mService;
    Intent intentBindService;

    boolean isBound;

    Button btnStartRecording;
    Button btnStopRecording;
    TextView txtMessage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        txtMessage = findViewById(R.id.txtMessage);

        btnStartRecording.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (mService.isDispatcherNull()) {
                    mService.initDispatcher();
                    mService.startMfccExtraction();
                    mService.startPitchDetection();
                } else {
                    intentBindService = new Intent(getApplicationContext(), RecordingMFCCService.class);
                    bindService(intentBindService, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });

        btnStopRecording.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
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
                final String msgFromService = intent.getStringExtra(RecordingMFCCService.COPA_MESSAGE);

                txtMessage.setText(msgFromService);
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