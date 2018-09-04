package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.ANC_STATUS;
import com.harman.bluetooth.constants.MESSAGE_CODE;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class ANCPayload {

    private final static String TAG = ANCPayload.class.getSimpleName();

    public ANCPayload(ANC_STATUS anc_status){
        this.anc_status = anc_status;
    }

    private ANC_STATUS anc_status;

    public byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (anc_status) {
            case OFF:
                payload[0] = (byte) 0x00;
                break;
            case ON:
                payload[0] = (byte) 0x01;
                break;
            default:
                Logger.e(TAG,"get pay load, no such anc status");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }
}
