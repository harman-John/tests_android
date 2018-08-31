package jbl.stc.com.manager;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.listener.OnCheckDevicesListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.SaveSetUtil;

public class ProductListManager {

    private static Map<String,MyDevice> myDeviceMap;
    private static final String TAG = ProductListManager.class.getSimpleName();

    private static class InstanceHolder {
        public static final ProductListManager instance = new ProductListManager();
    }

    public static ProductListManager getInstance() {
        return InstanceHolder.instance;
    }

    private OnCheckDevicesListener mOnCheckDevicesListener;
    public void setOnCheckDevicesListener(OnCheckDevicesListener onCheckDevicesListener){
        mOnCheckDevicesListener = onCheckDevicesListener;
    }

    public void initDeviceSet(Context context) {
        if (myDeviceMap == null){
            myDeviceMap = new HashMap<>();
        }
        myDeviceMap.clear();
        Set<MyDevice> devicesSet = SaveSetUtil.readSet(context);
        if (devicesSet == null){
            return;
        }
        for(MyDevice myDevice: devicesSet){
            Logger.i(TAG, "init device set, stored device key: " + myDevice.deviceKey);
            myDeviceMap.put(myDevice.mac,myDevice);
        }
    }

    public List<MyDevice> getMyDeviceList() {
        List<MyDevice> myDeviceList = new ArrayList<>();
        for(Map.Entry<String,MyDevice> entry: myDeviceMap.entrySet()){
            MyDevice myDevice = entry.getValue();
            myDeviceList.add(myDevice);
        }
        return myDeviceList;
    }

    public MyDevice getDeviceByKey(String key){
        Logger.i(TAG, "get device by key, according to key= " + key);
        if (myDeviceMap != null){
            myDeviceMap.get(key);
        }
        return null;
    }

    public MyDevice getSelectDevice(int status) {
        for(Map.Entry<String,MyDevice> entry: myDeviceMap.entrySet()){
            MyDevice myDevice = entry.getValue();
            if (myDevice.connectStatus == status) {
                Logger.i(TAG, "get select device, deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
                return myDevice;
            }
        }
        return null;
    }

    public void removeDevice(String mac){
        myDeviceMap.remove(mac);
    }

    public void checkHalfConnectDevice(Set<MyDevice> devicesSet) {
        if (devicesSet.size()<= 0){
            Logger.i(TAG, "check half connect devices, devicesSet size is 0, no need to check");
            return;
        }
        Logger.i(TAG, "check half connect devices, devicesSet size= " + devicesSet.size());
        for (MyDevice halfConnectDevice : devicesSet) {
            MyDevice myDevice = myDeviceMap.get(halfConnectDevice.mac);
            if (myDevice == null){
                myDeviceMap.put(halfConnectDevice.mac,halfConnectDevice);
            }else if (myDevice.connectStatus != ConnectStatus.DEVICE_CONNECTED){
                myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                Logger.i(TAG, "check half connect devices, half connect device = " + myDevice.deviceKey);
            }
        }
        mOnCheckDevicesListener.onCheckDevices();
    }

    public void checkConnectStatus(String mac, int connectStatus) {

        MyDevice myDeviceMem = myDeviceMap.get(mac);
        if (myDeviceMem == null){
            Logger.i(TAG, "check connect status, can't find my device in memory");
            return;
        }
        myDeviceMem.connectStatus = connectStatus;
        Logger.i(TAG, "check connect status, connect status: "+connectStatus);
    }
}
