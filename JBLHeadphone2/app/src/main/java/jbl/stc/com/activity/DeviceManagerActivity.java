package jbl.stc.com.activity;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Bluetooth;
import com.avnera.smartdigitalheadset.BluetoothSocketWrapper;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.ModuleId;
import com.avnera.smartdigitalheadset.USB;
import com.avnera.smartdigitalheadset.USBSocket;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.fragment.CalibrationFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.listener.AppLightXDelegate;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AmToolUtil;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;


public class DeviceManagerActivity extends BaseActivity implements Bluetooth.Delegate, LightX.Delegate, USB.Delegate, audioManager.AudioDeviceDelegate {
    private static final String TAG = DeviceManagerActivity.class.getSimpleName();
    private static final String TAGReconnect = TAG + " reconnection";
    public static boolean mIsInBootloader;
    private static final int RESET_TIME = 10 * 1000;
    private static final int RESET_TIME_FOR_150NC = 2 * 1000;
    private int resetTime = RESET_TIME;
    private AppLightXDelegate appLightXDelegate;
    private UsbManager usbManager;
    private boolean mIsConnectedPhysically;
    private boolean showCommunicationIssue = true;
    private Handler mHandler = new Handler();

    public void setAppLightXDelegate(AppLightXDelegate appLightXDelegate) {
        this.appLightXDelegate = appLightXDelegate;
        Logger.e(TAG, "setAppLightXDelegate = " + appLightXDelegate.toString());
    }

    public AppLightXDelegate getAppLightXDelegate() {
        return appLightXDelegate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate");
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateChange, intentFilter);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            initUSB();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                synchronized (this) {
//                    if (FirmwareUtil.isUpdatingFirmWare.get()) {
                        initUSB();
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        donSendCallback = false;
    }

    public void connectDeviceStatus(boolean isConnected) {
        Logger.i(TAG, "connectDeviceStatus isConnected = " + isConnected);
    }

    protected synchronized void initUSB() {
        Logger.d(TAG, "Initializing USB first");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Logger.d(TAG, "deviceList size = "+deviceList.size());
        for (UsbDevice usbDevice : deviceList.values()) {
            device = usbDevice;
        }
        if (device != null && (device.getProductId() == JBLConstant.USB_PRODUCT_ID
                || device.getProductId() == JBLConstant.USB_PRODUCT_ID2
                || device.getProductId() == JBLConstant.USB_PRODUCT_ID3
                || device.getProductId() == JBLConstant.USB_PRODUCT_ID_BOOT3)) {
            USBLib(usbManager, device);
        } else {
            Logger.d(TAG, "Aware not found. Initializing Bluetooth");
            initializeOrResetLibrary();
        }
    }

