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
        boolean isNotFirstEnterApp = PreferenceUtils.getBoolean(PreferenceKeys.FIRST_TIME_ENTER_APP,this);
        if (!isNotFirstEnterApp){
            //TODO: show JBL Brand related story.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDashBoard();
                }
            },5000);
        }else {
            showDashBoard();
        }
    }

    private void showDashBoard(){
        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
        finish();
    }
}