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


import com.harman.bluetooth.ret.RetAutoOff;
import com.harman.bluetooth.constants.Band;
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
import com.harman.bluetooth.ret.RetCurrentEQ;
import com.harman.bluetooth.ret.RetDevStatus;
import com.harman.bluetooth.ret.RetDeviceInfo;
import com.harman.bluetooth.ret.RetResponse;
import com.harman.bluetooth.ret.RetHeader;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


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

    public boolean setWriteCharacteristic() {
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
        if (!mBluetoothGatt.setCharacteristicNotification(mCharacteristicTx, true)) {
            Logger.e(TAG, "set write characteristic, set write characteristic notification error");
            return false;
        }
        Logger.i(TAG, "set write characteristic success");
        mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        return true;
    }

    public boolean setReadCharacteristic() {
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
            Logger.e(TAG, "set read characteristic, set read characteristic notification error");
            return false;
        }
        Logger.i(TAG, "set read characteristic, success");
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

    private void setCharacteristics() {
        synchronized (mListenerLock) {
            if (!setWriteCharacteristic()) {
                Logger.i(TAG, "set characteristic, set write characteristic failed");
                notifyConnectionStateChanged(false);
                return;
            }
            if (requestMtu(DEFAULT_MTU)) {
                Logger.i(TAG, "set characteristic, request mtu ok");
                if (!setReadCharacteristic()) {
                    Logger.i(TAG, "set characteristic, notification false ");
                    notifyConnectionStateChanged(false);
                    refresh();
                    close();
                    return;
                }
                notifyConnectionStateChanged(true);
                Logger.i(TAG, "set characteristic, done success");
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

    private RetResponse classifyCommand(BluetoothGattCharacteristic characteristic) {
        byte[] bytes = characteristic.getValue();
        String bytesStr = ArrayUtil.bytesToHex(bytes);
        Logger.d(TAG, "classify command, ret bytes: " + bytesStr);
        String cmdId = bytesStr.substring(0, 3);
        EnumCmdId enumCmdId = EnumCmdId.DEFAULT;
        RetResponse retResponse = new RetResponse();
        switch (cmdId) {
            case RetHeader.RET_DEV_ACK:
                enumCmdId = EnumCmdId.RET_DEV_ACK;
                String statusCode = bytesStr.substring(4, 5);
                retResponse.object = parseStatusCode(statusCode);
                break;
            case RetHeader.RET_DEV_BYE:
                enumCmdId = EnumCmdId.RET_DEV_BYE;
                String msgCode = bytesStr.substring(4, 5);
                retResponse.object = parseMsgCode(msgCode);
                break;

            case RetHeader.RET_DEV_FIN_ACK:
                enumCmdId = EnumCmdId.RET_DEV_FIN_ACK;
                retResponse.object = null;
                break;
            case RetHeader.RET_DEV_INFO:
                enumCmdId = EnumCmdId.RET_DEV_INFO;
                RetDeviceInfo retDeviceInfo = new RetDeviceInfo();
                retDeviceInfo.deviceName = parseHexStringToString(bytesStr.substring(4, 20));
                retDeviceInfo.productId = bytesStr.substring(21, 23);
                retDeviceInfo.modeId = bytesStr.substring(24, 25);
                retDeviceInfo.batteryStatus = Integer.valueOf(bytesStr.substring(26, 27));
                retDeviceInfo.macAddress = bytesStr.substring(28, 34);
                retDeviceInfo.firmwareVersion = bytesStr.substring(35, 38);

                retResponse.object = retDeviceInfo;
                break;
            case RetHeader.RET_DEV_STATUS:
                enumCmdId = EnumCmdId.RET_DEV_STATUS;
                String statusType = bytesStr.substring(4, 5);
                RetDevStatus retDevStatus = new RetDevStatus();
                EnumDeviceStatusType enumDeviceStatusType = null;
                switch (statusType) {
                    case RetHeader.ALL_STATUS_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.ALL_STATUS;
                        String anc = bytesStr.substring(6, 7);
                        retDevStatus.enumAncStatus = parseANC(anc);

                        String ambientAware = bytesStr.substring(8, 9);
                        retDevStatus.enumAAStatus = parseAmbientAware(ambientAware);

                        byte autoOffByte = bytes[10];
                        boolean onOff = Boolean.valueOf(String.valueOf(autoOffByte >> 7 & 0x1));
                        int time = autoOffByte & 0x7F;
                        retDevStatus.retAutoOff = new RetAutoOff(onOff, time);
                        String eqPresetIdx = bytesStr.substring(12, 13);
                        retDevStatus.enumEqPresetIdx = parsePresetIdx(eqPresetIdx);
                        break;
                    }
                    case RetHeader.ANC_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.ANC;
                        String anc = bytesStr.substring(6, 7);
                        retDevStatus.enumAncStatus = parseANC(anc);
                        break;
                    }
                    case RetHeader.AA_MODE_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.AMBIENT_AWARE_MODE;
                        String ambientAware = bytesStr.substring(8, 9);
                        retDevStatus.enumAAStatus = parseAmbientAware(ambientAware);
                        break;
                    }
                    case RetHeader.AUTO_OFF_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.AUTO_OFF;
                        byte autoOffByte = bytes[10];
                        boolean onOff = Boolean.valueOf(String.valueOf(autoOffByte >> 7 & 0x1));
                        int time = autoOffByte & 0x7F;
                        retDevStatus.retAutoOff = new RetAutoOff(onOff, time);
                        break;
                    }
                    case RetHeader.EQ_PRESET_TYPE: {
                        enumDeviceStatusType = EnumDeviceStatusType.EQ_PRESET;
                        String eqPresetIdx = bytesStr.substring(12, 13);
                        retDevStatus.enumEqPresetIdx = parsePresetIdx(eqPresetIdx);
                        break;
                    }
                }
                retDevStatus.enumDeviceStatusType = enumDeviceStatusType;
                retResponse.object = retDevStatus;
                break;
            case RetHeader.RET_CURRENT_EQ: {
                enumCmdId = EnumCmdId.RET_CURRENT_EQ;
                RetCurrentEQ dataCurrentEQ = new RetCurrentEQ();
                String presetIdx = bytesStr.substring(4, 5);
                dataCurrentEQ.enumEqPresetIdx = parsePresetIdx(presetIdx);

                String eqCategory = bytesStr.substring(6, 7);
                dataCurrentEQ.enumEqCategory = parseEqCategory(eqCategory);

                dataCurrentEQ.sampleRate = bytesStr.substring(8, 9);
                dataCurrentEQ.gain0 = bytesStr.substring(10, 11);
                dataCurrentEQ.gain1 = bytesStr.substring(12, 13);
                dataCurrentEQ.bandCount = Integer.valueOf(bytesStr.substring(14, 15));
                Band[] bands = new Band[dataCurrentEQ.bandCount];
                for (int i = 0; i < dataCurrentEQ.bandCount; i++) {
                    int pos = 13 * i;
                    int type = Integer.valueOf(bytesStr.substring(16 + pos, 17 + pos));
                    float gain = Float.valueOf(bytesStr.substring(18 + pos, 22 + pos));
                    int fc = Integer.valueOf(bytesStr.substring(23 + pos, 27 + pos));
                    float q = Float.valueOf(bytesStr.substring(28 + pos, 32 + pos));
                    bands[i] = new Band(type, gain, fc, q);
                }
                dataCurrentEQ.bands = bands;
                retResponse.object = dataCurrentEQ;
                break;
            }
            default:
                retResponse.object = bytes;
        }
        retResponse.enumCmdId = enumCmdId;
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
            List<BluetoothGattService> gattServices = gatt.getServices();
            for (BluetoothGattService bluetoothGattService : gattServices) {
                Logger.i(TAG, "on service discovered, service uuid: " + bluetoothGattService.getUuid());
                List<BluetoothGattCharacteristic> characteristicList = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristicList) {
                    Logger.i(TAG, "on service discovered, characteristic uuid: " + bluetoothGattCharacteristic.getUuid());
                    List<BluetoothGattDescriptor> bluetoothGattDescriptorList = bluetoothGattCharacteristic.getDescriptors();
                    for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptorList) {
                        Logger.i(TAG, "on service discovered, descriptor uuid: " + bluetoothGattDescriptor.getUuid());
                    }
                }
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                //TODO: what to do?
            } else {
                setCharacteristics();
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

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic == null) {
                Logger.i(TAG, "on characteristic changed, characteristic is null");
                return;
            }
            Logger.i(TAG, "on characteristic changed, read bytes: " + ArrayUtil.toHex(characteristic.getValue()));
            notifyReceive(classifyCommand(characteristic));
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
            } else {
                notifyMtuChanged(LE_ERROR, mtu);
            }
        }
    };
}
