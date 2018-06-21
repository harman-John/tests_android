package jbl.stc.com.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * ToastUtil
 * <p>
 * Created by darren.lu on 08/06/2017.
 */
public class ToastUtil {

    public static Toast gToast = null;

    public static void ToastShort(Context context, String msg) {
        cleanToast();
        gToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        gToast.show();
    }

    public static void ToastShort(Context context, int msgTxtId) {
        if (null != context) {
            ToastShort(context, context.getResources().getString(msgTxtId));
        }
    }

    public static void ToastLong(Context context, String msg) {
        cleanToast();
        gToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        gToast.show();
    }

    public static void ToastLong(Context context, int msgTxtId) {
        if (null != context) {
            ToastLong(context, context.getResources().getString(msgTxtId));
        }
    }

    public static void cleanToast() {
        if (null != gToast) {
            gToast.cancel();
            gToast = null;
        }
    }
}
