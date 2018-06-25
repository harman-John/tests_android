package jbl.stc.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends BaseActivity implements View.OnClickListener{
    private JblCircleView jblCircleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        LegalApi.INSTANCE.eulaInit(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(new HomeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }
        }, 2000);

        jblCircleView=findViewById(R.id.jblCircleView);
        jblCircleView.start();
    }

    @Override
    public void onClick(View v) {

    }

}
