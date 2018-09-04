package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.DEVICE_STATUS_TYPE;
import com.harman.bluetooth.constants.MESSAGE_CODE;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class StatusTypePayload {

    private final static String TAG = StatusTypePayload.class.getSimpleName();

    public StatusTypePayload(DEVICE_STATUS_TYPE device_status_type){
        this.device_status_type = device_status_type;
    }

    private DEVICE_STATUS_TYPE device_status_type;

    public byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (device_status_type) {
            case ALL_STATUS:
                payload[0] = (byte) 0x00;
                break;
            case ANC:
                payload[0] = (byte) 0x01;
                break;
            case AMBIENT_AWARE_MODE:
                payload[0] = (byte) 0x02;
                break;
            case AUTO_OFF:
                payload[0] = (byte) 0x03;
                break;
            case EQ_PRESET:
                payload[0] = (byte) 0x04;
                break;
            default:
                Logger.e(TAG,"get pay load, no this type");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }
}


