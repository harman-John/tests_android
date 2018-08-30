package com.harman.bluetooth.utils;

import android.util.Log;

import com.harman.bluetooth.BuildConfig;

public class Logger {

    public static void v(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.v(TAG, message);
    }

    public static void d(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }

    public static void i(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }
    public static void w(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.w(TAG, message);
    }
    public static void e(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message);
    }
}
