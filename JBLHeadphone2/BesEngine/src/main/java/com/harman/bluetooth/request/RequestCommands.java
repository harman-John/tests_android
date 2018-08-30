package com.harman.bluetooth.request;

import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class RequestCommands {
    private final static String TAG = RequestCommands.class.getSimpleName();

    public void setAppAck(byte[] payload) {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.SET_APP_ACK,payload);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    public void setAppBye(byte[] payload) {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.SET_APP_BYE,payload);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    public void reqDevInfo() {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.REQ_DEV_INFO);
        boolean isSend = BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
        Logger.d(TAG,"request device info, isSend = "+isSend+", command: "+ ArrayUtil.bytesToHex(RequestFormat.getCommand()));
    }

    public void reqDevStatus(byte[] type) {
        Logger.d(TAG,"request to set device status");
        RequestFormat.combine(RequestFormat.REQ_DEV_STATUS,type);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * Write ANC enable status.
     * @param ancOnOff 0x00/0x01 means OFF/ON
     */
    public void reqSetANC(byte[] ancOnOff) {
        Logger.d(TAG,"request to set ANC");
        RequestFormat.combine(RequestFormat.SET_ANC,ancOnOff);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * Write AA Mode status.
     * @param aaMode 0x00/0x01 means Talk Thru/Ambient Aware
     */
    public void reqSetAAMode(byte[] aaMode) {
        Logger.d(TAG,"request to set AA mode");
        RequestFormat.combine(RequestFormat.SET_AA_MODE,aaMode);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * Write AutoOff status.
     * @param onOff Payload length(1 bytes):
     *              1 bit(MSB): 0/1 means disable/enable
     *              7 bits(LSB): auto off time value /mins
     */
    public void reqSetAutoOff(byte[] onOff) {
        Logger.d(TAG,"request to set auto off");
        RequestFormat.combine(RequestFormat.SET_AUTO_OFF,onOff);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * To set up the EQ, use these two command EQ preset cmd(0x40) and EQ settings cmd(0x41).
     * EQ Presets has 4 types, off/jazz/vocal/bass.
     * Payload length(1 byte): 0 - off, 1 - jazz, 2 - vocal, 3 - bass
     */
    public void reqSetEQPreset(byte[] presetIndex) {
        Logger.d(TAG,"request to set EQ preset");
        RequestFormat.combine(RequestFormat.SET_EQ_PRESET,presetIndex);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * "custom" is only for using reqSetEQSettings.
     * Payload(0xnn):
     *      Preset index(1 byte) - Always value “4” for setting EQ bands
     *      EQ category(1 byte) - 0x00/ 0x01/ 0x02 Design EQ/ Graphic EQ/ Total EQ
     *      Calib(4 bytes) - Max gain calib value
     *      Sample Rate(1 byte) - Value * k (ex. Value = 48, actual rate = 48 * k)
     *      Gain0(1 byte) - Left gain value
     *      Gain1(1 byte) - Right gain value
     */
    public void reqSetEQSettings(byte[] payload) {
        Logger.d(TAG,"request to set EQ settings");
        RequestFormat.combine(RequestFormat.SET_EQ_SETTINGS,payload);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

    /**
     * App can query current EQ setting via reqCurrentEQ
     * Payload EQ category(1 bytes)
     *      Request the current EQ Type
     *      0x00/Design EQ
     *      0x01/Graphic EQ
     *      0x02/Total EQ
     */
    public void reqCurrentEQ(byte[] payLoad) {
        Logger.d(TAG,"request current EQ");
        RequestFormat.combine(RequestFormat.REQ_CURRENT_EQ,payLoad);
        BesEngine.getInstance().sendCommand(RequestFormat.getCommand());
    }

}
