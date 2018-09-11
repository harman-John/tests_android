package com.harman.bluetooth.engine;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.bluetooth.core.LeDevice;
import com.harman.bluetooth.listeners.BleListener;
import com.harman.bluetooth.ota.BleOta;
import com.harman.bluetooth.req.CmdEqSettingsSet;
import com.harman.bluetooth.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BesEngine {

    private static volatile BesEngine mBESEngine;

    private Map<String,LeDevice> mLeConnectorMap;
    private List<BleListener> bleListeners;

    private BleOta besOtaUpdate;

    private final Object mLock = new Object();
    private final static String TAG = BesEngine.class.getSimpleName();

//    private LeConnectorListener mLeConnectListener;

    private BesEngine() {
        mLeConnectorMap = new HashMap<>();
        bleListeners = new ArrayList<>();
//        besOtaUpdate = new BesOtaUpdate();

    }

    public static BesEngine getInstance() {
        if (mBESEngine == null) {
            synchronized (BesEngine.class) {
                if (mBESEngine == null) {
                    mBESEngine = new BesEngine();
                }
            }
        }
        return mBESEngine;
    }

    public boolean connect(Context context, BluetoothDevice bluetoothDevice) {
        LeDevice mLeDevice = mLeConnectorMap.get(bluetoothDevice.getAddress());
        if (mLeDevice == null){
            mLeDevice = new LeDevice();
            mLeConnectorMap.put(bluetoothDevice.getAddress(), mLeDevice);
        }
        mLeDevice.setBesListener(bleListeners);
        boolean result = mLeDevice.connect(context, bluetoothDevice);
        if(!result){
            Logger.d(TAG,"connect failed, close le connector");
            mLeDevice.close();
        }
        return result;
    }

    public void disconnect(String mac) {
        LeDevice mLeDevice = mLeConnectorMap.get(mac);
        if (mLeDevice != null){
            Logger.d(TAG,"disconnect, mac = "+mac);
            mLeDevice.close();
        }
    }

    public boolean isConnected() {
        for (Map.Entry<String, LeDevice> entry : mLeConnectorMap.entrySet()) {
            if (entry.getValue().isConnected()){
                return true;
            }
        }
        return false;
    }

    public boolean sendEqSettingData(String mac, CmdEqSettingsSet cmdEqSettingsSet){
        if (cmdEqSettingsSet == null) {
            Logger.e(TAG, "set eq settings data error, cmd eq settings is null");
            return false;
        }
        Logger.d(TAG,"set eq setting data, mac = "+mac);
        LeDevice mLeDevice = mLeConnectorMap.get(mac);
        return mLeDevice != null && mLeDevice.writeEqSettingsData(cmdEqSettingsSet,true);
    }

    public boolean sendCommand(String mac, byte[] command) {
        if (command.length <= 0) {
            Logger.e(TAG, "send command error, command is null");
            return false;
        }
        Logger.d(TAG,"send command, mac = "+mac);
        LeDevice mLeDevice = mLeConnectorMap.get(mac);
        return mLeDevice != null && mLeDevice.write(command,true);
    }

    public void addListener(BleListener listener) {
        synchronized (mLock) {
            if (!bleListeners.contains(listener)) {
                bleListeners.add(listener);
            }
        }
    }

    public void removeListener(BleListener listener) {
        synchronized (mLock) {
            bleListeners.remove(listener);
        }
    }

    public void updateImage(String mac, Context context) {
        besOtaUpdate.sendFileInfo(context);
        besOtaUpdate.setListener(bleListeners);
    }

}
