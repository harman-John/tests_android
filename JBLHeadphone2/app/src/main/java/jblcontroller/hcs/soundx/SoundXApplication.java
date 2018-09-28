package jblcontroller.hcs.soundx;

import android.app.Application;
import android.content.Context;

public class SoundXApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
