package jbl.stc.com.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.avnera.smartdigitalheadset.Logger;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.LegalLandingFragment;
import jbl.stc.com.listener.DismissListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;


public class SplashActivity extends FragmentActivity  {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate");
        setContentView(R.layout.activity_splash);

//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        boolean isShowJBLBrandManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME,this);
        if (!isShowJBLBrandManyTimes){
            //TODO: show JBL Brand related story.
            PreferenceUtils.setBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME, true, getApplicationContext());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    showDashBoard();
                    boolean legalPersist = PreferenceUtils.getBoolean(PreferenceKeys.LEGAL_PERSIST,getApplicationContext());
                    if (!legalPersist){
                        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        LegalLandingFragment legalLandingFragment = new LegalLandingFragment();
                        legalLandingFragment.setOnDismissListener(new DismissListener(){

                            @Override
                            public void onDismiss(int reason) {
                                showDashBoard();
                            }
                        });
                        if (fr == null) {
                            switchFragment(legalLandingFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        }else if (!(fr instanceof  LegalLandingFragment)) {
                            jbl.stc.com.logger.Logger.i(TAG, "LegalLandingFragment");
                            switchFragment(legalLandingFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        }
                    }
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

    public void switchFragment(Fragment baseFragment, int type) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            }else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT){
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            }else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT){
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            }
            if (getSupportFragmentManager().findFragmentById(R.id.relative_layout_splash) == null) {
                ft.add(R.id.relative_layout_splash, baseFragment);
            } else {
                ft.replace(R.id.relative_layout_splash, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }
}