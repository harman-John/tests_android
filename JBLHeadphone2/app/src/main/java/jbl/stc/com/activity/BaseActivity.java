package jbl.stc.com.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.listener.AppLightXDelegate;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.listener.ConnectListener;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.utils.StatusBarUtil;


public class BaseActivity extends FragmentActivity implements AppUSBDelegate ,View.OnTouchListener, AppLightXDelegate,OnDownloadedListener,ConnectListener {
    private final static String TAG = BaseActivity.class.getSimpleName();
    protected Context mContext;
    protected USBReceiver usbReceiver;
    public static boolean isOTADoing = false;
    protected static List<MyDevice> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceManager.getInstance(this).setConnectListener(this);
        mContext = this;
        LightX.sEnablePacketDumps = false;
        usbReceiver = new USBReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, intentFilter);
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
        DeviceManager.getInstance(this).setConnectListener(this);
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
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
        unregisterReceiver(usbReceiver);
    }

    public void checkMyDevice(){
        Set<String> deviceList = DeviceManager.getInstance(this).getDevicesSet();
        Logger.i(TAG, "MSG_CHECK_DEVICES deviceList = " + deviceList);
        if (hasNewDevice(deviceList)) {
            initMyDeviceList();
        }
        updateMyDeviceStatus(deviceList);
    }

    public void initMyDeviceList() {
        lists.clear();
        Set<String> devicesSet = PreferenceUtils.getStringSet(getApplicationContext(), PreferenceKeys.MY_DEVICES);
        Logger.i(TAG, "deviceSet = " + devicesSet);
        for (String value : devicesSet) {
            lists.add(AppUtils.getMyDevice(value));
        }
    }

    public void updateMyDeviceStatus(Set<String> deviceList) {
        Logger.i(TAG, "updateMyDeviceStatus lists= " + lists.size());
        for (MyDevice myDevice : lists) {
            myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
        }
        List<MyDevice> myDeviceListA2dp = new ArrayList<>();
        for (String key : deviceList) {
            myDeviceListA2dp.add(AppUtils.getMyDevice(key));
        }
        for (MyDevice myDeviceA2dp : myDeviceListA2dp) {
            for (MyDevice myDevice : lists) {
                if (myDeviceA2dp.equals(myDevice)) {
                    Logger.i(TAG, "myDeviceA2dp deviceKey= " + myDeviceA2dp.deviceKey);
                    if (myDevice.deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                        Logger.i(TAG, "isConnected = " + DeviceManager.getInstance(this).isConnected());
                        if (DeviceManager.getInstance(this).isConnected()) {
                            myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
                        }
                    } else {
                        if (DeviceManager.getInstance(this).getSpecifiedDevice() != null) {
                            String mainDeviceKey = DeviceManager.getInstance(this).getSpecifiedDevice().getName() + "-" + DeviceManager.getInstance(this).getSpecifiedDevice().getAddress();
                            Logger.i(TAG, "mainDeviceKey = " + mainDeviceKey);
                            if (DeviceManager.getInstance(this).isConnected() && mainDeviceKey.toUpperCase().equalsIgnoreCase(myDeviceA2dp.deviceKey.toUpperCase())) {
                                Logger.i(TAG, "DEVICE_CONNECTED = " + mainDeviceKey);
                                myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
                            } else {
                                myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                            }
                        } else {
                            myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                        }
                    }
                }
            }
        }
    }

    public void updateDisconnectedAdapter() {
        for (MyDevice myDevice : lists) {
            Logger.i(TAG, "updateDisconnectedAdapter deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
                break;
            }
        }
    }

    public MyDevice getMyDeviceConnected() {
        for (MyDevice myDevice : lists) {
            Logger.i(TAG, "getMyDeviceConnected deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                return myDevice;
            }
        }
        return null;
    }

    public boolean hasNewDevice(Set<String> deviceList) {
        Set<String> set1 = new HashSet<>(deviceList);
        Set<String> set2 = new HashSet<>();
        for (MyDevice myDevice : lists) {
            String device = myDevice.deviceKey;
            set2.add(device);
        }
        set2.retainAll(set1);
        if (set2.size() == set1.size()) {
            return false;
        }
        Logger.i(TAG, "has new Device");
        return true;
    }

    private boolean isStopped = false;
    public boolean isStopped(){
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
            }else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT){
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            }else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT){
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
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
            Logger.d(TAG,"fr is null");
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
    public void usbAttached(UsbDevice usbDevice) {

    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {

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

    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {

    }

    @Override
    public void receivedStatus(StatusEvent name, Object value) {

    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {

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

    public static boolean isConnectedCalled = false;
    @Override
    public void connectDeviceStatus(boolean isConnected) {
        if (isConnected) {
            isConnectedCalled = true;
        }
    }

    @Override
    public void checkDevices(Set<String> deviceList) {

    }

    private class USBReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = (UsbDevice) intent.getExtras().get(UsbManager.EXTRA_DEVICE);
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                BaseActivity.this.usbDetached(usbDevice);
            } else {
                BaseActivity.this.usbAttached(usbDevice);
            }
        }
    }

    public HashMap<String, UsbDevice> getAllAttachedUSBdeviced() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        return manager.getDeviceList();
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
                boolean isStopped = ((BaseActivity)(activityStack.get(i))).isStopped();
                Logger.i(TAG,"isStopped = "+isStopped +",activity = "+activityStack.get(i));
                if (isStopped){
                    count ++;
                }
            }
        }

        if (count == activityStack.size()){
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