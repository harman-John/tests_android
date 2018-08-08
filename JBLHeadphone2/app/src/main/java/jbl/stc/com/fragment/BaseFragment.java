package jbl.stc.com.fragment;

import android.app.Service;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.listener.AppLightXDelegate;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.listener.OnMainAppListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;

public class BaseFragment extends Fragment implements View.OnTouchListener, AppLightXDelegate, AppUSBDelegate {
    protected String TAG;
    protected Context mContext = null;
    protected View rootView;
    protected JBLApplication application;
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
    protected VelocityTracker mVelocityTracker;
    private boolean enableSideBack = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        TAG = this.getClass().getSimpleName();
        application = (JBLApplication) getActivity().getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (rootView != null) {
            rootView.setOnTouchListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        DeviceManager.getInstance(getActivity()).setAppLightXDelegate(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(TAG, "onDestroyView()");
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
                        getActivity().onBackPressed();
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
    public void onDestroy() {
        super.onDestroy();
    }

    public void switchFragment(Fragment baseFragment,int type) {
        try {
            android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            }else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT){
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            }else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT){
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            }
            if (getActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout) == null) {
                ft.add(R.id.containerLayout, baseFragment);
            } else {
                ft.replace(R.id.containerLayout, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAllFragment() {
        Fragment fr = getActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (fr == null) {
            Logger.d(TAG,"fr is null");
            return;
        }
        try {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            int count = manager.getBackStackEntryCount();
            Logger.d(TAG, "count = " + count);
            while (count > 0) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                manager = getActivity().getSupportFragmentManager();
                count = manager.getBackStackEntryCount();
                Logger.d(TAG, "back stack count = " + count);
            }
        }catch (Exception e){
            Logger.e(TAG,"Fragment is not shown, then popBack will have exception ");
        }
    }

    public void updateDeviceNameAndImage(String deviceName, ImageView imageViewDevice, TextView textViewDeviceName) {
        if (TextUtils.isEmpty(deviceName)) {
            return;
        }
        //update device name
        textViewDeviceName.setText(deviceName);
        //update device image
        if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_REFLECT_AWARE).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.reflect_aware_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_100_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_150nc_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_300_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_700_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_750nc_icon);
        }
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command var2, boolean var3, byte[] var4) {

    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command var2, byte[] var3) {

    }

    @Override
    public void lightXAppWriteResult(LightX var1, Command var2, boolean var3) {

    }

    @Override
    public void lightXError(LightX var1, Exception var2) {

    }

    @Override
    public boolean lightXFirmwareReadStatus(LightX var1, LightX.FirmwareRegion var2, int var3, byte[] var4) {
        return false;
    }

    @Override
    public boolean lightXFirmwareWriteStatus(LightX var1, LightX.FirmwareRegion var2, LightX.FirmwareWriteOperation var3, double var4, Exception var6) {
        return false;
    }

    @Override
    public void lightXIsInBootloader(LightX var1, boolean var2) {

    }

    @Override
    public void lightXReadConfigResult(LightX var1, Command var2, boolean var3, String var4) {

    }

    @Override
    public boolean lightXWillRetransmit(LightX var1, Command var2) {
        return false;
    }

    @Override
    public void isLightXInitialize() {

    }

    @Override
    public void headPhoneStatus(boolean isConnected) {

    }

    @Override
    public void lightXReadBootResult(LightX var1, Command command, boolean success, int var4, byte[] var5) {

    }

    @Override
    public void receivedAdminEvent(AdminEvent event, Object value) {
        switch (event){
            case AccessoryReady:{
                PreferenceUtils.setBoolean(PreferenceKeys.RECEIVE_READY, true, mContext);
                break;
            }
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {

    }

    @Override
    public void receivedStatus(StatusEvent name, Object value) {

    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {
        switch (command){
            case AmCmds.CMD_ANCNotification: {
                PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, Integer.valueOf(values.iterator().next().getValue().toString() ), getActivity());
                break;
            }
            case AmCmds.CMD_AmbientLevelingNotification: {
                int parseValue = Integer.valueOf(values.iterator().next().getValue().toString());
                PreferenceUtils.setInt(PreferenceKeys.AWARENESS, AppUtils.levelTransfer(parseValue), getActivity());
                PreferenceUtils.setBoolean(PreferenceKeys.RECEIVEPUSH, true, mContext);
            }
            break;
        }
    }

    @Override
    public void usbAttached(UsbDevice usbDevice) {

    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {

    }
}