package jbl.stc.com.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.FirmwareUtil;

public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private View view;
    private RelativeLayout relativeLayoutSmartButton;
    private RelativeLayout relativeLayoutAutoOffTimer;
    private RelativeLayout relativeLayoutTrueNote;
    private RelativeLayout relativeLayoutSoundXSetup;
    private RelativeLayout relativeLayoutSmartAssitant;
    private TextView textViewDeviceName;
    private TextView tv_toggleautoOff;
    private ImageView deviceImage;
    private Switch toggleVoicePrompt;
    private Switch toggleAutoOffTimer;
    private Handler mHandler = new Handler();
    private LightX lightX;
    private String deviceNameStr;
    private TextView textViewFirmware;
    private MyDevice myDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myDevice = getArguments().getParcelable(JBLConstant.KEY_MY_DEVICE);
        if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
            DashboardActivity.getDashboardActivity().startCheckingIfUpdateIsAvailable();
        }
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        textViewFirmware = view.findViewById(R.id.text_view_settings_firmware);
        textViewDeviceName = view.findViewById(R.id.deviceName);
        deviceImage = view.findViewById(R.id.deviceImage);
        tv_toggleautoOff = view.findViewById(R.id.tv_toggleautoOff);
        toggleVoicePrompt = view.findViewById(R.id.toggleVoicePrompt);
        view.findViewById(R.id.image_view_settings_back).setOnClickListener(this);
        if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
            toggleVoicePrompt.setOnClickListener(this);
            textViewFirmware.setOnClickListener(this);
            view.findViewById(R.id.voice_prompt_layout).setOnClickListener(this);
            view.findViewById(R.id.text_view_settings_product_help).setOnClickListener(this);
            view.findViewById(R.id.text_view_settings_smart_button).setOnClickListener(this);
        }
        toggleAutoOffTimer = (Switch) view.findViewById(R.id.toggleAutoOffTimer);
        Logger.i(TAG, "myDevice deviceName is " + myDevice.deviceName);
        relativeLayoutSmartButton = view.findViewById(R.id.relative_layout_settings_smart_button);
        if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_BUTTON)) {
            relativeLayoutSmartButton.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                relativeLayoutSmartButton.setOnClickListener(this);
            }
        } else {
            relativeLayoutSmartButton.setVisibility(View.GONE);
        }

        relativeLayoutAutoOffTimer = view.findViewById(R.id.relative_layout_settings_auto_off);
        if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AUTO_OFF_TIMER)) {
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            tv_toggleautoOff.setVisibility(View.VISIBLE);
            toggleAutoOffTimer.setVisibility(View.GONE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                relativeLayoutAutoOffTimer.setOnClickListener(this);
            }
        } else if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AUTO_OFF_TIMER_SWITCH)) {
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            tv_toggleautoOff.setVisibility(View.GONE);
            toggleAutoOffTimer.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                toggleAutoOffTimer.setOnClickListener(this);
            }

        } else {
            relativeLayoutAutoOffTimer.setVisibility(View.GONE);
        }
        relativeLayoutTrueNote = view.findViewById(R.id.relative_layout_settings_true_note);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_TRUE_NOTE)) {
            relativeLayoutTrueNote.setVisibility(View.GONE);
        } else {
            relativeLayoutTrueNote.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                relativeLayoutTrueNote.setOnClickListener(this);
            }
        }
        relativeLayoutSoundXSetup = view.findViewById(R.id.relative_layout_settings_sound_x_setup);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SOUND_X_SETUP)) {
            relativeLayoutSoundXSetup.setVisibility(View.GONE);
        } else {
            relativeLayoutSoundXSetup.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                relativeLayoutSoundXSetup.setOnClickListener(this);
            }
        }
        relativeLayoutSmartAssitant = view.findViewById(R.id.relative_layout_settings_smart_assistant);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_ASSISTANT)) {
            relativeLayoutSmartAssitant.setVisibility(View.GONE);
        } else {
            relativeLayoutSmartAssitant.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                relativeLayoutSmartAssitant.setOnClickListener(this);
            }
        }
        deviceNameStr = myDevice.deviceName;
