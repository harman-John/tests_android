package jblcontroller.hcs.soundx.base;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.req.CmdEqSettingsSet;

import java.util.ArrayList;

import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jblcontroller.hcs.soundx.ui.preference.PreferenceFragment;
import jblcontroller.hcs.soundx.utils.FontManager;

public class SoundXBaseFragment extends BaseFragment {
    private BasePresenter mPresenter;
    public static final String TAG = PreferenceFragment.class.getSimpleName();
    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
    protected ANCControlManager ancControlManager;
    private Handler mHandler = new Handler();
    private long readAppReturn;
    private long batteryValue;
    private boolean ancChangedViaUserClick = false;


    private final static int GET_ANC = 0;
    private final static int GET_AMBIENT = 1;
    private final static int GET_CURRENT_PRESET = 2;
    private final static int GET_BATTERY_LEVEL = 3;
    private final static int GET_RAW_STEP = 4;
    private final static int GET_FIRMWARE = 5;
    private final static int GET_FW_INFO = 7;
    private final static int UPDATE_ANC = 100;
    private final static int UPDATE_AMBIENT = 101;
    private final static int UPDATE_RAW_STEP = 102;
    private final static int UPDATE_BATTERY = 103;
    private final static int UPDATE_GEQ_CURRENT_PRE = 104;
    private final static int UPDATE_FIRMWARE_VERSION = 105;
    private final static int[] defaultEQ = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // ---- High eq settings for Demo ----------
    int[] a = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    int[] b = {6, 6, 6, 6, 6, 6, 6, 6, 6, 6};
    int[] c = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
    int[] d = {-5, -5, -5, -5, -5, -5, -5, -5, -5, -5};
    int[] e = {-10, -10, -10, -10, -10, -10, -10, -10, -10, -10};
    private ArrayList<int[]> eqList = new ArrayList<int[]>();

