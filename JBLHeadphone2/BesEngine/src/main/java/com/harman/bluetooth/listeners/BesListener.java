package com.harman.bluetooth.listeners;

import android.bluetooth.BluetoothDevice;

import com.harman.bluetooth.constants.BesAction;
import com.harman.bluetooth.constants.BesCommandType;
import com.harman.bluetooth.constants.BesUpdateState;

/**
 * Created by Wayne on 6/8/18.
 */

public interface BesListener {

    void onBesConnectStatus(BluetoothDevice bluetoothDevice, boolean isConnected);

    void onMtuChanged(BluetoothDevice bluetoothDevice, int status, int mtu);

    void onBesReceived(BluetoothDevice bluetoothDevice, byte[] data);

    void onBesUpdateImageState(BluetoothDevice bluetoothDevice, BesUpdateState state, int progress);

}
