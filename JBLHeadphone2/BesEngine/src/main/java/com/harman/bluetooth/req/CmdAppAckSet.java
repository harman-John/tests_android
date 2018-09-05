package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumMsgCode;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAppAckSet extends BaseCmd {

    private final static String TAG = CmdAppAckSet.class.getSimpleName();

    public CmdAppAckSet(EnumMsgCode enumMsgCode){
        this.enumMsgCode = enumMsgCode;
    }

    private EnumMsgCode enumMsgCode;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumMsgCode) {
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

    @Override
    public byte[] getCommand() {
        combine(Header.SET_APP_ACK,getPayload());
        return super.getCommand();
    }
}
