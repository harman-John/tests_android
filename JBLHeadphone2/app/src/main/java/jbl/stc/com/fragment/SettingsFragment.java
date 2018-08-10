package jbl.stc.com.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.activity.BaseActivity;
import jbl.stc.com.activity.CalibrationActivity;
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
import jbl.stc.com.utils.AppUtils;

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
    private TextView textViewFwVersion;
    private ImageView imageViewDownload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        myDevice = getArguments().getParcelable(JBLConstant.KEY_MY_DEVICE);
        myDevice = DashboardActivity.getDashboardActivity().getMyDeviceConnected();
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(this);
        textViewFirmware = view.findViewById(R.id.text_view_settings_firmware);
        textViewDeviceName = view.findViewById(R.id.deviceName);
        deviceImage = view.findViewById(R.id.deviceImage);
        tv_toggleautoOff = view.findViewById(R.id.tv_toggleautoOff);
        toggleVoicePrompt = view.findViewById(R.id.toggleVoicePrompt);
        view.findViewById(R.id.image_view_settings_back).setOnClickListener(this);
        if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            toggleVoicePrompt.setOnClickListener(this);
            textViewFirmware.setOnClickListener(this);
            view.findViewById(R.id.voice_prompt_layout).setOnClickListener(this);
            view.findViewById(R.id.relative_layout_settings_product_help).setOnClickListener(this);
            view.findViewById(R.id.text_view_settings_smart_button).setOnClickListener(this);
        }else{
            view.findViewById(R.id.scroll_view_settings).setAlpha((float) 0.5);
        }
        toggleAutoOffTimer = (Switch) view.findViewById(R.id.toggleAutoOffTimer);
        Logger.i(TAG, "myDevice deviceName is " + myDevice.deviceName);
        relativeLayoutSmartButton = view.findViewById(R.id.relative_layout_settings_smart_button);
        if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_BUTTON)) {
            relativeLayoutSmartButton.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
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
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutAutoOffTimer.setOnClickListener(this);
            }
        } else if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AUTO_OFF_TIMER_SWITCH)) {
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            tv_toggleautoOff.setVisibility(View.GONE);
            toggleAutoOffTimer.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
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
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutTrueNote.setOnClickListener(this);
            }
        }
        relativeLayoutSoundXSetup = view.findViewById(R.id.relative_layout_settings_sound_x_setup);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SOUND_X_SETUP)) {
            relativeLayoutSoundXSetup.setVisibility(View.GONE);
        } else {
            relativeLayoutSoundXSetup.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutSoundXSetup.setOnClickListener(this);
            }
        }
        relativeLayoutSmartAssitant = view.findViewById(R.id.relative_layout_settings_smart_assistant);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_SMART_ASSISTANT)) {
            relativeLayoutSmartAssitant.setVisibility(View.GONE);
        } else {
            relativeLayoutSmartAssitant.setVisibility(View.VISIBLE);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                relativeLayoutSmartAssitant.setOnClickListener(this);
            }
        }
        deviceNameStr = myDevice.deviceName;
