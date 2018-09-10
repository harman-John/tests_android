package com.harman.bluetooth.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EqData;
import com.harman.bluetooth.req.CmdAppAckSet;
import com.harman.bluetooth.ret.RetAutoOff;
import com.harman.bluetooth.constants.EnumAAStatus;
import com.harman.bluetooth.constants.EnumAncStatus;
import com.harman.bluetooth.constants.EnumCmdId;
import com.harman.bluetooth.constants.Constants;
import com.harman.bluetooth.constants.EnumDeviceStatusType;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.constants.EnumEqPresetIdx;
import com.harman.bluetooth.constants.EnumMsgCode;
import com.harman.bluetooth.constants.EnumStatusCode;
import com.harman.bluetooth.listeners.BleListener;
import com.harman.bluetooth.ret.RetBatteryStatus;
import com.harman.bluetooth.ret.RetCurrentEQ;
import com.harman.bluetooth.ret.RetDevStatus;
import com.harman.bluetooth.ret.RetDeviceInfo;
import com.harman.bluetooth.ret.RetResponse;
import com.harman.bluetooth.ret.RetHeader;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static java.lang.Integer.valueOf;


public class LeDevice {

    private final String TAG = LeDevice.class.getSimpleName();

    private static final int LE_SUCCESS = 0;
    private static final int LE_ERROR = 1;

    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristicTx;

//    private List<ConnectorListener> mConnectorListeners;

    private final Object mStateLock = new Object();
    private final Object mListenerLock = new Object();
    private int mConnectState = STATE_DISCONNECTED;

    private List<BleListener> mListBleListener;
    private static final int DEFAULT_MTU = 512;

    public LeDevice() {
        mConnectState = STATE_DISCONNECTED;
    }


    public void setBesListener(List<BleListener> listBleListener) {
        mListBleListener = listBleListener;
    }

