package jbl.stc.com.entity;

import android.graphics.drawable.Drawable;

public class MyDevice {
    public String deviceKey;
    public String deviceName;
    public Drawable drawable;
    public int connectStatus;

    @Override
    public boolean equals(Object obj) {
        if (this.deviceKey.equalsIgnoreCase(((MyDevice)obj).deviceKey)){
            return true;
        }
        return super.equals(obj);
    }
}
