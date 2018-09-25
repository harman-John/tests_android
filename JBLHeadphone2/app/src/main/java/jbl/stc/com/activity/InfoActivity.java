package jbl.stc.com.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;


import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.swipe.activity.ActivityLifecycleMgr;


public class InfoActivity extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_info);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        findViewById(R.id.text_jbl_com).setOnClickListener(this);
        findViewById(R.id.text_view_open_source_license).setOnClickListener(this);
        findViewById(R.id.text_view_eula).setOnClickListener(this);
        findViewById(R.id.text_view_harman_privacy_policy).setOnClickListener(this);
        findViewById(R.id.image_view_info_back).setOnClickListener(this);
        TextView textView = (TextView) findViewById(R.id.text_view_info_app_version);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            String version = getString(R.string.app_version) + packageInfo.versionName +"."+ packageInfo.versionCode;
            textView.setText(version);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_open_source_license:{
                LegalApi.INSTANCE.showOpenSource(this,false);
                break;
            }
            case R.id.text_view_eula:{
                LegalApi.INSTANCE.showEula(this,false);
                break;
            }
            case R.id.text_view_harman_privacy_policy:{
                LegalApi.INSTANCE.showPrivacyPolicy(this,false);
                break;
            }
            case R.id.image_view_info_back:{
//                onBackPressed();
                View decorView = this.getWindow().getDecorView();
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(decorView,"translationX",0, - decorView.getMeasuredWidth());
                objectAnimator.setDuration(600);
                objectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        finish();
                    }
                });
                objectAnimator.start();

                Activity activity = ActivityLifecycleMgr.getInstance().getPenultimateActivity();
                if (activity != null && !activity.isFinishing()) {
                    View decorView1 = activity.getWindow().getDecorView();
                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(decorView1,"translationX",decorView1.getMeasuredWidth(),0);
                    objectAnimator1.setDuration(500);
                    objectAnimator1.start();
                }

                break;
            }
            case R.id.text_jbl_com:{
                Uri uri = Uri.parse("http://www.jbl.com");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
                break;
            }
        }

    }
}
