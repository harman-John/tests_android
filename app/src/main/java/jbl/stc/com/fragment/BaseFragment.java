package jbl.stc.com.fragment;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;

import java.util.ArrayList;

import jbl.stc.com.activity.AnalyticsApplication;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.listener.OnMainAppListener;
import jbl.stc.com.utils.LogUtil;

/**
 * BaseFragment
 * <p>
 * Created by darren.lu on 08/06/2017.
 */
public class BaseFragment extends Fragment implements View.OnTouchListener, audioManager.AudioDeviceDelegate {
    protected String TAG;
    protected Context mContext = null;
    protected View rootView;
    protected AnalyticsApplication application;
    private OnMainAppListener onMainAppListener;

    protected static final int X_SPEED_MIN = 150;
    protected static final int Y_SPEED_MIN = 60;
    protected static final int X_DISTANCE_MIN = 150;
    protected static final int Y_DISTANCE_MIN = 100;
    protected static final int X_START_MIN = 60;
    private float xDown;
    private float xMove;
    protected float yDown;
    protected float yMove;
    protected boolean mIsWaitUpEvent = false;
    protected VelocityTracker mVelocityTracker;
    private boolean enableSideBack = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        TAG = this.getClass().getSimpleName();
        application = (AnalyticsApplication) getActivity().getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setStatusBar();
        //getOnMainAppListener().getMainActivity().setDeviceDelegate(this);
        if (rootView != null) {
            rootView.setOnTouchListener(this);
        }
    }

    protected boolean onBackPressed() {
        LogUtil.d(TAG, "onBackPressed()");
        return true;
    }

    protected void setStatusBar() {

    }

    public void disableSideBack() {
        enableSideBack = false;
    }

    public void enableSideBack() {
        enableSideBack = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getOnMainAppListener().getMainActivity().setDeviceDelegate(this);
        LogUtil.d(TAG, "onResume()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView()");
    }

    public OnMainAppListener getOnMainAppListener() {
        if (onMainAppListener == null) {//if not set OnMainAppListener
            onMainAppListener = new OnMainAppListener() {
                @Override
                public void backToDashboardPage() {

                }

                @Override
                public void refreshPage() {

                }

                @Override
                public DashboardActivity getMainActivity() {
                    return null;
                }

                @Override
                public void showOrHideFragment(boolean isShow, BaseFragment baseFragment) {

                }
            };
        }
        return onMainAppListener;
    }

    public void setOnMainAppListener(OnMainAppListener onMainAppListener) {
        this.onMainAppListener = onMainAppListener;
    }

    protected void hideSoftKeyBoard() {
        try {
            if (getActivity() != null && getActivity().getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Service.INPUT_METHOD_SERVICE);
                IBinder windowToken = getActivity().getCurrentFocus()
                        .getWindowToken();
                imm.hideSoftInputFromWindow(windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS);
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        if (enableSideBack) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xDown = event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    xMove = event.getRawX();
                    int distanceX = (int) (xMove - xDown);
                    int xSpeed = getScrollVelocity();
                    if (xDown < X_START_MIN && distanceX > X_DISTANCE_MIN && xSpeed > X_SPEED_MIN) {
                        getOnMainAppListener().getMainActivity().onBackPressed();
                        getOnMainAppListener().getMainActivity().refreshPage();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    recycleVelocityTracker();
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     */
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

    @Override
    public void receivedAdminEvent(AdminEvent adminEvent, Object value) {

    }

    @Override
    public void receivedResponse(String s, ArrayList<responseResult> arrayList, Status status) {

    }

    @Override
    public void receivedStatus(StatusEvent statusEvent, Object o) {

    }

    @Override
    public void receivedPushNotification(Action action, String s, ArrayList<responseResult> arrayList, Status status) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}