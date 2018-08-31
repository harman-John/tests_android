package com.harman.bluetooth.engine;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.bluetooth.connector.LeConnector;
import com.harman.bluetooth.listeners.BesListener;
import com.harman.bluetooth.ota.BesOtaUpdate;
import com.harman.bluetooth.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BesEngine implements IBesEngine {

    private static volatile BesEngine mBESEngine;

    private Map<String,LeConnector> mLeConnectorMap;
    private List<BesListener> besListeners;

    private BesOtaUpdate besOtaUpdate;

    private final Object mLock = new Object();
    private final static String TAG = BesEngine.class.getSimpleName();

//    private LeConnectorListener mLeConnectListener;

    private BesEngine() {
        mLeConnectorMap = new HashMap<>();
        besListeners = new ArrayList<>();
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

    @Override
    public boolean connect(Context context, BluetoothDevice bluetoothDevice) {
        LeConnector mLeConnector = mLeConnectorMap.get(bluetoothDevice.getAddress());
        if (mLeConnector == null){
            mLeConnector = new LeConnector();
            mLeConnectorMap.put(bluetoothDevice.getAddress(),mLeConnector);
        }
        mLeConnector.setBesListener(besListeners);
        boolean result = mLeConnector.connect(context, bluetoothDevice);
        if(!result){
            Logger.d(TAG,"connect failed, close le connector");
            mLeConnector.close();
        }
        return result;
    }

    @Override
    public void disconnect(String mac) {
        LeConnector mLeConnector = mLeConnectorMap.get(mac);
        if (mLeConnector != null){
            Logger.d(TAG,"disconnect, mac = "+mac);
            mLeConnector.close();
        }
    }

    @Override
    public boolean isConnected(String mac) {
        LeConnector mLeConnector = mLeConnectorMap.get(mac);
        return mLeConnector != null && mLeConnector.isConnected();
    }

    @Override
    public boolean sendCommand(String mac, byte[] command) {
        if (command.length <= 0) {
            Logger.e(TAG, "send command error, command is null");
            return false;
        }
        Logger.d(TAG,"send command, mac = "+mac);
        LeConnector mLeConnector = mLeConnectorMap.get(mac);
        return mLeConnector != null && mLeConnector.write(command);
    }

    @Override
    public void addListener(BesListener listener) {
        synchronized (mLock) {
            if (!besListeners.contains(listener)) {
                besListeners.add(listener);
            }
        }
    }

    @Override
    public void removeListener(BesListener listener) {
        synchronized (mLock) {
            besListeners.remove(listener);
        }
    }

    @Override
    public void updateImage(String mac, Context context) {
        besOtaUpdate.sendFileInfo(context);
        besOtaUpdate.setListener(besListeners);
    }

}
