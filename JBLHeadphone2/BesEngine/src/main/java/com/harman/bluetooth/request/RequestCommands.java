package com.harman.bluetooth.request;

import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class RequestCommands {
    private final static String TAG = RequestCommands.class.getSimpleName();

    /**
     * In App, there provides AppACK to acknowledge device; it depends on the features requirement.
     * @param mac
     * @param appAckPayload {@link AppAckPayload}
     */
    public void setAppAck(String mac, AppAckPayload appAckPayload) {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.SET_APP_ACK,appAckPayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * Device and App may need to disconnect with each other. Before establish on disconnection, a “ByeBye” command
     * can be used on this purpose. Once ACK was received, then the formal disconnection will be announced.
     * @param mac
     * @param appByePayload {@link AppByePayload}
     */
    public void setAppBye(String mac, AppByePayload appByePayload) {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.SET_APP_BYE, appByePayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * Two scenarios were involved here: - 1) Device Information request via command, 2)Auto feedback from device.
     * @param mac
     */
    public void reqDevInfo(String mac) {
        Logger.d(TAG,"request device info");
        RequestFormat.combine(RequestFormat.REQ_DEV_INFO);
        boolean isSend = BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
        Logger.d(TAG,"request device info, isSend = "+isSend+", command: "+ ArrayUtil.bytesToHex(RequestFormat.getCommand()));
    }

    /**
     * Two scenarios were involved here: - 1) Device Status request via command, 2)Auto feedback from device.
     * @param mac
     * @param statusTypePayload {@link StatusTypePayload}
     */
    public void reqDevStatus(String mac,StatusTypePayload statusTypePayload) {
        Logger.d(TAG,"request to set device status");
        RequestFormat.combine(RequestFormat.REQ_DEV_STATUS,statusTypePayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * Write ANC enable status.
     * @param ancPayload {@link ANCPayload}
     */
    public void reqSetANC(String mac,ANCPayload ancPayload) {
        Logger.d(TAG,"request to set ANC");
        RequestFormat.combine(RequestFormat.SET_ANC, ancPayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * Write AA Mode status.
     * @param aaModePayload 0x00/0x01 means Talk Thru/Ambient Aware
     */
    public void reqSetAAMode(String mac, AAModePayload aaModePayload) {
        Logger.d(TAG,"request to set AA mode");
        RequestFormat.combine(RequestFormat.SET_AA_MODE, aaModePayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * Write AutoOff status.
     * @param onOff Payload length(1 bytes):
     *              1 bit(MSB): 0/1 means disable/enable
     *              7 bits(LSB): auto off time value /mins
     */
    public void reqSetAutoOff(String mac,byte[] onOff) {
        Logger.d(TAG,"request to set auto off");
        RequestFormat.combine(RequestFormat.SET_AUTO_OFF,onOff);
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * To set up the EQ, use these two command EQ preset cmd(0x40) and EQ settings cmd(0x41).
     * EQ Presets has 4 types, off/jazz/vocal/bass.
     * Payload length(1 byte): 0 - off, 1 - jazz, 2 - vocal, 3 - bass
     * @param mac
     * @param EQPresetPayload {@link EQPresetPayload}
     */
    public void reqSetEQPreset(String mac, EQPresetPayload EQPresetPayload) {
        Logger.d(TAG,"request to set EQ preset");
        RequestFormat.combine(RequestFormat.SET_EQ_PRESET, EQPresetPayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
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
     * @param mac
     * @param eqPayload {@link EQPayload}
     */
    public void reqSetEQSettings(String mac,EQPayload eqPayload) {
        Logger.d(TAG,"request to set EQ settings");
        RequestFormat.combine(RequestFormat.SET_EQ_SETTINGS,eqPayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

    /**
     * App can query current EQ setting via reqCurrentEQ
     * Payload EQ category(1 bytes)
     *      Request the current EQ Type
     *      0x00/Design EQ
     *      0x01/Graphic EQ
     *      0x02/Total EQ
     * @param mac
     * @param eqCategoryPayload {@link EQCategoryPayload}
     */
    public void reqCurrentEQ(String mac,EQCategoryPayload eqCategoryPayload) {
        Logger.d(TAG,"request current EQ");
        RequestFormat.combine(RequestFormat.REQ_CURRENT_EQ,eqCategoryPayload.getPayload());
        BesEngine.getInstance().sendCommand(mac,RequestFormat.getCommand());
    }

}
