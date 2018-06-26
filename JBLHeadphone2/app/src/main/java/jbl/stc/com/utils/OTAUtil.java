package jbl.stc.com.utils;

import android.content.Context;

import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;


public class OTAUtil {

    private final static String OTA_URL_HEAD = "http://storage.harman.com/";

    private final static String OTA_TEST_URL_150NC = OTA_URL_HEAD + "Testing/JBL150NC/Elite150NC_Upgrade_Test_Index.xml";
    private final static String OTA_TEST_URL_750NC = OTA_URL_HEAD + "Testing/Everest/Android/Elite750NC_Upgrade_Test_Index.xml";
    private final static String OTA_TEST_URL_700 = OTA_URL_HEAD + "Testing/Everest/Android/V700NXT_Upgrade_Test_Index.xml";
    private final static String OTA_TEST_URL_300 = OTA_URL_HEAD + "Testing/Everest/Android/V300NXT_Upgrade_Test_Index.xml";
    private final static String OTA_TEST_URL_100 = OTA_URL_HEAD + "Testing/Everest/Android/V100NXT_Upgrade_Test_Index.xml";
    private final static String OTA_TEST_URL_AWARE = OTA_URL_HEAD + "Testing/Aware/Android/Aware_Android_Upgrade_Test_Index.xml";

    private final static String OTA_RELEASE_URL_150NC = OTA_URL_HEAD + "JBL150NC/Elite150NC_Upgrade_Index.xml";
    private final static String OTA_RELEASE_URL_750NC = OTA_URL_HEAD + "Everest/Android/Elite750NC_Upgrade_Index.xml";
    private final static String OTA_RELEASE_URL_700 = OTA_URL_HEAD + "Everest/Android/V700NXT_Upgrade_Index.xml";
    private final static String OTA_RELEASE_URL_300 = OTA_URL_HEAD + "Everest/Android/V300NXT_Upgrade_Index.xml";
    private final static String OTA_RELEASE_URL_100 = OTA_URL_HEAD + "Everest/Android/V100NXT_Upgrade_Index.xml";
    private final static String OTA_RELEASE_URL_AWARE = OTA_URL_HEAD + "Aware/Android/Aware_Android_Upgrade_Index.xml";

    /**
     * Decide which Resource to be downloaded.
     *
     * @return URL
     */
    public static String getURL(Context context) {
        String url = null;
        if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice) {
            url = getAwareUrl(context);
        }
        String modelNumber = AppUtils.getModelNumber(context);
        if (modelNumber.contains("100") || modelNumber.contains("EVB")) {
            url = get100IEUrl(context);
        } else if (modelNumber.contains("300")) {
            url = get300OEUrl(context);
        } else if (modelNumber.contains("700")) {
            url = get700AEUrl(context);
        } else if (modelNumber.contains("750")) {
            url = get750NCUrl(context);
        } else if (modelNumber.contains("150")) {
            url = get150NCUrl(context);
        } else {
            url = getAwareUrl(context);
        }
        return url;
    }

    /**
     * It will return this URL if headphone is IE.
     *
     * @return String
     */
    private static String get100IEUrl(Context context) {
        //if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_100;
        else
            return OTA_RELEASE_URL_100;
    }

    /**
     * * It will return this URL if headphone is OE.
     *
     * @return String
     */

    private static String get300OEUrl(Context context) {
        //        if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_300;
        else
            return OTA_RELEASE_URL_300;
    }

    /**
     * * It will return this URL if headphone is AE.
     *
     * @return String
     */
    private static String get700AEUrl(Context context) {
//        if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_700;
        else
            return OTA_RELEASE_URL_700;
    }

    /**
     * * It will return this URL if headphone is NC.
     *
     * @return String
     */
    private static String get750NCUrl(Context context) {
//        if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_750NC;
        else
            return OTA_RELEASE_URL_750NC;
    }

    /**
     * * It will return this URL if headphone is 150NC.
     *
     * @return String
     */
    private static String get150NCUrl(Context context) {
//        if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_150NC;
        else
            return OTA_RELEASE_URL_150NC;
    }

    /**
     * * It will return this URL if headphone is AWARE.
     *
     * @return String
     */
    private static String getAwareUrl(Context context) {
//        if (BuildConfig.isBuildwithTestURL)
        if (PreferenceUtils.getBoolean(PreferenceKeys.OTA_TEST_URL, context))
            return OTA_TEST_URL_AWARE;
        else
            return OTA_RELEASE_URL_AWARE;
    }
}
