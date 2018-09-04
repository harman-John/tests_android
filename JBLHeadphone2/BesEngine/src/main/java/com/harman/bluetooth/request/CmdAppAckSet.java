package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.MESSAGE_CODE;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAppAckSet extends BaseCmd {

    private final static String TAG = CmdAppAckSet.class.getSimpleName();

    public CmdAppAckSet(MESSAGE_CODE message_code){
        this.message_code = message_code;
    }

    private MESSAGE_CODE message_code;

    private byte[] getPayload(){
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

    @Override
    public byte[] getCommand() {
        combine(Header.SET_APP_ACK,getPayload());
        return super.getCommand();
    }
}
