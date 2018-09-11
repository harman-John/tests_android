package com.harman.bluetooth.req;

import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class CmdEqSettingsSet extends BaseCmd {

    private final static String TAG = CmdEqSettingsSet.class.getSimpleName();

    public CmdEqSettingsSet(int presetIndex,
                            EnumEqCategory eqCATEGORY,
                            float calib,
                            int sampleRate,
                            float gain0,
                            float gain1,
                            Band[] band) {
        this.presetIndex = presetIndex;
        this.eqCATEGORY = eqCATEGORY;
        this.calib = calib;
        this.sampleRate = sampleRate;
        this.gain0 = gain0;
        this.gain1 = gain1;
        this.band = band;
        combine(CmdHeader.SET_EQ_SETTINGS, getPayload());
    }

    private int presetIndex; //always 4

    private EnumEqCategory eqCATEGORY;

    private float calib;

    private int sampleRate;

    private float gain0;

    private float gain1;

    private Band[] band;

    public void setCalib(float calib) {
        this.calib = calib;
    }

    public int getPackageIndex() {
        return packageIndex;
    }

    public int getPresetIndex() {
        return presetIndex;
    }

    public EnumEqCategory getEqCATEGORY() {
        return eqCATEGORY;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public float getGain0() {
        return gain0;
    }

    public float getGain1() {
        return gain1;
    }

    public Band[] getBand() {
        return band;
    }

    private byte[] getPayload() {
        byte[] payload = new byte[19 + 16 * band.length];
        payload[0] = (byte) presetIndex;
        switch (eqCATEGORY) {
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
        byte[] cali = ArrayUtil.float2bytes(calib);
        System.arraycopy(cali, 0, payload, 2, 4);
        payload[6] = (byte) sampleRate;
        byte[] gain0 = ArrayUtil.float2bytes(this.gain0);
        System.arraycopy(gain0, 0, payload, 7, 4);

        byte[] gain1 = ArrayUtil.float2bytes(this.gain1);
        System.arraycopy(gain1, 0, payload, 11, 4);

        byte[] bandCount = ArrayUtil.intToByteArray(this.band.length);
        System.arraycopy(bandCount, 0, payload, 12, 4);

        for (int i = 0; i < band.length; i++) {
            int pos = 16 + 16 * i;
            payload[pos] = (byte) band[i].type;
            payload[pos + 4] = (byte) band[i].gain;
            payload[pos + 8] = (byte) band[i].fc;
            payload[pos + 12] = (byte) band[i].q;
        }
        Logger.d(TAG, "get payload: " + ArrayUtil.bytesToHex(payload));
        return payload;
    }

    @Override
    public byte[] getCommand() {
        return super.getCommand();
    }
}
