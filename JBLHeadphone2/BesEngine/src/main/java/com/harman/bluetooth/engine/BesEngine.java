package com.harman.bluetooth.engine;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.bluetooth.connector.LeConnector;
import com.harman.bluetooth.constants.BesAction;
import com.harman.bluetooth.constants.BesCommandType;
import com.harman.bluetooth.listeners.BesListener;
import com.harman.bluetooth.ota.BesOtaUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * @name AKG-Headphones-Android
 * @class name：com.harman.bluetooth.engine
 * @class describe
 * Created by Wayne on 6/8/18.
 */

public class BesEngine implements IBesEngine {

    private static volatile BesEngine mBESEngine;

    private LeConnector mLeConnector;

    private List<BesListener> listeners;

    private BesOtaUpdate besOtaUpdate;

    private Object mLock = new Object();

//    private LeConnectorListener mLeConnectListener;

    private BesEngine() {
        mLeConnector = new LeConnector();
        listeners = new ArrayList<>();
        besOtaUpdate = new BesOtaUpdate();

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
        mLeConnector.setListener(listeners);
        boolean result = mLeConnector.connect(context, bluetoothDevice);
        if(!result){
            mLeConnector.close();
        }
        return result;
    }

    @Override
    public void disconnect() {
        mLeConnector.close();
    }

    @Override
    public boolean isConnected() {
        return mLeConnector.isConnected();
    }

    private boolean discoverServices() {
        return mLeConnector.discoverServices();
    }

    public boolean requestMtu(int mtu) {
        return mLeConnector.requestMtu(mtu);
    }

    @Override
    public boolean sendCommand(BesCommandType commandType, BesAction besAction, byte[] payload) {
        switch(commandType){
            case EQ: {
                break;
            }
        }
        //byte[] data = {0x11};// create data based on command and action
        mLeConnector.write(payload);
        return false;
    }

    @Override
    public void addListener(BesListener listener) {
        synchronized (mLock) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void removeListener(BesListener listener) {
        synchronized (mLock) {
            listeners.remove(listener);
        }
    }

    @Override
    public void updateImage(Context context) {
        besOtaUpdate.sendFileInfo(context);
        besOtaUpdate.setListener(listeners);
    }
}