    //---------------------------------------------------------
//    private MyHandler myHandler = new MyHandler();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ancControlManager = ANCControlManager.getANCManager(getContext());
        eqList.add(defaultEQ);
        eqList.add(a);
        eqList.add(b);
        eqList.add(c);
        eqList.add(d);
        eqList.add(e);
    }

    public void setPresenter(BasePresenter presenter) {

        mPresenter = presenter;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    /**
     * Handle all changed when ANC changed to On
     */
    public void ANCOn() {
        PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, 1, getActivity());
//        remove_addPollingAgain();

    }

    /**
     * Handle all changed when ANC changed to off
     */
    public void ANCOff() {
        PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, 0, getActivity());
        if (AppUtils.is150NC(getActivity())) {
            awarenessPreset = ANCAwarenessPreset.None;
            PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, 0, getActivity());
            PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, 0, getActivity());
            presetVal = 0;
            lastsavedAwarenessState = null;
            PreferenceUtils.setInt(PreferenceKeys.AWARENESS, 0, getActivity());
        }
    }


    @Override
    public void onResume() {
        Logger.d(TAG, "ANCawarehome onResume()");
        super.onResume();
//        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
//        mHandler.removeCallbacks(readAppRunnable);
//        mHandler.postDelayed(readAppRunnable, timeInterval);
//        Logger.d(TAG, "===ON RESUME---" + DeviceConnectionManager.getInstance().getCurrentDevice());
//        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//            case Connected_BluetoothDevice:
//                if (AppUtils.is150NC(getActivity())) {
//                    if (PreferenceUtils.getBoolean(BaseFragment.RECEIVE_READY, getActivity())) {
//                        Logger.d(TAG, "onResume send msg GET_ANC");
//                        myHandler.sendEmptyMessage(GET_ANC);
//                        PreferenceUtils.setBoolean(BaseFragment.RECEIVE_READY, false, getActivity());
//                    } else {
//                        Logger.d(TAG, "onResume update");
//                        updateANC(PreferenceUtils.getInt(PreferenceKeys.ANC_VALUE, getActivity()) != 0);
//                        updateAmbientLevel(PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity()));
//                    }
//                }
//                break;
//        }
    }


    @Override
    public void onPause() {
        super.onPause();
//        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//            case Connected_USBDevice:
//                //Remove the readAppRunnable that reads the battery command.
//                mHandler.removeCallbacks(readAppRunnable);
//                mHandler.removeCallbacks(readANCRunnable);
//                break;
//            case Connected_BluetoothDevice:
//                mHandler.removeCallbacks(readAppRunnable);
//                break;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        try {
//            mHandler.removeCallbacks(readAppRunnable);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        /**
//         * safely remove handler
//         */
////        mHandler.removeCallbacks(ancToggleRunnable);
//        mHandler.removeCallbacks(awarenessRunnable);
//        mHandler = null;
    }


    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);
        switch (enumCommands) {

            case CMD_AMBIENT_LEVELING:
                int aaLevel = (int) objects[0];
                Logger.d(TAG, "on receive, cmd ambient: " + aaLevel);
                updateAmbientLevel(aaLevel);
                break;
            case CMD_ANC:
                if (objects[0] != null) {
                    int anc = (Integer) objects[0];
                    Logger.d(TAG, "on receive, cmd anc: " + anc);
                    updateANC(anc != 0);
                }
                break;
            case CMD_GEQ_CURRENT_PRESET:
                int currentPreset = (int) objects[0];
                Logger.d(TAG, "on receive, cmd eq current preset: " + currentPreset);
//                updateGraphicEQ((int) currentPreset);
                break;
            case CMD_BATTERY_LEVEL:
                batteryValue = (int) objects[0];
                Logger.d(TAG, "on receive, cmd battery level: " + batteryValue);
                updateBattery((int) batteryValue);
                break;
            case CMD_FIRMWARE_VERSION:
                if (objects[0] != null) {
                    PreferenceUtils.setString(AppUtils.getModelNumber(JBLApplication.getJBLApplicationContext()), PreferenceKeys.APP_VERSION, (String) objects[0], JBLApplication.getJBLApplicationContext());
                }
                break;
            case CMD_BootReadVersionFile: {
                if ((boolean) objects[1]) {
                    PreferenceUtils.setString(AppUtils.getModelNumber(getContext()), PreferenceKeys.RSRC_VERSION, (String) objects[0], JBLApplication.getJBLApplicationContext());
                }
                break;
            }
            case CMD_AppPushANCEnable:
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getANCValue();
                break;
            case CMD_AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getAmbientLeveling();
                break;
            }
            case CMD_ConfigProductName:
                String productName = (String) objects[0];
                Logger.d(TAG, "on receive, cmd config product name: " + productName);
                PreferenceUtils.setString(PreferenceKeys.PRODUCT, productName, JBLApplication.getJBLApplicationContext());
                break;

        }
    }

    private void updateANC(boolean ancResult) {
        Logger.d(TAG, "ANC result=" + ancResult);
//        Logger.d(TAG, "ancChangedViaUserClick: " + ancChangedViaUserClick + ",isAnimationRunning= " + control.isAnimationRunning());

        if (AppUtils.is150NC(getActivity())) {
            doANC(ancResult);
        } else if (!ancChangedViaUserClick) {
            doANC(ancResult);
        }
    }

    private void doANC(boolean ancResult) {
        Logger.d(TAG, "anc changed to " + ancResult);
        if (ancResult) {
            ANCOn();
        } else {
            ANCOff();
        }
    }

    private void updateAmbientLevel(int intValue) {
        boolean isRecPush = PreferenceUtils.getBoolean(BaseFragment.RECEIVEPUSH, getActivity());
        if (isRecPush || PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity()) != intValue) {
            if (isRecPush) {
                PreferenceUtils.setBoolean(BaseFragment.RECEIVEPUSH, false, getActivity());
            }
            boolean is150NC = AppUtils.is150NC(getActivity());
            Logger.d(TAG, "updateAmbientLevel: " + intValue + "," + PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity()) + ",is150NC=" + is150NC);
            switch (intValue) {
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
            PreferenceUtils.setBoolean(RECEIVEPUSH, false, getActivity());
            Logger.d(TAG, "AnimationNeeded");
        }

        PreferenceUtils.setInt(PreferenceKeys.AWARENESS, intValue, getActivity());
    }

    private void updateBattery(int value) {
        Logger.d(TAG, "battery value = " + value);
        PreferenceUtils.setInt(PreferenceKeys.BATTERY_VALUE, value, getActivity());
    }

