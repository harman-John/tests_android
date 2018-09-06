package jbl.stc.com.scan;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.utils.ArrayUtil;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LeLollipopScanner extends BaseScanner {

    private BluetoothLeScanner mLeScanner;
    private Context context;

    public LeLollipopScanner(Context context) {
        super(context);
        this.context = context;
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
        List<ScanFilter> filters = new ArrayList<>();
//        byte[] mid = new byte[]{0x0e, (byte) 0xcb};
//        ScanFilter filter = new ScanFilter.Builder().setManufacturerData(mid,).build();
//        filters.add(filter);
        if (mLeScanner != null) {
            mLeScanner.startScan(filters, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mCallback);
            onScanStart();
        }
    }

    @Override
    public void stopScan() {
        if (!isScanning())
            return;
        Logger.d(TAG,"stop scan");
        mLeScanner.stopScan(mCallback);
        onScanFinish();
    }

    @Override
    public void close() {

    }

    private android.bluetooth.le.ScanCallback mCallback = new android.bluetooth.le.ScanCallback() {

        @Override
        public void onScanFailed(int errorCode) {
            Logger.e(TAG, "on scan failed, le scan callback, erorr code: " + errorCode);
            super.onScanFailed(errorCode);
            onScanFinish();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (DeviceManager.getInstance((Activity) context).isConnected()){
                stopScan();
                return;
            }
            String deviceName = result.getDevice().getName();
            if (deviceName!= null && result.getScanRecord()!= null) {
                if (deviceName.contains("samsung")){
                    return;
                }
                Logger.d(TAG,"on scan result, le scan callback, "+ deviceName);
                byte[] manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(JBLConstant.HARMAN_VENDOR_ID);
                if (manufacturerSpecificData !=null && manufacturerSpecificData.length > 0) {
                    byte[] pid = new byte[2];
                    pid[0] = manufacturerSpecificData[0];
                    pid[1] = manufacturerSpecificData[1];
                    Logger.e(TAG, "on scan result, le scan callback, device name = " + deviceName
                            + ", Rssi = " + result.getRssi()
                            + ", data = " + ArrayUtil.toHex(pid));
                    // This is JBL Live 400BT
                    if (pid[0] == 0x11 && pid[1] == 0x1f) {
                        String sPid = ArrayUtil.toHexNoAppend(pid);
                        onFound(result.getDevice(), sPid);
                    }
                }
            }
        }
    };
}
