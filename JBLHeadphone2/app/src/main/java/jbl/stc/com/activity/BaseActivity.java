package jbl.stc.com.activity;

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
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Bluetooth;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.USB;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.listener.AppLightXDelegate;
import jbl.stc.com.listener.AppUSBDelegate;
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


public class BaseActivity extends FragmentActivity implements AppUSBDelegate ,View.OnTouchListener, AppLightXDelegate,OnDownloadedListener {
    private final static String TAG = BaseActivity.class.getSimpleName();
    protected Context mContext;
    public static final String JBL_HEADSET_MAC_ADDRESS = "com.jbl.headset.mac_address";
    public static final String JBL_HEADSET_NAME = "com.jbl.headset.name";
    public static final String ACTION_USB_PERMISSION = "com.stc.USB_PERMISSION";
    public LightX mLightX;
    protected boolean isNeedShowDashboard;
    protected boolean disconnected;
    // Bluetooth Delegate
    //Initialize donSendCallback with False. Tutorial screen issue fix.
    public boolean donSendCallback = true;
    public BluetoothDevice mBluetoothDevice;
    protected Bluetooth mBluetooth;
    audioManager bt150Manager = null;
    protected USB mUSB;
    protected PendingIntent mPermissionIntent;
    protected USBReceiver usbReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    protected void onResume() {
        super.onResume();
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
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

    /**
     * @param deviceList
     * @return return JBL Aware device if found
     */
    public UsbDevice foundJBLAwareDevice(HashMap<String, UsbDevice> deviceList) {
        if (deviceList == null)
            return null;
        UsbDevice usbDevice = null;
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            usbDevice = deviceIterator.next();
            Logger.d(TAG, "Device ===Product ID" + usbDevice.getProductId() + "-Vendor ID--" + usbDevice.getVendorId());
        }
        return usbDevice;
    }

    // Members and methods to support Avnera hardware
    public synchronized boolean shouldConnectToBluetoothDevice(BluetoothDevice bluetoothDevice) {
        String deviceMACAddress;
        String deviceName;
        String macAddressOfSavedJBLHeadset;
        boolean result = false;

        if (bluetoothDevice != null) {
            deviceMACAddress = bluetoothDevice.getAddress().toUpperCase();
            deviceName = bluetoothDevice.getName();
            macAddressOfSavedJBLHeadset = getMACAddressOfSavedJBLHeadset();
            if (macAddressOfSavedJBLHeadset != null && macAddressOfSavedJBLHeadset.equalsIgnoreCase(deviceMACAddress)) {
                result = true;
            }

            if (AppUtils.isMatchDeviceName(deviceName)) {
                saveJBLHeadsetInfo(deviceName, deviceMACAddress);
                result = true;
            }
        }
        Logger.d(TAG, "shouldConnectToBluetoothDevice result is " + result);
        return result;
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

    /**
     * @return last saved mac address
     */
    public String getMACAddressOfSavedJBLHeadset() {
        return PreferenceUtils.getString(PreferenceKeys.JBL_HEADSET_MAC_ADDRESS, this, null);
    }

    /**
     * Save JBL headset information if its valid case for connection
     */
    protected void saveJBLHeadsetInfo(String name, String macAddress) {
        PreferenceUtils.setString(PreferenceKeys.JBL_HEADSET_MAC_ADDRESS, macAddress, this);
        PreferenceUtils.setString(PreferenceKeys.JBL_HEADSET_NAME, name, this);    // advisory
    }

    /**
     * App exit dialog
     */
    public void showExitDialog(String message) {
        AlertsDialog.bluetoothAlertFinish(null, message, this);
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
}