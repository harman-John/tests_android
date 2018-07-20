package jbl.stc.com.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;


public interface A2dpObserver {

    void checkDevices(List<BluetoothDevice> deviceList);

}
