package com.example.testcase.bt_spp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testcase.R;

public class SppServerActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = SppServerActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spp_server);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTOnOff();
        checkPermission();
        initView();
        btServerHandler();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            checkPermission();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            //Not Enable, show toast
            Toast.makeText(SppServerActivity.this, "Please Open BT", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        acceptThread.cancel();
    }

    private void initView() {
        textView = (TextView)findViewById(R.id.textView);
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
    private SppServerThread acceptThread;
    private void btServerHandler() {
//        Set<BluetoothDevice> paireDevices = mBluetoothAdapter.getBondedDevices();
//        if (paireDevices.size() > 0) {
//            for (BluetoothDevice device : paireDevices) {
//                Log.i(TAG, "name =" + device.getName()
//                        + ", mac = " + device.getAddress()
//                        + ", bindState = " + device.getBondState()
//                        + ", type = " + device.getType());

                    Log.i(TAG,"Server start");
                    acceptThread = new SppServerThread();
                    acceptThread.run();
//            }
//        }

    }
}
