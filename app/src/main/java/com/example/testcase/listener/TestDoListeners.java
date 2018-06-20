package com.example.testcase.listener;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TestDoListeners {

    private static final String TAG = TestDoListeners.class.getSimpleName();
    private List<TestListener>  lists = new ArrayList<>();


    private static class InstanceHolder {
        public static final TestDoListeners instance = new TestDoListeners();
    }

    public static TestDoListeners getInstance() {
        return InstanceHolder.instance;
    }

    public void addListener(TestListener testListener){
        if (testListener == null){
            Log.i(TAG,"testListener is null, can't add");
            return;
        }
        if (!lists.contains(testListener)){
            lists.add(testListener);
        }
    }

    public void removeListener(TestListener testListener){
        if (testListener == null){
            Log.i(TAG,"testListener is null, can't remove");
            return;
        }
        if (lists.contains(testListener)){
            lists.remove(testListener);
        }
    }

    private void doListener(){
        for (TestListener listener: lists){
            listener.doTest();
        }
    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"start");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doListener();
            }
        }).start();
    }
}
