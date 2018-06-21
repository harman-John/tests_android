package jbl.stc.com.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.dialog.CreateMyOwnEqDialog;
import jbl.stc.com.fragment.ANCFragment;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.fragment.EqCustomFragment;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.listener.OnMainAppListener;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FastClickHelper;
import jbl.stc.com.utils.LogUtil;

public class DashboardActivity extends BaseActivity implements View.OnClickListener, OnMainAppListener {

    private ImageView logoImageView,
            settingImageView, smartAmbientImage,
            deviceImageView, autoOffImage,
            eqSwitchImageView;
    private LinearLayout eqSwitchLayout;
    private FrameLayout eqInfoLayout;
    private TextView eqNameText, titleEqText,
            batteryLevelText, eqTextView,
            autoOffTextView;
    private View eqDividerView;
    private ProgressBar batteryProgressBar;
    private CreateMyOwnEqDialog createMyOwnEqDialog;
    private boolean currPageIsDashboard = true;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initView();
    }

    private void initView() {
        logoImageView = findViewById(R.id.logoImageView);
        settingImageView = findViewById(R.id.settingImageView);
        smartAmbientImage = findViewById(R.id.smartAmbientImage);
        deviceImageView = findViewById(R.id.deviceImageView);
        autoOffImage = findViewById(R.id.autoOffImage);
        eqSwitchLayout = findViewById(R.id.eqSwitchLayout);
        eqInfoLayout = findViewById(R.id.eqInfoLayout);
        eqSwitchImageView = findViewById(R.id.eqSwitchImageView);
        eqNameText = findViewById(R.id.eqNameText);
        titleEqText = findViewById(R.id.titleEqText);
        eqDividerView = findViewById(R.id.eqDividerView);
        batteryProgressBar = findViewById(R.id.batteryProgressBar);
        batteryLevelText = findViewById(R.id.batteryLevelText);
        eqTextView = findViewById(R.id.eqTextView);
        autoOffTextView = findViewById(R.id.autoOffTextView);

        eqInfoLayout.setVisibility(View.VISIBLE);
        eqInfoLayout.setOnClickListener(this);

        createMyOwnEqDialog = new CreateMyOwnEqDialog(this);
        createMyOwnEqDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment());
            }

            @Override
            public void onCancel() {

            }
        });


        /*eqInfoLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                createVelocityTracker(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        yDown = event.getRawY();
                        xDown = event.getRawX();
                        yMove = yDown;
                        xMove = xDown;
                        mIsWaitUpEvent = true;
                        mHandler.postDelayed(mTimerForUpEvent, MAX_INTERVAL_FOR_CLICK);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        yMove = event.getRawY();
                        xMove = event.getRawX();
                        if (mIsWaitUpEvent) {
                            int distanceY = (int) (yDown - yMove);
                            int ySpeed = getScrollVelocity();
                            if (distanceY > Y_DISTANCE_MIN && ySpeed > Y_SPEED_MIN) {
                                mHandler.removeCallbacks(mTimerForUpEvent);
                                mIsWaitUpEvent = false;
                                eqInfoLayout.performClick();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        recycleVelocityTracker();
                        if (Math.abs(yDown - yMove) > MAX_DISTANCE_FOR_CLICK
                                || Math.abs(xDown - xMove) > MAX_DISTANCE_FOR_CLICK) {
                            mHandler.removeCallbacks(mTimerForUpEvent);
                            mIsWaitUpEvent = false;
                        }
                        LogUtil.d(TAG, "ACTION_UP mIsWaitUpEvent=$mIsWaitUpEvent");
                        if (mIsWaitUpEvent) {
                            mHandler.removeCallbacks(mTimerForUpEvent);
                            mIsWaitUpEvent = false;
                            eqInfoLayout.performClick();
                        }

                        break;
                }
                return true;
            }
        });*/

    }

    public void switchFragment(BaseFragment baseFragment) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            baseFragment.setOnMainAppListener(this);
            ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            currPageIsDashboard=false;
            if (getSupportFragmentManager().findFragmentById(R.id.containerLayout) == null) {
                ft.add(R.id.containerLayout, baseFragment);
            } else {
                ft.replace(R.id.containerLayout, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commit();
            //getSupportFragmentManager().executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            currPageIsDashboard = (backStackEntryCount == 1);
            LogUtil.d(TAG, "currPageIsDashboard=" + currPageIsDashboard);
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void backToDashboardPage() {

    }

    public void refreshPage() {
        LogUtil.d(TAG, "refreshPage()");
        //updateAutoOffImage(false);
        //updateEqSwitchImage(false);
        //updateSmartAmbientImage(false);
        //updateEqInfoLayout();
        //updateSettingImage();
    }

    @Override
    public DashboardActivity getMainActivity() {
        return this;
    }

    @Override
    public void showOrHideFragment(boolean isShow, BaseFragment baseFragment) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eqSwitchImageView:

                break;
            case R.id.eqInfoLayout: {
                if (FastClickHelper.isFastClick()) {
                    return;
                }
                switchFragment(new EqSettingFragment());
                //createMyOwnEqDialog.show();
                break;
            }
        }
    }
}