//    private void updateGraphicEQ(int value) {
//        switch (value) {
//            case 0:
//                PreferenceUtils.setString(EQSettingManager.EQKeyNAME, getString(R.string.text_off), getActivity());
//                break;
//            case 1:
//                PreferenceUtils.setString(EQSettingManager.EQKeyNAME, getString(R.string.text_jazz), getActivity());
//                break;
//            case 2:
//                PreferenceUtils.setString(EQSettingManager.EQKeyNAME, getString(R.string.text_vocal), getActivity());
//                break;
//            case 3:
//                PreferenceUtils.setString(EQSettingManager.EQKeyNAME, getString(R.string.text_bass), getActivity());
//                break;
//        }
//    }




//    @Override
//    public void lightXAppWriteResult(LightX var1, Command var2, boolean var3) {
//        super.lightXAppWriteResult(var1, var2, var3);
//        if (var3) {
//            switch (var2) {
//                case AppANCAwarenessPreset:
//                    avneraGetters();
//                    break;
//            }
//        }
//    }
//
//    private void avneraGetters() {
////        if (lightX != null) {
//        ancControlManager.getLeftANCvalue(lightX);
//        ancControlManager.getRightANCvalue(lightX);
////        }
//    }

//    @Override
//    public void lightXIsInBootloader(LightX var1, boolean var2) {
//        super.lightXIsInBootloader(var1, var2);
//        Logger.d(TAG, "lightXIsInBootloader:" + var2);
//        if (var2) {
//            /** With OTA update **/
//            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//                case NONE:
//                    break;
//                case Connected_USBDevice:
//                    mHandler.removeCallbacks(readANCRunnable);
//                case Connected_BluetoothDevice:
//
//                    //Bug 108 : Update Firmware: Auto Update: Downloading screen display multiple times, if we don't press OK on "Charging Devices" popup.
//                    // FIX Start
//                    //FIX End
//                    try {
//                        Logger.d(TAG, "enter into SettingsUpdateDeviceFragment page");
//                        Bundle bundle = new Bundle();
//                        bundle.putBoolean("lightXIsInBootloader", true);
////                       getActivity().applyFragment(SettingsUpdateDeviceFragment.TAG, bundle);
//                    } catch (Exception e) {
//                        e.getMessage();
//                    }
//                    break;
//            }
//        } else {
//            remove_addPollingAgain();
//            readValues();
//        }
//    }


