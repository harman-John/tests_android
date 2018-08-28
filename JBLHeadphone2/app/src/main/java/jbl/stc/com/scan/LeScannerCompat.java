package jbl.stc.com.scan;

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

import com.harman.bluetooth.constants.BesAction;
import com.harman.bluetooth.constants.BesCommandType;
import com.harman.bluetooth.constants.BesUpdateState;
import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.listeners.BesListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.ConnectListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.SaveSetUtil;

public class LeScannerCompat implements ScanListener, BesListener {

    private final static String TAG = LeScannerCompat.class.getSimpleName();
    private static final int PERMISSION_REQUEST_LOCATION = 1;
    private LeLollipopScanner leLollipopScanner;
    private Activity mContext;
    private boolean mIsConnected;

    private static class InstanceHolder {
        public static final LeScannerCompat instance = new LeScannerCompat();
    }

    public static LeScannerCompat getInstance() {
        return LeScannerCompat.InstanceHolder.instance;
    }

    private LeScannerCompat() {
        if (listeners == null)
            listeners = new ArrayList<>();
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
                    startBleScan();
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
                        startBleScan();
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
            myDevice = AppUtils.getMyDevice(mContext, JBLConstant.DEVICE_LIVE_400BT, ConnectStatus.A2DP_HALF_CONNECTED, pid, device.getAddress());
        }
        if (myDevice == null) {
            return;
        }
        devicesSet.add(myDevice);
        SaveSetUtil.saveSet(mContext,devicesSet);
        ProductListManager.getInstance().checkHalfConnectDevice(devicesSet);
        if (!DeviceManager.getInstance(mContext).isConnected()) {
            BesEngine.getInstance().addListener(this);
            boolean result = BesEngine.getInstance().connect(mContext, device);
            Logger.d(TAG, "on found, connect result = " + result);
        }
    }

    private final Object mLock = new Object();
    private List<ConnectListener> listeners;

    public void addListener(ConnectListener listener) {
        synchronized (mLock) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void removeListener(ConnectListener listener) {
        synchronized (mLock) {
            listeners.remove(listener);
        }
    }

    public boolean isConnected(){
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
            if (DeviceManager.getInstance(mContext).isConnected()){
                Logger.i(TAG,"bes connected, but other device is already connected");
                leHandler.sendEmptyMessageDelayed(MSG_DISCONNECT,200);
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
            ProductListManager.getInstance().checkConnectStatus(myDevice);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ConnectListener listener : listeners) {
                        listener.connectDeviceStatus(isConnected);
                    }
                }
            });
        }
    }

    @Override
    public void onMtuChanged(int status, int mtu) {
        Logger.d(TAG, "on mtu changed");
    }

    @Override
    public void onBesReceived(BesCommandType commandType, BesAction besAction, byte[] data) {
        Logger.d(TAG, "on bes received");
    }

    @Override
    public void onBesUpdateImageState(BesUpdateState state, int progress) {
        Logger.d(TAG, "on bes update image state");
    }

    private LeHandler leHandler = new LeHandler();
    private final static int MSG_CONNECTED = 0;
    private final static int MSG_DISCONNECT = 1;
    private final static int MSG_CONNECT_TIME_OUT = 2;

    private class LeHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONNECTED: {
                    break;
                }
                case MSG_DISCONNECT: {
                    BesEngine.getInstance().disconnect();
                    break;
                }
                case MSG_CONNECT_TIME_OUT:{
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }
}
