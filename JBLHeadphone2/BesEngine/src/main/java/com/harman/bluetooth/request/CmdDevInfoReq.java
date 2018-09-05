package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.EnumDeviceStatusType;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdDevInfoReq extends BaseCmd {

    private final static String TAG = CmdDevInfoReq.class.getSimpleName();

    public CmdDevInfoReq(EnumDeviceStatusType enumDeviceStatusType){
        this.enumDeviceStatusType = enumDeviceStatusType;
    }

    private EnumDeviceStatusType enumDeviceStatusType;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumDeviceStatusType) {
            case ALL_STATUS:
                payload[0] = (byte) 0x00;
                break;
            case ANC:
                payload[0] = (byte) 0x01;
                break;
            case AMBIENT_AWARE_MODE:
                payload[0] = (byte) 0x02;
                break;
            case AUTO_OFF:
                payload[0] = (byte) 0x03;
                break;
            case EQ_PRESET:
                payload[0] = (byte) 0x04;
                break;
            default:
                Logger.e(TAG,"get pay load, no this type");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        combine(Header.REQ_DEV_STATUS,getPayload());
        return super.getCommand();
    }
}