//        deviceNameStr=PreferenceUtils.getString(PreferenceKeys.MODEL, mContext, "");
        Logger.d(TAG, "deviceName:" + textViewDeviceName.getText());
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        updateDeviceNameAndImage(deviceNameStr, deviceImage, textViewDeviceName);
        if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
            ANCControlManager.getANCManager(getActivity()).getVoicePrompt(lightX);
        }
        updateUI();
        showOta(FirmwareUtil.isUpdatingFirmWare.get());
        return view;
    }

    public void showOta(boolean hasUpdate) {
        if (hasUpdate && textViewFirmware != null) {
            Drawable nav_up = ContextCompat.getDrawable(getActivity(), R.mipmap.download);
            nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
            textViewFirmware.setCompoundDrawables(null, null, nav_up, null);
        }
    }

    private void updateUI() {
        if (TextUtils.isEmpty(deviceNameStr)) {
            return;
        }
        if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())) {
            tv_toggleautoOff.setVisibility(View.GONE);
            toggleAutoOffTimer.setVisibility(View.VISIBLE);
            //get autooff timer
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
                ANCControlManager.getANCManager(getActivity()).getAutoOffFeature(lightX);
            }
        } else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_500BT).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_400BT).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_650BTNC).toUpperCase()) ||
                deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_LIVE_FREE_GA).toUpperCase())) {
            tv_toggleautoOff.setVisibility(View.VISIBLE);
            toggleAutoOffTimer.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        tv_toggleautoOff.setText(PreferenceUtils.getString(PreferenceKeys.AUTOOFFTIMER, getActivity(), getContext().getString(R.string.five_minute)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_settings_product_help: {
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
            case R.id.text_view_settings_firmware: {
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }

            case R.id.image_view_settings_back: {
                getActivity().onBackPressed();
                break;
            }
            case R.id.relative_layout_settings_true_note: {
                Log.d(TAG, "true note clicked");
                CalibrationFragment calibrationFragment = new CalibrationFragment();
                Bundle bundle = new Bundle();
                bundle.putString(CalibrationFragment.TAG, CalibrationFragment.class.getSimpleName());
                calibrationFragment.setArguments(bundle);
                switchFragment(calibrationFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.text_view_settings_smart_button: {
                Log.d(TAG, "smart button click");
                switchFragment(new SmartButtonFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.relative_layout_settings_auto_off: {
                switchFragment(new AutoOffTimeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }

    }

    private Runnable autoOffToggleRunnable = new Runnable() {
        @Override
        public void run() {
            writeAppAutoOffFeature(toggleAutoOffTimer.isChecked());
            AnalyticsManager.getInstance(getActivity()).reportAutoOffToggle(toggleAutoOffTimer.isChecked());
            Logger.d(TAG, "AutoOffFeature " + toggleAutoOffTimer.isChecked() + " sent");
        }

    };

    private Runnable enableVoicePromptRunnable = new Runnable() {
        @Override
        public void run() {
            writeEnableVoicePrompt(toggleVoicePrompt.isChecked());
            AnalyticsManager.getInstance(getActivity()).reportVoicePromptToggle(toggleVoicePrompt.isChecked());
            Logger.d(TAG, "VoicePrompt " + toggleVoicePrompt.isChecked() + " sent");
        }
    };

    private void writeAppAutoOffFeature(boolean checked) {
        if (lightX != null) {
            lightX.writeAppOnEarDetectionWithAutoOff(checked);
        } else {
            ANCControlManager.getANCManager(getContext()).setAutoOffFeature(lightX, checked);
        }
    }

    private void writeEnableVoicePrompt(boolean voiceprompt) {
        if (lightX != null) {
            lightX.writeAppVoicePromptEnable(voiceprompt);
        } else {
            ANCControlManager.getANCManager(getContext()).setVoicePrompt(lightX, voiceprompt);
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values == null || (values != null && values.size() == 0)) {
            return;
        }
        values.iterator().next().getValue().toString();
        Logger.d(TAG, "value:" + values.iterator().next().getValue().toString());
        switch (command) {
            case AmCmds.CMD_VoicePrompt: {
                String boolValue = "";
                if (values != null && values.size() > 0) {
                    boolValue = values.iterator().next().getValue().toString();
                }
                if (!TextUtils.isEmpty(boolValue) && boolValue.equals("true")) {
                    toggleVoicePrompt.setChecked(true);
                } else {
                    toggleVoicePrompt.setChecked(false);
                }
                break;
            }
            case AmCmds.CMD_AutoOffEnable: {
                String boolValue = "";
                if (values != null && values.size() > 0) {
                    boolValue = values.iterator().next().getValue().toString();
                }
                if (!TextUtils.isEmpty(boolValue) && boolValue.equals("true")) {
                    toggleAutoOffTimer.setChecked(true);
                } else {
                    toggleAutoOffTimer.setChecked(false);
                }
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(autoOffToggleRunnable);
        autoOffToggleRunnable = null;
        mHandler.removeCallbacks(enableVoicePromptRunnable);
        enableVoicePromptRunnable = null;
    }
}
