package jbl.stc.com.activity;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.avnera.audiomanager.audioManager;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.atomic.AtomicBoolean;

import jbl.stc.com.BuildConfig;
import jbl.stc.com.entity.DeviceInfo;
import jbl.stc.com.storage.DatabaseHelper;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.DebugHelper;
import jbl.stc.com.utils.LogUtil;

/**
 * /**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * {@link Tracker}.
 * Created by darren.lu on 2017/11/27.
 */

public class AnalyticsApplication extends Application {
    public DeviceInfo deviceInfo;
    public audioManager mAudioManager;
    public boolean isUpgradeFragment = false;
    public boolean isSmartAmbientFragment = false;
    public boolean isAddEqFragment = false;
    public boolean isUpgradeSuccessful = false;
    public boolean isUpgrading = false;
    public AtomicBoolean mDeviceConnected = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        deviceInfo = new DeviceInfo();
        if (AppUtils.IS_DEBUG) {
            new AkgCrashHandler().init(this);
            DebugHelper.init();
            if (BuildConfig.DEBUG) {
                //LeakCanary.install(this);
            }
        }
        if (!BuildConfig.DEBUG) {
            //Fabric.with(this, new Crashlytics());
        }
        //AnalyticsManager.getInstance(this).initTracker(getDefaultTracker());
        initDbHelper();
    }

    private void initDbHelper() {
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(getApplicationContext()).getReadableDatabase();
        } catch (Exception e) {
            LogUtil.e("AnalyticsApplication", e.getMessage(), e);
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
