package jbl.stc.com.data;

/**
 * Created by intahmad on 1/6/2016.
 */
public enum ConnectedDeviceType {
    NONE(-1),Connected_USBDevice(0),
    Connected_BluetoothDevice(1);

    int i;

    ConnectedDeviceType(int i) {
        this.i = i;
    }
}