    private void USBLib(UsbManager usbManager, UsbDevice device) {
//        mHandler.removeCallbacks(runnableToast);
        mHandler.removeCallbacks(resetRunnable);
        mUSB = new USB(this, this);
        mUSB.sUSBVendorIds.add(JBLConstant.USB_VENDOR_ID);
        mUSB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID);
        mUSB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID2);
        mUSB.sUSBVendorIds.add(JBLConstant.USB_VENDOR_ID3);
        mUSB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID3);
        mUSB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID_BOOT3);

        Logger.d(TAG, "Aware found. Requesting for permission");
        if (!usbManager.hasPermission(device)) {
            usbManager.requestPermission(device, mPermissionIntent);
        } else {
            mUSB.deviceAttached(device);
        }
    }

    private void close150NCManager() {
        if (bt150Manager != null) {
            bt150Manager.setDelegate(null);
            bt150Manager = null;
        }
    }

    /**
     * <p>Initialize or reset library. Its used when app start or headphone timeout a command from app<p/>
     */
    private void initializeOrResetLibrary() {
        Logger.d(TAG, "initializeOrResetLibrary");
        if (mBluetooth != null) {
            Logger.d(TAG, "discovery mBluetooth is not null");
            return;
        }

        isFound = false;
        specifiedDevice = null;
        try {
            disconnectAllBluetoothConnection();
            close150NCManager();
        } catch (Exception e) {
            Logger.d(TAG, "initializeOrResetLibrary Exception while disconnecting bluetooth library");
        }

        Logger.d(TAG, "begin to initializeOrResetLibrary ...");
        try {
            if (bt150Manager == null)
                bt150Manager = new audioManager();
            mBluetooth = new Bluetooth(this, this, true);
//            mBluetooth.start();
        } catch (Exception e) {
            showExitDialog("Unable to enable Bluetooth.");
        }
        mHandler.postDelayed(a2dpRunable, 500);
    }

    private void initAudioManager() {
        if (bt150Manager != null) {
            Logger.d(TAG, "150nc initManager");
            byte[] bytes = AmToolUtil.INSTANCE.readAssertResource(this, AmToolUtil.COMMAND_FILE);
            bt150Manager.initManager(this, this, this, AmToolUtil.COMMAND_FILE, bytes);
        }
    }

    private Runnable a2dpRunable = new Runnable() {
        @Override
        public void run() {
            if (isConnected && !isNeedOtaAgain) {
                Logger.d(TAG, "device is connected, return");
                return;
            }
            if (isFound ) {
                Logger.d(TAG, "device is found, return");
                return;
            }
            Logger.d(TAG, "startA2DPCheck ... isConnected = " + isConnected);
            startA2DPCheck();
            mHandler.postDelayed(a2dpRunable, 2000);
        }
    };


    public void checkDevices(Set<String> deviceList) {

    }

    public void startA2DPCheck() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            mBtAdapter.getProfileProxy(this, mListener, BluetoothProfile.A2DP);
        }
    }

    public void removeDeviceList(String key) {
        devicesSet.remove(key);
    }

    public BluetoothDevice getSpecifiedDevice() {
        return specifiedDevice;
    }

    public BluetoothDevice specifiedDevice = null;
    private static boolean isFound = false;
    private int position = 0;
    public Set<String> devicesSet = new HashSet<>();

    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                List<BluetoothDevice> deviceList = proxy.getConnectedDevices();
                String temp = null;
                for (String key : devicesSet) {
                    if (key.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                        temp = key;
                        break;
                    }
                }
                devicesSet.clear();
                if (temp != null)
                    devicesSet.add(temp);
                for (BluetoothDevice bluetoothDevice : deviceList) {
                    Logger.d(TAG, "A2DP connected device, name = " + bluetoothDevice.getName()
                            + ",address = " + bluetoothDevice.getAddress()
                            + ",position =" + position);
                    String key = bluetoothDevice.getName() + "-" + bluetoothDevice.getAddress();
                    devicesSet.add(key);
                    AppUtils.addToMyDevices(getApplicationContext(), key);
                }
                if (!isConnected && !isFound || isNeedOtaAgain) {
                    if (deviceList.size() > 0
                            && position < deviceList.size()
                            && deviceList.get(position).getName().toUpperCase().contains("JBL Everest".toUpperCase())) {
                        mHandler.removeCallbacks(a2dpRunable);
                        specifiedDevice = deviceList.get(position);
                        initAudioManager();
                        mBluetooth.start();
                        isFound = true;
                        position = 0;
                        Logger.d(TAG, "A2DP use first device to connect, name = " + specifiedDevice.getName()
                                + ",address = " + specifiedDevice.getAddress()
                                + ",position = " + position);
                    } else {
                        if (position < deviceList.size() - 1) {
                            position++;
                        } else position = 0;
                    }
                }
                checkDevices(devicesSet);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    private void disconnectAllBluetoothConnection() {
        try {
            if (mLightX != null) {
                mLightX.close();
                mLightX = null;
            }
            disconnectBluetoothLibrary();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d(TAG, "disconnectAllBluetoothConnection Exception while disconnecting bluetooth library");
        }
    }

    private void disconnectBluetoothLibrary() {
        try {
            if (mBluetooth != null) {
                mBluetooth.close();
                mBluetooth = null;
            }
            mBluetoothDevice = null;
        } catch (Exception e) {
            mBluetooth = null;
            mBluetoothDevice = null;
            Logger.d(TAG, "disconnectBluetoothLibrary Exception while disconnecting bluetooth library");
        }
    }

    // Members and methods to support Avnera hardware
    public synchronized void connect(BluetoothDevice bluetoothDevice) {
        if (mBluetooth == null) {
            Logger.d(TAG, "mBluetooth is null");
            return;
        }
        if (mIsConnectedPhysically) {
            return;
        }
        try {
            if (AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                    && mBluetoothDevice != null && !bluetoothDevice.getAddress().equalsIgnoreCase(mBluetoothDevice.getAddress())) {
                mBluetoothDevice = null; // New device find so initiate contact again.
            }
            if (isConnectedLoggerically()) {
                return;
            }
            if (!shouldConnectToBluetoothDevice(bluetoothDevice)) return;

            Logger.d(TAG, "Pairing Failed " + bluetoothDevice.getName() + " " + bluetoothDevice.getBondState());
            if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED && bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                disconnect();
                return;
            }
            Thread.sleep(2000);
            Logger.d(TAG, bluetoothDevice.getName() + " connecting...");
            mBluetooth.connect(bluetoothDevice);
            mBluetoothDevice = bluetoothDevice;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "Connect to device failed: " + e.getLocalizedMessage());
        }
    }

    public synchronized boolean isConnectedLoggerically() {
        return mBluetoothDevice != null;
    }

    public synchronized void disconnect() {
        if (isConnectedLoggerically()) {
            Logger.d(TAG, "Closing Loggerical connection to " + mBluetoothDevice.getName());
        }

        mIsConnectedPhysically = false;

        if (mBluetoothDevice != null) {
            try {
                if (mBluetooth != null) {
                    mBluetooth.disconnect(mBluetoothDevice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mBluetoothDevice = null;
        mLightX = null;
        AvneraManager.getAvenraManager(this).setLightX(null);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        donSendCallback = false;
//        mHandler.removeCallbacks(runnablePostResumeForUSB);
//        mHandler.postDelayed(runnablePostResumeForUSB, 1000);
    }

//    private Runnable runnablePostResumeForUSB = new Runnable() {
//        @Override
//        public void run() {
//            if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice && appLightXDelegate != null) {
//                if (appLightXDelegate != null) {
//                    appLightXDelegate.headPhoneStatus(isConnected);   //Commented as lightXisInBootloader was getting called twice. Bug 64495
//                }
//                connectDeviceStatus(isConnected);
//            }
//        }
//    };

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.MANUFACTURER.toLowerCase().contains("samsun")
                || Build.MANUFACTURER.toLowerCase().contains("htc")
                || Build.MANUFACTURER.toLowerCase().contains("unknown")) {
            donSendCallback = true;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d(TAG, "onRestart()");
        if (!FirmwareUtil.isUpdatingFirmWare.get()
                && DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
            initUSB();
            Logger.d(TAG, "onRestart() - initUSB()");
        }

        donSendCallback = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        donSendCallback = true;
        /**
         * Closing connection for reducing battery consumption while app goes in background. It doesn't close connection if Firmware update is running.
         */
        if (!FirmwareUtil.isUpdatingFirmWare.get() && mUSB != null
                && DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice) {
            if (mUSB != null) {
                mUSB.close();
                mUSB = null;
            }
            try {
                if (mLightX != null) {
                    mLightX.close();
                    mLightX = null;
                    AvneraManager.getAvenraManager(getApplicationContext()).setLightX(null);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            Logger.d(TAG, "USB Connection closed.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Bluetooth.REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_CANCELED) {
                    showExitDialog("Unable to enable Bluetooth.");
                } else {
                    if (mBluetooth == null) {
                        Logger.d(TAG, "mBluetooth is null");
                        return;
                    }
                    Logger.d(TAG, "discoverBluetoothDevices");
                    mBluetooth.discoverBluetoothDevices();
                }
            }
            break;
        }
    }

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isConnected) {
                Logger.d(TAG, "Reset disconnect ");
                mBluetooth = null;
                initializeOrResetLibrary();
            }
        }
    };

    @Override
    public void bluetoothAdapterChangedState(Bluetooth bluetooth, int currentState,
                                             int previousState) {
        Logger.d(TAG, "bluetoothAdapterChangedState");
        if (currentState != BluetoothAdapter.STATE_ON) {
            Logger.e(TAG, "The Bluetooth adapter is not enabled, cannot communicate with LightX device");
            // Could ask the user if it's ok to call bluetooth.enableBluetoothAdapter() here, otherwise abort
            if (specifiedDevice != null && specifiedDevice.getName() != null && specifiedDevice.getName().equalsIgnoreCase("150NC") && !disconnected) {

                Message message = new Message();
                message.what = MSG_DISCONNECTED;
                if (specifiedDevice != null) {
                    HashMap value = new HashMap();
                    value.put(specifiedDevice.getAddress(), specifiedDevice.getName());
                    message.obj = value;
                }
                myHandler.removeMessages(MSG_DISCONNECTED);
                myHandler.sendMessageDelayed(message, 5000);
            }
        }
    }

    @Override
    public void bluetoothDeviceBondStateChanged(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice, int currentState, int previousState) {
//        Logger.d(TAG,"bluetoothDeviceBondStateChanged");
    }

    @Override
    public void bluetoothDeviceConnected(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice, BluetoothSocket bluetoothSocket) {
        Logger.d(TAG, "bluetoothDeviceConnected");
        if (bluetoothDevice != null && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && !bluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())) {
            FirmwareUtil.disconnectHeadphoneText = getResources().getString(R.string.plsConnect);
            Logger.d(TAG, "Connected");
            AnalyticsManager.getInstance(getApplicationContext()).reportDeviceConnect(bluetoothDevice.getName());
            synchronized (this) {
                /** set device type **/
                DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.Connected_BluetoothDevice);
                showCommunicationIssue = true;
                mHandler.removeCallbacks(resetRunnable);
//                mHandler.removeCallbacks(runnableToast);
                AppUtils.setJBLDeviceName(this, bluetoothDevice.getName());
//	            EQSettingManager.EQKeyNAME = bluetoothDevice.getAddress();
                connectLightX(bluetooth, bluetoothDevice, bluetoothSocket);
                isNeedShowDashboard = true;
                Logger.d(TAG, "bluetoothDeviceConnected ....");
                resetTime = RESET_TIME;
            }
        }
    }

    private void connectLightX(Bluetooth bluetooth, BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) {
        if (mLightX != null && mLightX.getSocket().equals(bluetoothSocket)) {
            Logger.d(TAG, "bluetoothDeviceConnected() received for extant LightX/socket pair.  Ignoring.");
        } else {
            try {
                if (mLightX != null) {
                    mLightX.close();
                    mLightX = null;
                }
                mLightX = new LightX(ModuleId.Bluetooth, this, new BluetoothSocketWrapper(bluetoothSocket));
                LightX.mIs750Device = AppUtils.is750Device(DeviceManagerActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.e(TAG, "Unable to create LightX handler for " + bluetooth.deviceName(bluetoothDevice) + ": " + e.getLocalizedMessage());
            }
        }
        mIsConnectedPhysically = true;
        if (mLightX != null) {
            AvneraManager.getAvenraManager(DeviceManagerActivity.this).setLightX(mLightX);
            mLightX.readConfigModelNumber();
            isConnected = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null) {
                        appLightXDelegate.headPhoneStatus(true);
                        appLightXDelegate.isLightXInitialize();
                    }
                    if (!FirmwareUtil.isUpdatingFirmWare.get() && !isNeedOtaAgain) {
                        connectDeviceStatus(true);
                    }
                }
            });
        }
    }


    @Override
    public void bluetoothDeviceDisconnected(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice) {
        if (bluetoothDevice != null) {
            Logger.d(TAG, "bluetoothDeviceDisconnected bluetoothDevice is not null,name = " + bluetoothDevice.getName());
        }
        if (specifiedDevice != null) {
            Logger.d(TAG, "bluetoothDeviceDisconnected specifiedDevice is not null,name = " + specifiedDevice.getName());
        }
        if (bluetoothDevice != null
                && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())
                && !specifiedDevice.getName().toUpperCase().contains(JBLConstant.DEVICE_150NC)) {
            Logger.d(TAG, " -------> [bluetoothDeviceDisconnected] -------");
            disconnectDevice();
        }
    }

    private void disconnectDevice() {
        synchronized (this) {
            /** set device type **/
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            mIsConnectedPhysically = false;
            isConnected = false;
            disconnected = true;
            Logger.d(TAG, "Disconnected");
            if (specifiedDevice != null)
                AnalyticsManager.getInstance(getApplicationContext()).reportDeviceDisconnect(specifiedDevice.getName());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null) {
                        appLightXDelegate.headPhoneStatus(false);
                    }
                    connectDeviceStatus(false);
                }
            });


            Logger.d(TAG, "ResetDisconnect " + resetTime);
            Logger.d(TAGReconnect, "Bluetooth disconnected");
            //            checkForUSB_WhenBluetoothDisconnected();