//        deviceNameStr=PreferenceUtils.getString(PreferenceKeys.MODEL, mContext, "");
        Logger.d(TAG, "deviceName:" + textViewDeviceName.getText());
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        updateDeviceNameAndImage(deviceNameStr, deviceImage, textViewDeviceName);
        if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            ANCControlManager.getANCManager(getActivity()).getVoicePrompt(lightX);
            ANCControlManager.getANCManager(getActivity()).getFirmwareVersion(lightX);
        }
        updateUI();
        showOta(false);
        registerConnectivity();
        return view;
    }

    public void showOta(boolean hasUpdate) {
        imageViewDownload = view.findViewById(R.id.image_view_settings_download);
        textViewFwVersion = view.findViewById(R.id.text_view_settings_firmware_version);
        if (hasUpdate) {
            imageViewDownload.setVisibility(View.VISIBLE);
            textViewFwVersion.setVisibility(View.GONE);
            textViewFwVersion.setOnClickListener(this);
            view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(this);
        }else{
            imageViewDownload.setVisibility(View.GONE);
            textViewFwVersion.setVisibility(View.VISIBLE);
            String firmwareVersion = PreferenceUtils.getString(AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()), PreferenceKeys.APP_VERSION, getActivity(),"");
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                textViewFwVersion.setText(firmwareVersion);
            }else{
                textViewFwVersion.setText("");
            }
            textViewFwVersion.setOnClickListener(null);
            view.findViewById(R.id.relative_layout_settings_firmware).setOnClickListener(null);
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
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
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
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
        tv_toggleautoOff.setText(PreferenceUtils.getString(PreferenceKeys.AUTOOFFTIMER, getActivity(), getContext().getString(R.string.five_minute)));
        if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            if(getActivity() instanceof BaseActivity){
                ((BaseActivity)getActivity()).startCheckingIfUpdateIsAvailable(SettingsFragment.this);
            }
        }
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
            case R.id.text_view_settings_firmware_version:
            case R.id.relative_layout_settings_firmware: {
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }

            case R.id.image_view_settings_back: {
                getActivity().onBackPressed();
                break;
            }
            case R.id.relative_layout_settings_true_note: {
                Logger.d(TAG, "true note clicked");
                Bundle b = new Bundle();
                b.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
                Intent intent = new Intent(getActivity(), CalibrationActivity.class);
                intent.putExtra("bundle", b);
                startActivity(intent);
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

    private int reTry = 1;
    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
        super.lightXAppReadResult(var1, command, success, buffer);
        if (success) {
            boolean boolValue;
            switch (command) {
                case AppOnEarDetectionWithAutoOff:
                    boolValue = Utility.getBoolean(buffer, 0);
                    toggleAutoOffTimer.setChecked(boolValue);
                    break;
                case AppVoicePromptEnable:
                    boolValue = Utility.getBoolean(buffer, 0);
                    toggleVoicePrompt.setChecked(boolValue);
                    break;
                case AppFirmwareVersion:
                    int major = buffer[0];
                    int minor = buffer[1];
                    int revision = buffer[2];
                    Logger.d(TAG, "AppCurrVersion = " + major + "." + minor + "." + revision);
                    String version = major + "." + minor + "." + revision;
                    textViewFwVersion.setText(version);
                    break;
            }
        }else if (reTry <= 10) {
            switch (command) {
                case AppFirmwareVersion:
                    textViewFwVersion.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
                            ANCControlManager.getANCManager(getActivity()).getFirmwareVersion(lightX);
                            ++reTry;
                        }
                    }, 150 * reTry);
                    break;
            }
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values == null || values.size() == 0) {
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
            case AmCmds.CMD_FirmwareVersion:{
                AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager(getActivity()).getAudioManager().getAccessoryStatus();
                String version = accessoryInfo.getFirmwareRev();
                textViewFwVersion.setText(version);
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
        unregisterNetworkReceiverSafely();
    }

    private NetworkChangeReceiver networkChangeReceiver;
    private void registerConnectivity() {
        if (getActivity() == null)
            return;
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        Logger.i(TAG,"registerConnectivity");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(networkChangeReceiver, intentFilter);
        mReceiverTag = true;
    }

    private boolean mReceiverTag = false;
    private void unregisterNetworkReceiverSafely() {
        try {
            if (mReceiverTag) {
                mReceiverTag = false;
                Logger.i(TAG,"unregisterNetworkReceiverSafely");
                getActivity().unregisterReceiver(networkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(TAG,"onReceive");
            if (isAdded()) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnected()) {
                        if(getActivity() instanceof BaseActivity){
                            ((BaseActivity)getActivity()).startCheckingIfUpdateIsAvailable(SettingsFragment.this);
                        }
                    }else{
                        showOta(false);
                    }
                }
            }
        }
    }
}
