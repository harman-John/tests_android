package jbl.stc.com.scan;

import android.bluetooth.BluetoothDevice;

public interface ScanListener {
    void onFound(BluetoothDevice device, String pid);

    void onScanStart();

    void onScanFinish();
}
