package jbl.stc.com.manager;


import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;

public class AnalyticsManager {
    private static AnalyticsManager mInstance;
    private static String TEST_PROPERTY_ID = "UA-75438418-1"; //test
//    private static String TEST_PROPERTY_ID_1 = "UA-104536127-2"; //test
    private static String PROPERTY_ID = "UA-75438418-8"; //product
    private final static String TAG = AnalyticsManager.class.getSimpleName();
    public HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
    public enum TrackerName {
        APP_TRACKER, GLOBAL_TRACKER
    }

    public static AnalyticsManager getInstance(){
        if (mInstance == null);{
            mInstance = new AnalyticsManager();
        }
        return mInstance;
    }

    public synchronized Tracker getTracker(TrackerName appTracker) {
        if (!mTrackers.containsKey(appTracker)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(JBLApplication.getJBLApplicationContext());
            Tracker t;
            if(PreferenceUtils.getBoolean(PreferenceKeys.ANALYTICS_TEST_URL, JBLApplication.getJBLApplicationContext())) {
                t = analytics.newTracker(TEST_PROPERTY_ID);
            }else{
                t = analytics.newTracker(PROPERTY_ID);
            }
            GoogleAnalytics.getInstance(JBLApplication.getJBLApplicationContext()).setDryRun(false);
            mTrackers.put(appTracker, t);
        }
        return mTrackers.get(appTracker);
    }

    private void createEvent(String category, String action, String label, Object value) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        String deviceName =  AppUtils.getJBLDeviceName(JBLApplication.getJBLApplicationContext());
        if (value != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setLabel(label)
                    .setCustomDimension(1,deviceName)
                    .setAction(action)
                    .setValue(Integer.valueOf(value.toString()))
                    .build()
            );
        }else{
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setLabel(label)
                    .setCustomDimension(1,deviceName)
                    .setAction(action)
                    .build()
            );
        }
    }


    public static final String SCREEN_INTRO = "Intro";
    public static final String SCREEN_DEVICE_SEARCH = "Device Search";
    public static final String SCREEN_LAUNCH = "Launch Screen";
    public static final String SCREEN_DEVICE_CONNECTED = "Device Connected";
    public static final String SCREEN_TRUNOTE = "TruNote";
    public static final String SCREEN_CONTROL_PANEL = "Control Panel";
    public static final String SCREEN_CUSTOM_EQ = "Custom EQ";
    public static final String SCREEN_EQ_PRESETS_TABLE = "EQ Presets Table";
    public static final String SCREEN_PROGRAMMABLE_SMART_BUTTON = "Programmable Smart Button";
    public static final String SCREEN_SETTINGS_PANEL = "Settings Panel";
    public static final String SCREEN_UPDATE_DEVICE = "Update Device";
    public static final String SCREEN_EULA = "EULA";
    public static final String SCREEN_OPEN_SOURCE = "Open Source";
    public static final String SCREEN_EQ_MORE = "EQ more";
    public void setScreenName(String screenName) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void reportNewEQ(String eqName) {
        createEvent("User Action",  "New EQ",  eqName, null);
    }

    public void reportModifyEQ(String eqName) {
        createEvent("User Action",  "Modify EQ",  eqName, null);
    }

    public void reportSelectedNewEQ(String eqName) {
        createEvent("User Action",  "Select EQ",  eqName, null);
    }

    public void reportANCToggle(boolean isOn) {
        createEvent("User Action",  "REQ_ANC",  isOn ? "On" : "Off", null);
    }

    public void reportAwarenessLevelChanged(int value, boolean isLeft) {
        createEvent("User Action",  "Awareness Level",  isLeft ? "Left" : "Right ", value);
    }

    public void reportAwarenessLevelOff() {
        createEvent("User Action",  "Awareness Level",  "Off", null);
    }

    public void reportAwarenessLevelHigh() {
        createEvent("User Action",  "Awareness Level",  "High", null);
    }

    public void reportAwarenessLevelMedium() {
        createEvent("User Action",  "Awareness Level",  "Medium", null);
    }

    public void reportAwarenessLevelLow() {
        createEvent("User Action",  "Awareness Level",  "Low", null);
    }

    public void reportAutoCalStart() {
        createEvent("User Action",  "TruNote",  "Start", null);
    }

    public void reportAutoCalFail() {
        createEvent("User Action",  "TruNote",  "Fail", null);
    }

    public void reportAutoCalNoHeadsetFail() {
        createEvent("User Action",  "TruNote",  "Headset Not Worn", null);
    }

    public void reportAutoCalSuccess() {
        createEvent("User Action",  "TruNote",  "Success", null);
    }

    public void reportDidSkipCoachMarks() {
        createEvent("User Action",  "CoachMarks",  "Skipped", null);
    }

    public void reportDidCompleteCoachMarks() {
        createEvent("User Action",  "CoachMarks",  "Complete", null);
    }

    public void reportAutoOffToggle(boolean isOn) {
        createEvent("User Action",  "Auto Off",  isOn ? "On" : "Off", null);
    }

    public void reportVoicePromptToggle(boolean isOn) {
        createEvent("User Action",  "Voice Prompt",  isOn ? "On" : "Off", null);
    }

    public void reportSmartButtonChange(String smartButtonTitle) {
        createEvent("User Action",  "Smart Button Select",  smartButtonTitle, null);
    }

    public void reportDeviceConnect(String name) {
        createEvent("Device",  "Device Connect",  name, null);
    }

    public void reportDeviceDisconnect(String name) {
        createEvent("Device",  "Device Disconnect",  name != null ? name : "UNKNOWN", null);
    }

    public void reportFirmwareVersion(String version) {
        createEvent("Device",  "Firmware Version",  version != null ? version : "UNKNOWN", null);
    }

    public void reportFirmwareUpdateAvailable(String version) {
        createEvent("Firmware Update",  "Firmware Update Available",  version != null ? version : "UNKNOWN", null);
    }

    public void reportFirmwareUpToDate(String version) {
        createEvent("Firmware Update",  "Firmware Up To Date",  version != null ? version : "UNKNOWN", null);
    }

    public void reportFirmwareUpdateStarted(String version) {
        createEvent("Firmware Update",  "Firmware Update Begun",  version != null ? version : "UNKNOWN", null);
    }

    public void reportFirmwareUpdateComplete(String version) {
        createEvent("Firmware Update",  "Firmware Update Finished",  version != null ? version : "UNKNOWN", null);
    }

    public void reportFirmwareUpdateFailed(String reason) {
        createEvent("Firmware Update",  "Firmware Update Failed",  reason != null ? reason : "reason unknown", null);
    }

    public void reportUsbUpdateAlert(String reason) {
        createEvent("Firmware Update",  "Alerted user of USB update.",  reason != null ? reason : "reason unknown", null);
    }
}
