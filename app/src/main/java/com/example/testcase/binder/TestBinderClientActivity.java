package com.example.testcase.binder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.binderserver.ICompute;
import com.example.testcase.R;

public class TestBinderClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_binder_client);
        setTitle(R.string.test_binder);
        Intent intent = new Intent("com.example.binderserver.AIDL_MyService");
        intent.setPackage("com.example.binderserver");
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ICompute iCompute = ICompute.Stub.asInterface(service);
                try {
                    int a = iCompute.add(3,4);
                    Log.i("JohnGan","a = "+a);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
}
