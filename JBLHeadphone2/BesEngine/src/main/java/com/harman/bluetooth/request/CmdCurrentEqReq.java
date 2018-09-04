package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.EQ_CATEGORY;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdCurrentEqReq extends BaseCmd {

    private final static String TAG = CmdCurrentEqReq.class.getSimpleName();

    public CmdCurrentEqReq(EQ_CATEGORY eq_category){
        this.eq_category = eq_category;
    }

    private EQ_CATEGORY eq_category;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (eq_category) {
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
        combine(Header.REQ_CURRENT_EQ,getPayload());
        return super.getCommand();
    }
}
