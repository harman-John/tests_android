package jbl.stc.com.manager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.harman.bluetooth.constants.EnumOtaState;
import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.listeners.BleListener;
import com.harman.bluetooth.req.CmdAASet;
import com.harman.bluetooth.req.CmdAncSet;
import com.harman.bluetooth.req.CmdAppAckSet;
import com.harman.bluetooth.req.CmdAppByeSet;
import com.harman.bluetooth.req.CmdCurrEq;
import com.harman.bluetooth.req.CmdDevStatus;
import com.harman.bluetooth.req.CmdEqPresetSet;
import com.harman.bluetooth.req.CmdEqSettingsSet;
import com.harman.bluetooth.req.CmdHeader;
import com.harman.bluetooth.ret.RetCurrentEQ;
import com.harman.bluetooth.ret.RetDevStatus;
import com.harman.bluetooth.ret.RetDeviceInfo;
import com.harman.bluetooth.ret.RetResponse;
import com.harman.bluetooth.utils.ArrayUtil;

import java.util.HashSet;
import java.util.Set;

import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.constant.LeStatus;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.OnConnectStatusListener;
import jbl.stc.com.listener.OnRetListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.scan.LeLollipopScanner;
import jbl.stc.com.scan.ScanListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.SharePreferenceUtil;

public class LiveManager implements ScanListener, BleListener {

    private final static String TAG = LiveManager.class.getSimpleName();
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private LeLollipopScanner leLollipopScanner;
    private Activity mContext;
    private LeHandler leHandler = new LeHandler(Looper.getMainLooper());
    private final static int MSG_START_SCAN = 0;
    private final static int MSG_DISCONNECT = 1;
    private final static int MSG_CONNECT_TIME_OUT = 2;
    private LeStatus leStatus = LeStatus.START_SCAN;

    private static class InstanceHolder {
        public static final LiveManager instance = new LiveManager();
    }

    public static LiveManager getInstance() {
        return LiveManager.InstanceHolder.instance;
    }

    private LiveManager() {
        if (mOnConnectStatusListeners == null) {
            mOnConnectStatusListeners = new HashSet<>();
        }
    }

