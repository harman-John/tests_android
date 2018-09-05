package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumCmdId;
import com.harman.bluetooth.constants.EnumStatusCode;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAppByeSet extends BaseCmd {

    private final static String TAG = CmdAppByeSet.class.getSimpleName();

    public CmdAppByeSet(EnumCmdId requestCmdId, EnumStatusCode statusCode){
        this.requestCmdId = requestCmdId;
        this.statusCode = statusCode;
    }

    private EnumCmdId requestCmdId;
    private EnumStatusCode statusCode;

    private byte[] getPayload(){
        byte[] payload = new byte[2];

        switch (requestCmdId){
            case REQ_APP_ACK:
                payload[0] = (byte) 0x01;
                break;
            case REQ_APP_BYE:
                payload[0] = (byte) 0x03;
                break;
            case REQ_APP_FIN_ACK:
                payload[0] = (byte) 0x05;
                break;
            case REQ_DEV_INFO:
                payload[0] = (byte) 0x11;
                break;
            case REQ_DEV_STATUS:
                payload[0] = (byte) 0x21;
                break;
            case REQ_ANC:
                payload[0] = (byte) 0x31;
                break;
            case REQ_AA_MODE:
                payload[0] = (byte) 0x32;
                break;
            case REQ_AUTO_OFF:
                payload[0] = (byte) 0x33;
                break;
            case REQ_EQ_PRESET:
                payload[0] = (byte) 0x40;
                break;
            case REQ_EQ_SETTINGS:
                payload[0] = (byte) 0x41;
                break;
            case REQ_CURRENT_EQ:
                payload[0] = (byte) 0x42;
                break;
        }
        switch (statusCode) {
            case SUCCESS:
                payload[1] = (byte) 0x00;
                break;
            case FAILED:
                payload[1] = (byte) 0x01;
                break;
            default:
                payload[1] = (byte) 0x01;
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        combine(ReqHeader.SET_APP_BYE,getPayload());
        return super.getCommand();
    }
}
