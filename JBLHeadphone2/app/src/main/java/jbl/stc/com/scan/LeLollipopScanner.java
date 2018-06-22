package jbl.stc.com.scan;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LeLollipopScanner extends BaseScanner {

    private BluetoothLeScanner mLeScanner;

    public LeLollipopScanner(Context context) {
        super(context);
        mLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
    }

    @Override
    public void startScan(ScanListener listener) {
        super.startScan(listener);
        if (isScanning())
            return;
        if (mLeScanner == null) {
            mLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        }
        mLeScanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mCallback);
        onScanStart();
    }

    @Override
    public void stopScan() {
        if (!isScanning())
            return;
        mLeScanner.stopScan(mCallback);
        onScanFinish();
    }

    @Override
    public void close() {

    }

    private android.bluetooth.le.ScanCallback mCallback = new android.bluetooth.le.ScanCallback() {

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed " + errorCode);
            super.onScanFailed(errorCode);
            onScanFinish();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }
    };
}
