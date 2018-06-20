package com.example.testcase.fragment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.testcase.R;


public class TestShowFragmentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TestShowFragmentActivity.class.getSimpleName();

    private Button button,buttonAdd, buttonRemove,buttonStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_show_fragment);
        setTitle(R.string.test_show_fragment);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        buttonAdd = findViewById(R.id.button2);
        buttonAdd.setOnClickListener(this);
        buttonRemove = findViewById(R.id.button3);
        buttonRemove.setOnClickListener(this);
        buttonStart = findViewById(R.id.button4);
        buttonStart.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:{
                switchFragment(new TestFragment());
                break;
            }

        }
    }

    private void switchFragment(TestFragment baseFragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(TestFragment.class.getSimpleName());
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
        if (fragment != null) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.replace(R.id.container,baseFragment, baseFragment.getTag());
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();

    }

}
