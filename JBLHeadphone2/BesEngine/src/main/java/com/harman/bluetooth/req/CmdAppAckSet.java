package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumMsgCode;
import com.harman.bluetooth.constants.EnumStatusCode;
import com.harman.bluetooth.ret.RetHeader;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdAppAckSet extends BaseCmd {

    private final static String TAG = CmdAppAckSet.class.getSimpleName();

    public CmdAppAckSet(String commandId,EnumStatusCode enumStatusCode){
        this.commandId = commandId;
        this.enumStatusCode = enumStatusCode;
    }

    private String commandId;
    private EnumStatusCode enumStatusCode;

    private byte[] getPayload(){
        byte[] payload = new byte[2];
        payload[0] = getCommandId();
        switch (enumStatusCode) {
            case SUCCESS:
                payload[1] = (byte) 0x00;
                break;
            case FAILED:
                payload[1] = (byte) 0x01;
                break;
            default:
                payload[1] = (byte) 0x00;
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    private byte getCommandId(){
        byte cmd = 0;
        switch (commandId){
            case RetHeader.RET_CURRENT_EQ:
                cmd = 0x43;
                break;
        }
        return cmd;
    }

    @Override
    public byte[] getCommand() {
        combine(CmdHeader.SET_APP_ACK,getPayload());
        return super.getCommand();
    }
}
