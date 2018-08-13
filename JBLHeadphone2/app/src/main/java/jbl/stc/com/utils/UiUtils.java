package jbl.stc.com.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import jbl.stc.com.logger.Logger;

/**
 * UiUtils
 * Created by darren.lu on 08/06/2017.
 */
public class UiUtils {

    public static float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        return paint.measureText(text);
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = getScreenDisplayMetrics(context);

        int[] result = new int[2];
        result[0] = dm.widthPixels;
        result[1] = dm.heightPixels;
        return result;
    }

    public static DisplayMetrics getScreenDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        return dm;
    }

    public static void showSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view == null ? null : view.getWindowToken(), 0);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * getStatusHeight
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass
                        .getField("status_bar_height").get(localObject)
                        .toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    public static int getDashboardDeviceImageHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenheight = dm.heightPixels;
        int deviceHeight = 0;
        int statusHeight = getStatusHeight(context);
        deviceHeight = (int) (screenheight - UiUtils.dip2px(context, 200) - statusHeight) / 2;
        if (deviceHeight > UiUtils.dip2px(context, 240)) {
            deviceHeight = UiUtils.dip2px(context, 240);
        }
        return deviceHeight;

    }

    public static int getDeviceNameMarginTop(Context context) {
        int marginTop = 0;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenheight = dm.heightPixels;
        int dashboardDeviceNameHeight = dip2px(context, 25);
        int dashboardDeviceImageHeight = getDashboardDeviceImageHeight(context);
        int dashboardDeviceImage_marginTop = dip2px(context, 10);
        int dashboaddDeviceBaterryHeight = dip2px(context, 25);
        int dashboardDeviceBattery_marginTop = dip2px(context, 10);
        int dashboardNoiseCancleHeight = dip2px(context, 128);
        int dashboardNoiseCancle_marginTop = dip2px(context, 20);
        int deviceInfoHeight = dashboardDeviceNameHeight + dashboardDeviceImageHeight + dashboardDeviceImage_marginTop
                + dashboaddDeviceBaterryHeight + dashboardDeviceBattery_marginTop + dashboardNoiseCancleHeight
                + dashboardNoiseCancle_marginTop;
        int dashboardTitleBarHeight = dip2px(context, 62);
        int dashboardBottomEq = dip2px(context, 70);
        marginTop = (screenheight - getStatusHeight(context) - dashboardTitleBarHeight - dashboardBottomEq - deviceInfoHeight) / 2;
        return marginTop;
    }
}

