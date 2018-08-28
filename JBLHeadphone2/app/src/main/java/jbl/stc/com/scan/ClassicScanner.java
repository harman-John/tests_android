package jbl.stc.com.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ClassicScanner extends BaseScanner {

    private Context mContext;

    public ClassicScanner(Context context) {
        super(context);
        mContext = context;
        initReceiver();
    }

    @Override
    public void startScan(ScanListener listener) {
        super.startScan(listener);
        if (isScanning())
            return;
        getBluetoothAdapter().startDiscovery();
    }

    @Override
    public void stopScan() {
        if (!isScanning())
            return;
        getBluetoothAdapter().cancelDiscovery();
    }

    @Override
    public void close() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    onScanStart();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    onScanFinish();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) -100);
                    onFound(device, "");
                    break;
            }
        }
    };
}
