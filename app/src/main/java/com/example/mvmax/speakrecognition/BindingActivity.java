package com.example.mvmax.speakrecognition;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mvmax.speakrecognition.speakrecognition.RecordingMFCCService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BindingActivity extends Activity {

    private static final String appProcessName = "com.example.tarsosaudioproject";
    private static final String TAG = "MFCCBindingActivity";
    static final String Thesis_Tarsos_CSV_PATH = "Thesis/Tarsos/CSV";
    static final String csvFileName = "tarsos_mfcc.csv";
    static volatile FileOperations instance;

    FileOperations fileOprObj;
    BroadcastReceiver receiver;
    RecordingMFCCService mService;
    Intent intentBindService;

    boolean isBound;

    Button btnStartRecording, btnStopRecording;
    TextView txtMessage;

    public static FileOperations getInstance(final BindingActivity mainBindingActivity) {
        if (instance == null) {
            synchronized (FileOperations.class) {
                instance = new FileOperations(mainBindingActivity);
            }
        }
        return instance;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileOprObj = getInstance(this);

        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        txtMessage = findViewById(R.id.txtMessage);

        btnStartRecording.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (mService.isDispatcherNull()) {

                    mService.initDispatcher();

                    monitorBatteryUsage("Start");
                    monitorCpuAndMemoryUsage("Start");

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
                    Log.i(TAG, "onStopRecording Service unbinded & isbound : " + isBound);

                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
                } else {
                    Log.i(TAG, "onStopRecording Service unbinded & isbound : " + isBound);

                }

                if (!mService.isDispatcherNull()) {
                    Log.i(TAG, "onStopRecording isDispatcher : " + mService.isDispatcherNull());

                    mService.stopDispatcher();

                    txtMessage.setText("Recording stopped !");
                    monitorBatteryUsage("End  ");
                    monitorCpuAndMemoryUsage("End  ");

                    final Runnable runnable = new Runnable() {

                        @Override
                        public void run() {

                            try {
                                audioFeatures2csv(mService.getMfccList());
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    new Thread(runnable).start();
                } else {
                    Log.i(TAG, "onStopRecording isDispatcher : " + mService.isDispatcherNull());
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

    public void showToast(final CharSequence text) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder service) {
            Log.i(TAG, "onServiceConnected");

            final RecordingMFCCService.LocalBinder binder = (RecordingMFCCService.LocalBinder) service;
            mService = binder.getService();
            isBound = true;

            txtMessage.setText("Connected");

        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            Log.i(TAG, "onServiceDisConnected");

            isBound = false;
            txtMessage.setText("Disconnected");

        }
    };

    private void audioFeatures2csv(final Iterable<float[]> csvInput) throws IOException {
        final String csvfileStoragePath = Environment.getExternalStorageDirectory() + File.separator + Thesis_Tarsos_CSV_PATH;
        final File sdCsvStorageDir = new File(csvfileStoragePath);

        if (!sdCsvStorageDir.exists()) {
            sdCsvStorageDir.mkdirs();
        }

        if (sdCsvStorageDir.exists()) {

            final PrintWriter csvWriter;
            try {
                final String filePath = sdCsvStorageDir + File.separator + csvFileName;

                csvWriter = new PrintWriter(new FileWriter(filePath, false));

                for (final float[] oneline : csvInput) {
                    for (final float d : oneline) {
                        csvWriter.print(d + ",");

                    }
                    csvWriter.print("\r\n");

                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        txtMessage.setText("CSV Data Written Successfully !!");
                    }
                });

                csvWriter.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}