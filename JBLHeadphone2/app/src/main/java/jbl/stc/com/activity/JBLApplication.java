package jbl.stc.com.activity;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import jbl.stc.com.BuildConfig;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.entity.GlobalEqInfo;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.DatabaseHelper;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.swipe.activity.ActivityLifecycleMgr;


public class JBLApplication extends Application {
    public GlobalEqInfo globalEqInfo;
    public boolean isAddEqFragment = false;
    private static Context context;

    public static Context getJBLApplicationContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycleMgr.getInstance().init(this);
        context = this;
        if(BuildConfig.DEBUG){
            PreferenceUtils.setBoolean(PreferenceKeys.OTA_TEST_URL, true, this);
            PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_TEST_URL, true, this);
            PreferenceUtils.setBoolean(PreferenceKeys.ANALYTICS_TEST_URL, true, this);
        }else{
            PreferenceUtils.setBoolean(PreferenceKeys.OTA_TEST_URL, false, this);
            PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_TEST_URL, false, this);
            PreferenceUtils.setBoolean(PreferenceKeys.ANALYTICS_TEST_URL, false, this);
        }
        LegalApi.INSTANCE.eulaInit(this);
        globalEqInfo = new GlobalEqInfo();
        DeviceFeatureMap.init(this);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
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
}
