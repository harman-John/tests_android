package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumAAStatus;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAASet extends BaseCmd {

    private final static String TAG = CmdAASet.class.getSimpleName();

    public CmdAASet(EnumAAStatus enumAa_status){
        this.enumAa_status = enumAa_status;
    }

    private EnumAAStatus enumAa_status;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumAa_status) {
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

    @Override
    public byte[] getCommand() {
        combine(CmdHeader.SET_AA_MODE,getPayload());
        return super.getCommand();
    }
}
