package com.example.testcase.listener;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.testcase.R;


public class TestListeners2Activity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = TestListeners2Activity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_listener2);
        setTitle(R.string.test_listener);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
    }


    TestListener testListener = new TestListener() {
        @Override
        public void doTest() {
            Log.i(TAG,"message come");
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:{
                break;
            }
            case R.id.button2:{
                TestDoListeners.getInstance().addListener(testListener);
                break;
            }
            case R.id.button3:{
                TestDoListeners.getInstance().removeListener(testListener);
                break;
            }
            case R.id.button4:{
                TestDoListeners.getInstance().start();
                break;
            }
        }
    }
}
