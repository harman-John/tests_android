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
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;


import com.avnera.audiomanager.audioManager;
import com.avnera.smartdigitalheadset.Bluetooth;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.USB;

import java.util.HashMap;
import java.util.Iterator;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.StatusBarUtil;

/**
 * BaseActivity
 * Created by darren.lu on 08/06/2017.
 */
public class BaseActivity extends FragmentActivity implements AppUSBDelegate {
    private final static String TAG = BaseActivity.class.getSimpleName();
    protected Context mContext;
    public static final String JBL_HEADSET_MAC_ADDRESS = "com.jbl.headset.mac_address";
    public static final String JBL_HEADSET_NAME = "com.jbl.headset.name";
    public static final String ACTION_USB_PERMISSION = "com.stc.USB_PERMISSION";
    public LightX mLightX;
    public static boolean isConnected = false;
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
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

        try {
            FragmentManager manager = getSupportFragmentManager();
            int count = manager.getBackStackEntryCount();
            Log.i(TAG, "count = " + count);
            while (count > 0) {
                getSupportFragmentManager().popBackStackImmediate();
                manager = getSupportFragmentManager();
                count = manager.getBackStackEntryCount();
                Log.i(TAG, "back stack count = " + count);
            }
        }catch (Exception e){
            Log.e(TAG,"Fragment is not shown, then popBack will have exception ");
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
            Log.d(TAG, "Device ===Product ID" + usbDevice.getProductId() + "-Vendor ID--" + usbDevice.getVendorId());
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
        Log.d(TAG, "shouldConnectToBluetoothDevice result is " + result);
        return result;
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