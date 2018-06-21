package jbl.stc.com.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.akg.headphone.interfaces.ScanListener;
import com.harman.bluetooth.utils.BtHelper;

public abstract class BaseScanner implements BtScanner {

    protected final String TAG = getClass().getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;

    private ScanListener mScanListener;

    private boolean mScanning = false;

    public BaseScanner(Context context) {
        mBluetoothAdapter = BtHelper.getBluetoothAdapter(context);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    @Override
    public void startScan(ScanListener listener) {
        mScanListener = listener;
    }

    @Override
    public void close() {
        mScanListener = null;
    }

    protected void onFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (mScanListener != null) {
            mScanListener.onFound(device, rssi, scanRecord);
        }
    }

    protected void onScanStart() {
        mScanning = true;
        if (mScanListener != null) {
            mScanListener.onScanStart();
        }
    }

    protected void onScanFinish() {
        mScanning = false;
        if (mScanListener != null) {
            mScanListener.onScanFinish();
        }
    }

    protected boolean isScanning() {
        return mScanning;
    }
}
