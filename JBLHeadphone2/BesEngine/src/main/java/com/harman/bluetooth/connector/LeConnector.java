package com.harman.bluetooth.connector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.harman.bluetooth.constants.BesAction;
import com.harman.bluetooth.constants.BesCommandType;
import com.harman.bluetooth.constants.Constants;
import com.harman.bluetooth.listeners.BesListener;
import com.harman.bluetooth.utils.ArrayUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;


public class LeConnector implements BaseConnector{

    private final String TAG = getClass().getSimpleName();

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
    private int mConnState = STATE_DISCONNECTED;

    private UUID mDescriptor;

    private List<BesListener> mListBesListener;
    private static final int DEFAULT_MTU = 512;
    public LeConnector() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this.mBluetoothGatt.getDevice().getAddress().equals(obj.toString())){
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mBluetoothGatt.getDevice().getAddress().hashCode();
    }

    @Override
    public void setListener(List<BesListener> listBesListener) {
        mListBesListener = listBesListener;
    }

    @Override
    public boolean connect(Context context, String address) {
        return connect(context, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    @Override
    public boolean connect(Context context, BluetoothDevice device) {
        Log.i(TAG, "connect " + device + "; " + mConnState);
        synchronized (mStateLock) {
            if (mConnState != STATE_DISCONNECTED) {
                return true;
            }
            mConnState = STATE_CONNECTING;
        }
        mBluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);
        return mBluetoothGatt != null;
    }

    @Override
    public boolean discoverServices() {
        Log.i(TAG , "discoverServices");
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.discoverServices();
        }
        return false;
    }

    @Override
    public boolean requestMtu(int mtu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothGatt != null) {
                Log.i(TAG , "requestMtu");
                return mBluetoothGatt.requestMtu(mtu);
            }else{
                Log.i(TAG , "mBluetoothGatt is null");
            }
        }else{

            Log.i(TAG , "Sdk version is too low to request mtu");
        }
        return false;
    }

    @Override
    public boolean enableCharacteristicNotify(UUID service, UUID rxCharacteristic, UUID descriptor) {
        Log.i(TAG , "enable  characteristic notify");
        if (mBluetoothGatt != null) {
            BluetoothGattService gattService = mBluetoothGatt.getService(service);
            if (gattService == null) {
                Log.i(TAG,"enable characteristic notify, gatt service is null");
                return false;
            }
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(rxCharacteristic);
            if (gattCharacteristic == null) {
                Log.i(TAG,"enable characteristic notify, gatt characteristic is null");
                return false;
            }
            BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(descriptor);
            if (gattDescriptor == null) {
                Log.i(TAG,"enable characteristic notify, gatt descriptor is null");
                return false;
            }
            if (!mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true)) {
                Log.i(TAG , "enable characteristic notify set error");
                return false;
            }
            if (!gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                Log.i(TAG , "enable characteristic notify set value error");
                return false;
            }
            mDescriptor = descriptor;
            return mBluetoothGatt.writeDescriptor(gattDescriptor);
        }
        Log.i(TAG , "enable  characteristic notify, mBluetoothGatt is null");
        return false;
    }

    @Override
    public void close() {
        Log.i(TAG , "close");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        notifyConnectionStateChanged(false);
        mBluetoothGatt = null;
    }

    @Override
    public boolean isConnected() {
        return mConnState == STATE_CONNECTED;
    }

    @Override
    public boolean refresh() {
        try {
            if (mBluetoothGatt != null) {
                Method refresh = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                return (Boolean) refresh.invoke(mBluetoothGatt, new Object[0]);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setWriteCharacteristic(UUID service, UUID characteristic) {
        Log.i(TAG, "set write characteristic, service " + service.toString() + ",characteristic " + characteristic.toString());
        if (mBluetoothGatt == null) {
            return false;
        }
        BluetoothGattService gattService = mBluetoothGatt.getService(service);
        if (gattService == null) {
            return false;
        }
        mCharacteristicTx = gattService.getCharacteristic(characteristic);
        if (mCharacteristicTx == null) {
            return false;
        }
        Log.i(TAG, "set write characteristic, mCharacteristicTx is not null");
        mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        return true;
    }

    @Override
    public boolean write(byte[] data) {
        if (mBluetoothGatt != null) {
            boolean isValueSet = mCharacteristicTx.setValue(data);

            Log.i(TAG, "write, mBluetoothGatt is not null, properties = "+mCharacteristicTx.getProperties() );
            Log.i(TAG, "write, mBluetoothGatt is not null, is value set = "+isValueSet );
            Log.i(TAG, "write, mBluetoothGatt is not null, get value = "+ ArrayUtil.bytesToHex(mCharacteristicTx.getValue()));
            if (mCharacteristicTx.getService() == null){
                Log.i(TAG, "write, mBluetoothGatt is not null, service is null");
                return false;
            }

            if (isValueSet) {
                mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                mBluetoothGatt.setCharacteristicNotification(mCharacteristicTx, true);
                return mBluetoothGatt.writeCharacteristic(mCharacteristicTx);
            }
        }
        Log.i(TAG, "write, mBluetoothGatt is null" );
        return false;
    }

    @Override
    public boolean write_no_rsp(byte[] data) {
        if (mBluetoothGatt != null) {
            Log.i(TAG, "write no rsp, mBluetoothGatt is not null)" );
            mCharacteristicTx.setValue(data);
            mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            return mBluetoothGatt.writeCharacteristic(mCharacteristicTx) ;
        }
        Log.i(TAG, "write no rsp, mBluetoothGatt is null" );
        return false;
    }

    private void notifyConnectionStateChanged(boolean connected) {
        synchronized (mStateLock) {
            if (connected && mConnState != STATE_CONNECTED) {
                for(BesListener listener : mListBesListener){
                    listener.onBesConnectStatus(mBluetoothGatt.getDevice(),true);
                }
                mConnState = STATE_CONNECTED;
            } else if (!connected && mConnState != STATE_DISCONNECTED) {
                for (BesListener listener : mListBesListener) {
                    listener.onBesConnectStatus(mBluetoothGatt.getDevice(),false);
                }
                mConnState = STATE_DISCONNECTED;
            }
        }
    }

    private void enableCharacteristicNotification(){
            if (!enableCharacteristicNotify(Constants.BES_SERVICE_UUID, Constants.BES_CHARACTERISTIC_RX_UUID, Constants.BES_DESCRIPTOR_UUID)) {
                Log.i(TAG, "enable characteristic notification false ");
                notifyConnectionStateChanged(false);
                refresh();
                close();
            }else{
                notifyConnectionStateChanged(true);
                Log.i(TAG, "enable characteristic notification true");
            }
    }
    private void setCharacteristics() {
        synchronized (mListenerLock) {
                if (setWriteCharacteristic(Constants.BES_SERVICE_UUID, Constants.BES_CHARACTERISTIC_TX_UUID)) {
                    if (requestMtu(DEFAULT_MTU)) {
                        Log.i(TAG, "requestMtu DEFAULT_MTU = "+DEFAULT_MTU);
//                        updateInfo(R.string.configing_mtu);
                        enableCharacteristicNotification();
                    } else {
                        Log.i(TAG, "requestMtu result false");
                    }
                } else {
                    Log.i(TAG, "onServicesDiscovered error service");
//                    sendCmdDelayed(CMD_DISCONNECT, 1000);
                    notifyConnectionStateChanged(false);
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
            for (BesListener listener : mListBesListener) {
                listener.onMtuChanged(status, mtu);
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

    private void notifyReceive(byte[] data, BesCommandType besCommandType) {
        synchronized (mListenerLock) {
            BesAction currentAction = BesAction.READ;//do something to classify current action
            for (BesListener listener : mListBesListener) {
                listener.onBesReceived(data);
            }
        }
    }

    private BesCommandType classifyCommand(BluetoothGattCharacteristic characteristic){
        //do something to classify command type based on characteristic

        return BesCommandType.DEVICE_INFO;
    }
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange " + status + "; " + newState);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                if (!discoverServices()) {
                    Log.i(TAG,"discoverServices failed close");
                    close();
                }
            } else {
                notifyConnectionStateChanged(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "on service discovered " + status + "; " + status);
            mBluetoothGatt = gatt;
            List<BluetoothGattService> gattServices= gatt.getServices();
            for(BluetoothGattService bluetoothGattService: gattServices){
                Log.i(TAG, "bluetoothGattService: " + bluetoothGattService.getUuid());
                List<BluetoothGattCharacteristic> characteristicList = bluetoothGattService.getCharacteristics();
                for( BluetoothGattCharacteristic bluetoothGattCharacteristic: characteristicList ){
                    Log.i(TAG, "bluetoothGattCharacteristic: " + bluetoothGattCharacteristic.getUuid());
                    List<BluetoothGattDescriptor> bluetoothGattDescriptorList = bluetoothGattCharacteristic.getDescriptors();
                    for(BluetoothGattDescriptor bluetoothGattDescriptor: bluetoothGattDescriptorList){
                        Log.i(TAG, "bluetoothGattDescriptor: " + bluetoothGattDescriptor.getUuid());
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
            Log.i(TAG, "onCharacteristicWrite status is "  + status+ ArrayUtil.toHex(characteristic.getValue())); //打印等效与延时。故不能打印太多在此处。
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                notifyWrite(LE_SUCCESS);
            } else {
//                notifyWrite(LE_ERROR);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead status is "  + status+ ArrayUtil.toHex(characteristic.getValue())); //打印等效与延时。故不能打印太多在此处。
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                notifyRead(LE_SUCCESS);
            } else {
//                notifyRead(LE_ERROR);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(characteristic != null){
                Log.i(TAG, "onCharacteristicChanged "+ ArrayUtil.toHex(characteristic.getValue()));
            }else{
                Log.i(TAG, "onCharacteristicChanged characteristic is null");
            }
            notifyReceive(characteristic.getValue(), classifyCommand(characteristic));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite status is "+status);
            if (descriptor.getUuid().equals(mDescriptor)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    notifyCharacteristicNotifyEnabled(LE_SUCCESS);
                } else {
//                    notifyCharacteristicNotifyEnabled(LE_ERROR);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i(TAG, "onMtuChanged " + mtu + "; " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyMtuChanged(LE_SUCCESS, mtu);
            } else {
                notifyMtuChanged(LE_ERROR, mtu);
            }
        }
    };
}