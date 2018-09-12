package jbl.stc.com.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import jbl.stc.com.BuildConfig;
import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.A2dpObserver;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;

public class AppUtils {
    private static final String TAG = AppUtils.class.getSimpleName();
    public static boolean IS_DEBUG = BuildConfig.DEBUG ? true : false;
    public static boolean mTutorial = true;//是否显示Tutorial界面

    public static final String IsNeedToRefreshCommandRead = "IsNeedToRefreshCommandRead";
    public static final String APP_PATH = Environment
            .getExternalStorageDirectory().toString() + "/AKG_N700";

    public static final String BASE_DEVICE_NAME = "N700NC";
    public static final String EQ_NAME_AND_NUM_SEPARATE = " ";

    public static boolean mLegalPage = true;//是否显示Legal界面
    public static final int SEND_ORDER_TIME_DURATION = 30;
    public static final long DEVICE_VIEW_ANIMATION_TIME = 1500;
    public static final long AUDIO_MANAGER_INIT_TIME = 3000;
    public static final long GET_BATTERY_LEVEL_INTERVAL_TIME = 3 * 60 * 1000L;
    public static final int EQ_VIEW_DEFAULT_STEP = 20;
    public static final int EQ_BAND_NUMBER = 10;
    private static final String DEVICE_NAME_PREFIX = "JBL Everest";

    public static String getJBLDeviceName(Context context) {
        return PreferenceUtils.getString(JBLConstant.JBL_DEVICE_NAME, context, "");
    }

