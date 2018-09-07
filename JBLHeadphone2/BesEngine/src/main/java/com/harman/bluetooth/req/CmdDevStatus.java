package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.EnumDeviceStatusType;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdDevStatus extends BaseCmd {

    private final static String TAG = CmdDevStatus.class.getSimpleName();

    public CmdDevStatus(EnumDeviceStatusType enumDeviceStatusType){
        this.enumDeviceStatusType = enumDeviceStatusType;
    }

    private EnumDeviceStatusType enumDeviceStatusType;

    private byte[] getPayload(){
        byte[] payload = new byte[1];
        switch (enumDeviceStatusType) {
            case ALL_STATUS:
                payload[0] = (byte) 0x30;
                break;
            case ANC:
                payload[0] = (byte) 0x31;
                break;
            case AMBIENT_AWARE_MODE:
                payload[0] = (byte) 0x32;
                break;
            case AUTO_OFF:
                payload[0] = (byte) 0x33;
                break;
            case EQ_PRESET:
                payload[0] = (byte) 0x34;
                break;
            default:
                Logger.e(TAG,"get pay load, no this type");
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        combine(CmdHeader.REQ_DEV_STATUS,getPayload());
        return super.getCommand();
    }
}


