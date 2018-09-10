package jbl.stc.com.entity;

public class IIR_PARAM_T{
    public IIR_PARAM_T(int type,float gain, float fc, float Q){
        mType = type;
        mGain = gain;
        mFc = fc;
        mQ = Q;
    }
    public int         mType;
    public float       mGain;
    public float       mFc;
    public float       mQ;
}