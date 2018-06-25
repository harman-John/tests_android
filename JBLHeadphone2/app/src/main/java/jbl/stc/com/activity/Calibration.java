package jbl.stc.com.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import jbl.stc.com.R;
import jbl.stc.com.fragment.GlobalCalibration;
import jbl.stc.com.listener.OnHeadphoneconnectListener;
import jbl.stc.com.manager.CalibrationManager;


public class Calibration extends AppCompatActivity implements OnHeadphoneconnectListener {
    GlobalCalibration caligrabtionFragment;
    public static String TAG = Calibration.class.getSimpleName();
    private static Calibration calibration = null;
    CalibrationManager calibrationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.calibration);
        calibrationManager = CalibrationManager.getCalibrationManager(this);
        overridePendingTransition(R.anim.enter_from_right, R.anim.nothing);
        setCalibration(this);
        findViewById(R.id.image_view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibration = null;
                dummyStopCalibration();
            }
        });

        if (getIntent().getExtras().getString(Calibration.TAG).equalsIgnoreCase(GlobalCalibration.class.getSimpleName())) {
            caligrabtionFragment = new GlobalCalibration();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, caligrabtionFragment).commit();
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isMusicActive()) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
            abortMediaBroadCast = new AbortMediaBroadCast();
            this.registerReceiver(abortMediaBroadCast, intentFilter);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nothing, R.anim.exit_to_right);
    }

    @Override
    public void onBackPressed() {

        if (isCalibrationStarted)
            return;
        super.onBackPressed();

    }

    @Override
    public void onHeadPhoneState(boolean isConnect, String headphoneName) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        calibration = null;
        if (abortMediaBroadCast != null) {
            unregisterReceiver(abortMediaBroadCast);
        }
    }

    public static Calibration getCalibration() {
        return calibration;
    }

    public static void setCalibration(Calibration calibration) {
        Calibration.calibration = calibration;
    }

    /*  <p> Start calibration will start calibrating </p>    *
    * */

    public void startCalibration() {
        /*if (Calibration.getCalibration() != null) {
            retryWaitforCalibration = 0;
            isCalibrationStarted = true;
            isCalibrationComplete = false;
            calibrationManager.setLightX(AvneraManager.getAvenraManager(this).getLightX());
            calibrationManager.startCalibration();
            findViewById(R.id.txtCancel).setVisibility(View.INVISIBLE);
            handler.postDelayed(runnable, 5000);
        }*/
    }

    /*
    * <p>Stops the calibration process.</p>
    * */
    public void dummyStopCalibration() {
        handler.removeCallbacks(runnable);
        finish();
    }

    private boolean isCalibrationComplete, isCalibrationStarted;
    private int retryWaitforCalibration = 0;

    Handler handler = new Handler();

    /*
    * <p>Waits for calibration to complete within 10 second otherwise calibration will fail.</p>
    * */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isCalibrationComplete()) {
                handler.removeCallbacks(runnable);
                caligrabtionFragment.calibrationComplete();
            } else if (retryWaitforCalibration <= 1) {
                //** Wait another few second until calibration complete **//
                ++retryWaitforCalibration;
                calibrationManager.getCalibrationStatus();
                handler.postDelayed(runnable, 5000);
            } else {
                // Calibration failed
                calibrationFailed();
            }
        }
    };

    /**
     * Notify in case of failed calibration
     */
    public void calibrationFailed() {
        if (Calibration.getCalibration() != null) {
            isCalibrationStarted = false;
            caligrabtionFragment.calibrationFailed();
        }
    }

    /*
    *  Checks for calibration status.
    * */
    public boolean isCalibrationComplete() {
        return isCalibrationComplete;
    }

    /*
    *  <p> Present the status of Calibration to user. </p>
     *  If success show calibration complete else shoe calibration failed
    *  @param  isCalibrationComplete  contains the status of calibration.
    * */
    public void setIsCalibrationComplete(boolean isCalibrationComplete) {
        this.isCalibrationComplete = isCalibrationComplete;
        handler.removeCallbacks(runnable);
        if (isCalibrationComplete) {
            caligrabtionFragment.calibrationComplete();
        } else {
            caligrabtionFragment.calibrationFailed();
        }
    }

    AbortMediaBroadCast abortMediaBroadCast;

    private class AbortMediaBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
        }
    }
}
