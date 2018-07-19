package jbl.stc.com.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MyDevice implements Parcelable {
    public String deviceKey;
    public String deviceName;
    public int deviceIcon;
    public int connectStatus;

    public MyDevice(){

    }

    protected MyDevice(Parcel in) {
        deviceKey = in.readString();
        deviceName = in.readString();
        deviceIcon = in.readInt();
        connectStatus = in.readInt();
    }

    public static final Creator<MyDevice> CREATOR = new Creator<MyDevice>() {
        @Override
        public MyDevice createFromParcel(Parcel in) {
            return new MyDevice(in);
        }

        @Override
        public MyDevice[] newArray(int size) {
            return new MyDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceKey);
        dest.writeString(deviceName);
        dest.writeInt(deviceIcon);
        dest.writeInt(connectStatus);
    }
}
