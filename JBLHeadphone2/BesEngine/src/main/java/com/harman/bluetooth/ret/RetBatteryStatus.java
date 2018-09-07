package com.harman.bluetooth.ret;

public class RetBatteryStatus {
    public RetBatteryStatus(boolean charging, int percent){
        this.charging = charging;
        this.percent = percent;
    }
    public boolean charging;
    public int percent;
}
