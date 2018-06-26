package jbl.stc.com.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.avnera.audiomanager.AdminEvent;

import org.jetbrains.annotations.NotNull;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener{
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private JblCircleView jblCircleView;
    private static DashboardActivity dashboardActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        dashboardActivity = this;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                switchFragment(new HomeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
//            }
//        }, 2000);

        jblCircleView=findViewById(R.id.jblCircleView);
        jblCircleView.start();
    }


    public static DashboardActivity getDashboardActivity() {
        return dashboardActivity;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void receivedAdminEvent(@NotNull AdminEvent event, Object value) {
        super.receivedAdminEvent(event, value);
        switch (event) {
            case AccessoryReady: {
                Log.d(TAG, " ========> AccessoryReady <======== ");
                switchFragment(new HomeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }
}
