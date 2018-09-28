package jblcontroller.hcs.soundx.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import jbl.stc.com.R;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.InsertPredefinePreset;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.ui.audioeffect.AudioEffectActivity;
import jblcontroller.hcs.soundx.ui.dashboard.DashboardDemo;
import jblcontroller.hcs.soundx.ui.login.LoginActivity;
import jblcontroller.hcs.soundx.utils.AppConstants;

import static jblcontroller.hcs.soundx.utils.AppConstants.IS_SESSION_EXISTS;


public class SplashScreenActivity extends AppCompatActivity {
    public static final String TAG = SplashScreenActivity.class.getSimpleName();
    private String mUserName;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        mUserName = SoundXSharedPreferences.getUserName(SplashScreenActivity.this);
        mPassword = SoundXSharedPreferences.getPassword(SplashScreenActivity.this);


        if (!PreferenceUtils.getBoolean(AppConstants.IsAllDefaultInserted, this)) {
            InsertPredefinePreset insertdefaultValueTask = new InsertPredefinePreset();
            insertdefaultValueTask.executeOnExecutor(InsertPredefinePreset.THREAD_POOL_EXECUTOR, this);
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handleNextScreenLaunch();
            }
        }, 3000);
    }


    private void handleNextScreenLaunch() {
        if (!SoundXSharedPreferences.isOfflineEnabled(this)) {
            // Check authtoken is available or not
            if (SoundXSharedPreferences.getAuthToken(this) != null && !SoundXSharedPreferences.getAuthToken(this).equals("")) {
                if (SoundXSharedPreferences.isConfiguration(SplashScreenActivity.this))
                    launchDashboardHome();
                else
                    launchAudioEffectActivity();


            } else {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {

            if (SoundXSharedPreferences.isConfiguration(SplashScreenActivity.this))
                launchDashboardHome();
            else
                launchAudioEffectActivity();
        }
    }

    /**
     * This will launch the SignUpActivity
     */
    private void launchDashboardHome() {
        Intent intent = new Intent(getApplicationContext(), DashboardDemo.class);
        intent.putExtra(IS_SESSION_EXISTS, true);
        startActivity(intent);
        finish();
    }

    /**
     * This will launch the SignUpActivity
     */
    private void launchAudioEffectActivity() {
        Intent intent = new Intent(getApplicationContext(), AudioEffectActivity.class);
        startActivity(intent);
        finish();
    }

}
