package com.harman.bluetooth.constants;

public class Band{
    public Band(int type,float gain, float fc, float q){
        this.type = type;
        this.gain = gain;
        this.fc = fc;
        this.q = q;
    }
    public int type;
    public float gain;
    public float fc;
    public float q;
}
