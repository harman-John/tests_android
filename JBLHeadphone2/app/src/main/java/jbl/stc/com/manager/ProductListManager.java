package jbl.stc.com.manager;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.SaveSetUtil;

public class ProductListManager {

    protected static Set<MyDevice> mSet;
    private static final String TAG = ProductListManager.class.getSimpleName();

    private static class InstanceHolder {
        public static final ProductListManager instance = new ProductListManager();
    }

    public static ProductListManager getInstance() {
        return InstanceHolder.instance;
    }

    public Set<MyDevice> getDeviceSet() {
        return mSet;
    }

    public void updateDisconnectedAdapter() {
        for (MyDevice myDevice : mSet) {
            Logger.i(TAG, "update disconnected status, deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
                break;
            }
        }
    }

    public MyDevice getDevice(String key){
        Logger.i(TAG, "get my device according to key= " + key);
        for (MyDevice myDevice : mSet) {
            if (myDevice.deviceKey.equals(key)) {
                return myDevice;
            }
        }
        return null;
    }

    public MyDevice getConnectedDevice() {
        for (MyDevice myDevice : mSet) {
            Logger.i(TAG, "get my device connected, deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                return myDevice;
            }
        }
        return null;
    }

    public MyDevice getSelectDevice(int status) {
        Logger.i(TAG, "get select device. connect status = " + status);
        for (MyDevice myDevice : mSet) {
            Logger.i(TAG, "get select device deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == status) {
                return myDevice;
            }
        }
        return null;
    }

    public void removeDevice(MyDevice myDevice){
        mSet.remove(myDevice);
    }

    public void checkHalfConnectDevice(Set<MyDevice> devicesSet) {
        if (devicesSet.size() <=0){
            return;
        }
        Logger.i(TAG, "check my devices, mSet size= " + mSet.size());
        mSet.addAll(devicesSet);
        Logger.i(TAG, "check my devices, mSet size= " + mSet.size()+",device set size = "+devicesSet.size());
        for (MyDevice halfConnectDevice : devicesSet) {
            Logger.i(TAG, "half connect device key = " + halfConnectDevice.deviceKey);
            for (MyDevice myDevice : mSet) {
                Logger.i(TAG, "myDevice device key = " + myDevice.deviceKey);
                if (halfConnectDevice.deviceKey != null && halfConnectDevice.deviceKey.equals(myDevice.deviceKey)) {
                    if (myDevice.deviceName == null) {
                        continue;
                    }
                    if (myDevice.connectStatus != ConnectStatus.DEVICE_CONNECTED){
                        myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                    }
                }
            }
        }
    }

    public void checkConnectStatus(MyDevice myDevice) {
        Logger.i(TAG, "update mSet connect status");

        if (myDevice.deviceKey == null) {
            return;
        }

        for (MyDevice deviceInList : mSet) {
            if (deviceInList.deviceKey != null && deviceInList.deviceKey.equals(myDevice.deviceKey)) {
                deviceInList.connectStatus = myDevice.connectStatus;
                Logger.i(TAG, "update connection status, device key= " + myDevice.deviceKey + ",connect status = " + myDevice.connectStatus);
            }
        }
    }

    public void initDeviceSet(Context context) {
        if (mSet == null) {
            mSet = new HashSet<>();
        }
        mSet.clear();
        Set<MyDevice> devicesSet =SaveSetUtil.readSet(context);//PreferenceUtils.getStringSet(context, PreferenceKeys.MY_DEVICES);
        Logger.i(TAG, "stored devices, init = " + devicesSet);

        if (devicesSet != null) {
            mSet.addAll(devicesSet);
        }
//        for (MyDevice myDevice : devicesSet) {
//            MyDevice myDevice = AppUtils.getMyDevice(context, deviceKey, ConnectStatus.A2DP_UNCONNECTED,"","");
//            if (myDevice != null) {
//                mSet.add(myDevice);
//            }
//        }
    }

    private boolean hasNewDevice(Set<MyDevice> deviceList) {
        Set<String> set2 = new HashSet<>();
        for (MyDevice myDevice : mSet) {
            String device = myDevice.deviceKey;
            set2.add(device);
        }
        set2.retainAll(deviceList);
        if (set2.size() == deviceList.size()) {
            return false;
        }
        Logger.i(TAG, "has new Device");
        return true;
    }
}