    public boolean connect(Context context, String address) {
        return connect(context, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    public boolean connect(Context context, BluetoothDevice device) {
        Logger.i(TAG, "connect " + device + ", connect state: " + mConnectState);
        synchronized (mStateLock) {
            if (mConnectState != STATE_DISCONNECTED) {
                Logger.d(TAG, "connect, device state is not disconnected");
                return true;
            }
            mConnectState = STATE_CONNECTING;
        }
        mBluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);
        return mBluetoothGatt != null;
    }

    private boolean requestMtu(int mtu) {
        if (mBluetoothGatt == null) {
            Logger.i(TAG, "bluetooth gatt is null");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Logger.i(TAG, "request mtu: " + mtu);
            return mBluetoothGatt.requestMtu(mtu);
        } else {
            Logger.i(TAG, "request mtu failed, sdk version is too low");
        }
        return false;
    }

    public void close() {
        Logger.i(TAG, "close");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        notifyConnectionStateChanged(false);
        mBluetoothGatt = null;
    }

    public boolean isConnected() {
        return mConnectState == STATE_CONNECTED;
    }

    public boolean refresh() {
        try {
            if (mBluetoothGatt != null) {
                Logger.e(TAG, "refresh");
                Method refresh = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                return (Boolean) refresh.invoke(mBluetoothGatt, new Object[0]);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getWriteCharacteristic() {
        if (mBluetoothGatt == null) {
            Logger.e(TAG, "set write characteristic, bluetooth gatt is null");
            return false;
        }
        BluetoothGattService gattService = mBluetoothGatt.getService(Constants.BLE_RX_TX_SERVICE_UUID);
        if (gattService == null) {
            Logger.e(TAG, "set write characteristic, cant't get gatt service");
            return false;
        }
        mCharacteristicTx = gattService.getCharacteristic(Constants.TX_CHAR_UUID);
        if (mCharacteristicTx == null) {
            Logger.e(TAG, "set write characteristic, cant't get write characteristic");
            return false;
        }
//        if (!mBluetoothGatt.setCharacteristicNotification(mCharacteristicTx, true)) {
//            Logger.e(TAG, "set write characteristic, set write characteristic notification error");
//            return false;
//        }

        Logger.i(TAG, "set write characteristic success");
        mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        return true;
    }

    public boolean getReadCharacteristic() {
        if (mBluetoothGatt == null) {
            Logger.e(TAG, "set read characteristic, bluetooth is null");
            return false;
        }
        BluetoothGattService gattService = mBluetoothGatt.getService(Constants.BLE_RX_TX_SERVICE_UUID);
        if (gattService == null) {
            Logger.e(TAG, "set read characteristic, gatt service is null");
            return false;
        }
        BluetoothGattCharacteristic readCharacteristic = gattService.getCharacteristic(Constants.RX_CHAR_UUID);
        if (readCharacteristic == null) {
            Logger.e(TAG, "set read characteristic, gatt read characteristic is null");
            return false;
        }

        if (!mBluetoothGatt.setCharacteristicNotification(readCharacteristic, true)) {
            Logger.e(TAG, "set read characteristic, notify failed");
            return false;
        }

        BluetoothGattDescriptor gattDescriptor = readCharacteristic.getDescriptor(Constants.BES_DESCRIPTOR_UUID);
        if (gattDescriptor == null) {
            Logger.e(TAG, "set read characteristic, gattDescriptor is null");
            return false;
        }

        if (!gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
            Logger.e(TAG, "set read characteristic, enable notify failed");
            return false;
        }
        Logger.i(TAG, "set read characteristic, success");
        if (!mBluetoothGatt.writeDescriptor(gattDescriptor)) {
            Logger.e(TAG, "set read characteristic, write descriptor failed");
            return false;
        }

        return true;
    }

    public boolean write(byte[] data) {
        if (mBluetoothGatt == null) {
            Logger.e(TAG, "write, bluetooth gatt is null");
            return false;
        }

        if (mCharacteristicTx.getService() == null) {
            Logger.e(TAG, "write service is null");
            return false;
        }


        if (mCharacteristicTx.setValue(data)) {
            Logger.i(TAG, "write, get value = " + ArrayUtil.bytesToHex(mCharacteristicTx.getValue()) + ",write uuid = " + mCharacteristicTx.getUuid());
            mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mBluetoothGatt.setCharacteristicNotification(mCharacteristicTx, true);
            boolean isWriteSuccess = mBluetoothGatt.writeCharacteristic(mCharacteristicTx);
            Logger.i(TAG, "write characteristic status  = " + isWriteSuccess + ",mac = " + mBluetoothGatt.getDevice().getAddress() + ",name = " + mBluetoothGatt.getDevice().getName());
            return isWriteSuccess;
        }
        Logger.i(TAG, "write, set value false");
        return false;
    }

    public boolean write_no_rsp(byte[] data) {
        if (mBluetoothGatt == null) {
            Logger.e(TAG, "write no rsp, bluetooth gatt is null");
            return false;
        }
        mCharacteristicTx.setValue(data);
        Logger.i(TAG, "write no rsp, data = " + ArrayUtil.bytesToHex(mCharacteristicTx.getValue()));
        mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mBluetoothGatt.writeCharacteristic(mCharacteristicTx);
    }

    private void notifyConnectionStateChanged(boolean connected) {
        synchronized (mStateLock) {
            if (connected && mConnectState != STATE_CONNECTED) {
                for (BleListener listener : mListBleListener) {
                    listener.onLeConnectStatus(mBluetoothGatt.getDevice(), true);
                }
                mConnectState = STATE_CONNECTED;
            } else if (!connected && mConnectState != STATE_DISCONNECTED) {
                for (BleListener listener : mListBleListener) {
                    listener.onLeConnectStatus(mBluetoothGatt.getDevice(), false);
                }
                mConnectState = STATE_DISCONNECTED;
            }
        }
    }

    private void getCharacteristics() {
        synchronized (mListenerLock) {
            if (!getWriteCharacteristic()) {
                Logger.i(TAG, "set characteristic, set write characteristic failed");
                notifyConnectionStateChanged(false);
                return;
            }
            if (requestMtu(DEFAULT_MTU)) {

            } else {
                Logger.i(TAG, "set characteristic,requestMtu failed");
            }
        }
    }

//    private void notifyCharacteristicNotifyEnabled(int status) {
//        synchronized (mListenerLock) {
//            for (ConnectorListener listener : mConnectorListeners) {
//                if (listener instanceof LeConnectorListener) {
//                    ((LeConnectorListener) listener).onCharacteristicNotifyEnabled(status);
//                }
//            }
//        }
//    }

    private void notifyMtuChanged(int status, int mtu) {
        synchronized (mListenerLock) {
            for (BleListener listener : mListBleListener) {
                listener.onMtuChanged(mBluetoothGatt.getDevice(), status, mtu);
            }
        }
    }

//    private void notifyWrite(int status) {
//        synchronized (mListenerLock) {
//            for (ConnectorListener listener : mConnectorListeners) {
//                if (listener instanceof LeConnectorListener) {
//                    ((LeConnectorListener) listener).onWritten(status);
//                }
//            }
//        }
//    }

    private void notifyReceive(RetResponse retResponse) {
        synchronized (mListenerLock) {
            for (BleListener listener : mListBleListener) {
                listener.onRetReceived(mBluetoothGatt.getDevice(), retResponse);
            }
        }
    }

    private String parseHexStringToString(String hexString) {
        if (hexString == null || hexString.isEmpty())
            return null;

        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return new String(data);
    }

    private EqData eqData;

    private RetResponse classifyCommand(BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String bytesStr = ArrayUtil.bytesToHex(bytes).toLowerCase();
        Logger.d(TAG, "classify command, ret bytes: " + bytesStr);
        if (eqData != null && eqData.isEqReceived && eqData.count > 0) {
            eqData.count--;
            eqData.curEqData += bytesStr;
            Message msg = new Message();
            msg.what = MSG_APP_ACK;
            msg.obj = RetHeader.RET_CURRENT_EQ;
            leHandler.sendMessage(msg);
            return null;
        }
        String cmdId = bytesStr.substring(0, 4);
        Logger.d(TAG, "classify command, cmdId: " + cmdId);
        String payloadLen = bytesStr.substring(4, 6);
        Logger.d(TAG, "classify command, payloadLen: " + payloadLen);
        RetResponse retResponse = new RetResponse();
        retResponse.enumCmdId =  EnumCmdId.DEFAULT;
        switch (cmdId) {
            case RetHeader.RET_DEV_ACK:
                retResponse.enumCmdId = EnumCmdId.RET_DEV_ACK;
                String statusCode = bytesStr.substring(6, 8);
                retResponse.object = parseStatusCode(statusCode);
                break;
            case RetHeader.RET_DEV_BYE:
                retResponse.enumCmdId = EnumCmdId.RET_DEV_BYE;
                String msgCode = bytesStr.substring(6, 8);
                retResponse.object = parseMsgCode(msgCode);
                break;

            case RetHeader.RET_DEV_FIN_ACK:
                retResponse.enumCmdId = EnumCmdId.RET_DEV_FIN_ACK;
                retResponse.object = null;
                String cmdIdAck = bytesStr.substring(4, 6);
                Logger.d(TAG, "classify command, ret dev fin ack, command id: " + cmdIdAck);
                Logger.d(TAG, "classify command, ret dev fin ack, eq data: " + eqData.curEqData);
                if (cmdIdAck.equals("43")) {
                    String packageIdx = eqData.curEqData.substring(0, 2);
                    Logger.d(TAG, "classify command, ret dev fin ack, package index: " + packageIdx);
                    retResponse.enumCmdId = EnumCmdId.RET_CURRENT_EQ;
                    RetCurrentEQ dataCurrentEQ = new RetCurrentEQ();
                    String presetIdx = eqData.curEqData.substring(2, 4);
                    dataCurrentEQ.enumEqPresetIdx = parsePresetIdx(presetIdx);

                    Logger.d(TAG, "classify command, ret dev fin ack, preset index: " + dataCurrentEQ.enumEqPresetIdx);
                    String eqCategory = eqData.curEqData.substring(4, 6);
                    dataCurrentEQ.enumEqCategory = parseEqCategory(eqCategory);

                    Logger.d(TAG, "classify command, ret dev fin ack, eq category: " + dataCurrentEQ.enumEqCategory);
                    dataCurrentEQ.sampleRate = Integer.valueOf(eqData.curEqData.substring(6, 8),16);
                    Logger.d(TAG, "classify command, ret dev fin ack, sample rate: " + dataCurrentEQ.sampleRate);
                    dataCurrentEQ.gain0 =  ArrayUtil.hexStrToInt(eqData.curEqData.substring(8, 16));
                    Logger.d(TAG, "classify command, ret dev fin ack, gain0: " + dataCurrentEQ.gain0);
                    dataCurrentEQ.gain1 = ArrayUtil.hexStrToInt(eqData.curEqData.substring(16, 24));
                    Logger.d(TAG, "classify command, ret dev fin ack, gain1: " + dataCurrentEQ.gain1);
                    String bc = eqData.curEqData.substring(24, 32);
                    int j = ArrayUtil.hexStrToInt(bc);
                    Logger.d(TAG, "classify command, ret dev fin ack, bc: " + j);
                    dataCurrentEQ.bandCount = j; //Integer.valueOf(eqData.curEqData.substring(24, 32),16);
                    Logger.d(TAG, "classify command, ret dev fin ack, bound count: " + dataCurrentEQ.bandCount);
                    Band[] bands = new Band[dataCurrentEQ.bandCount];
                    for (int i = 0; i < dataCurrentEQ.bandCount; i++) {
                        int pos = 32 * i;
                        int type = ArrayUtil.hexStrToInt(eqData.curEqData.substring(32 + pos, 40 + pos));
                        Logger.d(TAG, "classify command, ret dev fin ack, band type: " + type);
                        float gain = ArrayUtil.hexStrToInt(eqData.curEqData.substring(40 + pos, 48 + pos));
                        Logger.d(TAG, "classify command, ret dev fin ack, band gain: "+gain);
                        int fc = ArrayUtil.hexStrToInt(eqData.curEqData.substring(48 + pos, 56 + pos));
                        Logger.d(TAG, "classify command, ret dev fin ack, band fc: "+fc);
                        float q = ArrayUtil.hexStrToInt(eqData.curEqData.substring(56 + pos, 64 + pos));
                        Logger.d(TAG, "classify command, ret dev fin ack, band type q: "+q);
                        bands[i] = new Band(type, gain, fc, q);
                    }
                    dataCurrentEQ.bands = bands;
                    retResponse.object = dataCurrentEQ;

                }
                break;
            case RetHeader.RET_DEV_INFO:
                retResponse.enumCmdId = EnumCmdId.RET_DEV_INFO;

                RetDeviceInfo retDeviceInfo = new RetDeviceInfo();
                retDeviceInfo.deviceName = parseHexStringToString(bytesStr.substring(6, 38));

                Logger.d(TAG, "classify command, ret dev info, device name: " + retDeviceInfo.deviceName);
                retDeviceInfo.productId = bytesStr.substring(38, 42);

                Logger.d(TAG, "classify command, ret dev info, product id: " + retDeviceInfo.productId);
                retDeviceInfo.modeId = bytesStr.substring(42, 44);

                Logger.d(TAG, "classify command, ret dev info, mode id: " + retDeviceInfo.modeId);
                String batteryStatus = String.valueOf(bytesStr.substring(44, 46));
                int batteryStatusInt = (Integer.valueOf(batteryStatus, 16) & 0x00000080) >> 7;
                boolean charging = batteryStatusInt == 1;
                int percent = Integer.valueOf(batteryStatus, 16) & 0x0000007F;
                retDeviceInfo.retBatteryStatus = new RetBatteryStatus(charging, percent);

                Logger.d(TAG, "classify command, ret dev info, battery charging: " + retDeviceInfo.retBatteryStatus.charging);
                Logger.d(TAG, "classify command, ret dev info, battery percent: " + retDeviceInfo.retBatteryStatus.percent);
                retDeviceInfo.macAddress = bytesStr.substring(46, 58);

                Logger.d(TAG, "classify command, ret dev info, mac: " + retDeviceInfo.macAddress);
                String firmwareVersion = bytesStr.substring(58, 64);
                int major = Integer.valueOf(firmwareVersion.substring(0, 2), 16);
                int minor = Integer.valueOf(firmwareVersion.substring(2, 4), 16);
                int revision = Integer.valueOf(firmwareVersion.substring(4, 6), 16);
                retDeviceInfo.firmwareVersion = major + "." + minor + "." + revision;

                Logger.d(TAG, "classify command, ret dev info, firmware version: " + retDeviceInfo.firmwareVersion);

                retResponse.object = retDeviceInfo;
                break;
            case RetHeader.RET_DEV_STATUS:
                retResponse.enumCmdId = EnumCmdId.RET_DEV_STATUS;
                String statusType = bytesStr.substring(6, 8);
                Logger.d(TAG, "classify command, ret dev status, type: " + statusType);
                RetDevStatus retDevStatus = new RetDevStatus();
                EnumDeviceStatusType enumDeviceStatusType = null;
                switch (statusType) {
                    case RetHeader.ALL_STATUS_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.ALL_STATUS;
                        String anc = bytesStr.substring(8, 10);
                        retDevStatus.enumAncStatus = parseANC(anc);

                        Logger.d(TAG, "classify command, ret dev status, anc: " + retDevStatus.enumAncStatus);
                        String ambientAware = bytesStr.substring(10, 12);
                        retDevStatus.enumAAStatus = parseAmbientAware(ambientAware);

                        Logger.d(TAG, "classify command, ret dev status, aa: " + retDevStatus.enumAAStatus);
                        String autoOff = String.valueOf(bytesStr.substring(12, 14));
                        int autoOffInt = (Integer.valueOf(autoOff, 16) & 0x00000080) >> 7;
                        boolean onOff = autoOffInt == 1;
                        int time = Integer.valueOf(autoOff, 16) & 0x0000007F;
                        Logger.d(TAG, "classify command, ret dev status, onOff: " + onOff + ",time: " + time);

                        retDevStatus.retAutoOff = new RetAutoOff(onOff, time);
                        String eqPresetIdx = bytesStr.substring(14, 16);
                        retDevStatus.enumEqPresetIdx = parsePresetIdx(eqPresetIdx);
                        Logger.d(TAG, "classify command, ret dev status, preset index: " + retDevStatus.enumEqPresetIdx);
                        break;
                    }
                    case RetHeader.ANC_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.ANC;
                        String anc = bytesStr.substring(8, 10);
                        Logger.d(TAG, "classify command, ret dev status,type anc: " + anc);
                        retDevStatus.enumAncStatus = parseANC(anc);
                        break;
                    }
                    case RetHeader.AA_MODE_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.AMBIENT_AWARE_MODE;
                        String ambientAware = bytesStr.substring(8, 10);
                        retDevStatus.enumAAStatus = parseAmbientAware(ambientAware);
                        Logger.d(TAG, "classify command, ret dev status, type aa: " + retDevStatus.enumAAStatus);
                        break;
                    }
                    case RetHeader.AUTO_OFF_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.AUTO_OFF;
                        String autoOff = String.valueOf(bytesStr.substring(8, 10));
                        int autoOffInt = (Integer.valueOf(autoOff, 16) & 0x00000080) >> 7;
                        boolean onOff = autoOffInt == 1;
                        int time = Integer.valueOf(autoOff, 16) & 0x0000007F;
                        Logger.d(TAG, "classify command, ret dev status, type auto off, onOff: " + onOff + ",time: " + time);
                        retDevStatus.retAutoOff = new RetAutoOff(onOff, time);
                        break;
                    }
                    case RetHeader.EQ_PRESET_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.EQ_PRESET;
                        String eqPresetIdx = bytesStr.substring(8, 10);
                        retDevStatus.enumEqPresetIdx = parsePresetIdx(eqPresetIdx);
                        Logger.d(TAG, "classify command, ret dev status, type preset index: " + retDevStatus.enumEqPresetIdx);
                        break;
                    }
                }
                retDevStatus.enumDeviceStatusType = enumDeviceStatusType;
                retResponse.object = retDevStatus;
                break;
            case RetHeader.RET_CURRENT_EQ: {
                String packageCount = bytesStr.substring(6, 8);
                eqData = new EqData();
                eqData.count = Integer.valueOf(packageCount);
                Logger.d(TAG, "classify command, ret current eqs, package count: " + packageCount);
                eqData.isEqReceived = true;
                Message msg = new Message();
                msg.what = MSG_APP_ACK;
                msg.obj = RetHeader.RET_CURRENT_EQ;
                leHandler.sendMessage(msg);
                break;
            }
            default:
                retResponse.object = bytes;
        }
        return retResponse;
    }

    private EnumStatusCode parseStatusCode(String statusCode) {
        EnumStatusCode enumStatusCode = null;
        switch (statusCode) {
            case "00":
                enumStatusCode = EnumStatusCode.SUCCESS;
                break;
            case "01":
                enumStatusCode = EnumStatusCode.FAILED;
                break;
        }
        return enumStatusCode;
    }

    private EnumMsgCode parseMsgCode(String msgCode) {
        EnumMsgCode enumMsgCode = null;
        switch (msgCode) {
            case "00":
                enumMsgCode = EnumMsgCode.UNKNOWN;
                break;
            case "01":
                enumMsgCode = EnumMsgCode.DEVICE_POWER_OFF;
                break;
        }
        return enumMsgCode;
    }

    private EnumAncStatus parseANC(String anc) {
        EnumAncStatus enumAncStatus = null;
        switch (anc) {
            case "00":
                enumAncStatus = EnumAncStatus.OFF;
                break;
            case "01":
                enumAncStatus = EnumAncStatus.ON;
                break;
        }
        return enumAncStatus;
    }

    private EnumAAStatus parseAmbientAware(String ambientAware) {
        EnumAAStatus enumAAStatus = null;
        switch (ambientAware) {
            case "00":
                enumAAStatus = EnumAAStatus.TALK_THRU;
                break;
            case "01":
                enumAAStatus = EnumAAStatus.AMBIENT_AWARE;
                break;
        }
        return enumAAStatus;
    }

    private EnumEqPresetIdx parsePresetIdx(String eqPresetIdx) {
        EnumEqPresetIdx enumEqPresetIdx = null;
        switch (eqPresetIdx) {
            case "00":
                enumEqPresetIdx = EnumEqPresetIdx.OFF;
                break;
            case "01":
                enumEqPresetIdx = EnumEqPresetIdx.JAZZ;
                break;
            case "02":
                enumEqPresetIdx = EnumEqPresetIdx.VOCAL;
                break;
            case "03":
                enumEqPresetIdx = EnumEqPresetIdx.BASS;
                break;
            case "04":
                enumEqPresetIdx = EnumEqPresetIdx.USER;
                break;
        }
        return enumEqPresetIdx;
    }

    private EnumEqCategory parseEqCategory(String eqCategory) {
        EnumEqCategory enumEqCategory = null;
        switch (eqCategory) {
            case "00":
                enumEqCategory = EnumEqCategory.DESIGN_EQ;
                break;
            case "01":
                enumEqCategory = EnumEqCategory.GRAPHIC_EQ;
                break;
            case "02":
                enumEqCategory = EnumEqCategory.TOTAL_EQ;
                break;
        }
        return enumEqCategory;
    }

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.i(TAG, "on connectionState change, status: " + status + ",new state: " + newState);
            mBluetoothGatt = gatt;
            if (mBluetoothGatt == null) {
                Logger.e(TAG, "on connectionState change, bluetooth gatt is null");
                return;
            }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                if (!mBluetoothGatt.discoverServices()) {
                    Logger.e(TAG, "on connectionState change, discover services failed");
                    close();
                }
            } else {
                notifyConnectionStateChanged(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.i(TAG, "on service discovered, status: " + status);
            mBluetoothGatt = gatt;
//            List<BluetoothGattService> gattServices = gatt.getServices();
//            for (BluetoothGattService bluetoothGattService : gattServices) {
//                Logger.i(TAG, "on service discovered, service uuid: " + bluetoothGattService.getUuid());
//                List<BluetoothGattCharacteristic> characteristicList = bluetoothGattService.getCharacteristics();
//                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristicList) {
//                    Logger.i(TAG, "on service discovered, characteristic uuid: " + bluetoothGattCharacteristic.getUuid());
//                    List<BluetoothGattDescriptor> bluetoothGattDescriptorList = bluetoothGattCharacteristic.getDescriptors();
//                    for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptorList) {
//                        Logger.i(TAG, "on service discovered, descriptor uuid: " + bluetoothGattDescriptor.getUuid());
//                    }
//                }
//            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                getCharacteristics();
            } else {
                Logger.e(TAG, "on service discovered, failed, status: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.i(TAG, "on characteristic write, status is " + status
                    + ",write bytes: " + ArrayUtil.bytesToHex(characteristic.getValue())
                    + ",mac = " + mBluetoothGatt.getDevice().getAddress()
                    + ",name = " + mBluetoothGatt.getDevice().getName()
                    + ",uuid = " + characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                notifyWrite(LE_SUCCESS);
            } else {
//                notifyWrite(LE_ERROR);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logger.i(TAG, "on characteristic read, status is " + status + ",read bytes: " + ArrayUtil.toHex(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                notifyRead(LE_SUCCESS);
            } else {
//                notifyRead(LE_ERROR);
            }
        }

        boolean isFreeToWrite = false;

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic == null) {
                Logger.i(TAG, "on characteristic changed, characteristic is null");
                return;
            }
            Logger.i(TAG, "on characteristic changed, read bytes: " + ArrayUtil.toHex(characteristic.getValue()));
            notifyReceive(classifyCommand(characteristic));
            isFreeToWrite = true;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Logger.i(TAG, "on descriptor write, status is " + status);
