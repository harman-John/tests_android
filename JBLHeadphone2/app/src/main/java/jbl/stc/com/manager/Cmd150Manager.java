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

    private static class InstanceHolder {
        public static final Cmd150Manager instance = new Cmd150Manager();
    }

    public static Cmd150Manager getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public void getANC(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_ANC);
    }

    @Override
    public void setANC(Object object, boolean anc) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_ANC, anc);
    }

    @Override
    public void getAmbientLeveling(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_AmbientLeveling);
    }

    @Override
    public void getANCLeft(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_RawLeft);
    }

    @Override
    public void getANCRight(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_RawRight);
    }

    @Override
    public void setANCLeft(Object object, int ancLeft) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_RawLeft, ancLeft);
    }

    @Override
    public void setANCRight(Object object, int ancRight) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_RawRight, ancRight);
    }

    public void setAmbientLeveling(Object object, int ambientLeveling) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_AmbientLeveling, ambientLeveling);
    }

    public void getRawSteps(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_RawSteps);
    }

    @Override
    public void getBatteryLevel(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_BatteryLevel);
    }

    @Override
    public void getFirmwareVersion(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_FirmwareVersion);
    }

    @Override
    public void getAutoOff(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_AutoOffEnable);
    }

    @Override
    public void setAutoOff(Object object, boolean autoOff) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_AutoOffEnable, autoOff);
    }

    @Override
    public void getVoicePrompt(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_VoicePrompt);
    }

    @Override
    public void setVoicePrompt(Object object, boolean voicePrompt) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_VoicePrompt, voicePrompt);
    }

    @Override
    public void getSmartButtion(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_SmartButton);
    }

    @Override
    public void setSmartButton(Object object, boolean smartButton) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_SmartButton, (!smartButton)? 0 : 1);
    }

    @Override
    public void getGeqCurrentPreset(Object object) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_Geq_Current_Preset);
    }

    public void setGeqCurrentPreset(Object object, int preset) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        Log.d(TAG, "set command is:" + AmCmds.CMD_Geq_Current_Preset + "params are:" + "preset:" + preset);
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_Geq_Current_Preset, preset);
    }

    public void setGeqBandGain(Object object, int preset, int band, int value) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_GrEqBandGains, band, value);
    }


    @Override
    public void getGeqBandFreq(Object object, int preset, int band) {
        if (object == null) {
            Log.i(TAG, "object is null, call setManager first");
            return;
        }
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_GraphicEqBandFreq);
    }

    /**
     * Sending command to get firmware info from accessory.
     * The result is callback "receivedResponse".
     * Get "current firmware" from the parameter of callback.
     */
    public void getFWInfo(Object object){
        if (object == null){
            Log.i(TAG,"object is null, call setManager first");
            return;
        }
        ((audioManager)object).getFWInfo();
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
     * <p>
     * command:
     * This is fixed parameter using: "UpdateImage"
     * <p>
     * address of first part.
     * parameter: 0x00038000
     * data:      0x00048000
     * firmware:  0x0010C000
     * <p>
     * Or address of second part.
     * parameter: 0x0021C000
     * data:      0x0022C000
     * firmware:  0x002F0000
     * <p>
     * image: Either downloaded form server or local. No matter from where, the file must be transfer to ByteArray.
     * <p>
     * type:
     * ImageType.Parameters
     * ImageType.Data
     * ImageType.Firmware
     * <p>
     * version: Firmware version.
     * <p>
     * set:
     * If current firmware is 0 then the value of parameter set is 0.
     * The same if current firmware is 1 then the value of parameter set is 1.
     */
    public void updateImage(Object object,byte[] image, String version, ImageType type, byte set){
        if (object == null){
            Log.i(TAG,"is not 150NC device");
            return;
        }
        int ver = Integer.valueOf(version,16);
        Status status;
        int address = 0;
        if (type == ImageType.Parameters){
            address = (set == 0) ? AmCmds.address1_Parameter: AmCmds.address0_Parameter;
        }else if (type == ImageType.Data){
            address = (set == 0) ? AmCmds.address1_Data: AmCmds.address0_Data;
        }else if (type == ImageType.Firmware){
            address = (set == 0) ? AmCmds.address1_Firmware: AmCmds.address0_Firmware;
        }
        Log.i(TAG,"updateImage current is "+set+",version is "
                + ver +", type is "+type+",image length is "+ image.length +",address is "+address);
        status = ((audioManager)object).sendCommand(AmCmds.CMD_UPDATE_IMAGE,
                address,image, ver,type, set);
        Log.i(TAG,"status = " + status);
    }

    /**
     * When OTA is upgraded, call this function to switch parameter.
     * set:
     * Current firmware is 0, then the value of set is 1.
     * The same current firmware is 1, then the value of set is 0.
     */
    public void startFirmware(Object object, int set) {
        ((audioManager) object).startFirmware(set);
    }

    /**
     * Note that this should be called after updating any image.
     * version:
     * new firmware version.
     */
    public void  setFirmwareVersion(Object object, int version ){
        if (object == null){
            Log.i(TAG,"is not 150NC device");
            return;
        }
        ((audioManager)object).setBundle(version);
    }

    /**
     * Disable accessory interrupts
     * start Image Update
     * After update is completed, enable the accessory interrupts again.
     * state: To disable accessory interrupts, call the command with parameter: 1
     * To enable accessory interrupts, call the command with parameter: 2
     * In the case where image update gets interrupted, the Android app needs to
     * handle the error conditions and enable the accessory functionality properly.
     */
    public void setFirmwareUpdateState(Object object,int state){
        if (object == null){
            Log.i(TAG,"is not 150NC device");
            return;
        }
        ((audioManager)object).sendCommand(Action.Set, AmCmds.CMD_FIRMWARE_UPDATE_STATE, state);
    }


    public void sendSetEqBandGains(Object object, int preset, int index_of_band, int value) {
        if (object == null){
            Log.i(TAG,"is not 150NC device");
            return;
        }
        Log.d(TAG, "set command is:" + AmCmds.CMD_GrEqBandGains + "params are:" + "preset:" + preset + "index of bands:" + index_of_band + "value:" + value);
        ((audioManager) object).sendCommand(Action.Set, AmCmds.CMD_GrEqBandGains, preset, index_of_band, value);
    }

    public void getEqBandGains(Object object, int preset, int band) {
        if (object == null) {
            Log.i(TAG, "is not 150NC device");
            return;
        }
        Log.d(TAG, "get command is:" + AmCmds.CMD_GrEqBandGains + "params are:" + "preset:" + preset + "index of bands:" + band);
        ((audioManager) object).sendCommand(Action.Get, AmCmds.CMD_GrEqBandGains, preset, band);

    }


}
