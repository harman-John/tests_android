package jbl.stc.com.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;


import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.utils.StatusBarUtil;

/**
 * BaseActivity
 * Created by darren.lu on 08/06/2017.
 */
public class BaseActivity extends FragmentActivity {
    protected String TAG = BaseActivity.class.getSimpleName();
    protected Context mContext;
    protected static final int Y_SPEED_MIN = 60;
    protected static final int Y_DISTANCE_MIN = 100;
    protected static final int MAX_DISTANCE_FOR_CLICK = 100;
    protected static final long MAX_INTERVAL_FOR_CLICK = 300;

    protected EQModel currEqModel = null;

    protected boolean mIsWaitUpEvent = false;
    protected float yDown;
    protected float yMove;
    protected float xDown;
    protected float xMove;
    protected VelocityTracker mVelocityTracker;
    protected Runnable mTimerForUpEvent = new Runnable() {
        public void run() {
            mIsWaitUpEvent = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        TAG = this.getClass().getSimpleName();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.statusBarBackground));
    }

    public void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    public void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    public int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

}