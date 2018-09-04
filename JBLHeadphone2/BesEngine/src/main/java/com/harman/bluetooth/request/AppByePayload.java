package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.COMMAND_ID;
import com.harman.bluetooth.constants.STATUS_CODE;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class AppByePayload {

    private final static String TAG = AppByePayload.class.getSimpleName();

    public AppByePayload(COMMAND_ID requestCmdId, STATUS_CODE statusCode){
        this.requestCmdId = requestCmdId;
        this.statusCode = statusCode;
    }

    private COMMAND_ID requestCmdId;
    private STATUS_CODE statusCode;

    public byte[] getPayload(){
        byte[] payload = new byte[2];

        switch (requestCmdId){
            case SET_APP_ACK:
                payload[0] = (byte) 0x01;
                break;
            case SET_APP_BYE:
                payload[0] = (byte) 0x03;
                break;
            case SET_APP_FIN_ACK:
                payload[0] = (byte) 0x05;
                break;
            case REQ_DEV_INFO:
                payload[0] = (byte) 0x11;
                break;
            case REQ_DEV_STATUS:
                payload[0] = (byte) 0x21;
                break;
            case SET_ANC:
                payload[0] = (byte) 0x31;
                break;
            case SET_AA_MODE:
                payload[0] = (byte) 0x32;
                break;
            case SET_AUTO_OFF:
                payload[0] = (byte) 0x33;
                break;
            case SET_EQ_PRESET:
                payload[0] = (byte) 0x40;
                break;
            case SET_EQ_SETTINGS:
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
}
