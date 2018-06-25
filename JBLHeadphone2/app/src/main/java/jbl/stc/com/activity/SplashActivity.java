package jbl.stc.com.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.utils.StatusBarUtil;
import jbl.stc.com.view.JblCircleView;

public class SplashActivity extends Activity {

    private JblCircleView jblCircleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LegalApi.INSTANCE.eulaInit(this);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.background));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        }, 10000);

        jblCircleView=(JblCircleView)findViewById(R.id.jblCircleView);
        jblCircleView.start();
    }
}
