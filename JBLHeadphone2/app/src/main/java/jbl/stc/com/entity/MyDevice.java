package jbl.stc.com.entity;

import java.io.Serializable;

public class MyDevice implements Serializable {
    public String deviceKey;
    public String deviceName;
    public String mac;
    public String pid;
    public int connectStatus;

    @Override
    public boolean equals(Object obj) {
        if (this.deviceKey.equalsIgnoreCase(((MyDevice)obj).deviceKey)){
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return deviceKey.hashCode();
    }
}
