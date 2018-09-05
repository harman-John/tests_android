package com.harman.bluetooth.listeners;

import android.bluetooth.BluetoothDevice;

import com.harman.bluetooth.constants.BesUpdateState;
import com.harman.bluetooth.ret.DevResponse;

/**
 * Created by Wayne on 6/8/18.
 */

public interface BesListener {

    void onBesConnectStatus(BluetoothDevice bluetoothDevice, boolean isConnected);

    void onMtuChanged(BluetoothDevice bluetoothDevice, int status, int mtu);

    void onBesReceived(BluetoothDevice bluetoothDevice, DevResponse devResponse);

    void onBesUpdateImageState(BluetoothDevice bluetoothDevice, BesUpdateState state, int progress);

}
