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

import com.harman.bluetooth.constants.EnumAAStatus;
import com.harman.bluetooth.constants.EnumAncStatus;
import com.harman.bluetooth.constants.EnumOtaState;
import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.listeners.BleListener;
import com.harman.bluetooth.ret.RetCurrentEQ;
import com.harman.bluetooth.ret.RetDevStatus;
import com.harman.bluetooth.ret.RetDeviceInfo;
import com.harman.bluetooth.ret.RetResponse;

import java.util.HashSet;
import java.util.Set;

import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.OnConnectStatusListener;
import jbl.stc.com.listener.OnRetListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.scan.LeLollipopScanner;
import jbl.stc.com.scan.ScanListener;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.SaveSetUtil;

public class LeManager implements ScanListener, BleListener {

    private final static String TAG = LeManager.class.getSimpleName();
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private LeLollipopScanner leLollipopScanner;
    private Activity mContext;
    private boolean mIsConnected;
    private LeHandler leHandler = new LeHandler(Looper.getMainLooper());
    private final static int MSG_START_SCAN = 0;
    private final static int MSG_DISCONNECT = 1;
    private final static int MSG_CONNECT_TIME_OUT = 2;

    private static class InstanceHolder {
        public static final LeManager instance = new LeManager();
    }

    public static LeManager getInstance() {
        return LeManager.InstanceHolder.instance;
    }

    private LeManager() {
        if (mOnRetListener == null)
            mOnRetListener = new HashSet<>();
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
        leLollipopScanner.startScan(this);
    }


    private Set<MyDevice> devicesSet = new HashSet<>();

    //For scan callbacks
    @Override
    public void onFound(BluetoothDevice device, String pid) {
        leLollipopScanner.stopScan();
        String key = pid + "-" + device.getAddress();
        devicesSet.clear();
        if (pid.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT_PID)) {
            Logger.d(TAG, "on found key = " + key);
            MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(device.getAddress());
            if (myDevice == null) {
                myDevice = AppUtils.getMyDevice(device.getName(), ConnectStatus.A2DP_UNCONNECTED, pid, device.getAddress());
                devicesSet.add(myDevice);
                SaveSetUtil.saveSet(mContext, devicesSet);
            }
            if (myDevice == null) {
                Logger.e(TAG, "on found, my device is null");
                return;
            }
            SaveSetUtil.saveSet(mContext, devicesSet);
            ProductListManager.getInstance().checkHalfConnectDevice(devicesSet);
            if (!DeviceManager.getInstance(mContext).isConnected()) {
                BesEngine.getInstance().addListener(this);
                if (device.getAddress().equals("12:34:56:09:23:56")) {
                    boolean result = BesEngine.getInstance().connect(mContext, device);
                    Logger.d(TAG, "on found, connect result = " + result);
                }
            }
        }
    }

    private final Object mLock = new Object();
    private Set<OnRetListener> mOnRetListener;

    public void setOnRetListener(OnRetListener listener) {
        synchronized (mLock) {
            if (!mOnRetListener.contains(listener)) {
                mOnRetListener.add(listener);
            }
        }
    }

    public void removeOnRetListener(OnRetListener listener) {
        synchronized (mLock) {
            mOnRetListener.remove(listener);
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

    public boolean isConnected() {
        return mIsConnected;
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
        Logger.d(TAG, "on bes connect status, isConnected = " + isConnected);
        synchronized (mLock) {
            if (DeviceManager.getInstance(mContext).isConnected()) {
                Logger.i(TAG, "bes connected, but other device is already connected");
                leHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 200);
                return;
            }
            mIsConnected = isConnected;
            MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(bluetoothDevice.getAddress());
            if (myDevice == null) {
                Logger.d(TAG, "on bes connect status, myDevice is null");
                return;
            }
            if (isConnected) {
                myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
            } else {
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
        Logger.d(TAG, "on bes received, mac = " + bluetoothDevice.getAddress() + " , cmdId = " + retResponse.enumCmdId+"");
        switch (retResponse.enumCmdId) {
            case RET_DEV_ACK:
            case RET_DEV_BYE:
            case RET_DEV_FIN_ACK:
                break;
            case RET_DEV_INFO:
                RetDeviceInfo retDeviceInfo = (RetDeviceInfo) retResponse.object;
                notifyUiUpdate(EnumCommands.CMD_ConfigProductName, retDeviceInfo.deviceName);
                notifyUiUpdate(EnumCommands.CMD_FIRMWARE_VERSION, retDeviceInfo.firmwareVersion,true);
                notifyUiUpdate(EnumCommands.CMD_BATTERY_LEVEL, retDeviceInfo.retBatteryStatus.percent,retDeviceInfo.retBatteryStatus.charging);
                break;
            case RET_DEV_STATUS:
                RetDevStatus retDevStatus = (RetDevStatus) retResponse.object;
                Logger.d(TAG, "on bes received, status type = "+ retDevStatus.enumDeviceStatusType);
                switch (retDevStatus.enumDeviceStatusType) {
                    case ALL_STATUS: {
                        notifyUiUpdate(EnumCommands.CMD_ANC, retDevStatus.enumAncStatus.ordinal());
                        notifyUiUpdate(EnumCommands.CMD_AMBIENT_LEVELING, retDevStatus.enumAAStatus.ordinal());
                        notifyUiUpdate(EnumCommands.CMD_AutoOffEnable, retDevStatus.retAutoOff.isOnOff,retDevStatus.retAutoOff.time);
                        notifyUiUpdate(EnumCommands.CMD_GEQ_CURRENT_PRESET, retDevStatus.enumEqPresetIdx.ordinal());
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
                RetCurrentEQ dataCurrentEQ =  (RetCurrentEQ) retResponse.object;
                notifyUiUpdate(EnumCommands.CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS, null, dataCurrentEQ);
                break;
            }

        }

    }

    private void notifyUiUpdate(final EnumCommands enumCommands, final Object... objects) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (OnRetListener onRetListener : mOnRetListener) {
                    Logger.d(TAG, "notify ui update, on ret listener:"+onRetListener);
                    onRetListener.onReceive(enumCommands, objects);
                }
            }
        });
    }

    @Override
    public void onLeOta(BluetoothDevice bluetoothDevice, EnumOtaState state, int progress) {
        Logger.d(TAG, "on bes update image state");
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
                        leHandler.removeMessages(MSG_START_SCAN);
                        leHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                    } else {
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
}
