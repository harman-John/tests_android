package jbl.stc.com.fragment;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.listener.AwarenessChangeListener;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.BlurBuilder;
import jbl.stc.com.view.ANCController;
import jbl.stc.com.view.CircularInsideLayout;

import jbl.stc.com.utils.FirmwareUtil;

import static java.lang.Integer.valueOf;


public class HomeFragment extends BaseFragment implements View.OnClickListener,AwarenessChangeListener, ANCController.OnSeekArcChangeListener{
    private static final String TAG = HomeFragment.class.getSimpleName();
    private View view, mBlurView;
    private CreateEqTipsDialog createEqTipsDialog;

    private HomeHandler homeHandler = new HomeHandler(Looper.getMainLooper());
    private final static int MSG_ANC = 0;
    private final static int MSG_BATTERY = 1;
    private final static int MSG_FIRMWARE_VERSION = 2;
    private final static int MSG_CURRENT_PRESET = 3;
    private final static int MSG_RAW_STEP = 4;
    private final static int MSG_AMBIENT_LEVEL = 5;
    private final static int MSG_READ_BATTERY_INTERVAL = 6;
    private final static int MSG_AA_LEFT = 32;
    private final static int MSG_AA_RIGHT = 33;
    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
    private ProgressBar progressBarBattery;
    private TextView textViewBattery;

    private PopupWindow popupWindow;
    private CheckBox checkBoxNoiseCancel;
    private LightX lightX;
    private ANCAwarenessPreset awarenessPreset, lastsavedAwarenessState;
    private ANCController ancController;
    private boolean isRequestingLeftANC, isRequestingRightANC;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,
                container, false);
        Log.i(TAG,"onCreateView");
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        view.findViewById(R.id.image_view_settings).setOnClickListener(this);
        view.findViewById(R.id.image_view_info).setOnClickListener(this);
        view.findViewById(R.id.image_view_ambient_aware).setOnClickListener(this);
        view.findViewById(R.id.deviceImageView);
        view.findViewById(R.id.eqSwitchLayout);
        view.findViewById(R.id.eqInfoLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.eqInfoLayout).setOnClickListener(this);
        checkBoxNoiseCancel = view.findViewById(R.id.image_view_noise_cancel);
        checkBoxNoiseCancel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked){
//                    ANCControlManager.getANCManager(getActivity()).setANCValue(lightX,true);
//                }else{
//                    ANCControlManager.getANCManager(getActivity()).setANCValue(lightX,false);
//                }
            }
        });
        checkBoxNoiseCancel.setOnClickListener(this);
        view.findViewById(R.id.eqNameText);
        view.findViewById(R.id.titleEqText);
        view.findViewById(R.id.eqDividerView);
        progressBarBattery = view.findViewById(R.id.batteryProgressBar);
        textViewBattery = view.findViewById(R.id.batteryLevelText);
        view.findViewById(R.id.text_view_ambient_aware);
        createEqTipsDialog = new CreateEqTipsDialog(getActivity());
        createEqTipsDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment(),JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
            }

            @Override
            public void onCancel() {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
        ANCControlManager.getANCManager(getActivity()).getBatterLeverl(lightX);
        homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL,timeInterval);
        Log.d(TAG, "onResume" + DeviceConnectionManager.getInstance().getCurrentDevice());
        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
            case NONE:
                break;
            case Connected_USBDevice:
                break;
            case Connected_BluetoothDevice:
                if (AppUtils.is150NC(getActivity())) {
                    if (PreferenceUtils.getBoolean(PreferenceKeys.RECEIVE_READY,getActivity())){
                        Log.d(TAG, "onResume send msg GET_ANC");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ANCControlManager.getANCManager(getContext()).getANCValue(lightX);
                        PreferenceUtils.setBoolean(PreferenceKeys.RECEIVE_READY,false,getActivity());
                    } else{
                        Log.d(TAG, "onResume update");
                        getDeviceInfo();
//                        updateANC(JBLPreferenceUtil.getInt(JBLPreferenceKeys.ANC_VALUE, getAppActivity()) != 0);
//                        updateAmbientLevel(JBLPreferenceUtil.getInt(JBLPreferenceKeys.AWARENESS, getAppActivity()));
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_ambient_aware: {
                showAncPopupWindow();
                break;
            }
            case R.id.eqInfoLayout: {
                switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_info:{
                switchFragment(new InfoFragment(),JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                break;
            }
            case R.id.image_view_settings:{
                switchFragment(new SettingsFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_noise_cancel:{
                if (checkBoxNoiseCancel.isChecked()){
                    ANCControlManager.getANCManager(getActivity()).setANCValue(lightX,false);
                }else{
                    ANCControlManager.getANCManager(getActivity()).setANCValue(lightX,false);
                }
                break;
            }
        }
    }

    protected void showAncPopupWindow() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.popup_window_anc, null,
                false);
        ancController = popupWindow_view.findViewById(R.id.circularSeekBar);
        CircularInsideLayout circularInsideLayout = popupWindow_view.findViewById(R.id.imageContainer);
        circularInsideLayout.setonAwarenesChangeListener(this);
        ancController.setCircularInsideLayout(circularInsideLayout);
        ancController.setOnSeekArcChangeListener(this);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);

        if(mBlurView == null){
            //generate blur view
            mBlurView = view.findViewById(R.id.blur_view);
            Bitmap image = BlurBuilder.blur(view);
            mBlurView.setBackground(new BitmapDrawable(getActivity().getResources(), image));
        }
        mBlurView.setVisibility(View.VISIBLE);
        // set animation effect
        popupWindow.setAnimationStyle(R.style.style_down_to_top);
        popupWindow_view.findViewById(R.id.aa_popup_close_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            }
        });

        popupWindow.showAsDropDown(view);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //dismiss blur view
                if(mBlurView != null){
                    mBlurView.setVisibility(View.GONE);
                }
            }
        });
        popupWindow_view.findViewById(R.id.noiseText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ancController.setSwitchOff(false);
                ANCControlManager.getANCManager(getActivity()).setAmbientLeveling(lightX, ANCAwarenessPreset.None);
            }
        });
        getAAValue();
    }

    @Override
    public void onMedium() {
        //on AA medium checked
        ANCControlManager.getANCManager(getActivity()).setAmbientLeveling(lightX, ANCAwarenessPreset.Medium);
    }

    @Override
    public void onLow() {
       //on AA low checked
        ANCControlManager.getANCManager(getActivity()).setAmbientLeveling(lightX, ANCAwarenessPreset.Low);
    }

    @Override
    public void onHigh() {
      //on AA high checked
        ANCControlManager.getANCManager(getActivity()).setAmbientLeveling(lightX, ANCAwarenessPreset.High);
    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {

        if (fromUser) {
            // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
            //Set animation to false and presetValue to -1
            int savedLeft = PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, getActivity());
            int savedRight = PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, getActivity());

            PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, leftProgress, getActivity());
            PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, rightProgress, getActivity());