//    /**
//     * read values from <b>Headphones<b/> to display
//     */
//    private void readValues() {
//        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
//        if (lightX == null) {
//            return;
//        }
//        ancControlManager.getANCValue(lightX);
//        EQSettingManager.getEQSettingManager(getActivity()).getCurrentPreset(lightX);
//        ancControlManager.getAmbientLeveling(lightX);
//        /**
//         * This prevents reading excessive  command. These command only need to be read once until disconnection.
//         */
//        if (!PreferenceUtils.getBoolean(AppUtils.IsNeedToRefreshCommandRead, getActivity())) {
//            // Read these command only once between connect and disconnect.
//            if (lightX != null) {
//                lightX.readConfigModelNumber();
//                lightX.readConfigProductName();
//                //            lightX.readAppFirmwareVersion();
//                lightX.readBootVersionFileResource();
//            }
//
//            Logger.d(TAG, "getFirmwareVersion 1");
//            ancControlManager.getFirmwareVersion(lightX);
//            PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, true, getActivity()); // set RSRC,APP version for Checking version at home.
//        }
//        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//            case Connected_BluetoothDevice:
//                ancControlManager.getBatterLeverl(lightX);
////                lightX.readApp(Command.AppBatteryLevel);
//                break;
//        }
//        mHandler.removeCallbacks(readAppRunnable);
//        mHandler.postDelayed(readAppRunnable, timeInterval);
//    }
//
//    @Override
//    public void isLightXintialize() {
//        super.isLightXintialize();
//    }
//
//    public static int timeDuration = 10;
//
//    @Override
//    public void headPhoneStatus(boolean isConnected) {
//        Logger.d(TAG, "headPhoneStatus");
//        super.headPhoneStatus(isConnected);
//    }

    // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
    //New varibale presetVal defined to draw the seekbar progress smoothly.
    int presetVal = -1;


    /**
     * code to spamming of click from user and to protect headphone from abuse
     */
    ANCAwarenessPreset awarenessPreset, lastsavedAwarenessState;

    /**
     * Removes the readAppRunnable from the handler used for POLLING (CASE USB )
     */
