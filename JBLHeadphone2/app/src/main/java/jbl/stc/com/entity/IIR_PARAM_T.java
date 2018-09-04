package jbl.stc.com.entity;

public class IIR_PARAM_T{
    public IIR_PARAM_T(int type,float gain, int fc, float Q){
        mType = type;
        mGain = gain;
        mFc = fc;
        mQ = Q;
    }
    public int         mType;
    public float       mGain;
    public int         mFc;
    public float       mQ;
}