//            AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelChanged(mLeftProgress, true);
//            AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelChanged(mRightProgress, false);
            lastsavedAwarenessState = null;
//            promptSeekAbuse.removeCallbacks(runnablepromptSeekAbuse);
//            promptSeekAbuse.postDelayed(runnablepromptSeekAbuse, 300);
            if (leftProgress != savedLeft) {
                ANCControlManager.getANCManager(getActivity()).setLeftAwarenessPresetValue(lightX, leftProgress);
            }
            if (rightProgress != savedRight) {
                ANCControlManager.getANCManager(getActivity()).setRightAwarenessPresetValue(lightX, rightProgress);
            }
        }
    }


    @Override
    public void onStartTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {

    }


    private void getDeviceInfo(){
        ANCControlManager.getANCManager(getContext()).getANCValue(lightX);
//        ANCControlManager.getANCManager(getContext()).getAmbientLeveling(lightX);
//        ANCControlManager.getANCManager(getContext()).getFirmwareInfo(lightX);
//        ANCControlManager.getANCManager(getContext()).getCurrentPreset(lightX);
    }

    private void getAAValue(){
        ANCControlManager.getANCManager(getContext()).getAmbientLeveling(lightX);
    }
    private class HomeHandler extends Handler {

        public HomeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_READ_BATTERY_INTERVAL:{
                    homeHandler.removeMessages(MSG_READ_BATTERY_INTERVAL);
                    ANCControlManager.getANCManager(getActivity()).getBatterLeverl(lightX);
                    homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL,timeInterval);
                    break;
                }
                case MSG_BATTERY:{
                    updateBattery(msg.arg1);
                    break;
                }
                case MSG_ANC: {
                    updateANC(msg.arg1 == 1);
                    break;
                }
                case MSG_FIRMWARE_VERSION:{
                    updateFirmwareVersion();
                    break;
                }
                case MSG_AMBIENT_LEVEL:{
                    updateAAUI(AppUtils.levelTransfer(msg.arg1));
                    break;
                }
                case MSG_AA_LEFT:
                    updateAALeft(msg.arg1);
                    break;
                case MSG_AA_RIGHT:
                    updateAARight(msg.arg1);
                    break;
                case MSG_CURRENT_PRESET:{
                    break;
                }
                case MSG_RAW_STEP:{
                    break;
                }
            }
        }
    }

    private void updateANC(boolean onOff){
        checkBoxNoiseCancel.setChecked(onOff);
    }

    private void updateBattery(int value) {
        Log.d(TAG, "battery value = " + value);
        PreferenceUtils.setInt(PreferenceKeys.BATTERY_VALUE, value, getActivity());
        if (value == 255) {
            progressBarBattery.setProgress(100);
            textViewBattery.setText("100%");
        } else {
            progressBarBattery.setProgress(value);
            textViewBattery.setText(String.format("%s%%", String.valueOf(value)));
        }
    }

    private void updateFirmwareVersion() {
        AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager(getActivity()).getAudioManager().getAccessoryStatus();
        PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), getActivity());
        AppUtils.setModelNumber(getActivity(), accessoryInfo.getModelNumber());
        String version = accessoryInfo.getFirmwareRev();
        if (version.length() >= 5) {
            Log.d(TAG, "currentVersion : " + version);
            PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.APP_VERSION, version, getActivity());
        }
        String hardVersion = accessoryInfo.getHardwareRev();
        if (hardVersion.length() >= 5) {
            Log.d(TAG, "hardVersion : " + hardVersion);
//                JBLPreferenceUtil.setString(AppUtils.RSRC_VERSION, fwVersion, getActivity());
            PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, hardVersion, getActivity());
        }
        AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(hardVersion);
        DashboardActivity.getDashboardActivity().startCheckingIfUpdateIsAvailable();
    }

    private void updateAALeft(int value){
        PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, value ,getActivity());
        isRequestingLeftANC = false;
        if(!isRequestingLeftANC && !isRequestingRightANC){
//            ancController.initProgress();
        }
        ancController.initLeftProgress(value);

