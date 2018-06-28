package jbl.stc.com.logger;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jbl.stc.com.BuildConfig;

public class Logger {

    public static void v(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.v(TAG, message);
        writeLog(TAG+" "+message);
    }

    public static void d(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
        writeLog(TAG+" "+message);
    }

    public static void i(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message);
        writeLog(TAG+" "+message);
    }
    public static void w(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.w(TAG, message);
    }
    public static void e(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message);
        writeLog(TAG+" "+message);
    }

    public static void writeLog(String log) {
        File file = Environment.getExternalStorageDirectory();
        File logger = new File(file, "jblheadphones_logs.txt");
        try {
            FileWriter fileWriter = new FileWriter(logger, true);
            fileWriter.write("\n" + log);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
