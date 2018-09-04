package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.MESSAGE_CODE;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class AppAckPayload {

    private final static String TAG = AppAckPayload.class.getSimpleName();

    public AppAckPayload(MESSAGE_CODE message_code){
        this.message_code = message_code;
    }

    private MESSAGE_CODE message_code;

    public byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (message_code) {
            case UNKNOWN:
                payload[0] = (byte) 0x00;
                break;
            case DEVICE_POWER_OFF:
                payload[0] = (byte) 0x01;
                break;
            default:
                payload[0] = (byte) 0x00;
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }
}
