package com.harman.bluetooth.ret;


import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.constants.EnumEqPresetIdx;

public class RetCurrentEQ {
    public EnumEqPresetIdx enumEqPresetIdx;
    public EnumEqCategory enumEqCategory;
    public int sampleRate;
    public float gain0;
    public float gain1;
    public int bandCount;
    public Band[] bands;
}