//            if (resetTime == RESET_TIME) {
//                mHandler.postDelayed(runnableToast, 5 * 1000);
//            }
            --resetTime;
            mHandler.removeCallbacks(resetRunnable);
            mHandler.postDelayed(resetRunnable, resetTime);
        }
    }

    Handler handlerDelayToast = new Handler();
//    Runnable runnableToast = new Runnable() {
//        @Override
//        public void run() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    AlertsDialog.showToast(DeviceManagerActivity.this, getString(R.string.taking_longer_time));
//                }
//            });
//        }
//    };

    @Override
    public void bluetoothDeviceDiscovered(Bluetooth bluetooth, BluetoothDevice bluetoothDevice) {
//        Logger.d(TAG,"bluetoothDeviceDiscovered ");
        if (bluetoothDevice != null && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && !bluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())) {
            Logger.d(TAG, "bluetoothDeviceDiscovered connect");
            connect(bluetoothDevice);
        }
    }

    @Override
    public void bluetoothDeviceFailedToConnect(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice, Exception e) {
        String name = bluetooth.deviceName(bluetoothDevice);
        Logger.d(TAG, "ACL Events");
        Logger.d(TAG, name + " failed to connect, waiting passively: " + e.getLocalizedMessage());
    }


    @Override
    public void lightXAppReadResult(final LightX lightX, final Command command, final boolean success, final byte[] buffer) {
        Logger.d(TAG, "command:" + command.toString() + "result:" + (success ? "true" : "false"));
        try {
            if (appLightXDelegate != null) {
                Logger.d(TAG, "appLightXDelegate != null");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (appLightXDelegate != null)
                            appLightXDelegate.lightXAppReadResult(lightX, command, success, buffer);
                    }
                });
            } else if (success) {
                switch (command) {
                    case App_0xB3:
                        Logger.d(TAG, "calibration");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (CalibrationFragment.getCalibration() != null)
                                    CalibrationFragment.getCalibration().setIsCalibrationComplete(true);
                                Logger.d(TAG, "Calibration Stopped");
                            }
                        });

                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertsDialog.showToast(DeviceManagerActivity.this, "Unable to communicate with Headphone.");
                }
            });
        }
    }

    @Override
    public void lightXAppReceivedPush(final LightX lightX, final Command command, final byte[] data) {
        Logger.d(TAG, "LightX instance sent push command " + command + ", buffer " + (data == null ? "is null" : "contains " + data.length + " bytes"));
        if (mLightX != null) {
            switch (command) {
                case AppPushANCAwarenessPreset: {
                    PreferenceUtils.setBoolean(PreferenceKeys.RECEIVEPUSH, true, this);
                }
                break;
            }
            if (appLightXDelegate != null && !donSendCallback) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (appLightXDelegate != null)
                            appLightXDelegate.lightXAppReceivedPush(lightX, command, data);
                    }
                });
            }
        }
    }

    // LightX Delegate
    @Override
    public void lightXAppWriteResult(final LightX lightX, final Command command, final boolean success) {
        Logger.d(TAG, "write " + command + " command " + (success ? " succeeded" : " failed"));
        if (appLightXDelegate != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXAppWriteResult(lightX, command, success);
                }
            });
        } else if (success) {
            switch (command) {
                case App_0xB3:
                    if (CalibrationFragment.getCalibration() != null)
                        CalibrationFragment.getCalibration().setIsCalibrationComplete(true);
                    Logger.d(TAG, "Calibration Stopped");
                    break;
            }
        } else {
            switch (command) {
                case App_0xB2:
                    if (CalibrationFragment.getCalibration() != null)
                        CalibrationFragment.getCalibration().calibrationFailed();
                    break;
            }
        }
    }

    public static boolean isNeedOtaAgain = false;
    @Override
    public boolean lightXAwaitingReply(LightX lightX, Command command, final int totalElapsedMsSinceFirstTransmission) {
        Logger.d(TAG, "lightXAwaitingReply:command is " + command + " totalElapsedMsSinceFirstTransmission is " + totalElapsedMsSinceFirstTransmission);

        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightXAwaitingReply called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return true;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showCommunicationIssue && totalElapsedMsSinceFirstTransmission > 5000) {
                    showCommunicationIssue = false;
                    AlertsDialog.showToast(getApplicationContext(), getString(R.string.timeout));
                }
            }
        });

        if (totalElapsedMsSinceFirstTransmission >= 30000) {
            // returning true from this function will cause a TimeoutException (which may be
            // desirable).  When this happens the write-side of the Bluetooth socket will be closed
            // and lightXError() will be called!
            //
            // Don't just copy this method into your code!  Ask the user what they want to do when
            // we can't communicate with the device.
            Logger.d(TAG, "Headphone is not responding. so app will resetting connection isUpdatingFirmWare = "+FirmwareUtil.isUpdatingFirmWare.get());
            if (FirmwareUtil.isUpdatingFirmWare.get()) {
                isNeedOtaAgain = true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBluetooth = null;
                    mHandler.removeCallbacks(resetRunnable);
                    switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                        case Connected_USBDevice:
                            initUSB();
                            break;
                        default:
                            initializeOrResetLibrary();
                            break;
                    }
                }
            }).start();
            return true;
        }
        return false;
    }

    @Override
    public void lightXError(LightX lightX, Exception exception) {
//        BluetoothDevice bluetoothDevice;
        Logger.e(TAG, "lightXError called, error is " + exception);
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightXError called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return;
            }
            mLightX = null;
