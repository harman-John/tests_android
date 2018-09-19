package jbl.stc.com.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.LightX;

import java.io.FileNotFoundException;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.listener.OnConnectStatusListener;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.listener.OnRetListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.swipe.SwipeBackActivity;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.utils.StatusBarUtil;
import jbl.stc.com.utils.UiUtils;


public class BaseActivity extends SwipeBackActivity implements AppUSBDelegate, View.OnTouchListener, OnDownloadedListener,OnRetListener, OnConnectStatusListener {
    private final static String TAG = BaseActivity.class.getSimpleName() + "aa";
    protected Context mContext;
    public static boolean isOTADoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        LiveManager.getInstance().setOnConnectStatusListener(this);
        LiveManager.getInstance().setOnRetListener(this);
        LightX.sEnablePacketDumps = false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStopped = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceManager.getInstance(this).setOnConnectStatusListener(this);
        LiveManager.getInstance().setOnRetListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishActivity(this);
    }


    private boolean isStopped = false;

    public boolean isStopped() {
        return isStopped;
    }

    private CheckUpdateAvailable checkUpdateAvailable;

    public void startCheckingIfUpdateIsAvailable(Object object) {
        Logger.d(TAG, "AppUtils.getModelNumber(this)=" + AppUtils.getModelNumber(this));
        Logger.d(TAG, "startCheckingIfUpdateIsAvailable isConnectionAvailable=" + FirmwareUtil.isConnectionAvailable(this));
        String srcSavedVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, this, "0.0.0");
        String currentVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.APP_VERSION, this, "");
        Logger.d(TAG, "srcSavedVersion = " + srcSavedVersion + ",currentVersion = " + currentVersion);
        if (FirmwareUtil.isConnectionAvailable(this) && !TextUtils.isEmpty(srcSavedVersion) && !TextUtils.isEmpty(currentVersion)) {
            Logger.d(TAG, "checkUpdateAvailable = " + checkUpdateAvailable);
            if (checkUpdateAvailable != null && checkUpdateAvailable.isRunnuning()) {
                Logger.d(TAG, "CheckUpdateAvailable is running so return");
                checkUpdateAvailable.cancel(true);
                checkUpdateAvailable = null;
            }
            Logger.d(TAG, "CheckUpdateAvailable.start()");
            checkUpdateAvailable = CheckUpdateAvailable.start(object, this, this, OTAUtil.getURL(this), srcSavedVersion, currentVersion);
        }
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.statusBarBackground));
    }

    public void switchFragment(BaseFragment baseFragment, int type) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            } else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT) {
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            } else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT) {
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            } else if (type == JBLConstant.FADE_IN_OUT) {
                ft.setCustomAnimations(R.anim.fadin, R.anim.fadeout, R.anim.fadin, R.anim.fadeout);
            }
            if (getSupportFragmentManager().findFragmentById(R.id.containerLayout) == null) {
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
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (fr == null) {
            Logger.d(TAG, "fr is null");
            return;
        }
        try {
            FragmentManager manager = getSupportFragmentManager();
            int count = manager.getBackStackEntryCount();
            while (count > 0) {
                getSupportFragmentManager().popBackStackImmediate();
                manager = getSupportFragmentManager();
                count = manager.getBackStackEntryCount();
                Logger.d(TAG, "back stack count = " + count);
            }
        } catch (Exception e) {
            Logger.e(TAG, "Fragment is not shown, then popBack will have exception ");
        }
    }

    public void updateDeviceNameAndImage(String deviceName, ImageView imageViewDevice, TextView textViewDeviceName) {
        UiUtils.setDeviceName(deviceName, textViewDeviceName);
        UiUtils.setDeviceImage(deviceName, imageViewDevice);
    }

    @Override
    public void usbAttached(UsbDevice usbDevice) {

    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwlist) throws FileNotFoundException {

    }

    @Override
    public void onFailedDownload() {

    }

    @Override
    public void onFailedToCheckUpdate() {

    }

    @Override
    public void onUpgradeUpdate(String liveVersion, String title) {

    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {

    }

    @Override
    public void onConnectStatus(Object... objects) {

    }

    private static Stack<Activity> activityStack;


    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    public boolean isForeground() {
        int count = 0;
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                boolean isStopped = ((BaseActivity) (activityStack.get(i))).isStopped();
                Logger.i(TAG, "isStopped = " + isStopped + ",activity = " + activityStack.get(i));
                if (isStopped) {
                    count++;
                }
            }
        }

        if (count == activityStack.size()) {
            return false;
        }
        return true;
    }

    public void exitApp(Context context) {
        try {
            finishAllActivity();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
        }
    }
}