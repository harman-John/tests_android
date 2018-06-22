package jbl.stc.com.scan;

import android.bluetooth.BluetoothDevice;

public interface ScanListener {
    void onFound(BluetoothDevice device, int rssi, byte[] scanRecord);

    void onScanStart();

    void onScanFinish();
}
