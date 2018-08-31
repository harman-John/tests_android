package com.harman.bluetooth.engine;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.bluetooth.constants.BesAction;
import com.harman.bluetooth.constants.BesCommandType;
import com.harman.bluetooth.listeners.BesListener;

/**
 * Created by Wayne on 6/8/18.
 */

public interface IBesEngine {

    boolean connect(Context context, BluetoothDevice bluetoothDevice);

    void disconnect(String mac);

    boolean isConnected(String mac);

    boolean sendCommand(String mac, byte[] command);

    void updateImage(String mac, Context context);

    void addListener(BesListener listener);

    void removeListener(BesListener listener);
}
