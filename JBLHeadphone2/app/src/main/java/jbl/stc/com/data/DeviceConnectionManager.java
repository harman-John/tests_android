package jbl.stc.com.data;

/**
 * DeviceConnectionManager
 * Created by intahmad on 1/6/2016.
 */
public class DeviceConnectionManager {
    private static DeviceConnectionManager ourInstance = new DeviceConnectionManager();

    public static DeviceConnectionManager getInstance() {
        return ourInstance;
    }

    private DeviceConnectionManager() {

    }

    private ConnectedDeviceType deviceType = ConnectedDeviceType.NONE;//起到共享数据效果

    public ConnectedDeviceType getCurrentDevice() {
        return deviceType;
    }

    public void setCurrentDevice(ConnectedDeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
