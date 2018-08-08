package jbl.stc.com.manager;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.audioManager;
import com.avnera.smartdigitalheadset.Bluetooth;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.USB;

import java.util.HashMap;
import java.util.Iterator;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;


public class BaseDeviceManager implements AppUSBDelegate {
    private final static String TAG = BaseDeviceManager.class.getSimpleName();
    protected Activity mContext;
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

    public void setOnCreate(Activity context) {
        mContext = context;
        LightX.sEnablePacketDumps = false;
        usbReceiver = new USBReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(usbReceiver, intentFilter);
    }

    public void setOnDestroy() {
        mContext.unregisterReceiver(usbReceiver);
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
        return PreferenceUtils.getString(PreferenceKeys.JBL_HEADSET_MAC_ADDRESS, mContext, null);
    }

    /**
     * Save JBL headset information if its valid case for connection
     */
    protected void saveJBLHeadsetInfo(String name, String macAddress) {
        PreferenceUtils.setString(PreferenceKeys.JBL_HEADSET_MAC_ADDRESS, macAddress, mContext);
        PreferenceUtils.setString(PreferenceKeys.JBL_HEADSET_NAME, name, mContext);    // advisory
    }

    /**
     * App exit dialog
     */
    public void showExitDialog(String message) {
        AlertsDialog.bluetoothAlertFinish(null, message, mContext);
    }

    private class USBReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = (UsbDevice) intent.getExtras().get(UsbManager.EXTRA_DEVICE);
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                BaseDeviceManager.this.usbDetached(usbDevice);
            } else {
                BaseDeviceManager.this.usbAttached(usbDevice);
            }
        }
    }

    public HashMap<String, UsbDevice> getAllAttachedUSBdeviced() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager.getDeviceList();
    }
}