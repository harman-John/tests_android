package com.example.testcase.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.testcase.R;

public class BroadcastActivity extends AppCompatActivity {

    public static final String TAG = "MainActivitytt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        Log.i(TAG,"onCreate" );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume" );
        start();
    }

    public synchronized void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction( Intent.ACTION_TIME_TICK );
        registerReceiver( receiver, filter );
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent ) {
            Log.i(TAG,"onReceive getAction = " + intent.getAction());
            final NotificationThread notificationThread = new NotificationThread();
            notificationThread.start();

//            try {
//                Thread.sleep(1000* 120);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Log.i(TAG,"onReceive over");
//                notificationThread.notifyMe();
        }
    };

    private class NotificationThread extends Thread {
        public void notifyMe(){

            synchronized (this) {
                Log.i(TAG,"notify" );
                notify();
            }
        }
        public void waitMe(){
            try {
                synchronized (this) {
                    Log.i(TAG,"waiting ..." );
                    wait();
                    Log.i(TAG,"waiting over" );
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            waitMe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause" );
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}