//    public void remove_addPollingAgain() {
//        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//            case Connected_USBDevice:
//                mHandler.removeCallbacks(readANCRunnable);
//                mHandler.postDelayed(readANCRunnable, 2000);
//                break;
//        }
//    }
//
//
//    @Override
//    public void receivedAdminEvent(AdminEvent event, Object value) {
//        Logger.d(TAG, "receivedAdminEvent event=" + event);
//        switch (event) {
//            case AccessoryAppeared:
//            case AccessoryReady: {
//                myHandler.removeMessages(GET_ANC);
//                myHandler.sendEmptyMessageDelayed(GET_ANC, 200);
//                break;
//            }
//            case AccessoryUnpaired:
//            case AccessoryVanished:
//            case AccessoryDisconnected: {
//                PreferenceUtils.setInt(PreferenceKeys.AWARENESS, -1, getActivity());
//                break;
//            }
//        }
//    }
//
//
//    private class MyHandler extends Handler {
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case GET_ANC: {
//                    ancControlManager.getANCValue(lightX);
//                    myHandler.sendEmptyMessageDelayed(GET_FIRMWARE, 200);
//                    break;
//                }
//                case GET_FIRMWARE: {
//                    if (!PreferenceUtils.getBoolean(AppUtils.IsNeedToRefreshCommandRead, getActivity())) {
//                        Logger.d(TAG, "handleMessage getFirmwareVersion 2");
//                        ancControlManager.getFirmwareVersion(lightX);
//                        PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, true, getActivity()); // set RSRC,APP version for Checking version at home.
//                    }
//                    myHandler.sendEmptyMessageDelayed(GET_RAW_STEP, 200);
//                    break;
//                }
//                case GET_RAW_STEP: {
//                    ancControlManager.getRawStepsByCmd(lightX);
//                    myHandler.sendEmptyMessageDelayed(GET_CURRENT_PRESET, 200);
//                    break;
//                }
//                case GET_CURRENT_PRESET: {
//                    EQSettingManager.getEQSettingManager(getActivity()).getCurrentPreset(lightX);
//                    myHandler.sendEmptyMessageDelayed(GET_BATTERY_LEVEL, 200);
//                    break;
//                }
//                case GET_BATTERY_LEVEL: {
//                    ancControlManager.getBatterLeverl(lightX);
//                    myHandler.sendEmptyMessageDelayed(GET_AMBIENT, 200);
//                    break;
//                }
//                case GET_AMBIENT: {
//                    ancControlManager.getAmbientLeveling(lightX);
//                    //myHandler.sendEmptyMessageDelayed(GET_FW_INFO, 200);
//                    break;
//                }
//                case GET_FW_INFO: {
//                    ancControlManager.getFirmwareInfo(lightX);
//                    break;
//                }
////                case GET_LR:{
////                    avneraGetters();
////                    break;
////                }
//                //-------------update UI.----------------------
//                case UPDATE_ANC: {
//                    updateANC(msg.arg1 == 1);
//                    break;
//                }
//                case UPDATE_AMBIENT: {
//                    updateAmbientLevel(AppUtils.levelTransfer(msg.arg1));
//                    break;
//                }
//                case UPDATE_RAW_STEP: {
//                    ancControlManager.setRawSteps(msg.arg1);
//                    break;
//                }
//                case UPDATE_BATTERY: {
//                    updateBattery((int) batteryValue);
//                    break;
//                }
//                case UPDATE_GEQ_CURRENT_PRE: {
//                    updateGraphicEQ(msg.arg1);
//                    break;
//                }
//                case UPDATE_FIRMWARE_VERSION: {
//                    updateFirmwareVersion();
//                    break;
//                }
//            }
//        }
//    }
//
//    private void sendMesageTo(int command, String arg1) {
//        Message msg = new Message();
//        msg.what = command;
//        if (arg1 != null)
//            msg.arg1 = valueOf(arg1);
//        myHandler.sendMessage(msg);
//    }
//
//    @Override
//    public void receivedResponse(final String command, final ArrayList<responseResult> values, Status status) {
//        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
//        switch (command) {
//            case Commands.CMD_ANC: {
//                String value = values.iterator().next().getValue().toString();
//                String tmp = "0";
//                if (value.equalsIgnoreCase("true")
//                        || value.equalsIgnoreCase("1")) {
//                    tmp = "1";
//                }
//                sendMesageTo(UPDATE_ANC, tmp);
//                break;
//            }
//            case Commands.CMD_AmbientLeveling: {
//                sendMesageTo(UPDATE_AMBIENT, values.iterator().next().getValue().toString());
//                break;
//            }
//            case Commands.CMD_RawSteps: {
//                sendMesageTo(UPDATE_RAW_STEP, values.iterator().next().getValue().toString());
//                break;
//            }
//            case Commands.CMD_BatteryLevel: {
//                String bl = values.iterator().next().getValue().toString();
//                batteryValue = valueOf(bl);
//                sendMesageTo(UPDATE_BATTERY, bl);
//                break;
//            }
//            case Commands.CMD_Geq_Current_Preset: {
//                sendMesageTo(UPDATE_GEQ_CURRENT_PRE, values.iterator().next().getValue().toString());
//                break;
//            }
//            case Commands.CMD_FirmwareVersion: {
//                sendMesageTo(UPDATE_FIRMWARE_VERSION, null);
//                break;
//            }
////            case Commands.CMD_FWInfo:
////                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
////                Logger.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
////                break;
//        }
//    }
//
//    @Override
//    public void receivedStatus(StatusEvent name, Object value) {
//
//    }
//
//    @Override
//    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {
//        Logger.d(TAG, "receivedPushNotification command=" + command + ",values=" + values + ",status=" + status);
//        switch (command) {
//            case Commands.CMD_ANCNotification: {
//                sendMesageTo(UPDATE_ANC, values.iterator().next().getValue().toString());
//                break;
//            }
//            case Commands.CMD_AmbientLevelingNotification: {
//                PreferenceUtils.setBoolean(BaseFragment.RECEIVEPUSH, true, getActivity());
//                sendMesageTo(UPDATE_AMBIENT, values.iterator().next().getValue().toString());
//                break;
//            }
//        }
//    }
//
//    private void updateFirmwareVersion() {
//        AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager(getActivity()).getAudioManager().getAccessoryStatus();
//        PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), getActivity());
//        AppUtils.setModelNumber(getActivity(), accessoryInfo.getModelNumber());
//        String version = accessoryInfo.getFirmwareRev();
//        if (version.length() >= 5) {
//            Logger.d(TAG, "currentVersion : " + version);
//            PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), AppUtils.APP_VERSION, version, getActivity());
//        }
//        String hardVersion = accessoryInfo.getHardwareRev();
//        if (hardVersion.length() >= 5) {
//            Logger.d(TAG, "hardVersion : " + hardVersion);
////                PreferenceUtils.setString(AppUtils.RSRC_VERSION, fwVersion, getActivity());
//            PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), AppUtils.RSRC_VERSION, hardVersion, getActivity());
//        }
////        AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(hardVersion);
//    }
//
//
//    // --------------------  Important runnables --------------------------------------------------------------------
//    private Runnable readAppRunnable = new Runnable() {
//        @Override
//        public void run() {
//            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//                case Connected_BluetoothDevice:
//                    //Battery read command will only work for BLUETOOTH headset.
////                        lightX.readApp(Command.AppBatteryLevel);
//                    ancControlManager.getBatterLeverl(lightX);
//                    mHandler.postDelayed(readAppRunnable, 60 * 1000);
//                    break;
//            }
//        }
//    };
//
//    private Runnable readANCRunnable = new Runnable() {
//        @Override
//        public void run() {
//            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//                case Connected_USBDevice:
//                    ancControlManager.getANCValue(lightX);
//                    ancControlManager.getAmbientLeveling(lightX);
//                    mHandler.postDelayed(readANCRunnable, pollingTime);
//                    break;
//                case Connected_BluetoothDevice:
//                case NONE:
//                    mHandler.removeCallbacks(readANCRunnable);
//                    break;
//            }
//        }
//    };
//
//
//    Runnable awarenessRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (awarenessPreset == null)
//                return;
//            else if (lastsavedAwarenessState != null && lastsavedAwarenessState == awarenessPreset
//                    && PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity()) == awarenessPreset.value()) {
//                return;
//            }
//            boolean is150NC = AppUtils.is150NC(getActivity());
//            Logger.d(TAG, "lastSavedAwarenessState:" + lastsavedAwarenessState
//                    + " AWARENESS = " + PreferenceUtils.getInt(PreferenceKeys.AWARENESS, getActivity())
//                    + " awarenessPreset =" + awarenessPreset
//                    + " is150NC = " + is150NC);
//            lastsavedAwarenessState = awarenessPreset;
//            switch (awarenessPreset) {
//                case None:
////                    if (lightX != null) {
//                    ancControlManager.setAmbientLeveling(lightX, ANCAwarenessPreset.None);
////                        lightX.writeAppANCAwarenessPreset(ANCAwarenessPreset.None);
//                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, 0, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, 0, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.AWARENESS, 0, getActivity());
//                    // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
//                    //set the animation flag to TRUE and the presetVal corresponding to the Awareness
//                    presetVal = 0;
////                    }
////                    AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelOff();
//                    Logger.debug("Presets", "None sent");
//                    break;
//                case Low:
////                    if (lightX != null) {
////                        lightX.writeAppANCAwarenessPreset(ANCAwarenessPreset.Low);
//                    ancControlManager.setAmbientLeveling(lightX, ANCAwarenessPreset.Low);
//                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 28 : 25, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 28 : 25, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.AWARENESS, 1, getActivity());
//                    // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
//                    //set the animation flag to TRUE and the presetVal corresponding to the Awareness
//                    presetVal = 1;
////                    }
////                    AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelLow();
//                    Logger.debug("Presets", "Low sent");
//                    break;
//                case Medium:
////                    if (lightX != null) {
////                        lightX.writeAppANCAwarenessPreset(ANCAwarenessPreset.Medium);
//                    ancControlManager.setAmbientLeveling(lightX, ANCAwarenessPreset.Medium);
//                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 58 : 55, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 58 : 55, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.AWARENESS, 2, getActivity());
//                    presetVal = 2;
////                    }
////                    AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelMedium();
//                    Logger.debug("Presets", "Medium sent");
//                    break;
//                case High:
////                    if (lightX != null) {
////                        lightX.writeAppANCAwarenessPreset(ANCAwarenessPreset.High);
//                    ancControlManager.setAmbientLeveling(lightX, ANCAwarenessPreset.High);
//                    PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 86 : 100, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 86 : 100, getActivity());
//                    PreferenceUtils.setInt(PreferenceKeys.AWARENESS, 3, getActivity());
//                    presetVal = 3;
////                    }
////                    AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelHigh();
//                    Logger.debug("Presets", "High sent");
//                    break;
//            }
//        }
//    };

    public void applyPresetEffect(double mPreferredBass, double mPreferredTreble) {
        try {
//            EQSettingManager eqSettingManager = EQSettingManager.getEQSettingManager(getActivity());
//            EQModel eqModel = new EQModel();
//            eqModel.setEqName("Custom");
//            eqModel.setHigh1((int) mPreferredBass);
//            eqModel.setHigh2(0);
//            eqModel.setHigh3(0);
//            eqModel.setMedium1(0);
//            eqModel.setMedium2(0);
//            eqModel.setMedium3(0);
//            eqModel.setMedium4(0);
//            eqModel.setLow1((int) mPreferredTreble);
//            eqModel.setLow2(0);
//            eqModel.setLow3(0);
//
//            eqSettingManager.applyPresetsWithBand(GraphicEQPreset.User, eqSettingManager.getBandFromEQModel(eqModel), lightX);
            int[] gains = {(int) mPreferredTreble, 0, 0, 0, 0, 0, 0, (int) mPreferredBass, 0, 0};
            CmdEqSettingsSet cmdEqSettingsSet = getEqSettingCmd(gains);
            LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).mac, cmdEqSettingsSet);
        } catch (Exception e) {

        }
    }

    public void applyPresetEffect(double mPreferredBass, double mPreferredTreble, int pos) {
        try {
            CmdEqSettingsSet cmdEqSettingsSet = getEqSettingCmd(eqList.get(pos));
            LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).mac, cmdEqSettingsSet);
        } catch (Exception e) {

        }
    }


    //@TODO- need to get default values from headset
    public void setDefault() {
        try {
//            EQSettingManager eqSettingManager = EQSettingManager.getEQSettingManager(getActivity());
//            EQModel eqModel = new EQModel();
//            eqModel.setEqName("Custom");
//            eqModel.setHigh1(0);
//            eqModel.setHigh2(0);
//            eqModel.setHigh3(0);
//            eqModel.setMedium1(0);
//            eqModel.setMedium2(0);
//            eqModel.setMedium3(0);
//            eqModel.setMedium4(0);
//            eqModel.setLow1(0);
//            eqModel.setLow2(0);
//            eqModel.setLow3(0);
//            eqSettingManager.applyPresetsWithBand(GraphicEQPreset.User, eqSettingManager.getBandFromEQModel(eqModel), lightX);

            CmdEqSettingsSet cmdEqSettingsSet = getEqSettingCmd(defaultEQ);
            LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).mac, cmdEqSettingsSet);
        } catch (Exception e) {

        }
    }

    private CmdEqSettingsSet getEqSettingCmd(int[] gains){
        int[] fc = new int[]{32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
        Band[] bands = new Band[gains.length];
        for (int i = 0; i < gains.length; i++) {
            bands[i] = new Band(1, gains[i], fc[i], 1.0f);
        }
        CmdEqSettingsSet cmdEqSettingsSet = new CmdEqSettingsSet(4,
                EnumEqCategory.GRAPHIC_EQ, 0,48000,0,0,bands);

        float calib = DashboardActivity.getDashboardActivity().getCalib(cmdEqSettingsSet);
        cmdEqSettingsSet.setCalib(calib);
        return cmdEqSettingsSet;
    }

    //------------------------ Font methods
    public Typeface getBoldFont() {

        return FontManager.getInstance(getActivity()).getBoldFont();
    }

    public Typeface getRegularFont() {

        return FontManager.getInstance(getActivity()).getRegularFont();
    }

    public Typeface getMediumFont() {

        return FontManager.getInstance(getActivity()).getMediumFont();
    }

}
