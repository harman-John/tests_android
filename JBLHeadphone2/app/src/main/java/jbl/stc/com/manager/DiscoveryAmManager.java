package jbl.stc.com.manager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.listener.DeviceListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.AmToolUtil;

public class DiscoveryAmManager implements audioManager.AudioDeviceDelegate  {

    private final static String TAG = DiscoveryAmManager.class.getSimpleName();
    private DeviceListener mDeviceListener;
    private BluetoothDevice mBluetoothDevice = null;
    private audioManager bt150Manager = null;


    private static class InstanceHolder {
        public static final DiscoveryAmManager instance = new DiscoveryAmManager();
    }

    public static DiscoveryAmManager getInstance() {
        return DiscoveryAmManager.InstanceHolder.instance;
    }

    public void setListener(DeviceListener deviceListener){
        mDeviceListener = deviceListener;
    }

    public void initAm(){
        if (bt150Manager == null)
            bt150Manager = new audioManager();
    }

    public void discovery(Activity activity,BluetoothDevice bluetoothDevice){
        mBluetoothDevice = bluetoothDevice;
        if (bt150Manager != null){
            Logger.d(TAG,"150nc initManager");
            byte[] bytes = AmToolUtil.INSTANCE.readAssertResource(activity,AmToolUtil.COMMAND_FILE);
            bt150Manager.initManager(activity, activity, this, AmToolUtil.COMMAND_FILE,bytes);
        }
    }

    public void closeAm(){
        if (bt150Manager != null){
            bt150Manager.setDelegate( null);
            bt150Manager = null;
        }
    }

    @Override
    public void receivedAdminEvent(@NotNull AdminEvent event, final Object value) {
        Logger.d(TAG, " ========> [receivedAdminEvent]   <======== value = " + value +",event = "+event);
        switch (event){
            /**
             * AccessoryReady
             * when call "connectDevice", if connected, will receive this event.
             * When received AccessoryReady, app can update UI,show Aware Home, and communicate with accessory.
             */
            case AccessoryReady:{
                if (mBluetoothDevice != null && mBluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)&& value == null
                        || mBluetoothDevice != null&& value!= null &&((HashMap)value).containsKey(mBluetoothDevice.getAddress())  ) {
                    mDeviceListener.deviceConnected(value);
                }
                break;
            }
            /**
             * Accessory is connected, do nothing.
             * This event comes earlier than AccessoryReady.
             */
            case AccessoryConnected:{
                Logger.d(TAG , " ========> [receivedAdminEvent] AccessoryConnected");
                break;
            }
            /**
             * Not used now.
             */
            case AccessoryNotReady:{
                Logger.d(TAG , " ========> [receivedAdminEvent] AccessoryNotReady");
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
            case AccessoryDisconnected:{
                if (mBluetoothDevice != null && mBluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC)
                        && value!= null &&((HashMap)value).containsKey(mBluetoothDevice.getAddress())
                        || mBluetoothDevice != null && mBluetoothDevice.getName().contains(JBLConstant.DEVICE_150NC) && value == null) {
                    mDeviceListener.deviceDisConnected(value);
                }
                break;
            }
            case TimeOut:
                mDeviceListener.deviceDisConnected(value);
                break;
            default:{
                Logger.d(TAG , " ========> [receivedAdminEvent] default :" + event);
                break;
            }
        }
    }

    @Override
    public void receivedStatus(@NotNull final StatusEvent name, @NotNull final Object value) {
        switch (name) {
            /**
             * Get this event when discovering.
             * The param value is a mSet of mac address.
             */
            case DeviceList: {
                Map<String, String> pairedDevices = (Map<String, String>) value;
                for (Map.Entry<String, String> entry : pairedDevices.entrySet()) {
                    if (entry.getValue() != null
                            && entry.getValue().toUpperCase().contains("JBL Everest".toUpperCase())
                            && entry.getValue().contains(JBLConstant.DEVICE_150NC)
                            && mBluetoothDevice != null
                            && mBluetoothDevice.getAddress().equalsIgnoreCase(entry.getKey())) {
                        Status status = bt150Manager.connectDevice(entry.getKey(), false);
                        if (status == Status.AccessoryNotConnected) {
                            mDeviceListener.deviceDisConnected(value);
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
    }

    @Override
    public void receivedResponse(@NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        mDeviceListener.receivedResponse(command,values,status);
    }

    @Override
    public void receivedPushNotification(@NotNull final Action action, @NotNull final String command, @NotNull final ArrayList<responseResult> values, @NotNull final Status status) {
        mDeviceListener.receivedPushNotification(action,command,values,status);
    }
}
