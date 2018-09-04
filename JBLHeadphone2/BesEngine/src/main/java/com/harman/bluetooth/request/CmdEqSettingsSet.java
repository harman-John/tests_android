package com.harman.bluetooth.request;

import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EQ_CATEGORY;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdEqSettingsSet extends BaseCmd {

    private final static String TAG = CmdEqSettingsSet.class.getSimpleName();

    public CmdEqSettingsSet(int presetIndex, EQ_CATEGORY eqCATEGORY, float calib, int sampleRate, float gain0, float gain1, Band[] band){
        this.presetIndex = presetIndex;
        this.eqCATEGORY = eqCATEGORY;
        this.calib = calib;
        this.sampleRate = sampleRate;
        this.gain0 = gain0;
        this.gain1 = gain1;
        this.band = band;
    }

    private int presetIndex; //always 4

    private EQ_CATEGORY eqCATEGORY;

    private float calib;

    private int sampleRate;

    private float gain0;

    private float gain1;

    private Band[] band;

    private byte[] getPayload(){
        byte[] payload = new byte[10 +9*band.length];
        payload[0] = (byte) presetIndex;
        switch (eqCATEGORY){
            case DESIGN_EQ:
                payload[1] = (byte) 0x00;
                break;
            case GRAPHIC_EQ:
                payload[1] = (byte) 0x01;
                break;
            case TOTAL_EQ:
                payload[1] = (byte) 0x02;
                break;
        }
        byte[] cali = float2bytes(calib);
        System.arraycopy(cali,0,payload,2,4);
        payload[6] = (byte) sampleRate;
        payload[7] = (byte) gain0;
        payload[8] = (byte) gain1;
        payload[9] = (byte) band.length;

        for (int i=0;i < band.length; i++) {
            int pos = 10 + 3* i;
            payload[pos] = (byte) band[i].type;
            payload[pos +1] = (byte) band[i].gain;
            payload[pos +2] = (byte) band[i].q;
        }
        Logger.d(TAG,"get payload: "+ ArrayUtil.bytesToHex(payload));
        return payload;
    }

    private static byte[] float2bytes(float f) {
        int fbit = Float.floatToIntBits(f);
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }
        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;

    }

    @Override
    public byte[] getCommand() {
        combine(Header.SET_EQ_SETTINGS,getPayload());
        return super.getCommand();
    }
}
