package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.EQ_PRESET_INDEX;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class EQPresetPayload {

    private final static String TAG = EQPresetPayload.class.getSimpleName();

    public EQPresetPayload(EQ_PRESET_INDEX EQPreset_index){
        this.EQPreset_index = EQPreset_index;
    }

    private EQ_PRESET_INDEX EQPreset_index;

    public byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (EQPreset_index) {
            case OFF:
                payload[0] = (byte) 0x00;
                break;
            case JAZZ:
                payload[0] = (byte) 0x01;
                break;
            case VOCAL:
                payload[0] = (byte) 0x02;
                break;
            case BASS:
                payload[0] = (byte) 0x03;
                break;
            default:
                Logger.e(TAG,"get pay load, no such eq preset status");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }
}
