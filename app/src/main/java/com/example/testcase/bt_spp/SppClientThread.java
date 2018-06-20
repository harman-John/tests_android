package com.example.testcase.bt_spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


import java.io.IOException;

public class SppClientThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private final static String TAG = SppClientThread.class.getSimpleName();
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public SppClientThread(BluetoothDevice device) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mmDevice = device;
        BluetoothSocket tmp = null;
        try {
            //尝试建立安全的连接
            tmp = mmDevice.createRfcommSocketToServiceRecord(Constants.MY_UUID);
            //尝试建立不安全的连接
            //tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        try {
            Log.i(TAG,"Connect socket");
            mmSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mmSocket != null && mmSocket.isConnected()) {
            Log.i(TAG,"Connected");
            String temp = "hello";
            try {
                mmSocket.getOutputStream().write(temp.getBytes());
                Log.i(TAG,"Client send : "+temp);
                byte[] buffer = new byte[1024];
                int bytes = mmSocket.getInputStream().read(buffer);
                String strRead = new String(buffer);
                strRead = String.copyValueOf(strRead.toCharArray(), 0, bytes);
                Log.i(TAG,"Client get data : "+strRead);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
