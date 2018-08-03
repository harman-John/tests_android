package jbl.stc.com.activity;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.avnera.audiomanager.audioManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.atomic.AtomicBoolean;

import io.fabric.sdk.android.Fabric;
import jbl.stc.com.BuildConfig;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.entity.DeviceInfo;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.DatabaseHelper;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;


public class JBLApplication extends Application {
    public DeviceInfo deviceInfo;
    public audioManager mAudioManager;
    public boolean isUpgradeFragment = false;
    public boolean isSmartAmbientFragment = false;
    public boolean isAddEqFragment = false;
    public boolean isUpgradeSuccessful = false;
    public boolean isUpgrading = false;
    public AtomicBoolean mDeviceConnected = new AtomicBoolean(false);
    private static Context context;

    public static Context getJBLApplicationContext() {
        return context;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if(BuildConfig.DEBUG){
            // turn using test url since debug type
            PreferenceUtils.setBoolean(PreferenceKeys.OTA_TEST_URL, true, this);
            PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_TEST_URL, true, this);
            PreferenceUtils.setBoolean(PreferenceKeys.ANALYTICS_TEST_URL, true, this);
        }else{
            PreferenceUtils.setBoolean(PreferenceKeys.OTA_TEST_URL, false, this);
            PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_TEST_URL, false, this);
            PreferenceUtils.setBoolean(PreferenceKeys.ANALYTICS_TEST_URL, false, this);
        }
        LegalApi.INSTANCE.eulaInit(this);
        deviceInfo = new DeviceInfo();
        DeviceFeatureMap.init(this);
        if (AppUtils.IS_DEBUG) {
            if (BuildConfig.DEBUG) {
                //LeakCanary.install(this);
            }
        }
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        //AnalyticsManager.getInstance(this).initTracker(getDefaultTracker());
        initDbHelper();
    }

    private void initDbHelper() {
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(getApplicationContext()).getReadableDatabase();
        } catch (Exception e) {
            Logger.e("AnalyticsApplication", e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
//    synchronized public Tracker getDefaultTracker() {
//        if (mTracker == null) {
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//            mTracker = analytics.newTracker(R.xml.global_tracker);
//        }
//        return mTracker;
//    }
}
