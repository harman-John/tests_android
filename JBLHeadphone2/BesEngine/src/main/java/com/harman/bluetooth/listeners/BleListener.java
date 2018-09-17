package com.harman.bluetooth.listeners;

import android.bluetooth.BluetoothDevice;

import com.harman.bluetooth.constants.EnumOtaState;
import com.harman.bluetooth.ret.RetResponse;

public interface BleListener {

    void onLeConnectStatus(BluetoothDevice bluetoothDevice, boolean isConnected);

    void onMtuChanged(BluetoothDevice bluetoothDevice, int status, int mtu);

    void onRetReceived(BluetoothDevice bluetoothDevice, RetResponse retResponse);

    void onLeOta(BluetoothDevice bluetoothDevice, EnumOtaState state, int progress);

    void onWritten(int status);
}
