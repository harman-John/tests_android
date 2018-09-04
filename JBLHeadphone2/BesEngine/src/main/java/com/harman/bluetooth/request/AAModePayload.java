package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.AA_MODE_STATUS;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class AAModePayload {

    private final static String TAG = AAModePayload.class.getSimpleName();

    public AAModePayload(AA_MODE_STATUS aa_mode_status){
        this.aa_mode_status = aa_mode_status;
    }

    private AA_MODE_STATUS aa_mode_status;

    public byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (aa_mode_status) {
            case TALK_THRU:
                payload[0] = (byte) 0x00;
                break;
            case AMBIENT_AWARE:
                payload[0] = (byte) 0x01;
                break;
            default:
                Logger.e(TAG,"get pay load, no such aa mode status");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }
}