    public void checkPermission(Activity context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                ActivityCompat.requestPermissions(mContext,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            } else {
                if (isGPSEnabled()) {
                    Logger.i(TAG, " check permission, then start ble scan");
                    leHandler.sendEmptyMessage(MSG_START_SCAN);
                } else {
                    openGpsSetting(PERMISSION_REQUEST_LOCATION);
                }
            }
        }
    }

    private LocationManager manager;

    private boolean isGPSEnabled() {
        if (manager == null) {
            manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isGPSEnabled()) {
                        Logger.i(TAG, " on request permission result, start ble scan");
                        leHandler.removeMessages(MSG_START_SCAN);
                        leHandler.sendEmptyMessage(MSG_START_SCAN);
                    } else {
                        openGpsSetting(requestCode);
                    }
                }
                break;
            }
        }
    }

    private void openGpsSetting(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mContext.startActivityForResult(intent, requestCode);
    }

    private void startBleScan() {
        if (leLollipopScanner == null) {
            leLollipopScanner = new LeLollipopScanner(mContext);
        }
        leStatus = LeStatus.START_SCAN;
        Logger.i(TAG, "start ble scan");
        leLollipopScanner.startScan(this);
    }


    private Set<MyDevice> devicesSet = new HashSet<>();

    //For scan callbacks
    @Override
    public void onFound(BluetoothDevice device, String pid) {
//        Logger.d(TAG,"on found device, stop scan");
//        leLollipopScanner.stopScan();
        String key = pid + "-" + device.getAddress();
        devicesSet.clear();
//        if (pid.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT_PID)) {
        Logger.d(TAG, "on found key = " + key+", name = " + device.getName());
        MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(device.getAddress());
        if (myDevice == null) {
            myDevice = AppUtils.getMyDevice(device.getName(), ConnectStatus.A2DP_HALF_CONNECTED, pid, device.getAddress());
            devicesSet.add(myDevice);
            SharePreferenceUtil.saveSet(mContext, SharePreferenceUtil.PRODUCT_DEVICE_LIST_PER_KEY, devicesSet);
        }
        if (myDevice == null) {
            Logger.e(TAG, "on found, my device is null");
            return;
        }
        if (leStatus == LeStatus.CONNECTING || leStatus == LeStatus.CONNECTED) {
            Logger.e(TAG, "on found, status is connecting or connected");
            return;
        }
        SharePreferenceUtil.saveSet(mContext, SharePreferenceUtil.PRODUCT_DEVICE_LIST_PER_KEY, devicesSet);
        ProductListManager.getInstance().checkHalfConnectDevice(devicesSet);
        if (!DeviceManager.getInstance(mContext).isConnected()) {
            BesEngine.getInstance().addListener(this);
            //if (device.getAddress().equals("12:34:56:09:23:56")) {
            leStatus = LeStatus.CONNECTING;
            boolean result = BesEngine.getInstance().connect(mContext, device);
            Logger.d(TAG, "on found, connect result = " + result);
            // }
        }
//        }
    }

    private final Object mLock = new Object();
    private OnRetListener mOnRetListener;

    public void setOnRetListener(OnRetListener listener) {
        synchronized (mLock) {
            mOnRetListener = listener;
        }
    }


    private Set<OnConnectStatusListener> mOnConnectStatusListeners;

    public void setOnConnectStatusListener(OnConnectStatusListener onConnectStatusListener) {
        synchronized (mLock) {
            if (!mOnConnectStatusListeners.contains(onConnectStatusListener)) {
                mOnConnectStatusListeners.add(onConnectStatusListener);
            }
        }
    }

    public void removeOnConnectStatusListener(OnConnectStatusListener onConnectStatusListener) {
        synchronized (mLock) {
            mOnConnectStatusListeners.remove(onConnectStatusListener);
        }
    }

    private boolean isConnecting() {
        return leStatus == LeStatus.CONNECTING || leStatus == LeStatus.CONNECTED;
    }

    public boolean isConnected() {
        return BesEngine.getInstance().isConnected();
    }

    @Override
    public void onScanStart() {
        Logger.d(TAG, "on scan start");
    }

    @Override
    public void onScanFinish() {
        Logger.d(TAG, "on scan finished");
    }

    //For connection callbacks
    @Override
    public void onLeConnectStatus(final BluetoothDevice bluetoothDevice, final boolean isConnected) {
        Logger.d(TAG, "on bes connect status, isConnecting = " + isConnected);
        synchronized (mLock) {
            if (DeviceManager.getInstance(mContext).isConnected()) {
                Logger.i(TAG, "bes connected, but other device is already connected");
                leHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 200);
                return;
            }

            if (!isByConnectedDevice(bluetoothDevice.getAddress())) {
                Logger.d(TAG, "on bes connect status, not by connected device, throw connection callback away");
                return;
            }
            MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(bluetoothDevice.getAddress());
            if (isConnected) {
                Logger.d(TAG, "on bes connect status, connected, stop scan");
                leLollipopScanner.stopScan();
                leStatus = LeStatus.CONNECTED;
                myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
                AppUtils.setModelNumber(JBLApplication.getJBLApplicationContext(), JBLConstant.DEVICE_LIVE_400BT);
            } else {
                Logger.d(TAG, "on bes connect status, disconnected");
                leStatus = LeStatus.DISCONNECTED;
                myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
            }
            final String mac = myDevice.mac;
            final int status = myDevice.connectStatus;
            Logger.d(TAG, "on bes connect status, my device is " + myDevice.deviceKey);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProductListManager.getInstance().checkConnectStatus(mac, status);
                    for (OnConnectStatusListener listener : mOnConnectStatusListeners) {
                        listener.onConnectStatus(isConnected);
                    }
                }
            });
        }
    }

    @Override
    public void onMtuChanged(BluetoothDevice bluetoothDevice, int status, int mtu) {
        Logger.d(TAG, "on mtu changed");
    }

    @Override
    public void onRetReceived(BluetoothDevice bluetoothDevice, RetResponse retResponse) {
        if (bluetoothDevice == null || retResponse == null) {
            Logger.d(TAG, "on ret received, bt device is null or retResponse is null");
            return;
        }

        if (!isByConnectedDevice(bluetoothDevice.getAddress())) {
            Logger.d(TAG, "on ret received, not by connected device, throw receive msg callback away");
            return;
        }

        Logger.d(TAG, "on ret received, mac = " + bluetoothDevice.getAddress() + " , cmd id = " + retResponse.enumCmdId + "");
        switch (retResponse.enumCmdId) {
            case RET_DEV_ACK:
            case RET_DEV_BYE:
            case RET_DEV_FIN_ACK:
                break;
            case RET_DEV_INFO:
                RetDeviceInfo retDeviceInfo = (RetDeviceInfo) retResponse.object;
                notifyUiUpdate(EnumCommands.CMD_ConfigProductName, retDeviceInfo.deviceName);
                notifyUiUpdate(EnumCommands.CMD_FIRMWARE_VERSION, retDeviceInfo.firmwareVersion, true);
                notifyUiUpdate(EnumCommands.CMD_BATTERY_LEVEL, retDeviceInfo.retBatteryStatus.percent, retDeviceInfo.retBatteryStatus.charging);
                break;
            case RET_DEV_STATUS:
                RetDevStatus retDevStatus = (RetDevStatus) retResponse.object;
                Logger.d(TAG, "on bes received, status type = " + retDevStatus.enumDeviceStatusType);
                switch (retDevStatus.enumDeviceStatusType) {
                    case ALL_STATUS: {
                        if (retDevStatus.enumAncStatus != null) {
                            notifyUiUpdate(EnumCommands.CMD_ANC, retDevStatus.enumAncStatus.ordinal());
                        }
                        if (retDevStatus.enumAAStatus != null) {
                            notifyUiUpdate(EnumCommands.CMD_AMBIENT_LEVELING, retDevStatus.enumAAStatus.ordinal());
                        }
                        notifyUiUpdate(EnumCommands.CMD_AutoOffEnable, retDevStatus.retAutoOff.isOnOff, retDevStatus.retAutoOff.time);
                        if (retDevStatus.enumAAStatus != null) {
                            notifyUiUpdate(EnumCommands.CMD_GEQ_CURRENT_PRESET, retDevStatus.enumEqPresetIdx.ordinal());
                        }
                        break;
                    }
                    case ANC: {
                        notifyUiUpdate(EnumCommands.CMD_ANC, retDevStatus.enumAncStatus.ordinal());
                        break;
                    }
                    case AMBIENT_AWARE_MODE: {
                        notifyUiUpdate(EnumCommands.CMD_AMBIENT_LEVELING, retDevStatus.enumAAStatus.ordinal());
                        break;
                    }
                    case AUTO_OFF: {
                        notifyUiUpdate(EnumCommands.CMD_AutoOffEnable, retDevStatus.retAutoOff.isOnOff, retDevStatus.retAutoOff.time);
                        break;
                    }
                    case EQ_PRESET: {
                        notifyUiUpdate(EnumCommands.CMD_GEQ_CURRENT_PRESET, retDevStatus.enumEqPresetIdx.ordinal());
                        break;
                    }
                }
                break;
            case RET_CURRENT_EQ: {
                RetCurrentEQ dataCurrentEQ = (RetCurrentEQ) retResponse.object;
                notifyUiUpdate(EnumCommands.CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS, null, dataCurrentEQ);
                break;
            }

        }

    }

    private boolean isByConnectedDevice(String mac) {
        MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(mac);
        if (myDevice == null) {
            Logger.d(TAG, "is by connected device, myDevice is null");
            return false;
        }
        MyDevice myDeviceConnected = ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED);
        if (myDeviceConnected != null && !myDeviceConnected.mac.equals(myDevice.mac)) {
            Logger.d(TAG, "is by connected device, not connected device callback");
            return false;
        }
        return true;
    }

    private void notifyUiUpdate(final EnumCommands enumCommands, final Object... objects) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "notify ui update, on ret listener:" + mOnRetListener);
                mOnRetListener.onReceive(enumCommands, objects);
            }
        });
    }

    @Override
    public void onLeOta(BluetoothDevice bluetoothDevice, EnumOtaState state, int progress) {
        Logger.d(TAG, "on bes update image state");
        BesEngine.getInstance().updateImage(bluetoothDevice.getAddress());
    }

    private class LeHandler extends Handler {

        LeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_SCAN: {
                    if (!isConnected()) {
                        startBleScan();
//                        leHandler.removeMessages(MSG_START_SCAN);
//                        leHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                        Logger.d(TAG, "handle message, in msg start scan, start scan cycle.");
                    } else {
                        Logger.d(TAG, "handle message, in msg start scan, stop scan.");
                        leLollipopScanner.stopScan();
                    }
                    break;
                }
                case MSG_DISCONNECT: {
//                    BesEngine.getInstance().disconnect();
                    break;
                }
                case MSG_CONNECT_TIME_OUT: {
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    /**
     * In App, there provides AppACK to acknowledge device; it depends on the features requirement.
     *
     * @param mac          mac address
     * @param cmdAppackset {@link CmdAppAckSet}
     */
    public void setAppAck(String mac, CmdAppAckSet cmdAppackset) {
        Logger.d(TAG, "request device info");
        BesEngine.getInstance().sendCommand(mac, cmdAppackset.getCommand());
    }

    /**
     * Device and App may need to disconnect with each other. Before establish on disconnection, a “ByeBye” command
     * can be used on this purpose. Once ACK was received, then the formal disconnection will be announced.
     *
     * @param mac
     * @param cmdappbye {@link CmdAppByeSet}
     */
    public void setAppBye(String mac, CmdAppByeSet cmdappbye) {
        Logger.d(TAG, "request device info");
        BesEngine.getInstance().sendCommand(mac, cmdappbye.getCommand());
    }

    /**
     * Two scenarios were involved here: - 1) Device Information request via command, 2)Auto feedback from device.
     *
     * @param mac
     */
    public void reqDevInfo(String mac) {
        Logger.d(TAG, "request device info");
        CmdHeader.combine(CmdHeader.REQ_DEV_INFO);
        boolean isSend = BesEngine.getInstance().sendCommand(mac, CmdHeader.getCommand());
        Logger.d(TAG, "request device info, isSend = " + isSend + ", command: " + ArrayUtil.bytesToHex(CmdHeader.getCommand()));
    }

    /**
     * Two scenarios were involved here: - 1) Device Status request via command, 2)Auto feedback from device.
     *
     * @param mac
     * @param cmdDevInfoReq {@link CmdDevStatus}
     */
    public void reqDevStatus(String mac, CmdDevStatus cmdDevInfoReq) {
        Logger.d(TAG, "request to set device status");
        BesEngine.getInstance().sendCommand(mac, cmdDevInfoReq.getCommand());
    }

    /**
     * Write REQ_ANC enable status.
     *
     * @param commandAncset {@link CmdAncSet}
     */
    public void reqSetANC(String mac, CmdAncSet commandAncset) {
        Logger.d(TAG, "request to set REQ_ANC");
        BesEngine.getInstance().sendCommand(mac, commandAncset.getCommand());
    }

    /**
     * Write AA Mode status.
     *
     * @param cmdAaSet 0x00/0x01 means Talk Thru/Ambient Aware
     */
    public void reqSetAAMode(String mac, CmdAASet cmdAaSet) {
        Logger.d(TAG, "request to set AA mode");
        BesEngine.getInstance().sendCommand(mac, cmdAaSet.getCommand());
    }

    /**
     * Write AutoOff status.
     *
     * @param onOff Payload length(1 bytes):
     *              1 bit(MSB): 0/1 means disable/enable
     *              7 bits(LSB): auto off time value /mins
     */
    public void reqSetAutoOff(String mac, byte[] onOff) {
        Logger.d(TAG, "request to set auto off");
        CmdHeader.combine(CmdHeader.SET_AUTO_OFF, onOff);
        BesEngine.getInstance().sendCommand(mac, CmdHeader.getCommand());
    }

    /**
     * To set up the EQ, use these two command EQ preset cmd(0x40) and EQ settings cmd(0x41).
     * EQ Presets has 4 types, off/jazz/vocal/bass.
     * Payload length(1 byte): 0 - off, 1 - jazz, 2 - vocal, 3 - bass
     *
     * @param mac
     * @param reqEqPresetSet {@link CmdEqPresetSet}
     */
    public void reqSetEQPreset(String mac, CmdEqPresetSet reqEqPresetSet) {
        Logger.d(TAG, "request to set EQ preset");
        BesEngine.getInstance().sendCommand(mac, reqEqPresetSet.getCommand());
    }

    /**
     * "custom" is only for using reqSetEQSettings.
     * Payload(0xnn):
     * Preset index(1 byte) - Always value “4” for setting EQ bands
     * EQ category(1 byte) - 0x00/ 0x01/ 0x02 Design EQ/ Graphic EQ/ Total EQ
     * Calib(4 bytes) - Max gain calib value
     * Sample Rate(1 byte) - Value * k (ex. Value = 48, actual rate = 48 * k)
     * Gain0(1 byte) - Left gain value
     * Gain1(1 byte) - Right gain value
     *
     * @param mac
     * @param reqEqSettingsSet {@link CmdEqSettingsSet}
     */
    public void reqSetEQSettings(String mac, CmdEqSettingsSet reqEqSettingsSet) {
        Logger.d(TAG, "request to set EQ settings");
        BesEngine.getInstance().sendEqSettingData(mac, reqEqSettingsSet);
    }

    /**
     * App can query current EQ setting via reqCurrentEQ
     * Payload EQ category(1 bytes)
     * Request the current EQ Type
     * 0x00/Design EQ
     * 0x01/Graphic EQ
     * 0x02/Total EQ
     *
     * @param mac
     * @param reqCurrEq {@link CmdCurrEq}
     */
    public void reqCurrentEQ(String mac, CmdCurrEq reqCurrEq) {
        Logger.d(TAG, "request current EQ");
        BesEngine.getInstance().sendCommand(mac, reqCurrEq.getCommand());
    }

    public void updateImage(String mac) {
        BesEngine.getInstance().setOtaCharacter(mac, JBLApplication.getJBLApplicationContext());
    }
}
