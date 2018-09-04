package jbl.stc.com.entity;

public class designer_cfg {

    public designer_cfg(float gain0,float gain1, int num, int sampleRate){
        mGain0 = gain0;
        mGain1 = gain1;
        mNum = num;
        mSampleRate = sampleRate;
    }

    public float   mGain0;
    public float   mGain1;
    public int     mNum;
    public int mSampleRate;
}
