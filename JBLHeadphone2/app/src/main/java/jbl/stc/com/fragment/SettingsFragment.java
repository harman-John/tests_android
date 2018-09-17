package jbl.stc.com.fragment;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.avnera.audiomanager.AccessoryInfo;
import com.harman.bluetooth.constants.EnumDeviceStatusType;
import com.harman.bluetooth.req.CmdDevStatus;

import java.util.Arrays;

import jbl.stc.com.R;
import jbl.stc.com.activity.BaseActivity;
import jbl.stc.com.activity.CalibrationActivity;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.UiUtils;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private View view;
    private TextView tvToggleAutoOff;
    private Switch toggleVoicePrompt;
    private Switch toggleAutoOffTimer;
    private Handler mHandler = new Handler();
    private String deviceNameStr;
    private MyDevice myDevice;
    private TextView textViewFwVersion;
    private int reTry = 1;
    private ImageView imageViewDownload;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private LinearLayout ll_deviceImage;
    private ImageView deviceImageView;
    private int screenWidth;
    private int screenHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.widthPixels;
        myDevice = ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED);
        Logger.d(TAG, "deviceName:" + ((myDevice!=null)?myDevice.deviceName:"device is null"));
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(this);
        view.findViewById(R.id.text_view_settings_product_help).setOnClickListener(this);
        TextView textViewFirmware = view.findViewById(R.id.text_view_settings_firmware);
        TextView textViewDeviceName = view.findViewById(R.id.deviceName);
        ImageView deviceImage = view.findViewById(R.id.deviceImage);
        deviceImage.setOnClickListener(this);
        tvToggleAutoOff = view.findViewById(R.id.tv_toggleautoOff);
        toggleVoicePrompt = view.findViewById(R.id.toggleVoicePrompt);
        view.findViewById(R.id.image_view_settings_back).setOnClickListener(this);
        if (myDevice!=null && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            toggleVoicePrompt.setOnClickListener(this);
            textViewFirmware.setOnClickListener(this);
            view.findViewById(R.id.voice_prompt_layout).setOnClickListener(this);
            view.findViewById(R.id.relative_layout_settings_product_help).setOnClickListener(this);
            view.findViewById(R.id.text_view_settings_smart_button).setOnClickListener(this);
        } else {
            view.findViewById(R.id.scroll_view_settings).setAlpha((float) 0.5);
        }
        toggleAutoOffTimer = view.findViewById(R.id.toggleAutoOffTimer);
        if (myDevice!= null) {
            Logger.i(TAG, "myDevice deviceName is " + myDevice.deviceName);
        }
        RelativeLayout relativeLayoutSmartButton = view.findViewById(R.id.relative_layout_settings_smart_button);
        if (myDevice!= null &&DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_BUTTON)) {
            relativeLayoutSmartButton.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutSmartButton.setOnClickListener(this);
            }
        } else {
            relativeLayoutSmartButton.setVisibility(View.GONE);
        }

        RelativeLayout relativeLayoutAutoOffTimer = view.findViewById(R.id.relative_layout_settings_auto_off);
        if (myDevice!=null && DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AUTO_OFF_TIMER)) {
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            tvToggleAutoOff.setVisibility(View.VISIBLE);
            toggleAutoOffTimer.setVisibility(View.GONE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutAutoOffTimer.setOnClickListener(this);
            }
        } else if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AUTO_OFF_TIMER_SWITCH)) {
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            tvToggleAutoOff.setVisibility(View.GONE);
            toggleAutoOffTimer.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                toggleAutoOffTimer.setOnClickListener(this);
            }

        } else {
            relativeLayoutAutoOffTimer.setVisibility(View.GONE);
        }
        RelativeLayout relativeLayoutTrueNote = view.findViewById(R.id.relative_layout_settings_true_note);
        if (myDevice!=null && !DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_TRUE_NOTE)) {
            relativeLayoutTrueNote.setVisibility(View.GONE);
        } else {
            relativeLayoutTrueNote.setVisibility(View.VISIBLE);
            if (myDevice!=null && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutTrueNote.setOnClickListener(this);
            }
        }
        RelativeLayout relativeLayoutSoundXSetup = view.findViewById(R.id.relative_layout_settings_sound_x_setup);
        if (myDevice!=null && !DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SOUND_X_SETUP)) {
            relativeLayoutSoundXSetup.setVisibility(View.GONE);
        } else {
            relativeLayoutSoundXSetup.setVisibility(View.VISIBLE);
            if (myDevice!=null && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutSoundXSetup.setOnClickListener(this);
            }
        }
        RelativeLayout relativeLayoutSmartAssitant = view.findViewById(R.id.relative_layout_settings_smart_assistant);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_ASSISTANT)) {
            relativeLayoutSmartAssitant.setVisibility(View.GONE);
        } else {
            relativeLayoutSmartAssitant.setVisibility(View.VISIBLE);
            if (myDevice!=null && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutSmartAssitant.setOnClickListener(this);
            }
        }

        if (myDevice!=null && !DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_VOICE_PROMPT)) {
            view.findViewById(R.id.relative_layout_settings_voice_prompt).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.relative_layout_settings_voice_prompt).setVisibility(View.VISIBLE);
        }
        if (myDevice!=null) {
            deviceNameStr = myDevice.deviceName;
            Logger.d(TAG, "deviceName:" + textViewDeviceName.getText());
            updateDeviceNameAndImage(deviceNameStr, deviceImage, textViewDeviceName);
            updateUI();
        }
        showOta(false);
        registerConnectivity();
        return view;
    }

    public void showOta(boolean hasUpdate) {
        ImageView imageViewDownload = view.findViewById(R.id.image_view_settings_download);
        textViewFwVersion = view.findViewById(R.id.text_view_settings_firmware_version);
        if (hasUpdate) {
            imageViewDownload.setVisibility(View.VISIBLE);
            textViewFwVersion.setVisibility(View.GONE);
            textViewFwVersion.setOnClickListener(this);
            view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(this);
        } else {
            imageViewDownload.setVisibility(View.GONE);
            textViewFwVersion.setVisibility(View.VISIBLE);
            String firmwareVersion = PreferenceUtils.getString(AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()), PreferenceKeys.APP_VERSION, getActivity(), "");
            if (myDevice!=null &&myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                textViewFwVersion.setText(firmwareVersion);
            } else {
                textViewFwVersion.setText("");
            }
            textViewFwVersion.setOnClickListener(null);
            view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(null);
        }
    }

    private void updateUI() {
        if (myDevice!=null &&myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            ANCControlManager.getANCManager(getActivity()).getVoicePrompt();
            ANCControlManager.getANCManager(getActivity()).getFirmwareVersion();
        }
        if (TextUtils.isEmpty(deviceNameStr)) {
            return;
        }
        if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())) {
            tvToggleAutoOff.setVisibility(View.GONE);
            toggleAutoOffTimer.setVisibility(View.VISIBLE);
            //get autooff timer
            if (myDevice!=null &&myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                ANCControlManager.getANCManager(getActivity()).getAutoOffFeature();
            }
        } else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_500BT).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_400BT).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_650BTNC).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_FREE_GA).toUpperCase())) {
            tvToggleAutoOff.setVisibility(View.VISIBLE);
            toggleAutoOffTimer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
        tvToggleAutoOff.setText(PreferenceUtils.getString(PreferenceKeys.AUTOOFFTIMER, getActivity(), getContext().getString(R.string.five_minute)));
        if (myDevice!=null && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).startCheckingIfUpdateIsAvailable(SettingsFragment.this);
            }
        }
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    createDeviceImageView(getActivity());
                    return true;
                }
                return false;
            }
        });
        if (LiveManager.getInstance().isConnected()) {
            getBleDeviceInfo();
        }
    }

    private void getBleDeviceInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(myDevice!=null) {
                    CmdDevStatus cmdDevStatus = new CmdDevStatus(EnumDeviceStatusType.AUTO_OFF);
                    LiveManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(myDevice.connectStatus).mac, cmdDevStatus);
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_settings_product_help: {
                Uri uri = Uri.parse("http://www.jbl.com");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
                break;
            }
            case R.id.toggleAutoOffTimer: {
                mHandler.removeCallbacks(autoOffToggleRunnable);
                mHandler.postDelayed(autoOffToggleRunnable, 1000);
                break;
            }
            case R.id.toggleVoicePrompt: {
                mHandler.removeCallbacks(enableVoicePromptRunnable);
                mHandler.postDelayed(enableVoicePromptRunnable, 1000);
                break;
            }
            case R.id.text_view_settings_firmware_version:
            case R.id.relative_layout_settings_firmware: {
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.deviceImage: {

                createDeviceImageView(getActivity());
                break;
            }
            case R.id.image_view_settings_back: {
//                getActivity().onBackPressed();

                createDeviceImageView(getActivity());
                break;
            }
            case R.id.relative_layout_settings_true_note: {
                Logger.d(TAG, "true note clicked");
                startActivity(new Intent(getActivity(), CalibrationActivity.class));
                break;
            }
            case R.id.text_view_settings_smart_button: {
                Logger.d(TAG, "smart button click");
                switchFragment(new SmartButtonFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.relative_layout_settings_auto_off: {
                switchFragment(new AutoOffTimeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.relative_layout_settings_smart_assistant:{
                switchFragment(new VoiceAssistantFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }

    }

    private void createDeviceImageView(final Context context) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        float x = (screenWidth - UiUtils.dip2px(getActivity(), 120)) / 2;
        float y = UiUtils.dip2px(getActivity(), 105) + UiUtils.getStatusHeight(context);
        view.findViewById(R.id.rl_deviceImage).setVisibility(View.INVISIBLE);
        int dashboardImageHeight = UiUtils.getDashboardDeviceImageHeight(context);
        final int height = UiUtils.dip2px(getActivity(), 120);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = (int) x;
        mWindowLayoutParams.y = (int) y;
        mWindowLayoutParams.alpha = 1.0f;
        mWindowLayoutParams.width = screenWidth;
        mWindowLayoutParams.height = screenHeight;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ll_deviceImage = new LinearLayout(context);
        WindowManager.LayoutParams ll_params = new WindowManager.LayoutParams();
        ll_params.gravity = Gravity.TOP | Gravity.LEFT;
        ll_params.height = height;
        ll_params.width = height;
        ll_deviceImage.setLayoutParams(ll_params);
        deviceImageView = new ImageView(context);
        deviceImageView.setBackgroundResource(R.drawable.shape_dashboard_device_circle);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = height;
        params.height = height;
        UiUtils.setDeviceImage(deviceNameStr, deviceImageView);
        ll_deviceImage.addView(deviceImageView, params);
        mWindowManager.addView(ll_deviceImage, mWindowLayoutParams);

        ll_deviceImage.clearAnimation();
        deviceImageView.clearAnimation();

        final float startX = x;
        float startY = y;
        Logger.d(TAG, "createDeviceImageView x:" + x + "y:" + y);
        float endX = startX;
        float endY = startY - UiUtils.dip2px(context, 35) + ((UiUtils.getDeviceImageMarginTop(context) + UiUtils.dip2px(context, 62)) + (dashboardImageHeight - height) / 2 - (UiUtils.dip2px(context, 105) + height));

        ObjectAnimator animX = ObjectAnimator.ofFloat(ll_deviceImage, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(ll_deviceImage, "translationY",
                startY, endY);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(deviceImageView, "scaleY",
                1, (float) (dashboardImageHeight) / (float) (height));
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(deviceImageView, "scaleX",
                1, (float) (dashboardImageHeight) / (float) (height));
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY, animScaleX, animScaleY);
        animSetXY.setDuration(400);
        animSetXY.setInterpolator(new AccelerateDecelerateInterpolator());
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                getActivity().onBackPressed();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.findViewById(R.id.rl_deviceImage).setVisibility(View.VISIBLE);
                mWindowLayoutParams.alpha = 0f;
                mWindowManager.updateViewLayout(ll_deviceImage, mWindowLayoutParams);
                if (ll_deviceImage != null) {
                    mWindowManager.removeView(ll_deviceImage);
                    ll_deviceImage = null;
                    deviceImageView = null;

                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();


    }

    private Runnable autoOffToggleRunnable = new Runnable() {
        @Override
        public void run() {
            ANCControlManager.getANCManager(getContext()).setAutoOffFeature((toggleAutoOffTimer.isChecked()));
            AnalyticsManager.getInstance().reportAutoOffToggle(toggleAutoOffTimer.isChecked());
            Logger.d(TAG, "AutoOffFeature " + toggleAutoOffTimer.isChecked() + " sent");
        }

    };

    private Runnable enableVoicePromptRunnable = new Runnable() {
        @Override
        public void run() {
            ANCControlManager.getANCManager(getContext()).setVoicePrompt(toggleVoicePrompt.isChecked());
            AnalyticsManager.getInstance().reportVoicePromptToggle(toggleVoicePrompt.isChecked());
            Logger.d(TAG, "VoicePrompt " + toggleVoicePrompt.isChecked() + " sent");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(autoOffToggleRunnable);
        autoOffToggleRunnable = null;
        mHandler.removeCallbacks(enableVoicePromptRunnable);
        enableVoicePromptRunnable = null;
        unregisterNetworkReceiverSafely();
    }

    private NetworkChangeReceiver networkChangeReceiver;

    private void registerConnectivity() {
        if (getActivity() == null)
            return;
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        Logger.i(TAG, "registerConnectivity");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(networkChangeReceiver, intentFilter);
        mReceiverTag = true;
    }

    private boolean mReceiverTag = false;

    private void unregisterNetworkReceiverSafely() {
        try {
            if (mReceiverTag) {
                mReceiverTag = false;
                Logger.i(TAG, "unregisterNetworkReceiverSafely");
                getActivity().unregisterReceiver(networkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(TAG, "onReceive");
            if (isAdded()) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnected()) {
                        if (getActivity() instanceof BaseActivity) {
                            ((BaseActivity) getActivity()).startCheckingIfUpdateIsAvailable(SettingsFragment.this);
                        }
                    } else {
                        showOta(false);
                    }
                }
            }
        }
    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);
        Logger.i(TAG, "on receive cmd: " + enumCommands + ",object: " + Arrays.toString(objects));
        switch (enumCommands) {
            case CMD_VoicePrompt: {
                toggleVoicePrompt.setChecked((boolean) objects[0]);
                break;
            }
            case CMD_AutoOffEnable: {
                toggleAutoOffTimer.setChecked((boolean) objects[0]);
                if (objects.length>1&&objects[1] != null) {
                    tvToggleAutoOff.setText(String.valueOf(objects[1])+"min");
                }
                break;
            }
            case CMD_FIRMWARE_VERSION: {
                String version = (String) objects[0];
                if (version == null && AvneraManager.getAvenraManager().getAudioManager()!= null) {
                    AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager().getAudioManager().getAccessoryStatus();
                    String firmwareRev = accessoryInfo.getFirmwareRev();
                    textViewFwVersion.setText(firmwareRev);
                } else {
                    if ((boolean) objects[1]) {
                        textViewFwVersion.setText(version);
                    } else {
                        textViewFwVersion.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ANCControlManager.getANCManager(getActivity()).getFirmwareVersion();
                                ++reTry;
                            }
                        }, 150 * reTry);
                    }
                }
                break;
            }
        }
    }
}
