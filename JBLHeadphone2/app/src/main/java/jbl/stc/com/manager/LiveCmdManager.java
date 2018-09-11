package jbl.stc.com.manager;

import com.harman.bluetooth.engine.BesEngine;
import com.harman.bluetooth.req.CmdAASet;
import com.harman.bluetooth.req.CmdAncSet;
import com.harman.bluetooth.req.CmdAppAckSet;
import com.harman.bluetooth.req.CmdAppByeSet;
import com.harman.bluetooth.req.CmdCurrEq;
import com.harman.bluetooth.req.CmdDevStatus;
import com.harman.bluetooth.req.CmdEqPresetSet;
import com.harman.bluetooth.req.CmdEqSettingsSet;
import com.harman.bluetooth.req.CmdHeader;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;



public class LiveCmdManager {
    private final static String TAG = LiveCmdManager.class.getSimpleName();

    private static class InstanceHolder {
        public static final LiveCmdManager instance = new LiveCmdManager();
    }

    public static LiveCmdManager getInstance() {
        return LiveCmdManager.InstanceHolder.instance;
    }


    /**
     * In App, there provides AppACK to acknowledge device; it depends on the features requirement.
     * @param mac mac address
     * @param cmdAppackset {@link CmdAppAckSet}
     */
    public void setAppAck(String mac, CmdAppAckSet cmdAppackset) {
        Logger.d(TAG,"request device info");
        BesEngine.getInstance().sendCommand(mac, cmdAppackset.getCommand());
    }

    /**
     * Device and App may need to disconnect with each other. Before establish on disconnection, a “ByeBye” command
     * can be used on this purpose. Once ACK was received, then the formal disconnection will be announced.
     * @param mac
     * @param cmdappbye {@link CmdAppByeSet}
     */
    public void setAppBye(String mac, CmdAppByeSet cmdappbye) {
        Logger.d(TAG,"request device info");
        BesEngine.getInstance().sendCommand(mac, cmdappbye.getCommand());
    }

    /**
     * Two scenarios were involved here: - 1) Device Information request via command, 2)Auto feedback from device.
     * @param mac
     */
    public void reqDevInfo(String mac) {
        Logger.d(TAG,"request device info");
        CmdHeader.combine(CmdHeader.REQ_DEV_INFO);
        boolean isSend = BesEngine.getInstance().sendCommand(mac, CmdHeader.getCommand());
        Logger.d(TAG,"request device info, isSend = "+isSend+", command: "+ ArrayUtil.bytesToHex(CmdHeader.getCommand()));
    }

    /**
     * Two scenarios were involved here: - 1) Device Status request via command, 2)Auto feedback from device.
     * @param mac
     * @param cmdDevInfoReq {@link CmdDevStatus}
     */
    public void reqDevStatus(String mac,CmdDevStatus cmdDevInfoReq) {
        Logger.d(TAG,"request to set device status");
        BesEngine.getInstance().sendCommand(mac, cmdDevInfoReq.getCommand());
    }

    /**
     * Write REQ_ANC enable status.
     * @param commandAncset {@link CmdAncSet}
     */
    public void reqSetANC(String mac,CmdAncSet commandAncset) {
        Logger.d(TAG,"request to set REQ_ANC");
        BesEngine.getInstance().sendCommand(mac, commandAncset.getCommand());
    }

    /**
     * Write AA Mode status.
     * @param cmdAaSet 0x00/0x01 means Talk Thru/Ambient Aware
     */
    public void reqSetAAMode(String mac, CmdAASet cmdAaSet) {
        Logger.d(TAG,"request to set AA mode");
        BesEngine.getInstance().sendCommand(mac, cmdAaSet.getCommand());
    }

    /**
     * Write AutoOff status.
     * @param onOff Payload length(1 bytes):
     *              1 bit(MSB): 0/1 means disable/enable
     *              7 bits(LSB): auto off time value /mins
     */
    public void reqSetAutoOff(String mac,byte[] onOff) {
        Logger.d(TAG,"request to set auto off");
        CmdHeader.combine(CmdHeader.SET_AUTO_OFF,onOff);
        BesEngine.getInstance().sendCommand(mac, CmdHeader.getCommand());
    }

    /**
     * To set up the EQ, use these two command EQ preset cmd(0x40) and EQ settings cmd(0x41).
     * EQ Presets has 4 types, off/jazz/vocal/bass.
     * Payload length(1 byte): 0 - off, 1 - jazz, 2 - vocal, 3 - bass
     * @param mac
     * @param reqEqPresetSet {@link CmdEqPresetSet}
     */
    public void reqSetEQPreset(String mac, CmdEqPresetSet reqEqPresetSet) {
        Logger.d(TAG,"request to set EQ preset");
        BesEngine.getInstance().sendCommand(mac, reqEqPresetSet.getCommand());
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
     * @param reqEqSettingsSet {@link CmdEqSettingsSet}
     */
    public void reqSetEQSettings(String mac,CmdEqSettingsSet reqEqSettingsSet) {
        Logger.d(TAG,"request to set EQ settings");
        BesEngine.getInstance().sendEqSettingData(mac, reqEqSettingsSet);
    }

    /**
     * App can query current EQ setting via reqCurrentEQ
     * Payload EQ category(1 bytes)
     *      Request the current EQ Type
     *      0x00/Design EQ
     *      0x01/Graphic EQ
     *      0x02/Total EQ
     * @param mac
     * @param reqCurrEq {@link CmdCurrEq}
     */
    public void reqCurrentEQ(String mac,CmdCurrEq reqCurrEq) {
        Logger.d(TAG,"request current EQ");
        BesEngine.getInstance().sendCommand(mac, reqCurrEq.getCommand());
    }

}
