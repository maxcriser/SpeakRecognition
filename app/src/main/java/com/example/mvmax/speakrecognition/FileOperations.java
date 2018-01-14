package com.example.mvmax.speakrecognition;

import android.os.Environment;
import android.util.Log;

import com.example.mvmax.speakrecognition.BindingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

class FileOperations {

    private static final File SD_PATH = Environment.getExternalStorageDirectory();
    private static final String SD_FOLDER_PATH_PARENT = "/Thesis";
    private static String SD_FOLDER_PATH = "/Thesis/Tarsos";
    private static String SD_FOLDER_PATH_LOGS = SD_FOLDER_PATH + "/Logs";
    public static String SD_FOLDER_PATH_CSV = SD_FOLDER_PATH + "/CSV";

    final String csvFileName = "tarsos_mfcc.csv";
    final String batteryFileName = "battery_data.txt";
    final String memcpuFileName = "memcpu_data.txt";

    final static String TAG = "VoiceRecognizerSP"; //Voice Recognizer with Superpowered functionality

    BindingActivity activityObj;

    public FileOperations(BindingActivity mainBindingActivity) {
        activityObj = mainBindingActivity;

        resolveDirs();

    }

    public void resolveDirs() {

        if (checkDirsExist()) {
            //if successfully dir created
            appendToBatteryFile("____New Experiment____");
            appendToMemCpuFile("____New Experiment____");
        }

    }

    //http://examples.javacodegeeks.com/core-java/io/fileoutputstream/append-output-to-file-with-fileoutputstream/

    /**
     * To add battery stats data to the file in Logs folder
     *
     * @param dataToAppend
     */
    public void appendToBatteryFile(String dataToAppend) {

        String logsfileStoragePath = Environment.getExternalStorageDirectory() + File.separator + SD_FOLDER_PATH_LOGS;
        File sdLogsStorageDir = new File(logsfileStoragePath);

        File file = new File(sdLogsStorageDir.toString() + File.separator + batteryFileName);

        if (sdLogsStorageDir.exists()) {

            PrintWriter pw;

            try {

                FileOutputStream fos = new FileOutputStream(file, true);
                pw = new PrintWriter(fos);
                pw.println(dataToAppend);
                pw.flush();
                pw.close();

                Log.i(TAG, "MFCC battery data appended : " + dataToAppend);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * To check if Logs Dir exist or if not then create it
     */
    private boolean checkDirsExist() {
        File dirParent = new File(SD_PATH + SD_FOLDER_PATH_PARENT);
        File dirMain = new File(SD_PATH + SD_FOLDER_PATH);
        File dirCSV = new File(SD_PATH + SD_FOLDER_PATH_CSV);
        File dirLogs = new File(SD_PATH + SD_FOLDER_PATH_LOGS);

        try {
            if (!dirParent.exists()) {
                dirParent.mkdir();
            }
            if (!dirMain.exists()) {
                dirMain.mkdir();
            }
            if (!dirCSV.exists()) {
                dirCSV.mkdir();
            }
            if (!dirLogs.exists()) {
                dirLogs.mkdir();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public void resetFiles() {
        String logsfileStoragePath = Environment.getExternalStorageDirectory() + File.separator + SD_FOLDER_PATH_LOGS;
        File sdLogsStorageDir = new File(logsfileStoragePath);

        File fileBattery = new File(sdLogsStorageDir.toString() + File.separator + batteryFileName);
        File fileMemCpu = new File(sdLogsStorageDir.toString() + File.separator + memcpuFileName);

        fileBattery.delete();
        fileMemCpu.delete();

        String csvfileStoragePath = Environment.getExternalStorageDirectory() + File.separator + SD_FOLDER_PATH_CSV;
        File sdCsvStorageDir = new File(csvfileStoragePath);

        File fileCSV = new File(sdCsvStorageDir.toString() + File.separator + csvFileName);
        fileCSV.delete();

    }

    /**
     * To add cpu and memory stats data to the file in Logs folder
     *
     * @param dataToAppend
     */
    public void appendToMemCpuFile(String dataToAppend) {

        String logsfileStoragePath = Environment.getExternalStorageDirectory() + File.separator + SD_FOLDER_PATH_LOGS;
        File sdLogsStorageDir = new File(logsfileStoragePath);

        File file = new File(sdLogsStorageDir.toString() + File.separator + memcpuFileName);

        //if (file.exists()) {
        if (sdLogsStorageDir.exists()) {

            PrintWriter pw;

            try {

                FileOutputStream fos = new FileOutputStream(file, true);
                pw = new PrintWriter(fos);
                pw.println(dataToAppend);
                pw.flush();
                pw.close();

                Log.i(TAG, "MFCC Memory & CPU data appended : " + dataToAppend);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}