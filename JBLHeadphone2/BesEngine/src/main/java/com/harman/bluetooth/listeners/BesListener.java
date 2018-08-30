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

    void onMtuChanged(int status, int mtu);

    void onBesReceived(byte[] data);

    void onBesUpdateImageState(BesUpdateState state, int progress);

}
