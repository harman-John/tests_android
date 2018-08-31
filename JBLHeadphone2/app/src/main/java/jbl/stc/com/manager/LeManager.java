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
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.harman.bluetooth.constants.BesUpdateState;
import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.listeners.BesListener;

import java.util.Arrays;
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
import jbl.stc.com.utils.SaveSetUtil;

public class LeManager implements ScanListener, BesListener {

    private final static String TAG = LeManager.class.getSimpleName();
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private LeLollipopScanner leLollipopScanner;
    private Activity mContext;
    private boolean mIsConnected;
    private LeHandler leHandler = new LeHandler();
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
        if (mOnConnectStatusListeners == null){
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
        Logger.d(TAG, "on found key = " + key);
        MyDevice myDevice = null;
        if (pid.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT_PID)) {
            myDevice = ProductListManager.getInstance().getDeviceByKey(device.getAddress());
        }
        if (myDevice == null) {
            Logger.d(TAG,"on found, my device is null, firstly, connect device in bt settings.");
            return;
        }
        devicesSet.add(myDevice);
        SaveSetUtil.saveSet(mContext, devicesSet);
        ProductListManager.getInstance().checkHalfConnectDevice(devicesSet);
        if (!DeviceManager.getInstance(mContext).isConnected()) {
            BesEngine.getInstance().addListener(this);
            boolean result = BesEngine.getInstance().connect(mContext, device);
            Logger.d(TAG, "on found, connect result = " + result);
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
    public void setOnConnectStatusListener(OnConnectStatusListener onConnectStatusListener){
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
    public void onBesConnectStatus(BluetoothDevice bluetoothDevice, final boolean isConnected) {
        Logger.d(TAG, "on bes connect status, isConnected = " + isConnected);
        synchronized (mLock) {
            if (DeviceManager.getInstance(mContext).isConnected()) {
                Logger.i(TAG, "bes connected, but other device is already connected");
                leHandler.sendEmptyMessageDelayed(MSG_DISCONNECT, 200);
                return;
            }
            mIsConnected = isConnected;
            MyDevice myDevice = null;
            for (MyDevice tempDevice : devicesSet) {
                if (tempDevice.mac != null && tempDevice.mac.equals(bluetoothDevice.getAddress())) {
                    myDevice = tempDevice;
                }
            }
            if (myDevice == null) {
                return;
            }
            if (isConnected) {
                myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
            } else {
                myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
            }

            Logger.d(TAG, "on bes connect status, my device is " + myDevice.deviceKey);
            ProductListManager.getInstance().checkConnectStatus(myDevice.mac,myDevice.connectStatus);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
    public void onBesReceived(BluetoothDevice bluetoothDevice, byte[] data) {
        String dataString = Arrays.toString(data);
        Logger.d(TAG, "on bes received, data string = " + dataString);
//        switch (command){
//            case ReportFormat.RET_DEV_ACK:{
//
//            }
//        }
    }

    @Override
    public void onBesUpdateImageState(BluetoothDevice bluetoothDevice, BesUpdateState state, int progress) {
        Logger.d(TAG, "on bes update image state");
    }

    private class LeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_SCAN: {
                    if (!isConnected()) {
                        startBleScan();
                        leHandler.removeMessages(MSG_START_SCAN);
                        leHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
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
