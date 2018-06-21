package jbl.stc.com.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.akg.headphone.interfaces.ScanListener;


public class LeJBScanner extends BaseScanner {

    public LeJBScanner(Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startScan(ScanListener listener) {
        if (isScanning()) {
            return;
        }
        if (getBluetoothAdapter().startLeScan(mLeScanCallback)) {
            onScanStart();
        } else {
            onScanFinish();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void stopScan() {
        if (!isScanning())
            return;
        getBluetoothAdapter().stopLeScan(mLeScanCallback);
        onScanFinish();
    }

    @Override
    public void close() {

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            onFound(device, rssi, scanRecord);
        }
    };
}
