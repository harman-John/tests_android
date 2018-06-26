package jbl.stc.com.manager;

import android.content.Context;
import android.util.Log;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import jbl.stc.com.storage.PreferenceUtils;

/**
 * ANCControlManager
 * Created by intahmad on 7/24/2015.
 */
public class ANCControlManager {
    private String TAG = ANCControlManager.class.getSimpleName();
    private static String LEFTANC = "LEFTANC";
    private static String RIGHTANC = "RIGHTANC";
    private static String ANCVALUE = "ANCVALUE";

    private static ANCControlManager ancControlManager;
    private final Context context;

    private ANCControlManager(Context context) {
        this.context = context;
    }

    public static ANCControlManager getANCManager(Context context) {
        if (ancControlManager == null)
            ancControlManager = new ANCControlManager(context);
        return ancControlManager;
    }

    /**
     * Get ANC value from headphone.
     *
     * @param lightX object
     */
    public void getANCValue(LightX lightX) {
        if (lightX != null) {
            lightX.readAppANCEnable();
        } else {
            Cmd150Manager.getInstance().getANC(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getANCValue ");
        }
    }

    /**
     * Set ANC value
     *
     * @param ancValue boolean anc value, true for ANC ON and false for ANC Off.
     * @param lightX   object.
     */
    public void setANCValue(boolean ancValue, LightX lightX) {
        if (lightX != null) {
            lightX.writeAppANCEnable(ancValue);
        } else {
            Cmd150Manager.getInstance().setANC(AvneraManager.getAvenraManager(context).getAudioManager(), ancValue);
            Log.d(TAG, "SendCommand setANCValue");
        }
    }

    /**
     * Set Left Awareness seek bar value.
     *
     * @param lightX       Object
     * @param leftANCvalue int value to set seek bar.
     */
    public void setLeftAwarenessPresetValue(LightX lightX, int leftANCvalue) {
        int rawSteps = ancValueConverter(lightX,leftANCvalue);
        if (lightX != null) {
            lightX.writeAppWithUInt32Argument(Command.AppAwarenessRawLeft, (long) rawSteps);
            PreferenceUtils.setInt(LEFTANC, leftANCvalue, context);
        } else {
            Cmd150Manager.getInstance().setANCLeft(AvneraManager.getAvenraManager(context).getAudioManager(),rawSteps);
            Log.d(TAG, "SendCommand setLeftAwarenessPresetValue");
        }
    }

    /**
     * Set right Awareness seek bar value.
     *
     * @param lightX        Object
     * @param rightANCvalue int value to set seek bar
     */
    public void setRightAwarenessPresetValue(LightX lightX, int rightANCvalue) {
        int rawSteps = ancValueConverter(lightX,rightANCvalue);
        if (lightX != null) {
            lightX.writeAppWithUInt32Argument(Command.AppAwarenessRawRight, (long) rawSteps);
            PreferenceUtils.setInt(RIGHTANC, rightANCvalue, context);
        } else {
            Cmd150Manager.getInstance().setANCRight(AvneraManager.getAvenraManager(context).getAudioManager(),rawSteps);
            Log.d(TAG, "SendCommand setRightAwarenessPresetValue");
        }

    }

    /**
     * Convert ANC (0-100 ) range to ANC steps (0-9).
     *
     * @param ancValue int ANC awareness seek bar value.
     * @return ANC steps
     */
    private int ancValueConverter(LightX lightX, int ancValue) {
        if (lightX != null) {
            return (ancValue * 7) / 100;
        }else{
            return (ancValue * mRawsteps) /100;
        }
    }

    /**
     * Get left ANC value from headphone.
     *
     * @param lightX Object
     */
    public void getLeftANCvalue(LightX lightX) {
        if (lightX != null) {
            lightX.readAppAwarenessRawLeft();
        }else{
            Cmd150Manager.getInstance().getANCLeft(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getLeftANCvalue");
        }
    }

    /**
     * Get right ANC value from headphone.
     *
     * @param lightX Object
     */
    public void getRightANCvalue(LightX lightX) {
        if (lightX != null) {
            lightX.readAppAwarenessRawRight();
        }else{
            Cmd150Manager.getInstance().getANCRight(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getRightANCvalue");
        }
    }

    public void getRawStepsByCmd(LightX lightX){
        if (lightX != null){
            Log.e(TAG, "getRawStepsByCmd ");
        }else{
            Cmd150Manager.getInstance().getRawSteps(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getRawStepsByCmd");
        }
    }

    private int mRawsteps = 7;
    public void setRawSteps(int rawSteps){
        mRawsteps = rawSteps;
    }

    /**
     * Get ambient leveling
     */
    public void getAmbientLeveling(LightX lightX) {
        if (lightX != null) {
            lightX.readAppANCAwarenessPreset();
        }else {
            Cmd150Manager.getInstance().getAmbientLeveling(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getAmbientLeveling");
        }
    }


    /**
     * Set ambient leveling
     */
    public void setAmbientLeveling(LightX lightX, ANCAwarenessPreset value) {
        if (lightX != null) {
            lightX.writeAppANCAwarenessPreset(value);
        }else {
            int ambientLeveling = 0;
            switch(value){
                case None:
                    ambientLeveling = 0;
                    break;
                case Low:
                    ambientLeveling = 2;
                    break;
                case Medium:
                    ambientLeveling = 4;
                    break;
                case High:
                    ambientLeveling = 6;
                    break;
                case First:
                    ambientLeveling = 2;
                    break;
                case Last:
                    ambientLeveling = 6;
                    break;
                default:
                    ambientLeveling = 0;
                    break;
            }

            Cmd150Manager.getInstance().setAmbientLeveling(AvneraManager.getAvenraManager(context).getAudioManager(),ambientLeveling);
            Log.d(TAG, "SendCommand setAmbientLeveling ambientLeveling="+ambientLeveling);
        }
    }

    public void getBatterLeverl(LightX lightX){
        if (lightX != null){
            lightX.readApp(Command.AppBatteryLevel);
        }else{
            Cmd150Manager.getInstance().getBatteryLevel(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getBatterLevel");
        }
    }

    public void getFirmwareVersion(LightX lightX){
        if (lightX != null){
            lightX.readAppFirmwareVersion();
        }else{
            Cmd150Manager.getInstance().getFirmwareVersion(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getFirmwareVersion");
        }
    }

    public void getFirmwareInfo(LightX lightX){
        if (lightX == null) {
            Cmd150Manager.getInstance().getFirmwareVersion(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getFirmwareInfo");
        }
    }

    public void getAutoOffFeature(LightX lightX) {
        if (lightX != null) {
            lightX.readAppOnEarDetectionWithAutoOff();
        }else{
            Cmd150Manager.getInstance().getAutoOff(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getAutoOffFeature");
        }
    }

    public void setAutoOffFeature(LightX lightX, boolean autoOff) {
        if (lightX != null) {
            lightX.writeAppOnEarDetectionWithAutoOff(autoOff);
        }else{
            Cmd150Manager.getInstance().setAutoOff(AvneraManager.getAvenraManager(context).getAudioManager(),autoOff);
            Log.d(TAG, "SendCommand setAutoOffFeature autoOff ="+autoOff);
        }
    }

    public void getVoicePrompt(LightX lightX) {
        if (lightX != null) {
            lightX.readAppVoicePromptEnable();
        }else{
            Cmd150Manager.getInstance().getVoicePrompt(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getVoicePrompt");
        }
    }

    public void setVoicePrompt(LightX lightX, boolean voicePrompt){
        if (lightX != null) {
            lightX.writeAppVoicePromptEnable(voicePrompt);
        }else{
            Cmd150Manager.getInstance().setVoicePrompt(AvneraManager.getAvenraManager(context).getAudioManager(),voicePrompt);
            Log.d(TAG, "SendCommand setVoicePrompt voicePrompt ="+voicePrompt);
        }
    }

    public void getBootVersionFileResource(LightX lightX) {
        if (lightX != null) {
            lightX.readBootVersionFileResource();
        }else{
            Log.e(TAG, "getBootVersionFileResource else ");
        }
    }


    public void getSmartButton(LightX lightX) {
        if (lightX != null) {
            lightX.readAppSmartButtonFeatureIndex();
        }else{
            Cmd150Manager.getInstance().getSmartButtion(AvneraManager.getAvenraManager(context).getAudioManager());
            Log.d(TAG, "SendCommand getSmartButton");
        }
    }

    public void setSmartButton(LightX lightX, boolean noise) {
        if (lightX != null) {
            lightX.writeAppSmartButtonFeatureIndex(noise);
        }else{
            Cmd150Manager.getInstance().setSmartButton(AvneraManager.getAvenraManager(context).getAudioManager(),noise);
            Log.d(TAG, "SendCommand setSmartButton noise = "+noise);
        }
    }

    public void readBootImageType(LightX lightX){
        if (lightX != null){
            lightX.readBootImageType();
        }
    }
}
