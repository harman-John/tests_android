package jbl.stc.com.manager;

import android.util.Log;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.ImageType;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.audioManager;

import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.utils.AmToolUtil;

public class Cmd150Manager implements BaseManager {


    private static final String TAG = Cmd150Manager.class.getSimpleName();
    private audioManager mAudioManager;

    private static class InstanceHolder {
        public static final Cmd150Manager instance = new Cmd150Manager();
    }

    public static Cmd150Manager getInstance() {
        return InstanceHolder.instance;
    }

    public void setManager(audioManager aManager){
        mAudioManager = aManager;
    }

    @Override
    public void getANC() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_ANC);
    }

    @Override
    public void setANC(boolean anc) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_ANC,anc);
    }

    @Override
    public void getAmbientLeveling() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_AmbientLeveling);
    }

    @Override
    public void getANCLeft() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_RawLeft);
    }

    @Override
    public void getANCRight() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_RawRight);
    }

    @Override
    public void setANCLeft(int ancLeft) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_RawLeft,ancLeft);
    }

    @Override
    public void setANCRight(int ancRight) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_RawRight,ancRight);
    }

    public void setAmbientLeveling(int ambientLeveling) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_AmbientLeveling,ambientLeveling);
    }

    public void getRawSteps() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_RawSteps);
    }

    @Override
    public void getBatteryLevel() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_BatteryLevel);
    }

    @Override
    public void getFirmwareVersion() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_FirmwareVersion);
    }

    @Override
    public void getAutoOff() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_AutoOffEnable);
    }

    @Override
    public void setAutoOff(boolean autoOff) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_AutoOffEnable,autoOff);
    }

    @Override
    public void getVoicePrompt() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_VoicePrompt);
    }

    @Override
    public void setVoicePrompt(boolean voicePrompt) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_VoicePrompt,voicePrompt);
    }

    @Override
    public void getSmartButtion() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_SmartButton);
    }

    @Override
    public void setSmartButton(boolean smartButton) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_SmartButton,smartButton);
    }

    @Override
    public void getGeqCurrentPreset() {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_Geq_Current_Preset);
    }

    public void setGeqCurrentPreset(int preset) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_Geq_Current_Preset,preset);
    }

    public void setGeqBandGain(int preset, int band, int value) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_GrEqBandGains,band,value);
    }

    @Override
    public void getGeqBandFreq(int preset, int band) {
        if (mAudioManager == null){
            Log.i(TAG,"mAudioManager is null, call setManager first");
            return;
        }
        mAudioManager.sendCommand(Action.Get, AmCmds.CMD_GraphicEqBandFreq);
    }

    /**
     * Sending command to get firmware info from accessory.
     * The result is callback "receivedResponse".
     * Get "current firmware" from the parameter of callback.
     */
    public void getFWInfo(){
        mAudioManager.getFWInfo();
    }

    /**
     * Use this command to update image.This is for OTA.
     * Steps:
     * 1. update "parameter" partition.
     * 2. update "data" partition.
     * 3. update "firmware" partition.
     * There are two parts of flash which every part can be upgraded, run independently.
     * They can be only one part of operation at the same time.
     * The "current firmware is 0" or "app set 0" means accessory running in first part.
     * The same as "current firmware is 1" or "app set 1" means accessory running in second part.
     * When sending "parameter" over, there will be a callback "receivedStatus" from sdk.
     * According to the the parameter of callback, app will know the status of OTA.
     * Such as "ImageUpdateFinalize", "ImageUpdateComplete", app would better to send next partition data in "ImageUpdateFinalize".
     * In "UpdateProgress", app can update progress bar.
     * When three partitions is done, app receive "ImageUpdateFinalize" too and need call "startFirmware" to switch current firmware.
     *
     * command:
     * This is fixed parameter using: "UpdateImage"
     *
     * address of first part.
     * parameter: 0x00038000
     * data:      0x00048000
     * firmware:  0x0010C000
     *
     * Or address of second part.
     * parameter: 0x0021C000
     * data:      0x0022C000
     * firmware:  0x002F0000
     *
     * image: Either downloaded form server or local. No matter from where, the file must be transfer to ByteArray.
     *
     * type:
     * ImageType.Parameters
     * ImageType.Data
     * ImageType.Firmware
     *
     * version: Firmware version.
     *
     * set:
     * If current firmware is 0 then the value of parameter set is 0.
     * The same if current firmware is 1 then the value of parameter set is 1.
     */
    public void updateImage(byte[] image, String version, ImageType type, byte set){
        Status status = Status.Failed;
        if (type == ImageType.Parameters){
            status = mAudioManager.sendCommand(AmCmds.CMD_UPDATE_IMAGE,
                    set == 0 ? AmCmds.address1_Parameter: AmCmds.address0_Parameter,
                    image, Integer.valueOf(AmToolUtil.INSTANCE.transferCurrentVersion(version),16),
                    type, set);
        }else if (type == ImageType.Data){
            status = mAudioManager.sendCommand(AmCmds.CMD_UPDATE_IMAGE,
                    set == 0 ? AmCmds.address1_Data: AmCmds.address0_Data,
                    image, Integer.valueOf(AmToolUtil.INSTANCE.transferCurrentVersion(version),16),
                    type, set);
        }else if (type == ImageType.Firmware){
            status = mAudioManager.sendCommand(AmCmds.CMD_UPDATE_IMAGE,
                    set == 0 ? AmCmds.address1_Firmware: AmCmds.address0_Firmware,
                    image, Integer.valueOf(AmToolUtil.INSTANCE.transferCurrentVersion(version),16),
                    type, set);
        }
        Log.i(TAG,"status = " + status);
    }

    /**
     * When OTA is upgraded, call this function to switch parameter.
     * set:
     * Current firmware is 0, then the value of set is 1.
     * The same current firmware is 1, then the value of set is 0.
     */
    public void startFirmware(int set){
        mAudioManager.startFirmware(set);
    }

    /**
     * Note that this should be called after updating any image.
     * version:
     * new firmware version.
     */
    public void  setFirmwareVersion( int version ){
        mAudioManager.setBundle(version);
    }

    /**
     * Disable accessory interrupts
     * start Image Update
     * After update is completed, enable the accessory interrupts again.
     * state: To disable accessory interrupts, call the command with parameter: 1
     *        To enable accessory interrupts, call the command with parameter: 2
     * In the case where image update gets interrupted, the Android app needs to
     * handle the error conditions and enable the accessory functionality properly.
     */
    public void setFirmwareUpdateState(int state){
        mAudioManager.sendCommand(Action.Set, AmCmds.CMD_FIRMWARE_UPDATE_STATE, state);
    }
}
