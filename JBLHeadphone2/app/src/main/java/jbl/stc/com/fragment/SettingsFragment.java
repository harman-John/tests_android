package jbl.stc.com.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.LightX;

import jbl.stc.com.R;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.JBLConstant;
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
    private TextView deviceName;
    private TextView toggleautoOff;
    private ImageView deviceImage;
    private Switch toggleVoicePrompt;
    private Handler mHandler = new Handler();
    private LightX lightX;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.voice_prompt_layout).setOnClickListener(this);
        view.findViewById(R.id.text_view_settings_product_help).setOnClickListener(this);
        view.findViewById(R.id.text_view_settings_firmware).setOnClickListener(this);
        view.findViewById(R.id.image_view_settings_back).setOnClickListener(this);
        view.findViewById(R.id.text_view_settings_smart_button).setOnClickListener(this);
        view.findViewById(R.id.relative_layout_settings_auto_off).setOnClickListener(this);
        deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceImage=(ImageView) view.findViewById(R.id.deviceImage);
        updateDeviceName();
        //deviceName.setText(PreferenceUtils.getString(PreferenceKeys.MODEL, mContext, ""));
        toggleautoOff=(TextView) view.findViewById(R.id.toggleautoOff);
        toggleVoicePrompt=(Switch) view.findViewById(R.id.toggleVoicePrompt);
        Logger.i(TAG,"Model number is "+AppUtils.getModelNumber(getActivity()));
        if (DeviceFeatureMap.isFeatureSupported(AppUtils.getModelNumber(getActivity()), Feature.ENABLE_SMART_BUTTON)){
            relativeLayoutSmartButton = view.findViewById(R.id.relative_layout_settings_smart_button);
            relativeLayoutSmartButton.setOnClickListener(this);
            relativeLayoutSmartButton.setVisibility(View.VISIBLE);
        }

        relativeLayoutAutoOffTimer = view.findViewById(R.id.relative_layout_settings_auto_off);
        if (!DeviceFeatureMap.isFeatureSupported(AppUtils.getModelNumber(getActivity()), Feature.ENABLE_AUTO_OFF_TIMER)){
            relativeLayoutAutoOffTimer.setVisibility(View.GONE);
        }else{
            relativeLayoutAutoOffTimer.setVisibility(View.VISIBLE);
            relativeLayoutAutoOffTimer.setOnClickListener(this);
        }
        relativeLayoutTrueNote = view.findViewById(R.id.relative_layout_settings_true_note);
        if (!DeviceFeatureMap.isFeatureSupported(AppUtils.getModelNumber(getActivity()), Feature.ENABLE_TRUE_NOTE)){
            relativeLayoutTrueNote.setVisibility(View.GONE);
        }else{
            relativeLayoutTrueNote.setOnClickListener(this);
            relativeLayoutTrueNote.setVisibility(View.VISIBLE);
        }
        relativeLayoutSoundXSetup = view.findViewById(R.id.relative_layout_settings_sound_x_setup);
        if (!DeviceFeatureMap.isFeatureSupported(AppUtils.getModelNumber(getActivity()), Feature.ENABLE_SOUND_X_SETUP)){
            relativeLayoutSoundXSetup.setVisibility(View.GONE);
        }else{
            relativeLayoutSoundXSetup.setOnClickListener(this);
            relativeLayoutSoundXSetup.setVisibility(View.VISIBLE);
        }
        relativeLayoutSmartAssitant = view.findViewById(R.id.relative_layout_settings_smart_assistant);
        if (!DeviceFeatureMap.isFeatureSupported(AppUtils.getModelNumber(getActivity()), Feature.ENABLE_SMART_ASSISTANT)){
            relativeLayoutSmartAssitant.setVisibility(View.GONE);
        }else{
            relativeLayoutSmartAssitant.setOnClickListener(this);
            relativeLayoutSmartAssitant.setVisibility(View.VISIBLE);
        }
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        return view;
    }

    private void updateDeviceName() {
        String deviceNameStr=PreferenceUtils.getString(PreferenceKeys.MODEL, mContext, "");
        Logger.d(TAG,"deviceName:"+deviceName);
        //update device name
        deviceName.setText(deviceNameStr);
        //update device image
        if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_REFLECT_AWARE).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.reflect_aware_small);
        }else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.everest_elite_100_small);
        }else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.everest_elite_150nc_small);
        }else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.everest_elite_300_small);
        }else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.everest_elite_700_small);
        }else if (deviceNameStr.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())){
            deviceImage.setImageResource(R.mipmap.everest_elite_750_small);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        toggleautoOff.setText(PreferenceUtils.getString(PreferenceKeys.AUTOOFFTIMER,getActivity(),getContext().getString(R.string.five_minute)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_settings_product_help:{
                break;
            }
            case R.id.toggleVoicePrompt:{
                mHandler.removeCallbacks(enableVoicePromptRunnable);
                mHandler.postDelayed(enableVoicePromptRunnable, 1000);
                break;
            }
            case R.id.text_view_settings_firmware:{
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }

            case R.id.image_view_settings_back:{
                getActivity().onBackPressed();
                break;
            }
            case R.id.relative_layout_settings_true_note: {
                Log.d(TAG, "true note clicked");
                CalibrationFragment calibrationFragment=new CalibrationFragment();
                Bundle bundle=new Bundle();
                bundle.putString(CalibrationFragment.TAG, CalibrationFragment.class.getSimpleName());
                calibrationFragment.setArguments(bundle);
                switchFragment(calibrationFragment,JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.text_view_settings_smart_button:{
                Log.d(TAG,"smart button click");
                switchFragment(new SmartButtonFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.relative_layout_settings_auto_off:{
                switchFragment(new AutoOffTimeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }

    }

    private Runnable enableVoicePromptRunnable = new Runnable() {
        @Override
        public void run() {
            writeEnableVoicePrompt(toggleVoicePrompt.isChecked());
            AnalyticsManager.getInstance(getActivity()).reportVoicePromptToggle(toggleVoicePrompt.isChecked());
            Logger.d(TAG, "VoicePrompt " + toggleVoicePrompt.isChecked() + " sent");
        }
    };

    private void writeEnableVoicePrompt(boolean voiceprompt) {
        if (lightX != null){
            lightX.writeAppVoicePromptEnable(voiceprompt);
        }else{
            ANCControlManager.getANCManager(getContext()).setVoicePrompt(lightX,voiceprompt);
        }
    }
}