//            bluetoothDevice = mBluetoothDevice;
        }

        Logger.e(TAG, "lightXError bluetooth device");

//        lightX.close();

//        Logger.d(TAG, "LightX error calling disconnect()");
//        disconnect();

        /*if (bluetoothDevice != null) {
            Logger.d(TAG, "LightXError calling connect to attempt to reestablish connection to " + bluetoothDevice.getName());
            connect(bluetoothDevice);
        }*/
    }

    @Override
    public boolean lightXFirmwareReadStatus(final LightX lightX, final LightX.FirmwareRegion region, int offset, final byte[] buffer, Exception e) {
        if (appLightXDelegate != null) {
            final int finalOffset = offset;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXFirmwareReadStatus(lightX, region, finalOffset, buffer);
                }
            });

            return false;
        }
        return false;
    }

    @Override
    public boolean lightXFirmwareWriteStatus(final LightX lightX, final LightX.FirmwareRegion firmwareRegion,
                                             final LightX.FirmwareWriteOperation firmwareWriteOperation,
                                             final double progress, final Exception exception) {
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightXFirmwareWriteStatus called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return true;
            }
        }

        if (appLightXDelegate != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXFirmwareWriteStatus(lightX, firmwareRegion, firmwareWriteOperation, progress, exception);
                }
            });
            return false;
        }

        if (exception != null) {
            Logger.d(TAG, String.format("%s firmware %s exception: %s", firmwareWriteOperation, firmwareRegion, exception.getLocalizedMessage()));
        } else {
            Logger.d(TAG, String.format("%s firmware %s: %.02f%%", firmwareWriteOperation, firmwareRegion, progress * 100.0));
        }
        return false;
    }

    @Override
    public void lightXIsInBootloader(final LightX lightX, final boolean isInBootloader) {
        //Added mIsInBootLoader to control back-press event during Upgrade. Restrict Back-press in the middle of upgrade process.
        mIsInBootloader = isInBootloader;
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightXIsInBootloader called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return;
            }
        }

        if (appLightXDelegate != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXIsInBootloader(lightX, isInBootloader);
                }
            });
        }
    }

    @Override
    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean b, final int i, final byte[] bytes) {
        if (appLightXDelegate != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXReadBootResult(lightX, command, b, i, bytes);
                }
            });
        }
    }

    @Override
    public void lightXReadConfigResult(final LightX lightX, final Command command, final boolean success, final String value) {
        switch (command) {
            case ConfigModelNumber: {
                if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_BluetoothDevice) {
                    AppUtils.setModelNumber(getApplicationContext(), value);
                }
            }
        }
        if (appLightXDelegate != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null)
                        appLightXDelegate.lightXReadConfigResult(lightX, command, success, value);
                }
            });
            return;
        }
        if (success) {
            Logger.d(TAG, "config string for " + command + ": " + value);
        } else {
            Logger.e(TAG, "failed to read config for " + command);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(resetRunnable);
        Logger.d(TAG, "LightXB destroy");
        disconnectAllBluetoothConnection();
        try {
            /**
             * Unregister all receiver
             */
            unregisterReceiver(mBluetoothStateChange);
            unregisterReceiver(mUsbReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            Process.killProcess(Process.myPid());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    @Override
    public void usbAttached(UsbDevice usbDevice) {
        if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_BluetoothDevice)
            return;
        mUSB = null;
        Logger.d("Connection", "USB attached.Initializing Bluetooth.");
        mHandler.removeCallbacks(resetRunnable);
//        initUSB();
    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {
        Logger.d(TAG, "Device detached.");
        Logger.d(TAG, "USB " + DeviceConnectionManager.getInstance().getCurrentDevice().toString());
        if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.NONE
                || DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice) {
            Logger.d(TAG, "usbDetached USB disconnected");
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            if (appLightXDelegate != null) {
                appLightXDelegate.headPhoneStatus(false);
            }
            isConnected = false;
            if (!FirmwareUtil.isUpdatingFirmWare.get()) {
                connectDeviceStatus(false);
            }
            disconnected = true;
            String key = usbDevice.getProductName();
            if (key != null && usbDevice.getProductName().contains("Bootloader")) {
                key = key.substring(0, usbDevice.getProductName().length() - "Bootloader".length() - 1);
            }
            key = key + "-" + usbDevice.getManufacturerName();
            devicesSet.remove(key);
            Logger.d(TAG, "usbDetached Initializing Bluetooth.");
            if (DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice)
                if (mLightX != null) {
                    mLightX.close();
                    mLightX = null;
                    AvneraManager.getAvenraManager(getApplicationContext()).setLightX(null);
                }
            if (mUSB != null) {
                mUSB.close();
                mUSB = null;
            }

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    initializeOrResetLibrary();
                }
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    // This fix is to avoid interruption in the middle of Upgrade process thread. Tutorial check is to manage first launch.
                    //These were issues raised by China team.
                    //Fix start
                    boolean showTutorial = getSupportFragmentManager().findFragmentById(R.id.containerLayout) instanceof HomeFragment;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(AppUtils.mTutorial && showTutorial) && (usbManager.hasPermission(device) && isConnected)) {
                        return;
                    }
                    //Fix End

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Logger.d(TAG, "USB permission granted.");
                            Logger.d(TAG, "Attaching to USB device");
                            if (mUSB != null)
                                mUSB.deviceAttached(device);
                        }
                    } else if (!JBLConstant.USB_PERMISSION_CHECK || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        //if update is not running only then reset
                        Logger.d(TAG, "USB permission denied.Initialize Bluetooth.");
                        initializeOrResetLibrary();
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mBluetoothStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {

                            case NONE:
                                initializeOrResetLibrary();
                                break;
                            case Connected_USBDevice:
                                break;
                            case Connected_BluetoothDevice:
                                break;
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    public void usbDeviceConnected(USB usb, UsbDevice usbDevice, USBSocket usbSocket) {
        /** set device type **/
        Logger.d("Connection", "USB device connected.");
        if (DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
            try {
                synchronized (this) {
                    if (mLightX != null) {
                        if (mLightX.getSocket().equals(usbSocket)) {
                            Logger.d(TAG, "usbDeviceConnected() received for extant LightX/socket pair.  Ignoring.");
                            return;
                        }

                        mLightX.close();
                        mLightX = null;
                    }
                    specifiedDevice = null;
                    mHandler.removeCallbacks(a2dpRunable);
                    mLightX = new LightX(ModuleId.USB, this, usbSocket);
                    LightX.mIs750Device = AppUtils.is750Device(DeviceManagerActivity.this);
                    mIsConnectedPhysically = false;
                    Logger.d(TAG, "USB getDeviceName " + usbDevice.getDeviceName());
                    Logger.d(TAG, "USB getManufacturerName " + usbDevice.getManufacturerName());
                    Logger.d(TAG, "USB getSerialNumber " + usbDevice.getSerialNumber());
                    Logger.d(TAG, "USB getDeviceClass " + usbDevice.getDeviceClass());
                    Logger.d(TAG, "USB getDeviceProtocol " + usbDevice.getDeviceProtocol());
                    Logger.d(TAG, "USB getDeviceSubclass " + usbDevice.getDeviceSubclass());
                    Logger.d(TAG, "USB getDeviceId " + usbDevice.getDeviceId());
                    Logger.d(TAG, "USB getProductId " + usbDevice.getProductId());
                    Logger.d(TAG, "USB getVendorId " + usbDevice.getVendorId());
                    DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.Connected_USBDevice);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            disconnectBluetoothLibrary();
                        }
                    }).start();
                    try {
                        PreferenceKeys.CURR_EQ_NAME = usbDevice.getDeviceId() + "";
                    } catch (Exception e) {
                        PreferenceKeys.CURR_EQ_NAME = "eqName";
                    }
                    //Remove this condition
                    AppUtils.setJBLDeviceName(this, usbDevice.getDeviceName());
                    AvneraManager.getAvenraManager(this).setLightX(mLightX);
                    isConnected = true;
                    Logger.d(TAGReconnect, "USB connected");
                    isNeedShowDashboard = true;
                    String key = usbDevice.getProductName();
                    if (key != null && usbDevice.getProductName().contains("Bootloader")) {
                        key = key.substring(0, usbDevice.getProductName().length() - "Bootloader".length() - 1);
                    }
                    AppUtils.setModelNumber(getApplicationContext(), key);
                    key = key + "-" + usbDevice.getManufacturerName();
                    AppUtils.addToMyDevices(getApplicationContext(), key);
                    devicesSet.add(key);
                    checkDevices(devicesSet);
                    Logger.d(TAGReconnect, "isUpdatingFirmWare = "+FirmwareUtil.isUpdatingFirmWare.get());
                    if (!FirmwareUtil.isUpdatingFirmWare.get() && !isNeedOtaAgain) {
                        connectDeviceStatus(isConnected);
                    }
                    Logger.d(TAGReconnect, "key = "+key);
                    if (appLightXDelegate != null) {
                        appLightXDelegate.isLightXInitialize();
                    }
                }
                onWindowFocusChanged(false);
                Logger.e(TAG, "USB device \"" + usbDevice.getDeviceName() + "\" connected");
            } catch (Exception e) {
                if (usbDevice != null) {
                    Logger.e(TAG, "Unable to create LightX handler for " + usbDevice.getDeviceName() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public void usbDeviceDisconnected(USB usb, UsbDevice usbDevice) {
        if (DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
            if (mLightX != null) {
                mLightX.close();
                mLightX = null;
                Logger.i(TAG, "usbDeviceDisconnected set lightX null");
                AvneraManager.getAvenraManager(getApplicationContext()).setLightX(null);
            }
        }
        if (usb != null) {
            usb.close();
        }
    }

    @Override
    public void receivedAdminEvent(@NotNull AdminEvent event, final Object value) {
        Logger.d(TAG, " ========> [receivedAdminEvent]   <======== value = " + value + ",event = " + event);
        switch (event) {
            /**
             * AccessoryReady
             * when call "connectDevice", if connected, will receive this event.
             * When received AccessoryReady, app can update UI,show Aware Home, and communicate with accessory.
             */
            case AccessoryReady: {
                if (specifiedDevice != null && specifiedDevice.getName().contains(JBLConstant.DEVICE_150NC) && value == null
                        || specifiedDevice != null && value != null && ((HashMap) value).containsKey(specifiedDevice.getAddress())) {
                    if (mLightX != null) {
                        mLightX.close();
                        mLightX = null;
                        AvneraManager.getAvenraManager(getApplicationContext()).setLightX(null);
                    }
                    AccessoryInfo accessoryInfo = bt150Manager.getAccessoryStatus();
                    PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), getApplicationContext());
                    AppUtils.setModelNumber(getApplicationContext(), accessoryInfo.getModelNumber());
                    Message message = new Message();
                    message.what = MSG_CONNECTED;
                    message.obj = value;
                    myHandler.removeMessages(MSG_CONNECTED);
                    myHandler.sendMessageDelayed(message, 200);
                    Logger.d(TAG, " ========> AccessoryReady <======== ");
                }
                break;
            }
            /**
             * Accessory is connected, do nothing.
             * This event comes earlier than AccessoryReady.
             */
            case AccessoryConnected: {
                Logger.d(TAG, " ========> [receivedAdminEvent] AccessoryConnected");
                break;
            }
            /**
             * Not used now.
             */
            case AccessoryNotReady: {
                Logger.d(TAG, " ========> [receivedAdminEvent] AccessoryNotReady");
                break;
            }
            /**
             * Receive this event while unpaired accessory,shutdown accessory,close BT.
             *
             */
            case AccessoryUnpaired:
            case AccessoryVanished:
            case AccessoryAppeared:
                break;
            case AccessoryDisconnected: {
                if (specifiedDevice != null && specifiedDevice.getName().contains(JBLConstant.DEVICE_150NC)
                        && value != null && ((HashMap) value).containsKey(specifiedDevice.getAddress())
                        || specifiedDevice != null && specifiedDevice.getName().contains(JBLConstant.DEVICE_150NC) && value == null) {
                    Message message = new Message();
                    message.what = MSG_DISCONNECTED;
                    message.obj = value;
                    myHandler.removeMessages(MSG_DISCONNECTED);
                    myHandler.sendMessageDelayed(message, 200);
                }
                break;
            }
            case TimeOut:
                disconnectDevice();
                break;
            default: {
                Logger.d(TAG, " ========> [receivedAdminEvent] default :" + event);
                break;
            }
        }
    }

    /**
     * Receive response.
     *
     * @param command
     * @param values
     * @param status
     */
    @Override
    public void receivedResponse(@NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appLightXDelegate != null) {
                    appLightXDelegate.receivedResponse(command, values, status);
                }
            }
        });
    }

    /**
     * Receive status
     *
     * @param name
     * @param value
     */
    @Override
    public void receivedStatus(@NotNull final StatusEvent name, @NotNull final Object value) {
        switch (name) {
            /**
             * Get this event when discovering.
             * The param value is a list of mac address.
             */
            case DeviceList: {
                Map<String, String> pairedDevices = (Map<String, String>) value;
                for (Map.Entry<String, String> entry : pairedDevices.entrySet()) {
                    if (entry.getValue() != null
                            && entry.getValue().toUpperCase().contains("JBL Everest".toUpperCase())
                            && entry.getValue().contains(JBLConstant.DEVICE_150NC)
                            && specifiedDevice != null
                            && specifiedDevice.getAddress().equalsIgnoreCase(entry.getKey())) {
                        Status status = bt150Manager.connectDevice(entry.getKey(), false);
                        if (status == Status.AccessoryNotConnected) {
                            disconnectDevice();
                        }
                        Logger.d(TAG, " ========> [receivedStatus] found device, connect device:" + entry.getKey() + "," + entry.getValue() + ",status=" + status);
                    }
                }
                break;
            }
            /**
             * Get this event when device is doing OTA.
             */
            case UpdateProgress: {
                break;
            }
            /**
             * Get this event when finished one OTA step.
             * OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateComplete: {
                break;
            }
            /**
             * Get this event when finished one OTA step.
             * OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateFinalize: {
                break;
            }
            default: {

            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appLightXDelegate != null) {
                    appLightXDelegate.receivedStatus(name, value);
                }
            }
        });
    }

    @Override
    public void receivedPushNotification(@NotNull final Action action, @NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appLightXDelegate != null) {
                    appLightXDelegate.receivedPushNotification(action, command, values, status);
                }
            }
        });
    }

    private MyHandler myHandler = new MyHandler();
    private final static int MSG_CONNECTED = 0;
    private final static int MSG_DISCONNECTED = 1;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONNECTED: {
                    connectedToDevice(msg.obj);
                    break;
                }
                case MSG_DISCONNECTED: {
                    disconnectToDevice(msg.obj);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void connectedToDevice(final Object value) {
        Logger.d(TAG, " MSG_CONNECTED value = " + value);
        FirmwareUtil.disconnectHeadphoneText = getResources().getString(R.string.plsConnect);
        synchronized (this) {
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.Connected_BluetoothDevice);
            showCommunicationIssue = true;
            mHandler.removeCallbacks(resetRunnable);
//            handlerDelayToast.removeCallbacks(runnableToast);
            AppUtils.setJBLDeviceName(getApplicationContext(), specifiedDevice.getName());
            AnalyticsManager.getInstance(getApplicationContext()).reportDeviceConnect(bt150Manager.getAccessoryStatus().getName());
//            EQSettingManager.EQKeyNAME = specifiedDevice == null ? "" : specifiedDevice.getAddress();
            mIsConnectedPhysically = true;
            isConnected = true;
            AvneraManager.getAvenraManager(getApplicationContext()).setAudioManager(bt150Manager);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appLightXDelegate != null) {
                        appLightXDelegate.headPhoneStatus(true);
                        appLightXDelegate.receivedAdminEvent(AdminEvent.AccessoryReady, value);
                    }
                    connectDeviceStatus(true);
                }
            });
            Logger.d(TAG, " isConnected = " + isConnected);
            isNeedShowDashboard = true;
            resetTime = RESET_TIME_FOR_150NC;
        }
    }

    private void disconnectToDevice(final Object value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, " MSG_DISCONNECTED value = " + value);
                if (appLightXDelegate != null) {
                    appLightXDelegate.receivedAdminEvent(AdminEvent.AccessoryDisconnected, value);
                }
                disconnectDevice();
            }
        });
    }
}

