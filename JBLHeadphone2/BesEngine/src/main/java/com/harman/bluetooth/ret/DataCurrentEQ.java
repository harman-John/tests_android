package com.harman.bluetooth.ret;


import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.constants.EnumEqPresetIdx;

public class DataCurrentEQ {
    public EnumEqPresetIdx enumEqPresetIdx;
    public EnumEqCategory enumEqCategory;
    public String sampleRate;
    public String gain0;
    public String gain1;
    public int bandCount;
    public Band[] bands;
}