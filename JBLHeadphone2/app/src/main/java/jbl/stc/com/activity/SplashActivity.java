package jbl.stc.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.avnera.smartdigitalheadset.Logger;

import jbl.stc.com.R;


public class SplashActivity extends FragmentActivity  {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate");
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}