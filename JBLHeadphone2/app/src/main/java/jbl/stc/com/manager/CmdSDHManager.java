package jbl.stc.com.manager;

import android.util.Log;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;

public class CmdSDHManager implements BaseManager {


    private static final String TAG = CmdSDHManager.class.getSimpleName();
    private LightX mLightX;

    private static class InstanceHolder {
        public static final CmdSDHManager instance = new CmdSDHManager();
    }

    public static CmdSDHManager getInstance() {
        return InstanceHolder.instance;
    }

    public void setManager(LightX lightX){
        mLightX = lightX;
    }

    @Override
    public void getANC() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppANCEnable();
    }

    @Override
    public void setANC(boolean anc) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppANCEnable(anc);
    }

    @Override
    public void getAmbientLeveling() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppANCAwarenessPreset();
    }

    @Override
    public void getANCLeft() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppAwarenessRawLeft();
    }

    @Override
    public void getANCRight() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppAwarenessRawRight();
    }

    @Override
    public void setANCLeft(int ancLeft) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppWithUInt32Argument(Command.AppAwarenessRawLeft, (long) ancLeft);
    }

    @Override
    public void setANCRight(int ancRight) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppWithUInt32Argument(Command.AppAwarenessRawRight, (long) ancRight);
    }

    public void setAmbientLeveling(ANCAwarenessPreset value) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppANCAwarenessPreset(value);
    }

    @Override
    public void getBatteryLevel() {
        if (mLightX == null) {
            Log.i(TAG, "mLightX is null, call setManager first");
            return;
        }
        mLightX.readApp(Command.AppBatteryLevel);
    }

    @Override
    public void getFirmwareVersion() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppFirmwareVersion();
    }

    @Override
    public void getAutoOff() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppOnEarDetectionWithAutoOff();
    }

    @Override
    public void setAutoOff(boolean autoOff) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppOnEarDetectionWithAutoOff(autoOff);
    }

    @Override
    public void getVoicePrompt() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppVoicePromptEnable();
    }

    @Override
    public void setVoicePrompt(boolean voicePrompt) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppVoicePromptEnable(voicePrompt);
    }

    @Override
    public void getSmartButtion() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppSmartButtonFeatureIndex();
    }

    @Override
    public void setSmartButton(boolean smartButton) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppSmartButtonFeatureIndex(smartButton);
    }

    @Override
    public void getGeqCurrentPreset() {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppGraphicEQCurrentPreset();
    }

    public void setGeqCurrentPreset(GraphicEQPreset preset) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppGraphicEQCurrentPreset(preset);
    }

    public void setGeqBandGain(GraphicEQPreset presetType, int band, int value) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeAppGraphicEQBand(presetType,band,value);
    }

    @Override
    public void getGeqBandFreq(int preset, int band) {
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.readAppGraphicEQBandFreq();
    }

    public void updateImage(LightX.FirmwareRegion region, byte[] data){
        if (mLightX == null){
            Log.i(TAG,"mLightX is null, call setManager first");
            return;
        }
        mLightX.writeFirmware(region, data);
    }

}
