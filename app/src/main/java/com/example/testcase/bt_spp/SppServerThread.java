package com.example.testcase.bt_spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

public class SppServerThread extends Thread {
    private final static String TAG = SppServerThread.class.getSimpleName();
    private final BluetoothServerSocket mmServerSocket;


    public SppServerThread() {
        BluetoothServerSocket tmp = null;
        try {
            tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("YourAPPName", Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                Log.i(TAG,"Server is running");
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (socket != null) {

                byte[] buffer = new byte[1024];
                try {
                    int bytes = socket.getInputStream().read(buffer);
                    String strRead = new String(buffer);
                    strRead = String.copyValueOf(strRead.toCharArray(), 0, bytes);
                    Log.i(TAG,"Server buffer is : "+strRead);
                    String temp = "John";
                    socket.getOutputStream().write(temp.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

