package com.example.testcase.listener;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.testcase.R;
import com.example.testcase.fragment.TestShowFragmentActivity;


public class TestListenersActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TestShowFragmentActivity.class.getSimpleName();

    private Button button,buttonAdd, buttonRemove,buttonStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_listeners);
        setTitle(R.string.test_listener);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        buttonAdd = findViewById(R.id.button2);
        buttonAdd.setOnClickListener(this);
        buttonRemove = findViewById(R.id.button3);
        buttonRemove.setOnClickListener(this);
        buttonStart = findViewById(R.id.button4);
        buttonStart.setOnClickListener(this);
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
                Intent intent= new Intent(TestListenersActivity.this,TestListeners2Activity.class);
                startActivity(intent);
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
