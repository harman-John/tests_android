package jbl.stc.com.fragment;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

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
    public static final String TAG = HomeFragment.class.getSimpleName();
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
    private final static int MSG_SEND_CMD_GET_FIRMWARE = 7;
    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
    private ProgressBar progressBarBattery;
    private TextView textViewBattery;
    private TextView textViewCurrentEQ;
    private TextView textViewDeviceName;
    private PopupWindow popupWindow;
    private ImageView imageViewDevice;
    private CheckBox checkBoxNoiseCancel;
    private LinearLayout linearLayoutBattery;
    private LightX lightX;
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
        textViewDeviceName = view.findViewById(R.id.text_view_home_device_name);
        imageViewDevice = view.findViewById(R.id.deviceImageView);
        view.findViewById(R.id.eqSwitchLayout);
        view.findViewById(R.id.eqInfoLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.eqInfoLayout).setOnClickListener(this);
        checkBoxNoiseCancel = view.findViewById(R.id.image_view_noise_cancel);
        checkBoxNoiseCancel.setOnClickListener(this);
        textViewCurrentEQ = view.findViewById(R.id.eqNameText);
        view.findViewById(R.id.titleEqText);
        view.findViewById(R.id.eqDividerView);
        linearLayoutBattery = view.findViewById(R.id.linear_layout_battery);
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
        updateDeviceName();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
        Log.d(TAG, "onResume" + DeviceConnectionManager.getInstance().getCurrentDevice());
        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
            case NONE:
                break;
            case Connected_USBDevice:
                linearLayoutBattery.setVisibility(View.INVISIBLE);
                break;
            case Connected_BluetoothDevice:
                linearLayoutBattery.setVisibility(View.VISIBLE);
                ANCControlManager.getANCManager(getActivity()).getBatterLeverl(lightX);
                homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL,timeInterval);
                break;
        }
        getDeviceInfo();
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
                    ANCControlManager.getANCManager(getActivity()).setANCValue(lightX,true);
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
        final ANCController ancController = popupWindow_view.findViewById(R.id.circularSeekBar);
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
        popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
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
            }
        });

    }

    @Override
    public void onMedium() {
        //on AA medium checked
    }

    @Override
    public void onLow() {
       //on AA low checked
    }

    @Override
    public void onHigh() {
      //on AA high checked
    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {
       //controller progress
    }


    @Override
    public void onStartTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {

    }


    private void getDeviceInfo(){

        ANCControlManager.getANCManager(getContext()).getANCValue(lightX);
        updateFirmwareVersion();
        ANCControlManager.getANCManager(getContext()).getCurrentPreset(lightX);

        if (lightX != null) {
            Log.i(TAG,"getDeviceInfo");
            lightX.readConfigModelNumber();
            lightX.readConfigProductName();
            lightX.readBootVersionFileResource();
        }
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
                    break;
                }
                case MSG_CURRENT_PRESET:{
                    updateCurrentEQ(msg.arg1);
                    break;
                }
                case MSG_RAW_STEP:{
                    break;
                }
                case MSG_SEND_CMD_GET_FIRMWARE:{
                    ANCControlManager.getANCManager(getActivity()).getFirmwareVersion(lightX);
                    break;
                }
            }
        }
    }

    private void updateDeviceName(){
        textViewDeviceName.setText(PreferenceUtils.getString(PreferenceKeys.MODEL,mContext,""));
    }

    private void updateANC(boolean onOff){
        checkBoxNoiseCancel.setChecked(onOff);
    }

    private void updateCurrentEQ(int index){
        switch (index) {
            case 0:{
                textViewCurrentEQ.setText(getString(R.string.off));
                break;
            }
            case 1:{
                textViewCurrentEQ.setText(getString(R.string.jazz));
                break;
            }
            case 2:{
                textViewCurrentEQ.setText(getString(R.string.vocal));
                break;
            }
            case 3:{
                textViewCurrentEQ.setText(getString(R.string.bass));
                break;
            }
            case 4:{
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getActivity(), null);
                if (name != null) {
                    textViewCurrentEQ.setText(name);
                    if (textViewCurrentEQ.getText().length() >= JBLConstant.MAX_MARQUEE_LEN) {
                        textViewCurrentEQ.setSelected(true);
                        textViewCurrentEQ.setMarqueeRepeatLimit(-1);
                    }
                }else{
                    textViewCurrentEQ.setText(getString(R.string.custom_eq));
                }
                break;
            }
            default:
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getActivity(), null);
                textViewCurrentEQ.setText( name != null ? name :getString(R.string.off));
                break;
        }
    }

    private void updateBattery(int value) {
        Log.d(TAG, "battery value = " + value);
        if (getActivity() == null){
            return;
        }
        PreferenceUtils.setInt(PreferenceKeys.BATTERY_VALUE, value, getActivity());
        if (value == 255) {
            progressBarBattery.setProgress(100);
            textViewBattery.setText("100%");
        } else {
            progressBarBattery.setProgress(value);
            textViewBattery.setText(String.format("%s%%", String.valueOf(value)));
        }
    }

    private void updateUSBBattery(){
        progressBarBattery.setVisibility(View.INVISIBLE);
        textViewBattery.setVisibility(View.INVISIBLE);
        textViewBattery.setVisibility(View.INVISIBLE);
    }

    private void updateFirmwareVersion() {
        audioManager am =  AvneraManager.getAvenraManager(getActivity()).getAudioManager();
        if (am == null){
            Log.i(TAG, "am is null, not 150NC device");
            return;
        }
        AccessoryInfo accessoryInfo = am.getAccessoryStatus();
        PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), getActivity());
        AppUtils.setModelNumber(getActivity(), accessoryInfo.getModelNumber());
        Log.d(TAG, "modelName : " + accessoryInfo.getModelNumber());
        updateDeviceName();
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
            case AmCmds.CMD_FWInfo: {
                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
                Log.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            }
        }

    }

    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] var4) {
        super.lightXAppReadResult(var1, command, success, var4);
        if (success) {
            switch (command) {
                case App_0xB3:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().setIsCalibrationComplete(Utility.getBoolean(var4, 0));
                    break;
                case AppANCAwarenessPreset:
                    Log.d(TAG, "AppANCAwarenessPreset");
//                    int intValue = com.avnera.smartdigitalheadset.Utility.getInt(var4, 0);
//                    update(intValue);
                    break;
                case AppANCEnable:
                    if (var4 != null) {
                        boolean ancResult = Utility.getBoolean(var4, 0);
                        updateANC(ancResult);
                    }
                    break;
                case AppAwarenessRawLeft:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppAwarenessRawRight:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppAwarenessRawSteps:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppGraphicEQCurrentPreset:
                    long currentPreset = Utility.getUnsignedInt(var4, 0);
                    Log.d(TAG, command + " is " + currentPreset);
                    updateCurrentEQ((int) currentPreset);
                    break;
                case AppGraphicEQBandFreq:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppBatteryLevel:
                    long batteryValue = Utility.getUnsignedInt(var4, 0);
                    Log.d(TAG, command + " is " + batteryValue);
                    updateBattery((int) batteryValue);
                    break;
                case AppFirmwareVersion:
                    int major = var4[0];
                    int minor = var4[1];
                    int revision = var4[2];
                    Log.d(TAG, "AppCurrVersion = " + major + "." + minor + "." + revision);
                    PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.APP_VERSION, major + "." + minor + "." + revision, getActivity());
                    break;
            }
        } else {
            switch (command) {
                case AppGraphicEQCurrentPreset:
                    updateCurrentEQ(8);
                    break;
                case AppANCEnable:
                    boolean anc = Utility.getBoolean(var4, 0);
                    updateANC(anc);
                    break;
            }
        }
    }

    @Override
    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean success, final int i, final byte[] buffer) {
        Log.d(TAG, "lightXReadBootResult command is " + command + " result is " + success);
        if (getActivity() == null){
            Log.d(TAG,"Activity is null");
            return;
        }
        if (!isAdded()){
            Log.d(TAG,"This fragment is null");
            return;
        }
        if (success) {
            switch (command) {
                case BootReadVersionFile: {
                    int result[] = AppUtils.parseVersionFromASCIIbuffer(buffer);
                    int major = result[0];
                    int minor = result[1];
                    int revision = result[2];
                    String rsrcSavedVersion = major + "." + minor + "." + revision;
                    PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, rsrcSavedVersion, getActivity());
                    Log.d(TAG, "rsrcSavedVersion=" + rsrcSavedVersion);
                    DashboardActivity.getDashboardActivity().startCheckingIfUpdateIsAvailable(); /** Now start checking for update to show red bubble on setting icon*/
                }
                break;
            }
        } else {
            DashboardActivity.getDashboardActivity().startCheckingIfUpdateIsAvailable();
        }
    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command command, byte[] var4) {
        super.lightXAppReceivedPush(var1, command, var4);
        Log.d(TAG, "lightXAppReceivedPush command is " + command);
        switch (command) {
            case AppPushANCEnable:
                ANCControlManager.getANCManager(mContext).getANCValue(lightX);
                break;
            case AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(mContext).getAmbientLeveling(lightX);
            }
            break;
        }
    }

    @Override
    public void lightXReadConfigResult(LightX var1, Command command, boolean success, String var4) {
        super.lightXReadConfigResult(var1, command, success, var4);
        Log.i(TAG,"lightXReadConfigResult");
        if (success) {
            switch (command) {
                case ConfigProductName:
                    PreferenceUtils.setString(PreferenceKeys.PRODUCT, var4, getActivity());
                    break;
                case ConfigModelNumber:
                    AppUtils.setModelNumber(getActivity(), var4);
                    updateDeviceName();
                    homeHandler.sendEmptyMessageDelayed(MSG_SEND_CMD_GET_FIRMWARE,200);
                    switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                        case Connected_USBDevice:
                            break;
                        case Connected_BluetoothDevice:
                            try {
                                if (AppUtils.is100(getActivity())
                                        || AppUtils.is150NC(getContext())) {
//                                    getAppActivity().rightBtnText.setVisibility(View.INVISIBLE);
//                                    imgHPicon.setImageResource(R.drawable.aware_icon_100);

                                } else {
//                                    getAppActivity().rightBtnText.setVisibility(View.VISIBLE);
//                                    getAppActivity().rightBtnText.setText(Html.fromHtml(getResources().getString(R.string.TrueNote)));
//                                    getAppActivity().rightBtnText.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            getAppActivity().startCalibration();
//                                        }
//                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public void lightXAppWriteResult(LightX var1, Command var2, boolean var3) {
        super.lightXAppWriteResult(var1, var2, var3);
        Log.i(TAG,"lightXAppWriteResult");
        if (var3) {
            switch (var2) {
                case App_0xB3:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().setIsCalibrationComplete(true);
                    break;
                case AppANCAwarenessPreset:
//                    avneraGetters();
                    break;
            }
        } else {
            switch (var2) {
                case App_0xB2:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().calibrationFailed();
                    break;
            }
        }
    }

    @Override
    public void lightXIsInBootloader(LightX var1, boolean isInBootloaderMode) {
        super.lightXIsInBootloader(var1, isInBootloaderMode);
        Log.d(TAG, "lightXIsInBootloader =" + isInBootloaderMode);
        if (isInBootloaderMode) {
            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                case NONE:
                    break;
                case Connected_USBDevice:
                case Connected_BluetoothDevice:
                    if (getActivity().getSupportFragmentManager().findFragmentByTag(OTAFragment.TAG) != null) {
                        Log.d(TAG, "OTA is not finish, so call OTAFragment to continue");
                        return;
                    }
                    try {
                        Log.d(TAG, "Enter OTAFragment page");
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("lightXIsInBootloader", true);
                        switchFragment(new OTAFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    break;
            }
        }
    }

}
