package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdCurrEq extends BaseCmd {

    private final static String TAG = CmdCurrEq.class.getSimpleName();

    public CmdCurrEq(EnumEqCategory enumEqCategory){
        this.enumEqCategory = enumEqCategory;
    }

    private EnumEqCategory enumEqCategory;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumEqCategory) {
            case DESIGN_EQ:
                payload[0] = (byte) 0x00;
                break;
            case GRAPHIC_EQ:
                payload[0] = (byte) 0x01;
                break;
            case TOTAL_EQ:
                payload[0] = (byte) 0x02;
                break;
            default:
                Logger.e(TAG,"get pay load, no such eq category status");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        combine(CmdHeader.REQ_CURRENT_EQ,getPayload());
        return super.getCommand();
    }
}
