package jbl.stc.com.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.listener.OnHeadphoneconnectListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.CalibrationManager;
import jbl.stc.com.view.AppButton;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.ShadowLayout;


public class CalibrationFragment extends BaseFragment implements OnHeadphoneconnectListener, View.OnClickListener {
    public static String TAG = CalibrationFragment.class.getSimpleName();
    private static CalibrationFragment calibration = null;
    CalibrationManager calibrationManager;
    private View view;
    TextView txtConnectMessage, txthelp, txtChangeMessage, txtCalibrating;
    ProgressBar progressBar;
    int timing = 10 * 1000;
    private View informationLayout;
    private AppImageView imageViewBack;
    private AppButton tv_calibratingDone;
    private ShadowLayout shadowLayout;
    private ImageView iv_complete;

    private boolean isCalibrationComplete;
    private int retryWaitForCalibration = 0;

    Handler handler = new Handler();

    private String tag;

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
        informationLayout = view.findViewById(R.id.informationLayout);
        informationLayout.setOnClickListener(this);
        tv_calibratingDone = (AppButton) view.findViewById(R.id.tv_calibratingDone);
        tv_calibratingDone.setOnClickListener(this);
        shadowLayout = (ShadowLayout) view.findViewById(R.id.shadowLayout);
        iv_complete = (ImageView) view.findViewById(R.id.iv_complete);
        if (this.getArguments() != null) {
            tag = this.getArguments().getString(CalibrationFragment.TAG);
            Logger.d(TAG, tag);
        }
        return view;
    }


    @Override
    public void onHeadPhoneState(boolean isConnect, String headphoneName) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.informationLayout:
                startCalibration();
                informationLayout.setOnClickListener(null);
                break;
            case R.id.image_view_calibration_global_back:
                calibration = null;
                dummyStopCalibration();
                break;
            case R.id.tv_calibratingDone:
                calibration = null;
                dummyStopCalibration();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    calibration = null;
                    dummyStopCalibration();
                    return true;
                }
                return false;
            }
        });
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
    }

    /*
     * <p>Stops the calibration process.</p>
     * */
    public void dummyStopCalibration() {
        handler.removeCallbacks(runnable);
        if (!TextUtils.isEmpty(tag) && tag.equals(CalibrationFragment.TAG)) {
            //enter from setting
            getActivity().onBackPressed();
        } else {
            removeAllFragment();
            switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
        }
    }

    /**
     * Calibration start screen will be visible.
     */
    public void calibrationComplete() {
        imageViewBack.setVisibility(View.GONE);
        informationLayout.setVisibility(View.GONE);
        txthelp.setVisibility(View.GONE);
        txtCalibrating.setVisibility(View.GONE);
        iv_complete.setVisibility(View.VISIBLE);

        txtConnectMessage.setText(Html.fromHtml(getString(R.string.autoComplete)));
        tv_calibratingDone.setVisibility(View.VISIBLE);
        shadowLayout.setVisibility(View.VISIBLE);

        /*txthelp.postDelayed(new Runnable() {
            @Override
            public void run() {
//                getActivity().finish();
            }
        }, 2000);*/
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


    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
        super.lightXAppReadResult(var1, command, success, buffer);
        if (success) {
            switch (command) {
                case App_0xB3:
                    if (CalibrationFragment.getCalibration() != null)
                        CalibrationFragment.getCalibration().setIsCalibrationComplete(true);
                    break;
            }
        }
    }
}
