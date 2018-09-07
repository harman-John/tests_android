package com.harman.bluetooth.req;

import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class ReqAutoOff extends BaseReq {

    private final static String TAG = ReqAutoOff.class.getSimpleName();

    public ReqAutoOff(boolean isOnOff, int time){
        this.isOnOff = isOnOff? 1: 0;
        this.time = time;
    }
    int isOnOff;
    int time;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        payload[0] = (byte) (( isOnOff & 0x01) << 7 & (time &0x7F));

        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        combine(ReqHeader.SET_ANC,getPayload());
        return super.getCommand();
    }
}
