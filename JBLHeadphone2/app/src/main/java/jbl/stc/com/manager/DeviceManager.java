package jbl.stc.com.manager;

import android.app.Activity;
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
import com.avnera.smartdigitalheadset.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbl.stc.com.R;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.OnConnectStatusListener;
import jbl.stc.com.listener.OnRetListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AmToolUtil;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.SaveSetUtil;


public class DeviceManager extends BaseDeviceManager implements Bluetooth.Delegate, LightX.Delegate, USB.Delegate, audioManager.AudioDeviceDelegate {
    private static final String TAG = DeviceManager.class.getSimpleName() + "aa";
    private static final String TAGReconnect = TAG + " reconnection";
    private static final int RESET_TIME = 10 * 1000;
    private static final int RESET_TIME_FOR_150NC = 2 * 1000;
    private int resetTime = RESET_TIME;
    private UsbManager usbManager;
    private static boolean isConnected = false;
    private boolean mIsConnectedPhysically;
    private boolean showCommunicationIssue = true;
    private Handler mHandler = new Handler();
    private static DeviceManager mInstance;

    private static Activity mContext;

    private MyHandler myHandler = new MyHandler();
    private final static int MSG_CONNECTED = 0;
    private final static int MSG_DISCONNECTED = 1;
    private final static int MSG_CONNECT_TIME_OUT = 2;

    private static boolean mIsInBootloader;
    private OnRetListener onRetListener;
    private OnConnectStatusListener mOnConnectStatusListener;

    private DeviceManager() {
    }

    public static DeviceManager getInstance(Activity context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new DeviceManager();
        }
        return mInstance;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setOnConnectStatusListener(OnConnectStatusListener onConnectStatusListener) {
        this.mOnConnectStatusListener = onConnectStatusListener;
        Logger.e(TAG, "set on connect status listener = " + onConnectStatusListener.toString());
    }


    public void setOnRetListener(OnRetListener onRetListener) {
        this.onRetListener = onRetListener;
        Logger.e(TAG, "set on ret listener = " + onRetListener.toString());
    }

    private boolean mIsFromHome = false;

    public boolean isFromHome() {
        return mIsFromHome;
    }

    public void setIsFromHome(boolean isFromHome) {
        mIsFromHome = isFromHome;
    }

