package jbl.stc.com.utils;

import android.util.Log;

/**
 * LogUtil
 * Created by darren.lu on 08/06/2017.
 */
public class LogUtil {

    public static void e(String tag, String msg) {
        if (AppUtils.IS_DEBUG) {
            Log.e(tag, msg);
            DebugHelper.writeDebugContent(tag + ":" + msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (AppUtils.IS_DEBUG) {
            Log.e(tag, msg, tr);
            DebugHelper.writeDebugContent(tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (AppUtils.IS_DEBUG) {
            Log.i(tag, msg);
            DebugHelper.writeDebugContent(tag + ":" + msg);
        }
    }

    public static void v(String tag, String msg) {
        if (AppUtils.IS_DEBUG) {
            Log.v(tag, msg);
            DebugHelper.writeDebugContent(tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (AppUtils.IS_DEBUG) {
            Log.d(tag, msg);
            DebugHelper.writeDebugContent(tag + ":" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (AppUtils.IS_DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void o(String tag, String msg) {
        if (tag != null) {
            i(tag, msg);
        } else {
            i("out", msg);
        }
    }

    public static void o(String msg) {
        i("out", msg);
    }

    public static void o(Object o) {
        if (o != null) {
            i("out", o.toString());
        } else {
            i("out", "null");
        }
    }

}