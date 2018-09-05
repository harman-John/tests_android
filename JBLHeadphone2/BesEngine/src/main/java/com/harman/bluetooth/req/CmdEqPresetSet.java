package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumEqPresetIdx;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdEqPresetSet extends BaseCmd {

    private final static String TAG = CmdEqPresetSet.class.getSimpleName();

    public CmdEqPresetSet(EnumEqPresetIdx EQPreset_index){
        this.EQPreset_index = EQPreset_index;
    }

    private EnumEqPresetIdx EQPreset_index;

    private byte[] getPayload(){
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

    @Override
    public byte[] getCommand() {
        combine(ReqHeader.SET_EQ_PRESET,getPayload());
        return super.getCommand();
    }
}