    public void setOnCreate() {
        super.setOnCreate(mContext);
        Logger.d(TAG, "onCreate");
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothStateChange, intentFilter);

        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        String action = mContext.getIntent().getAction();
        if (TextUtils.isEmpty(action)) {
            initUSB();
        }
    }

    public void setOnResume() {
        try {
            Intent intent = mContext.getIntent();
            String action = intent.getAction();
            Logger.d(TAG, "onResume action =" + action);
            if (!isConnected() && !TextUtils.isEmpty(action)
                    && "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                synchronized (this) {
//                    if (FirmwareUtil.isUpdatingFirmWare.get()) {
                    initUSB();
//                    }
                }
            }
        } catch (Exception e) {
            Logger.d(TAG, "onResume Exception =" + e);
            e.printStackTrace();
        }
        donSendCallback = false;
    }

    private synchronized void initUSB() {
        Logger.d(TAG, "Initializing USB first");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Logger.d(TAG, "usbManager is null. Initializing Bluetooth");
            initializeOrResetLibrary();
            return;
        }
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Logger.d(TAG, "deviceList size = " + deviceList.size());
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
        mUSB = new USB(mContext, this);
        USB.sUSBVendorIds.add(JBLConstant.USB_VENDOR_ID);
        USB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID);
        USB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID2);
        USB.sUSBVendorIds.add(JBLConstant.USB_VENDOR_ID3);
        USB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID3);
        USB.sUSBProductIds.add(JBLConstant.USB_PRODUCT_ID_BOOT3);

        Logger.d(TAG, "Aware found. Requesting for permission: " + usbManager.hasPermission(device));
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
        Logger.d(TAG, "initialize or reset library");
        if (mBluetooth != null) {
            Logger.d(TAG, "initialize or reset library, mBluetooth is not null");
            return;
        }

        isFound = false;
        specifiedDevice = null;
        try {
            disconnectAllBluetoothConnection();
            close150NCManager();
        } catch (Exception e) {
            Logger.d(TAG, "initialize or reset library, exception when disconnect");
        }

        Logger.d(TAG, "initialize or reset library, begin");
        try {
            if (bt150Manager == null)
                bt150Manager = new audioManager();
            mBluetooth = new Bluetooth(this, mContext, true);
        } catch (Exception e) {
            showExitDialog("initialize or reset library, exception when new object");
        }
        mHandler.postDelayed(a2dpRunnable, 500);
    }

    private void initAudioManager() {
        if (bt150Manager != null) {
            Logger.d(TAG, "init audio manager for 150nc");
            byte[] bytes = AmToolUtil.INSTANCE.readAssertResource(mContext, AmToolUtil.COMMAND_FILE);
            bt150Manager.initManager(mContext, mContext, this, AmToolUtil.COMMAND_FILE, bytes);
        }
    }

    private Runnable a2dpRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnected && !isNeedOtaAgain) {
                Logger.d(TAG, "a2dp runnable, device is connected, return");
                return;
            }
            if (isFound) {
                Logger.d(TAG, "a2dp runnable, device is found, return");
                return;
            }
            Logger.d(TAG, "a2dp runnable ... isConnected = " + isConnected);
            startA2DPCheck();
            mHandler.postDelayed(a2dpRunnable, 2000);
        }
    };

    public void startA2DPCheck() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            mBtAdapter.getProfileProxy(mContext, mListener, BluetoothProfile.A2DP);
        }
    }

    private BluetoothDevice specifiedDevice = null;
    private static boolean isFound = false;
    private int position = 0;
    private Set<MyDevice> devicesSet = new HashSet<>();


    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                List<BluetoothDevice> deviceList = proxy.getConnectedDevices();
                devicesSet.clear();
                for (BluetoothDevice bluetoothDevice : deviceList) {
                    Logger.d(TAG, "a2dp listener, connected device, name = " + bluetoothDevice.getName()
                            + ",address = " + bluetoothDevice.getAddress()
                            + ",position =" + position);
                    MyDevice myDevice = AppUtils.getMyDevice(bluetoothDevice.getName(), ConnectStatus.A2DP_HALF_CONNECTED, "", bluetoothDevice.getAddress());
                    if (myDevice != null) {
                        devicesSet.add(myDevice);
                        SaveSetUtil.saveSet(mContext, devicesSet);
                    }
                }
                ProductListManager.getInstance().checkHalfConnectDevice(devicesSet);
                if (!isConnected && !isFound || isNeedOtaAgain) {
                    if (deviceList.size() > 0
                            && position < deviceList.size()
                            && deviceList.get(position).getName().toUpperCase().contains("JBL Everest".toUpperCase())) {
                        mHandler.removeCallbacks(a2dpRunnable);
                        specifiedDevice = deviceList.get(position);
                        initAudioManager();
                        mBluetooth.start();
                        isFound = true;
                        position = 0;
                        Logger.d(TAG, "a2dp listener, use first device to connect, name = " + specifiedDevice.getName()
                                + ",address = " + specifiedDevice.getAddress()
                                + ",position = " + position);
                    } else {
                        if (position < deviceList.size() - 1) {
                            position++;
                        } else position = 0;
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    private void disconnectAllBluetoothConnection() {
        Logger.d(TAG, "disconnect all bluetooth connection");
        try {
            if (mLightX != null) {
                mLightX.close();
                mLightX = null;
                AvneraManager.getAvenraManager().setLightX(null);
            }
            disconnectBluetoothLibrary();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d(TAG, "disconnect all bluetooth connection, exception while disconnecting bluetooth library");
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
            Logger.d(TAG, "disconnect bluetooth library, exception while disconnecting bluetooth library");
        }
    }

    public synchronized void connect(BluetoothDevice bluetoothDevice) {
        if (mBluetooth == null) {
            Logger.d(TAG, "connect, while mBluetooth is null");
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

            if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED && bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                disconnect();
                Logger.d(TAG, "connect, Pairing Failed " + bluetoothDevice.getName() + " " + bluetoothDevice.getBondState());
                return;
            }
            Thread.sleep(2000);
            Logger.d(TAG, bluetoothDevice.getName() + "connect, connecting...");
            mBluetooth.connect(bluetoothDevice);
            mBluetoothDevice = bluetoothDevice;
            myHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIME_OUT, 5000);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "connect, connect to device exception: " + e.getLocalizedMessage());
        }
    }

    private synchronized boolean isConnectedLoggerically() {
        return mBluetoothDevice != null;
    }

    private synchronized void disconnect() {
        if (isConnectedLoggerically()) {
            Logger.d(TAG, "disconnect, is connected logger true, device name " + mBluetoothDevice.getName());
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
        AvneraManager.getAvenraManager().setLightX(null);
    }

    public void setOnPostResume() {
        donSendCallback = false;
    }

    public void setOnPause() {
        if (Build.MANUFACTURER.toLowerCase().contains("samsun")
                || Build.MANUFACTURER.toLowerCase().contains("htc")
                || Build.MANUFACTURER.toLowerCase().contains("unknown")) {
            donSendCallback = true;
        }
    }

    public void setOnRestart() {
        Logger.d(TAG, "set on restart");
        if (!isConnected() && !FirmwareUtil.isUpdatingFirmWare.get()
                && DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
            Logger.d(TAG, "set on restart, init usb");
            initUSB();
        }

        donSendCallback = false;
    }

    public void setUsbDeviceStop() {
        donSendCallback = true;
        /*
          Closing connection for reducing battery consumption while app goes in background. It doesn't close connection if Firmware update is running.
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
                    AvneraManager.getAvenraManager().setLightX(null);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            Logger.d(TAG, "set usb device stop");
        }
    }

    public void setOnActivityResult(int requestCode, int resultCode) {
        switch (requestCode) {
            case Bluetooth.REQUEST_ENABLE_BT: {
                if (resultCode == Activity.RESULT_CANCELED) {
                    showExitDialog("set on activity result, Unable to enable Bluetooth.");
                } else {
                    if (mBluetooth == null) {
                        Logger.d(TAG, "set on activity result, mBluetooth is null");
                        return;
                    }
                    Logger.d(TAG, "set on activity result, discovery bluetooth devices");
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
                Logger.d(TAG, "reset runnable, initialize or reset library");
                mBluetooth = null;
                initializeOrResetLibrary();
            }
        }
    };

    @Override
    public void bluetoothAdapterChangedState(Bluetooth bluetooth, int currentState,
                                             int previousState) {
        Logger.d(TAG, "bluetooth adapter changed state");
        if (currentState != BluetoothAdapter.STATE_ON) {
            Logger.e(TAG, "bluetooth adapter changed state, not enabled, cannot communicate with LightX device");
            // Could ask the user if it's ok to call bluetooth.enableBluetoothAdapter() here, otherwise abort
            if (specifiedDevice != null && specifiedDevice.getName() != null && specifiedDevice.getName().equalsIgnoreCase("150NC") && !disconnected) {

                Message message = new Message();
                message.what = MSG_DISCONNECTED;
                if (specifiedDevice != null) {
                    HashMap<String, String> value = new HashMap<>();
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
    }

    @Override
    public void bluetoothDeviceConnected(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice, BluetoothSocket bluetoothSocket) {
        Logger.d(TAG, "bluetooth device connected");
        if (bluetoothDevice != null && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && !bluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())) {
            if (!LeManager.getInstance().isConnected()) {
                FirmwareUtil.disconnectHeadphoneText = mContext.getResources().getString(R.string.plsConnect);
                AnalyticsManager.getInstance(mContext).reportDeviceConnect(bluetoothDevice.getName());
                synchronized (this) {
                    myHandler.removeMessages(MSG_CONNECT_TIME_OUT);
                    DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.Connected_BluetoothDevice);
                    showCommunicationIssue = true;
                    mHandler.removeCallbacks(resetRunnable);
                    AppUtils.setJBLDeviceName(mContext, bluetoothDevice.getName());
                    connectLightX(bluetooth, bluetoothDevice, bluetoothSocket);
                    isNeedShowDashboard = true;
                    Logger.d(TAG, "bluetooth device connected, Connected");
                    resetTime = RESET_TIME;
                }
            } else {
                Logger.d(TAG, "bluetooth device connected, le device is already connected");
                initializeOrResetLibrary();
            }
        }
    }

    private void connectLightX(final Bluetooth bluetooth, final BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) {
        if (mLightX != null && mLightX.getSocket().equals(bluetoothSocket)) {
            Logger.d(TAG, "connect lightX, lightX is not null and same");
        } else {
            try {
                if (mLightX != null) {
                    mLightX.close();
                    mLightX = null;
                }
                mLightX = new LightX(ModuleId.Bluetooth, this, new BluetoothSocketWrapper(bluetoothSocket));
                LightX.mIs750Device = AppUtils.is750Device(mContext);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.e(TAG, "connect lightX, exception, unable to create LightX handler for " + bluetooth.deviceName(bluetoothDevice) + ": " + e.getLocalizedMessage());
            }
        }
        mIsConnectedPhysically = true;
        if (mLightX != null) {
            AvneraManager.getAvenraManager().setLightX(mLightX);
            mLightX.readConfigModelNumber();
            isConnected = true;
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (onRetListener != null) {
                        onRetListener.onReceive(EnumCommands.CMD_IsLightXInitialize);
                    }
                    if (!FirmwareUtil.isUpdatingFirmWare.get() && !isNeedOtaAgain) {
                        Logger.d(TAG, "connect lightX, success, call listener: " + onRetListener);
                        ProductListManager.getInstance().checkConnectStatus(bluetoothDevice.getAddress(),ConnectStatus.DEVICE_CONNECTED);
                        if (mOnConnectStatusListener != null) {
                            mOnConnectStatusListener.onConnectStatus(true);
                        }
                    }
                }
            });
        }
    }


    @Override
    public void bluetoothDeviceDisconnected(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice) {
        if (bluetoothDevice != null) {
            Logger.d(TAG, "bluetooth device disconnected, bluetoothDevice name = " + bluetoothDevice.getName());
        }
        if (specifiedDevice != null) {
            Logger.d(TAG, "bluetooth device disconnected, specifiedDevice name = " + specifiedDevice.getName());
        }
        if (bluetoothDevice != null
                && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())
                && !specifiedDevice.getName().toUpperCase().contains(JBLConstant.DEVICE_150NC)) {
            Logger.d(TAG, " bluetooth device disconnected, disconnect device");
            disconnectDevice();
        }
    }

    private void disconnectDevice() {
        synchronized (this) {
            /* set device type **/
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            mIsConnectedPhysically = false;
            isConnected = false;
            disconnected = true;
            Logger.d(TAG, "disconnect device");
            if (specifiedDevice != null)
                AnalyticsManager.getInstance(mContext).reportDeviceDisconnect(specifiedDevice.getName());

            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProductListManager.getInstance().checkConnectStatus(specifiedDevice.getAddress(),ConnectStatus.A2DP_UNCONNECTED);
                    if (mOnConnectStatusListener != null) {
                        mOnConnectStatusListener.onConnectStatus(false);
                    }
                }
            });


            Logger.d(TAG, "disconnect device, reset time: " + resetTime);
            --resetTime;
            mHandler.removeCallbacks(resetRunnable);
            mHandler.postDelayed(resetRunnable, resetTime);
        }
    }

    @Override
    public void bluetoothDeviceDiscovered(Bluetooth bluetooth, BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null && AppUtils.isMatchDeviceName(bluetoothDevice.getName())
                && !bluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)
                && specifiedDevice != null
                && specifiedDevice.getAddress().equalsIgnoreCase(bluetoothDevice.getAddress())
                && !LeManager.getInstance().isConnected()) {
            Logger.d(TAG, "bluetooth device discovered, so call connect");
            connect(bluetoothDevice);
        }
    }

    @Override
    public void bluetoothDeviceFailedToConnect(Bluetooth bluetooth, BluetoothDevice
            bluetoothDevice, Exception e) {
        String name = bluetooth.deviceName(bluetoothDevice);
        Logger.d(TAG, "bluetooth device failed to connect, ACL Events, device name: "+name + ",failed to connect, waiting passively: " + e.getLocalizedMessage());
    }


    @Override
    public void lightXAppReadResult(final LightX lightX, final Command command, final boolean success, final byte[] buffer) {
        Logger.d(TAG, "lightX app read result, command:" + command.toString() + "result:" + (success ? "true" : "false"));
        try {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (onRetListener != null) {
                        switch (command) {
                            case AppANCAwarenessPreset: {
                                Logger.d(TAG, "lightX app read result, AppANCAwarenessPreset");
                                int intValue = Utility.getInt(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_AMBIENT_LEVELING, intValue);
                                break;
                            }
                            case AppANCEnable: {
                                if (buffer != null) {
                                    boolean ancResult = Utility.getBoolean(buffer, 0);
                                    onRetListener.onReceive(EnumCommands.CMD_ANC, ancResult ? 1: 0);
                                }
                                break;
                            }
                            case AppAwarenessRawSteps: {
                                int rawSteps = Utility.getInt(buffer, 0) - 1;
                                onRetListener.onReceive(EnumCommands.CMD_RAW_STEPS, rawSteps);
                                break;
                            }
                            case AppGraphicEQCurrentPreset: {
                                int currentPreset = Utility.getInt(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_GEQ_CURRENT_PRESET, currentPreset);
                                break;
                            }
                            case AppBatteryLevel: {
                                int batteryValue = Utility.getInt(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_BATTERY_LEVEL, batteryValue);
                                break;
                            }
                            case AppFirmwareVersion: {
                                int major = buffer[0];
                                int minor = buffer[1];
                                int revision = buffer[2];
                                String version = major + "." + minor + "." + revision;
                                onRetListener.onReceive(EnumCommands.CMD_FIRMWARE_VERSION, version,success);
                                break;
                            }
                            case AppGraphicEQPresetBandSettings: {
                                onRetListener.onReceive(EnumCommands.CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS, (Object) buffer);
                                break;
                            }
                            case App_0xB3:{
                                if (success) {
                                    onRetListener.onReceive(EnumCommands.CMD_App_0xB3);
                                }
                                break;
                            }
                            case AppOnEarDetectionWithAutoOff:
                                boolean boolValue = Utility.getBoolean(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_AutoOffEnable,boolValue);
                                break;
                            case AppVoicePromptEnable:
                                boolean prompt = Utility.getBoolean(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_VoicePrompt,prompt);
                                break;
                            case AppSmartButtonFeatureIndex:{
                                boolean smartTyp = Utility.getBoolean(buffer, 0);
                                onRetListener.onReceive(EnumCommands.CMD_SMART_BUTTON,smartTyp);
                                break;
                            }
                        }
                    }
                }
            });
