package jbl.stc.com.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.listener.OnHeadphoneconnectListener;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.CalibrationManager;


public class CalibrationFragment extends BaseFragment implements OnHeadphoneconnectListener,View.OnClickListener {
    public static String TAG = CalibrationFragment.class.getSimpleName();
    private static CalibrationFragment calibration = null;
    CalibrationManager calibrationManager;
    private View view;
    TextView txtConnectMessage, txthelp, txtChangeMessage, txtCalibrating, txtExtraHelp;
    ProgressBar progressBar;
    int timing = 10 * 1000;
    private View informationLayout;
    private ImageView imageViewBack;

    private boolean isCalibrationComplete;
    private int retryWaitForCalibration = 0;

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calibration,
                container, false);
        calibrationManager = CalibrationManager.getCalibrationManager(getActivity());
        calibration = this;

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (!audioManager.isMusicActive()) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
            abortMediaBroadCast = new AbortMediaBroadCast();
            getActivity().registerReceiver(abortMediaBroadCast, intentFilter);
        }
        txtConnectMessage = (TextView) view.findViewById(R.id.txtConnectMessage);
        //txtConnectMessage.setText(Html.fromHtml(getResources().getString(R.string.personalizemessgae)));
        txtConnectMessage.setText(getResources().getString(R.string.personalizemessgae));
        imageViewBack = view.findViewById(R.id.image_view_calibration_global_back);
        imageViewBack.setOnClickListener(this);
        txthelp = (TextView) view.findViewById(R.id.txthelp);
        txthelp.setText(getResources().getString(R.string.help));
        txtChangeMessage = (TextView) view.findViewById(R.id.txtChangeMessage);
        txtCalibrating = (TextView) view.findViewById(R.id.txtCalibrating);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtExtraHelp = (TextView) view.findViewById(R.id.txtExtraHelp);
        informationLayout = view.findViewById(R.id.informationLayout);
        informationLayout.setOnClickListener(this);
        return view;
    }


    @Override
    public void onHeadPhoneState(boolean isConnect, String headphoneName) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.informationLayout: {
                startCalibration();
                informationLayout.setOnClickListener(null);
                break;
            }
            case R.id.image_view_calibration_global_back:{
                calibration = null;
                dummyStopCalibration();
                getActivity().onBackPressed();
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        calibration = null;
        if (abortMediaBroadCast != null) {
            getActivity().unregisterReceiver(abortMediaBroadCast);
        }
    }

    public static CalibrationFragment getCalibration() {
        return calibration;
    }

    public void startCalibration() {
        txtCalibrating.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txtChangeMessage.setText("");

        retryWaitForCalibration = 0;
        isCalibrationComplete = false;
        calibrationManager.setLightX(AvneraManager.getAvenraManager(getActivity()).getLightX());
        calibrationManager.startCalibration();
        handler.postDelayed(runnable, 5000);
        /*if (Calibration.getCalibration() != null) {
            retryWaitForCalibration = 0;
            isCalibrationStarted = true;
            isCalibrationComplete = false;
            calibrationManager.setLightX(AvneraManager.getAvenraManager(this).getLightX());
            findViewById(R.id.txtCancel).setVisibility(View.INVISIBLE);
            handler.postDelayed(runnable, 5000);
        }*/
    }

    /*
    * <p>Stops the calibration process.</p>
    * */
    public void dummyStopCalibration() {
        handler.removeCallbacks(runnable);
        getActivity().onBackPressed();
    }

    /**
     * Calibration start screen will be visible.
     */
    public void calibrationComplete() {
        imageViewBack.setVisibility(View.GONE);
        txtConnectMessage.setVisibility(View.INVISIBLE);
        txtChangeMessage.setBackgroundResource(R.drawable.ic_donetick);
        progressBar.setVisibility(View.GONE);
        txthelp.setVisibility(View.VISIBLE);
        txtExtraHelp.setVisibility(View.INVISIBLE);
        txtCalibrating.setVisibility(View.GONE);

        txthelp.setText(Html.fromHtml(getString(R.string.autoComplete)));

        txthelp.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        }, 2000);
    }

    /*
    * <p>Waits for calibration to complete within 10 second otherwise calibration will fail.</p>
    * */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isCalibrationComplete) {
                handler.removeCallbacks(runnable);
                calibrationComplete();
            } else if (retryWaitForCalibration <= 1) {
                //** Wait another few second until calibration complete **//
                ++retryWaitForCalibration;
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
        txtCalibrating.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        txtChangeMessage.setText(getString(R.string.start));
        txtCalibrating.setText(getString(R.string.failcalibration));
        txtCalibrating.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_alert), null, null, null);
        txtCalibrating.setCompoundDrawablePadding(5);
        informationLayout.setOnClickListener(this);
    }

    public void setIsCalibrationComplete(boolean isCalibrationComplete) {
        this.isCalibrationComplete = isCalibrationComplete;
        handler.removeCallbacks(runnable);
        if (isCalibrationComplete) {
            calibrationComplete();
        } else {
            calibrationFailed();
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
