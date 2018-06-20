package com.example.testcase.bt_spp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.example.testcase.R;

import java.util.Set;

public class SppClientActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = SppClientActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.test_bt_spp);
        setContentView(R.layout.activity_spp_client);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTOnOff();
        checkPermission();
        initView();
        pairedDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            checkPermission();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            //Not Enable, show toast
            Toast.makeText(SppClientActivity.this, "Please Open BT", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sppClientThread.cancel();
    }

    private void initView() {

    }

    private void checkBTOnOff() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //check whether have coarse permission
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_BT);
            } else {
                //own coarse permission
            }
        } else {
            //system is lower than 6.0
        }
    }

    private SppClientThread sppClientThread;
    private void pairedDevice() {
        Set<BluetoothDevice> paireDevices = mBluetoothAdapter.getBondedDevices();
        if (paireDevices.size() > 0) {
            for (BluetoothDevice device : paireDevices) {
                Log.i(TAG, "name =" + device.getName()
                        + ", mac = " + device.getAddress()
                        + ", bindState = " + device.getBondState()
                        + ", type = " + device.getType());

                if (device.getAddress().equalsIgnoreCase("40:4E:36:4A:3A:BA")){
                    Log.i(TAG,"connect to "+device.getName());
                    sppClientThread = new SppClientThread(device);
                    sppClientThread.run();
                }
            }
        }

    }
}
