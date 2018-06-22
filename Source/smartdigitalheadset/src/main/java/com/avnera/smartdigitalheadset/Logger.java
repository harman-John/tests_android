package com.avnera.smartdigitalheadset;

import android.util.Log;

/**
 * Created by darren.lu on 28/3/2017.
 */
public class Logger {
    public static boolean DEBUG = true;

    /**
     * <p>Logs message with ERROR tag.</p>
     *
     * @param TAG
     * @param message
     */
    public static void e(String TAG, String message) {
        if (DEBUG)
            Log.e(TAG, message);
    }

    /**
     * <p>Logs message with VERBOSE tag</p>
     *
     * @param TAG
     * @param message
     */
    public static void v(String TAG, String message) {
        if (DEBUG)
            Log.e(TAG, message);
    }

    public static void w(String TAG, String message) {
        if (DEBUG)
            Log.w(TAG, message);
    }

    /**
     * <p>Logs message with DEBUG tag.</p>
     *
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (DEBUG)
            Log.d(tag, message);
    }
}
