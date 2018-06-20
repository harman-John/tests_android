package com.example.testcase.binder;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.binderserver.ICompute;

public class TestBinderService extends Service {
    public TestBinderService() {
    }

    private Binder mBinder = new ICompute.Stub() {
        @Override
        public int add(int a, int b) throws RemoteException {
            return a + b;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
