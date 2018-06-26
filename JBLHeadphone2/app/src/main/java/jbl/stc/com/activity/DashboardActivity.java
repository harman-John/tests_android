package jbl.stc.com.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avnera.audiomanager.AdminEvent;

import org.jetbrains.annotations.NotNull;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener{
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private JblCircleView jblCircleView;
    private static DashboardActivity dashboardActivity;
    private ImageView image_view_logo;
    private LinearLayout ll_cannot_see;
    private TextView txtTips;
    private final static int SHOW_UNDISCOVERY_TIPS = 0;
    private DashboardHandler dashboardHandler = new DashboardHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        dashboardActivity = this;
        initview();
        dashboardHandler.sendEmptyMessageDelayed(SHOW_UNDISCOVERY_TIPS,10000);
    }

    private void initview() {
        jblCircleView = (JblCircleView) findViewById(R.id.jblCircleView);
        jblCircleView.addWave();
        jblCircleView.start();
        image_view_logo = (ImageView) findViewById(R.id.image_view_logo);
        ll_cannot_see = (LinearLayout) findViewById(R.id.ll_cannot_see);
        txtTips = (TextView) findViewById(R.id.txtTips);
    }


    public static DashboardActivity getDashboardActivity() {
        return dashboardActivity;
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

    public void receivedAdminEvent(@NotNull AdminEvent event, Object value) {
        super.receivedAdminEvent(event, value);
        switch (event) {
            case AccessoryReady: {
                Log.d(TAG, " ========> AccessoryReady <======== ");
                dashboardHandler.removeMessages(SHOW_UNDISCOVERY_TIPS);
                switchFragment(new HomeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }

    private class DashboardHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_UNDISCOVERY_TIPS: {
                    jblCircleView.setVisibility(View.GONE);
                    jblCircleView.stop();
                    image_view_logo.setVisibility(View.GONE);
                    ll_cannot_see.setVisibility(View.VISIBLE);
                    txtTips.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }
}
