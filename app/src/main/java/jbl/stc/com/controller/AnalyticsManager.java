package jbl.stc.com.controller;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.LogUtil;

/**
 * AnalyticsManager
 * Created by darren.lu on 2017/11/27.
 */
public class AnalyticsManager {
    private String TAG = AnalyticsManager.class.getSimpleName();
    private static WeakReference<AnalyticsManager> mInstance;
    private static String TEST_PROPERTY_ID = "UA-75438418-7"; //test
    private static String PROPERTY_ID = "UA-75438418-9"; //product
    public HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
    private Context mContext;

    public enum TrackerName {
        APP_TRACKER, GLOBAL_TRACKER
    }

    private AnalyticsManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static AnalyticsManager getInstance(Context context) {
        if (mInstance == null || mInstance.get() == null) {
            mInstance = new WeakReference<>(new AnalyticsManager(context));
        }
        return mInstance.get();
    }

    public synchronized Tracker getTracker(TrackerName appTracker) {
        LogUtil.i(TAG, " getTracker");
        if (!mTrackers.containsKey(appTracker)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            Tracker tracker;
            if (AppUtils.IS_DEBUG) {
                tracker = analytics.newTracker(TEST_PROPERTY_ID);
            } else {
                tracker = analytics.newTracker(PROPERTY_ID);
            }
            GoogleAnalytics.getInstance(mContext).setDryRun(false);
            mTrackers.put(appTracker, tracker);
        }
        return mTrackers.get(appTracker);
    }

    private void createEvent(String category, String action, String label, Object value) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        String dimensionValue = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        if (value != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setLabel(label)
                    .setCustomDimension(1, dimensionValue)
                    .setAction(action)
                    .setValue(Integer.valueOf(value.toString()))
                    .build()
            );
        } else {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setLabel(label)
                    .setCustomDimension(1, dimensionValue)
                    .setAction(action)
                    .build()
            );
        }
    }

    private void createEvent(String category, String action, String label) {
        createEvent(category, action, label, null);
    }

    public static final String SCREEN_WALK_THROUGH = "WalkThrough";
    public static final String SCREEN_DASHBOARD = "DashBoard";
    public static final String SCREEN_CONNECT = "Connect";
    public static final String SCREEN_EQ_START = "EQ Start";
    public static final String SCREEN_EQ_Edit = "EQ Edit";
    public static final String SCREEN_EQ_MANAGER = "EQ Manager";
    public static final String SCREEN_EQ_SETTING = "EQ Setting";
    public static final String SCREEN_SETTINGS = "Menu";
    public static final String SCREEN_UPGRADE = "Upgrade";
    public static final String SCREEN_EULA = "EULA";
    public static final String SCREEN_PRIVACY_POLICY = "Privacy Policy";
    public static final String SCREEN_OPEN_SOURCE = "Open Source";
    public static final String SCREEN_LAUNCH = "Launch";
    public static final String SCREEN_TIPS = "Tips";

    public void setScreenName(String screenName) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void reportNewEQ(String eqName) {
        this.createEvent("User Action", "New EQ", eqName);
    }

    public void reportModifyEQ(String eqName) {
        this.createEvent("User Action", "Modify EQ", eqName);
    }

    public void reportSelectedNewEQ(String eqName) {
        this.createEvent("User Action", "Select EQ", eqName);
    }

    public void reportAutoOffToggle(boolean isOn) {
        this.createEvent("User Action", "Auto Off", isOn ? "On" : "Off");
    }

    public void reportSmartButtonChange(String smartButtonTitle) {
        this.createEvent("User Action", "Smart Button Select", smartButtonTitle);
    }

    public void reportDeviceConnect(String name) {
        this.createEvent("Device", "Device Connect", name);
    }

    public void reportDeviceDisconnect(String name) {
        this.createEvent("Device", "Device Disconnect", name != null ? name : "UNKNOWN");
    }

    public void reportFirmwareVersion(String version) {
        this.createEvent("Device", "Firmware Version", version != null ? version : "UNKNOWN");
    }

    public void reportFirmwareUpdateAvailable(String version) {
        this.createEvent("Firmware Update", "Firmware Update Available", version != null ? version : "UNKNOWN");
    }

    public void reportFirmwareUpToDate(String version) {
        this.createEvent("Firmware Update", "Firmware Up To Date", version != null ? version : "UNKNOWN");
    }

    public void reportFirmwareUpdateStarted(String version) {
        this.createEvent("Firmware Update", "Firmware Update Begun", version != null ? version : "UNKNOWN");
    }

    public void reportFirmwareUpdateComplete(String version) {
        this.createEvent("Firmware Update", "Firmware Update Finished", version != null ? version : "UNKNOWN");
    }

    public void reportFirmwareUpdateFailed(String reason) {
        this.createEvent("Firmware Update", "Firmware Update Failed", reason != null ? reason : "reason unknown");
    }

}
