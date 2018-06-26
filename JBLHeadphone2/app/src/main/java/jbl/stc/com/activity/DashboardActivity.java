package jbl.stc.com.activity;

import android.app.Fragment;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends BaseActivity implements View.OnClickListener {
    private JblCircleView jblCircleView;
    private ImageView image_view_logo;
    private LinearLayout ll_cannot_see;
    private TextView txtTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        LegalApi.INSTANCE.eulaInit(this);
        initview();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                jblCircleView.setVisibility(View.GONE);
                jblCircleView.stop();
                image_view_logo.setVisibility(View.GONE);
                ll_cannot_see.setVisibility(View.VISIBLE);
                txtTips.setVisibility(View.VISIBLE);

            }
        }, 2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }
        }, 5000);


    }

    private void initview() {
        jblCircleView = (JblCircleView) findViewById(R.id.jblCircleView);
        jblCircleView.addWave();
        jblCircleView.start();
        image_view_logo = (ImageView) findViewById(R.id.image_view_logo);
        ll_cannot_see = (LinearLayout) findViewById(R.id.ll_cannot_see);
        txtTips = (TextView) findViewById(R.id.txtTips);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        if (jblCircleView != null)
            jblCircleView.stop();

        super.onDestroy();
    }

}
