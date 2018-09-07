package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumAncStatus;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAncSet extends BaseCmd {

    private final static String TAG = CmdAncSet.class.getSimpleName();

    public CmdAncSet(EnumAncStatus enumAncStatus){
        this.enumAncStatus = enumAncStatus;
    }

    private EnumAncStatus enumAncStatus;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumAncStatus) {
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

    @Override
    public byte[] getCommand() {
        combine(CmdHeader.SET_ANC,getPayload());
        return super.getCommand();
    }
}
