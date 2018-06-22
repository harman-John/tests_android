package jbl.stc.com.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * DeviceInfo
 * Created by darren.lu on 2017/8/8.
 */

public class DeviceInfo {
    public static final int DEVICE_IS_CHARGING_CODE = 255;
    public static final int DEVICE_BATTERY_LOWER_THEN = 20;
    public static final int DEVICE_UPGRADE_BATTERY_ABOVE = 50;


    public boolean autoOffEnable = false;
    public boolean eqOn = false;//true is on ,false is off
    public boolean hasEq = false;//false eq is empty, true eq is not empty

    public boolean needUpgrade = false;
    public int updateFwSet;
    public String firmwareVersion = "";
    public String fwParamsVersion = "";
    public String fwDataVersion = "";
    public String newFwVersion = "";
    public int maxEqId = 0;
    public int currentBattery = 0;//value is 255 mean device is charging, or 0 ~ 100 mean app battery
    public int currentEqId = 0;


    @Override
    public String toString() {
        return "DeviceInfo{" +
                "autoOffEnable=" + autoOffEnable +
                ", eqOn=" + eqOn +
                ", hasEq=" + hasEq +
                ", needUpgrade=" + needUpgrade +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", fwParamsVersion='" + fwParamsVersion + '\'' +
                ", fwDataVersion='" + fwDataVersion + '\'' +
                ", maxEqId=" + maxEqId +
                ", currentBattery=" + currentBattery +
                '}';
    }
}