//            if ( success) {
//                switch (command) {
//                    case App_0xB3:
//                        Logger.d(TAG, "calibration");
//                        mContext.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (CalibrationActivity.getCalibration() != null)
//                                    CalibrationActivity.getCalibration().setIsCalibrationComplete(true);
//                                Logger.d(TAG, "Calibration Stopped");
//                            }
//                        });
//
//                        break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertsDialog.showToast(mContext, "Unable to communicate with Headphone.");
                }
            });
        }
    }

    @Override
    public void lightXAppReceivedPush(final LightX lightX, final Command command, final byte[] data) {
        Logger.d(TAG, "lightX app received push command " + command + ", buffer " + (data == null ? "is null" : "contains " + data.length + " bytes"));
        if (mLightX != null) {
            switch (command) {
                case AppPushANCAwarenessPreset: {
                    PreferenceUtils.setBoolean(PreferenceKeys.RECEIVEPUSH, true, mContext);
                }
                break;
            }
            if (!donSendCallback) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onRetListener != null) {
                            switch (command) {
                                case AppPushANCEnable:
                                    int intValue = Utility.getInt(data, 0);
                                    onRetListener.onReceive(EnumCommands.CMD_ANC_NOTIFICATION, intValue);
                                    break;
                                case AppPushANCAwarenessPreset: {
                                    int aaLevel = Utility.getInt(data, 0);
                                    onRetListener.onReceive(EnumCommands.CMD_AA_Notification, aaLevel);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void lightXAppWriteResult(final LightX lightX, final Command command, final boolean success) {
        Logger.d(TAG, "lightX app write result, command: " + command + ", success " + (success ? " succeeded" : " failed"));
//        if (success) {
//            switch (command) {
//                case App_0xB3:
//                    if (CalibrationActivity.getCalibration() != null)
//                        CalibrationActivity.getCalibration().setIsCalibrationComplete(true);
//                    Logger.d(TAG, "Calibration Stopped");
//                    break;
//            }
//        } else {
//            switch (command) {
//                case App_0xB2:
//                    if (CalibrationActivity.getCalibration() != null)
//                        CalibrationActivity.getCalibration().calibrationFailed();
//                    break;
//            }
//        }
    }

    private boolean isNeedOtaAgain = false;

    public boolean isNeedOtaAgain() {
        return isNeedOtaAgain;
    }

    public void setIsNeedOtaAgain(boolean isNeed) {
        isNeedOtaAgain = isNeed;
    }

    @Override
    public boolean lightXAwaitingReply(LightX lightX, Command command, final int totalElapsedMsSinceFirstTransmission) {
        Logger.d(TAG, "lightX awaiting reply, command: " + command + " totalElapsedMsSinceFirstTransmission is " + totalElapsedMsSinceFirstTransmission);

        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightX awaiting reply, called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return true;
            }
        }
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showCommunicationIssue && totalElapsedMsSinceFirstTransmission > 5000) {
                    showCommunicationIssue = false;
                    AlertsDialog.showToast(mContext, mContext.getString(R.string.timeout));
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
            Logger.d(TAG, "lightX awaiting reply, Headphone is not responding. so app will resetting connection isUpdatingFirmWare = " + FirmwareUtil.isUpdatingFirmWare.get());
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
        Logger.e(TAG, "lightX error called, error is " + exception);
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightXError called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return;
            }
            mLightX = null;
        }

        Logger.e(TAG, "lightX error bluetooth device");
    }

    @Override
    public boolean lightXFirmwareReadStatus(final LightX lightX, final LightX.FirmwareRegion region, int offset, final byte[] buffer, Exception e) {
//        if (appLightXDelegate != null) {
//            final int finalOffset = offset;
//            mContext.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (appLightXDelegate != null)
//                        appLightXDelegate.lightXFirmwareReadStatus(lightX, region, finalOffset, buffer);
//                }
//            });
//
//            return false;
//        }
        return false;
    }

    @Override
    public boolean lightXFirmwareWriteStatus(final LightX lightX, final LightX.FirmwareRegion firmwareRegion,
                                             final LightX.FirmwareWriteOperation firmwareWriteOperation,
                                             final double progress, final Exception exception) {
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightX firmware write status, called but mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return true;
            }
        }

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_FirmwareWriteStatus, firmwareWriteOperation, progress,exception);
                }
            }
        });
        return false;
    }

    public boolean isInBootloader() {
        return mIsInBootloader;
    }

    @Override
    public void lightXIsInBootloader(final LightX lightX, final boolean isInBootloader) {
        //Added mIsInBootLoader to control back-press event during Upgrade. Restrict Back-press in the middle of upgrade process.
        mIsInBootloader = isInBootloader;
        synchronized (this) {
            if (mLightX == null || lightX != mLightX) {
                Logger.e(TAG, "lightX is in bootloader, mLightX != lightX (" + mLightX + ", " + lightX + ")");
                return;
            }
        }

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_IsInBootloader, isInBootloader);
                }
            }
        });
    }

    @Override
    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean b, final int i, final byte[] bytes) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    switch (command) {
                        case BootReadVersionFile: {
                            int result[] = AppUtils.parseVersionFromASCIIbuffer(bytes);
                            int major = result[0];
                            int minor = result[1];
                            int revision = result[2];
                            String rsrcSavedVersion = major + "." + minor + "." + revision;
                            onRetListener.onReceive(EnumCommands.CMD_BootReadVersionFile, rsrcSavedVersion,b);
                            break;
                        }
                    }
                }

            }
        });
    }

    @Override
    public void lightXReadConfigResult(final LightX lightX, final Command command, final boolean success, final String value) {
        switch (command) {
            case ConfigModelNumber: {
                if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_BluetoothDevice) {
                    AppUtils.setModelNumber(mContext, value);
                }
            }
        }
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    switch (command) {
                        case ConfigProductName:
                        case ConfigModelNumber:
                            onRetListener.onReceive(EnumCommands.CMD_ConfigProductName, value);
                            break;
                    }
                }
            }
        });
    }

    public void setOnDestroy() {
        super.setOnDestroy();
        mHandler.removeCallbacks(resetRunnable);
        Logger.d(TAG, "set on destroy");
        disconnectAllBluetoothConnection();
        try {
            mContext.unregisterReceiver(mBluetoothStateChange);
            mContext.unregisterReceiver(mUsbReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void usbAttached(UsbDevice usbDevice) {
        super.usbAttached(usbDevice);
        Logger.d(TAG, "usb attached.");
        if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_BluetoothDevice) {
            Logger.d(TAG, "not usb device return.");
            return;
        }
        mUSB = null;
        Logger.d(TAG,"usb attached, remove reset runnable, init usb");
        mHandler.removeCallbacks(resetRunnable);
        initUSB();
    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {
        super.usbDetached(usbDevice);
        Logger.d(TAG, "usb detached, device name: " + DeviceConnectionManager.getInstance().getCurrentDevice().toString());
        if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.NONE
                || DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice) {
            Logger.d(TAG, "usb detached, disconnect");
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.NONE);
            isConnected = false;
            String key = usbDevice.getProductName();
            if (key != null && usbDevice.getProductName().contains("Bootloader")) {
                key = key.substring(0, usbDevice.getProductName().length() - "Bootloader".length() - 1);
            }
            AppUtils.setModelNumber(mContext, key);
            key = key + "-" + usbDevice.getManufacturerName();
            MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(key);
            myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
            ProductListManager.getInstance().checkConnectStatus(key,ConnectStatus.A2DP_UNCONNECTED);
            SaveSetUtil.remove(mContext, myDevice);
            if (!FirmwareUtil.isUpdatingFirmWare.get()) {
                if (mOnConnectStatusListener != null) {
                    mOnConnectStatusListener.onConnectStatus(false);
                }
            }
            disconnected = true;

            Logger.d(TAG, "Usb detached, then Initializing Bluetooth.");
            if (DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
                disconnectBluetoothLibrary();
                if (mLightX != null) {
                    mLightX.close();
                    mLightX = null;
                    AvneraManager.getAvenraManager().setLightX(null);
                }
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(AppUtils.mTutorial /*&& showTutorial*/) && (usbManager.hasPermission(device) && isConnected)) {
                        return;
                    }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Logger.d(TAG, "usb receiver, permission granted. Attaching to USB device");
                            if (mUSB != null)
                                mUSB.deviceAttached(device);
                        }
                    } else if (!JBLConstant.USB_PERMISSION_CHECK || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        //if update is not running only then reset
                        Logger.d(TAG, "usb receiver, permission denied.Initialize Bluetooth.");
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
            if (action == null){
                return;
            }
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
                                Logger.d(TAG,"on receive, bluetooth state receiver, initialize or reset library");
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
        Logger.d(TAG, "usb device connected.");
        if (DeviceConnectionManager.getInstance().getCurrentDevice() != ConnectedDeviceType.Connected_BluetoothDevice) {
            try {
                synchronized (this) {
                    if (mLightX != null) {
                        if (mLightX.getSocket().equals(usbSocket)) {
                            Logger.d(TAG, "usb device connected, received for extant LightX/socket pair.  Ignoring.");
                            return;
                        }

                        mLightX.close();
                        mLightX = null;
                    }
                    specifiedDevice = null;
                    mHandler.removeCallbacks(a2dpRunnable);
                    mLightX = new LightX(ModuleId.USB, this, usbSocket);
                    LightX.mIs750Device = AppUtils.is750Device(mContext);
                    mIsConnectedPhysically = false;
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
                    AppUtils.setJBLDeviceName(mContext, usbDevice.getDeviceName());
                    AvneraManager.getAvenraManager().setLightX(mLightX);
                    isConnected = true;
                    Logger.d(TAGReconnect, "usb device connected");
                    isNeedShowDashboard = true;
                    String key = usbDevice.getProductName();
                    if (key != null && usbDevice.getProductName().contains("Bootloader")) {
                        key = key.substring(0, usbDevice.getProductName().length() - "Bootloader".length() - 1);
                    }
                    AppUtils.setModelNumber(mContext, key);
                    key = key + "-" + usbDevice.getManufacturerName();
                    MyDevice myDevice = ProductListManager.getInstance().getDeviceByKey(key);
                    myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
                    ProductListManager.getInstance().checkConnectStatus(key,ConnectStatus.A2DP_UNCONNECTED);
                    Set<MyDevice> set = new HashSet<>();
                    set.add(myDevice);
                    SaveSetUtil.saveSet(mContext, set);
                    Logger.d(TAGReconnect, "usb device connected, firmware is updating: " + FirmwareUtil.isUpdatingFirmWare.get());
                    if (!FirmwareUtil.isUpdatingFirmWare.get() && !isNeedOtaAgain) {
                        if (mOnConnectStatusListener != null) {
                            mOnConnectStatusListener.onConnectStatus(isConnected);
                        }
                    }
                    Logger.d(TAGReconnect, "usb device connected, device key is: " + key);
                    if (onRetListener != null) {
                        onRetListener.onReceive(EnumCommands.CMD_IsLightXInitialize);
                    }
                }
                mContext.onWindowFocusChanged(false);
                Logger.e(TAG, "usb device connected, device name is: "+ usbDevice.getDeviceName());
            } catch (Exception e) {
                if (usbDevice != null) {
                    Logger.e(TAG, "usb device connected,Unable to create LightX handler for " + usbDevice.getDeviceName() + ": " + e.getLocalizedMessage());
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
                Logger.i(TAG, "usb device disconnected, set lightX null");
                AvneraManager.getAvenraManager().setLightX(null);
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
            /*
              AccessoryReady
              when call "connectDevice", if connected, will receive this event.
              When received AccessoryReady, app can update UI,show Aware Home, and communicate with accessory.
             */
            case AccessoryReady: {
                if (specifiedDevice != null && specifiedDevice.getName().contains(JBLConstant.DEVICE_150NC) && value == null
                        || specifiedDevice != null && value != null && ((HashMap) value).containsKey(specifiedDevice.getAddress())) {

                    if (!LeManager.getInstance().isConnected()) {
                        if (mLightX != null) {
                            mLightX.close();
                            mLightX = null;
                        }
                        myHandler.removeMessages(MSG_CONNECT_TIME_OUT);
                        AvneraManager.getAvenraManager().setLightX(null);
                        AccessoryInfo accessoryInfo = bt150Manager.getAccessoryStatus();
                        PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), mContext);
                        AppUtils.setModelNumber(mContext, accessoryInfo.getModelNumber());
                        Message message = new Message();
                        message.what = MSG_CONNECTED;
                        message.obj = value;
                        myHandler.removeMessages(MSG_CONNECTED);
                        myHandler.sendMessageDelayed(message, 200);
                        Logger.d(TAG, "received admin event ========> AccessoryReady <======== ");
                    } else {
                        Message message = new Message();
                        message.what = MSG_DISCONNECTED;
                        message.obj = value;
                        myHandler.removeMessages(MSG_DISCONNECTED);
                        myHandler.sendMessageDelayed(message, 200);
                    }
                }
                break;
            }
            /*
              Accessory is connected, do nothing.
              This event comes earlier than AccessoryReady.
             */
            case AccessoryConnected: {
                Logger.d(TAG, "received admin event  ========> [receivedAdminEvent] AccessoryConnected");
                break;
            }
            /*
              Not used now.
             */
            case AccessoryNotReady: {
                Logger.d(TAG, "received admin event  ========> [receivedAdminEvent] AccessoryNotReady");
                break;
            }
            /*
              Receive this event while unpaired accessory,shutdown accessory,close BT.

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
                Logger.d(TAG, "received admin event  ========> [receivedAdminEvent] default :" + event);
                break;
            }
        }
    }

    @Override
    public void receivedResponse(@NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        Logger.d(TAG, "receive response, command = " + command);
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    switch (command) {
                        case AmCmds.CMD_ANC: {
                            Logger.d(TAG, "receive response, do get anc");
                            String value = values.iterator().next().getValue().toString();
                            boolean onOff = false;
                            if (value.equalsIgnoreCase("true")
                                    || value.equalsIgnoreCase("1")) {
                                onOff = true;
                            }
                            onRetListener.onReceive(EnumCommands.CMD_ANC, onOff?1:0);
                            break;
                        }
                        case AmCmds.CMD_RawSteps: {
                            onRetListener.onReceive(EnumCommands.CMD_RAW_STEPS, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_BatteryLevel: {
                            onRetListener.onReceive(EnumCommands.CMD_BATTERY_LEVEL, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_RawLeft: {
                            onRetListener.onReceive(EnumCommands.CMD_RAW_LEFT, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_RawRight: {
                            onRetListener.onReceive(EnumCommands.CMD_RAW_RIGHT, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_Geq_Current_Preset: {
                            onRetListener.onReceive(EnumCommands.CMD_GEQ_CURRENT_PRESET, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_AmbientLeveling: {
                            onRetListener.onReceive(EnumCommands.CMD_AMBIENT_LEVELING, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_FirmwareVersion: {
                            onRetListener.onReceive(EnumCommands.CMD_FIRMWARE_VERSION,null,status);
                            break;
                        }
                        case AmCmds.CMD_FWInfo: {
                            onRetListener.onReceive(EnumCommands.CMD_FW_INFO, Integer.valueOf(values.get(3).getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_GraphicEqPresetBandSettings: {
                            onRetListener.onReceive(EnumCommands.CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS, values.iterator().next().getValue());
                            break;
                        }
                        case AmCmds.CMD_SmartButton: {
                            String smartType = values.iterator().next().getValue().toString();
                            boolean boolValue = smartType.equals("1");
                            onRetListener.onReceive(EnumCommands.CMD_SMART_BUTTON, boolValue);
                            break;
                        }
                        case AmCmds.CMD_VoicePrompt: {
                            boolean prompt;
                            String boolValue = "";
                            if (values.size() > 0) {
                                boolValue = values.iterator().next().getValue().toString();
                            }
                            prompt = !TextUtils.isEmpty(boolValue) && boolValue.equals("true");
                            onRetListener.onReceive(EnumCommands.CMD_VoicePrompt, prompt);
                            break;
                        }
                        case AmCmds.CMD_AutoOffEnable: {
                            boolean autoOff;
                            String boolValue = "";
                            if (values.size() > 0) {
                                boolValue = values.iterator().next().getValue().toString();
                            }
                            autoOff = !TextUtils.isEmpty(boolValue) && boolValue.equals("true");
                            onRetListener.onReceive(EnumCommands.CMD_AutoOffEnable, autoOff);
                            break;
                        }
                    }
                }

            }
        });
    }


    @Override
    public void receivedStatus(@NotNull final StatusEvent name, @NotNull final Object value) {
        switch (name) {
            /*
              Get this event when discovering.
              The param value is a mSet of mac address.
             */
            case DeviceList: {
                Map<String, String> pairedDevices = (Map<String, String>) value;
                for (Map.Entry<String, String> entry : pairedDevices.entrySet()) {
                    if (entry.getValue() != null
                            && entry.getValue().toUpperCase().contains("JBL Everest".toUpperCase())
                            && entry.getValue().contains(JBLConstant.DEVICE_150NC)
                            && specifiedDevice != null
                            && specifiedDevice.getAddress().equalsIgnoreCase(entry.getKey())
                            && !LeManager.getInstance().isConnected()) {
                        Status status = bt150Manager.connectDevice(entry.getKey(), false);
                        if (status == Status.AccessoryNotConnected) {
                            disconnectDevice();
                        }
                        Logger.d(TAG, "receive status ========> [receivedStatus] found device, connect device:" + entry.getKey() + "," + entry.getValue() + ",status=" + status);
                    }
                }
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_DeviceList, false);
                }
                break;
            }
            /*
              Get this event when device is doing OTA.
             */
            case UpdateProgress: {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_OTA_UpdateProgress, value.toString());
                }
                break;
            }
            case ImageUpdatePreparing: {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_OTA_UpdateProgress, value.toString());
                }
                break;
            }
            /*
              Get this event when finished one OTA step.
              OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateComplete: {
                break;
            }
            /*
              Get this event when finished one OTA step.
              OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateFinalize: {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_OTA_ImageUpdateFinalize, value.toString());
                }
                break;
            }
            case PrepImageError: {
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_OTA_PrepImageError);
                }
                break;
            }
            default: {

            }
        }
    }

    @Override
    public void receivedPushNotification(@NotNull final Action action, @NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        Logger.d(TAG, "receive push notification, command = " + command);
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (onRetListener != null) {
                    switch (command) {
                        case AmCmds.CMD_ANCNotification: {
                            onRetListener.onReceive(EnumCommands.CMD_ANC_NOTIFICATION, Integer.valueOf(values.iterator().next().getValue().toString()));
                            break;
                        }
                        case AmCmds.CMD_AmbientLevelingNotification: {
                            onRetListener.onReceive(EnumCommands.CMD_AA_Notification, AppUtils.levelTransfer(Integer.valueOf(values.iterator().next().getValue().toString())));
                            break;
                        }
                    }
                }
            }
        });
    }

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
                case MSG_CONNECT_TIME_OUT: {
                    initializeOrResetLibrary();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void connectedToDevice(final Object value) {
        Logger.d(TAG, "connected to device, value = " + value);
        FirmwareUtil.disconnectHeadphoneText = mContext.getResources().getString(R.string.plsConnect);
        synchronized (this) {
            DeviceConnectionManager.getInstance().setCurrentDevice(ConnectedDeviceType.Connected_BluetoothDevice);
            showCommunicationIssue = true;
            mHandler.removeCallbacks(resetRunnable);
            AppUtils.setJBLDeviceName(mContext, specifiedDevice.getName());
            AnalyticsManager.getInstance(mContext).reportDeviceConnect(bt150Manager.getAccessoryStatus().getName());
            mIsConnectedPhysically = true;
            isConnected = true;
            AvneraManager.getAvenraManager().setAudioManager(bt150Manager);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProductListManager.getInstance().checkConnectStatus(specifiedDevice.getAddress(),ConnectStatus.DEVICE_CONNECTED);
                    if (mOnConnectStatusListener != null) {
                        mOnConnectStatusListener.onConnectStatus(true);
                    }
                    if (onRetListener != null) {
                        onRetListener.onReceive(EnumCommands.CMD_AccessoryReady, value);
                    }
                }
            });
            Logger.d(TAG, "connected to device, isConnected = " + isConnected);
            isNeedShowDashboard = true;
            resetTime = RESET_TIME_FOR_150NC;
        }
    }

    private void disconnectToDevice(final Object value) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "disconnect to device, value = " + value);
                if (onRetListener != null) {
                    onRetListener.onReceive(EnumCommands.CMD_AccessoryDisconnected, value);
                }
                disconnectDevice();
            }
        });
    }
}