    public static boolean isMatchDeviceName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        return name.toUpperCase().contains(DEVICE_NAME_PREFIX.toUpperCase());
    }

    public static void hideFromForeground(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        String version = "";
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    public static int getVersionCode(Context context) {
        int verCode = -1;
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            verCode = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return verCode;
    }

    /**
     * <p>Read data from Input stream</p>
     *
     * @param inputStream
     * @return
     */
    public static byte[] readInputStream(InputStream inputStream) {
        byte[] buffer;
        int read;
        ByteArrayOutputStream outputStream;

        buffer = new byte[4096];
        outputStream = new ByteArrayOutputStream();
        try {
            for (; ; ) {
                if ((read = inputStream.read(buffer)) < 0) break;

                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * <p>Checks whether firmware update is available by comparing the liveVersion with currentVersion</p>
     *
     * @param liveVersion
     * @param currentVersion
     * @return True is update is available else FALSE
     */
    public static boolean isUpdateAvailable(String liveVersion, String currentVersion) {
        String[] liveArray = new String[3];
        String[] currentArray = new String[3];
        int counter = 0;
        StringTokenizer st = new StringTokenizer(liveVersion, ".");

        while (st.hasMoreTokens()) {
            String x = st.nextToken();
            liveArray[counter++] = x;

        }
        counter = 0;
        st = new StringTokenizer(currentVersion, ".");
        while (st.hasMoreTokens()) {
            String x = st.nextToken();
            currentArray[counter++] = x;

        }
        try {
            if (Integer.parseInt(liveArray[0]) > Integer
                    .parseInt(currentArray[0])) {
                return true;
            } else if (Integer.parseInt(liveArray[0]) == Integer
                    .parseInt(currentArray[0])) {
                // Checking for second index
                if (Integer.parseInt(liveArray[1]) > Integer
                        .parseInt(currentArray[1])) {
                    return true;
                } else if (Integer.parseInt(liveArray[1]) == Integer.parseInt(currentArray[1])) {
                    // Checking for third Index
                    if (Integer.parseInt(liveArray[2]) > Integer
                            .parseInt(currentArray[2])) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public static String getN70NCDownloadUrl() {
        if (BuildConfig.DEBUG) {
            return "http://storage.harman.com/Testing/AKGN70NC/AKGN70NC_Upgrade_Test_Index.xml";
        } else {
            return "http://storage.harman.com/AKGN70NC/AKGN70NC_Upgrade_Index.xml";
        }
    }

    public static String getN200NCDownloadUrl() {
        if (IS_DEBUG) {
            return "";
        } else {
            return "";
        }
    }


    public static void releaseImageViewResource(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

//    public static void tryRecycleAnimationDrawable(AnimationDrawable animationDrawable) {
//        if (animationDrawable != null) {
//            animationDrawable.stop();
//            for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
//                Drawable frame = animationDrawable.getFrame(i);
//                if (frame instanceof BitmapDrawable) {
//                    ((BitmapDrawable) frame).getBitmap().recycle();
//                }
//                frame.setCallback(null);
//            }
//            Logger.d(TAG, "tryRecycleAnimationDrawable()");
//            animationDrawable.setCallback(null);
//        }
//    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int inSampleSize) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * This function called must after HomeFragment showed.
     * Because not prepare to set Mode is in HomeFragment.
     * So before HomeFragment showed must not use this function.
     *
     * @param context
     * @return
     */
    public static boolean is150NC(Context context) {
        return getModelNumber(context).contains("150");
    }

    public static String getModelNumber(Context context) {
        String modelNumber = PreferenceUtils.getString(PreferenceKeys.MODEL, context, "");
        if (TextUtils.isEmpty(modelNumber)) {
            modelNumber = getJBLDeviceName(context);
        }
        return modelNumber;
    }

    public static void setJBLDeviceName(Context context, String value) {
        PreferenceUtils.setString(PreferenceKeys.JBL_DEVICE_NAME, value, context);
    }

    public static boolean is750Device(Context context) {
        return getModelNumber(context).contains("750");
    }

    public static boolean isOldDevice(String deviceName) {
        boolean isOldDevice = false;
        if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_REFLECT_AWARE).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())) {
            isOldDevice = true;
        }
        return isOldDevice;
    }

    public static boolean isNewDevice(String deviceName) {
        boolean isNewDevice = false;
        if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_LIVE_500BT).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_LIVE_400BT).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_LIVE_650BTNC).toUpperCase()) ||
                deviceName.toUpperCase().contains((JBLConstant.DEVICE_LIVE_FREE_GA).toUpperCase())) {
            isNewDevice = true;
        }
        return isNewDevice;
    }

    public static int levelTransfer(int ambLevel150) {
        switch (ambLevel150) {
            case 0: {
                return 0;
            }
            case 2: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 6: {
                return 3;
            }
        }
        return 0;
    }

    public static void setModelNumber(Context context, String value) {
        PreferenceUtils.setString(PreferenceKeys.MODEL, value, context);
    }

    public static MyDevice getMyDevice(String deviceName, int connectStatus, String pid, String mac) {
        MyDevice myDevice = new MyDevice();
        myDevice.deviceKey = deviceName + "-" + mac;
        myDevice.connectStatus = connectStatus;
        myDevice.pid = pid;
        myDevice.mac = mac;

        if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
            myDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_750NC;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
            myDevice.deviceName = JBLConstant.DEVICE_REFLECT_AWARE;
            myDevice.mac = deviceName + "-" + mac;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
            myDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_150NC;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
            myDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_700;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
            myDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_100;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
            myDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_300;
        } else if (pid != null && pid.toUpperCase().equals(JBLConstant.DEVICE_LIVE_FREE_GA_PID)) {
            myDevice.deviceName = JBLConstant.DEVICE_LIVE_FREE_GA;
        } else if (pid != null && pid.toUpperCase().equals(JBLConstant.DEVICE_LIVE_650BTNC_PID)) {
            myDevice.deviceName = JBLConstant.DEVICE_LIVE_650BTNC;
        } else if (pid != null && pid.toUpperCase().equals(JBLConstant.DEVICE_LIVE_400BT_PID)) {
            myDevice.deviceName = JBLConstant.DEVICE_LIVE_400BT;
        } else if (pid != null && pid.toUpperCase().equals(JBLConstant.DEVICE_LIVE_500BT_PID)) {
            myDevice.deviceName = JBLConstant.DEVICE_LIVE_500BT;
        } else {
            return null;
        }
        return myDevice;
    }

    public static Drawable getDeviceIcon(Context context, String deviceName) {
        Drawable drawable = null;
        if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.everest_elite_750nc_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.reflect_aware_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.everest_elite_150nc_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.everest_elite_700_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.everest_elite_100_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.everest_elite_300_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_FREE_GA)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.live_650_btnc_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_650BTNC)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.live_650_btnc_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_400BT)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.live_400_bt_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_500BT)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.live_500_bt_icon);
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_PLUS)) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.big_addition);
        }
        return drawable;
    }

    public static String transferDeviceName(String deviceName) {
        String name = deviceName;
        if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
            name = JBLConstant.DEVICE_EVEREST_ELITE_750NC;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
            name = JBLConstant.DEVICE_REFLECT_AWARE;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
            name = JBLConstant.DEVICE_EVEREST_ELITE_150NC;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
            name = JBLConstant.DEVICE_EVEREST_ELITE_700;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
            name = JBLConstant.DEVICE_EVEREST_ELITE_100;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
            name = JBLConstant.DEVICE_EVEREST_ELITE_300;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_650BTNC)) {
            name = JBLConstant.DEVICE_LIVE_650BTNC;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_400BT)) {
            name = JBLConstant.DEVICE_LIVE_400BT;
        } else if (deviceName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_500BT)) {
            name = JBLConstant.DEVICE_LIVE_500BT;
        }
        return name;
    }

    public static int[] parseVersionFromASCIIbuffer(byte[] bytes) {
        int[] result = new int[3];
        String major = "0", minor = "0", revision = "0";

        int count = 0;
        try {
            for (byte bx : bytes) {
                switch (count) {
                    case 0:
                        if (bx == 46) {
                            result[0] = (Integer.valueOf(major));
                            count++;
                        } else if (bx >= 48 && bx <= 57) {
                            major += Character.getNumericValue(bx);
                        }
                        break;
                    case 1:
                        if (bx == 46) {
                            result[1] = Integer.valueOf(minor);
                            count++;
                        } else if (bx >= 48 && bx <= 57) {
                            minor += Character.getNumericValue(bx);
                        }
                        break;
                    case 2:
                        if (bx == 0) {
                            result[2] = Integer.valueOf(revision);
                            return result;
                        } else if (bx >= 48 && bx <= 57) {
                            revision += Character.getNumericValue(bx);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean is100(Context context) {
        return getModelNumber(context).contains("100");
    }
}