//        ancController.initProgress(value, PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, getActivity()), value);
    }
    private void updateAARight(int value){
        PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, value ,getActivity());
        isRequestingRightANC = false;
        if(!isRequestingLeftANC && !isRequestingRightANC){

        }
        ancController.initRightProgress(value);
//        ancController.initProgress(PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, getActivity()), value, value);
    }
    private void updateAAUI(int aaLevelingValue){
            boolean is150NC = AppUtils.is150NC(getActivity());
        Log.d(TAG, "updateAmbientLevel: " + aaLevelingValue + "," + PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity()) + ",is150NC=" + is150NC);
            switch (aaLevelingValue) {
                case 0:
                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, 0, getActivity());
                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, 0, getActivity());
                    lastsavedAwarenessState = ANCAwarenessPreset.None;
                    break;
                case 1: //ANCAwarenessPreset.Low
                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 28 : 25, getActivity());
                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 28 : 25, getActivity());
                    lastsavedAwarenessState = ANCAwarenessPreset.Low;
                    break;
                case 2: //ANCAwarenessPreset.Medium
                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 58 : 55, getActivity());
                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 58 : 55, getActivity());
                    lastsavedAwarenessState = ANCAwarenessPreset.Medium;
                    break;
                case 3://ANCAwarenessPreset.High
                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 86 : 100, getActivity());
                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 86 : 100, getActivity());
                    lastsavedAwarenessState = ANCAwarenessPreset.High;
                    break;
            }
        ancController.initProgress(PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, getActivity()),
                PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, getActivity()), aaLevelingValue);
        PreferenceUtils.setInt(PreferenceKeys.AWARENESS, aaLevelingValue, getActivity());

        isRequestingLeftANC = true;
        isRequestingRightANC = true;
        ANCControlManager.getANCManager(getContext()).getLeftANCvalue(lightX);
        ANCControlManager.getANCManager(getContext()).getRightANCvalue(lightX);
    }
    private void sendMessageTo(int command, String arg1) {
        Message msg = new Message();
        msg.what = command;
        if (arg1 != null)
            msg.arg1 = valueOf(arg1);
        homeHandler.sendMessage(msg);
    }


    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        super.receivedResponse(command, values, status);
        Log.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values.size() <= 0){
            Log.d(TAG, "return, values size is " + values.size());
            return;
        }
        switch (command) {
            case AmCmds.CMD_ANC: {
                String value = values.iterator().next().getValue().toString();
                String tmp = "0";
                if (value.equalsIgnoreCase("true")
                        || value.equalsIgnoreCase("1")) {
                    tmp = "1";
                }
                sendMessageTo(MSG_ANC, tmp);
                break;
            }
            case AmCmds.CMD_AmbientLeveling: {
                sendMessageTo(MSG_AMBIENT_LEVEL, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_RawSteps: {
                sendMessageTo(MSG_RAW_STEP, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_BatteryLevel: {
                String bl = values.iterator().next().getValue().toString();
                //batteryValue = valueOf(bl);
                sendMessageTo(MSG_BATTERY, bl);
                break;
            }
            case AmCmds.CMD_Geq_Current_Preset: {
                sendMessageTo(MSG_CURRENT_PRESET, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_FirmwareVersion: {
                sendMessageTo(MSG_FIRMWARE_VERSION, null);
                break;
            }
            case AmCmds.CMD_FWInfo:
                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
                Log.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            case AmCmds.CMD_RawLeft:
                Log.d(TAG, "CMD_RawLeft =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_LEFT, values.iterator().next().getValue().toString());
                break;
            case AmCmds.CMD_RawRight:
                Log.d(TAG, "CMD_RawRight =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_RIGHT, values.iterator().next().getValue().toString());
                break;
        }

    }
}