//            if (descriptor.getUuid().equals(mDescriptor)) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                    notifyCharacteristicNotifyEnabled(LE_SUCCESS);
            } else {
//                    notifyCharacteristicNotifyEnabled(LE_ERROR);
            }
//            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Logger.i(TAG, "on mtu changed, mtu: " + mtu + ",status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyMtuChanged(LE_SUCCESS, mtu);
                Logger.i(TAG, "set characteristic, request mtu ok");
                leHandler.sendEmptyMessage(MSG_ON_MTU_CHANGED);
            } else {
                notifyMtuChanged(LE_ERROR, mtu);
            }
        }
    };


    private LeHandler leHandler = new LeHandler(Looper.getMainLooper());
    private final static int MSG_ON_MTU_CHANGED = 0;
    private final static int MSG_APP_ACK = 1;

    private class LeHandler extends Handler {

        LeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ON_MTU_CHANGED: {
                    if (!getReadCharacteristic()) {
                        Logger.i(TAG, "set characteristic, notification false ");
                        notifyConnectionStateChanged(false);
                        refresh();
                        close();
                        return;
                    }
                    notifyConnectionStateChanged(true);
                    Logger.i(TAG, "set characteristic, done success");
                    break;
                }
                case MSG_APP_ACK: {
                    CmdAppAckSet cmdAppAckSet = new CmdAppAckSet((String) msg.obj, EnumStatusCode.SUCCESS);
                    write(cmdAppAckSet.getCommand());
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    ;
}
