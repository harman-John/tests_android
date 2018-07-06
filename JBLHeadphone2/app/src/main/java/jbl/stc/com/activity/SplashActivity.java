package jbl.stc.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.avnera.smartdigitalheadset.Logger;

import jbl.stc.com.R;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;


public class SplashActivity extends FragmentActivity  {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate");
        setContentView(R.layout.activity_splash);
        boolean isShowJBLBrandManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME,this);
        if (!isShowJBLBrandManyTimes){
            //TODO: show JBL Brand related story.
            PreferenceUtils.setBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME, true, getApplicationContext());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDashBoard();
                }
            },3000);
        }else {
            showDashBoard();
        }
    }

    private void showDashBoard(){
        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
        finish();
    }
}