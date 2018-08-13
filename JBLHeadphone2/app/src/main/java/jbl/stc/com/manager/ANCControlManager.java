package jbl.stc.com.manager;

import android.content.Context;
import android.util.Log;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Logger;

import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;

/**
 * ANCControlManager
 * Created by intahmad on 7/24/2015.
 */
public class ANCControlManager {
    private String TAG = ANCControlManager.class.getSimpleName();
    private static String LEFTANC = "LEFTANC";
    private static String RIGHTANC = "RIGHTANC";
    private static String ANCVALUE = "ANCVALUE";
    private static final int kGraphicEQNumBands = 10;

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
     */
    public void getANCValue() {
        if (AvneraManager.getAvenraManager(context).getLightX() != null) {
            AvneraManager.getAvenraManager(context).getLightX().readAppANCEnable();
        } else {
            Cmd150Manager.getInstance().getANC(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getANCValue ");
        }
    }

    /**
     * Set ANC value
     *
     * @param ancValue boolean anc value, true for ANC ON and false for ANC Off.
     */
    public void setANCValue(boolean ancValue) {
        if (AvneraManager.getAvenraManager(context).getLightX() != null) {
            AvneraManager.getAvenraManager(context).getLightX().writeAppANCEnable(ancValue);
        } else {
            Cmd150Manager.getInstance().setANC(AvneraManager.getAvenraManager(context).getAudioManager(), ancValue);
            Logger.d(TAG, "SendCommand setANCValue");
        }
    }

    /**
     * Set Left Awareness seek bar value.
     *
     * @param leftANCvalue int value to set seek bar.
     */
    public void setLeftAwarenessPresetValue(int leftANCvalue) {
        int rawSteps = ancValueConverter(leftANCvalue);
        Logger.d(TAG, "Send left rawstep = " + rawSteps);
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppWithUInt32Argument(Command.AppAwarenessRawLeft, (long) rawSteps);
            PreferenceUtils.setInt(LEFTANC, leftANCvalue, context);
        } else {
            Cmd150Manager.getInstance().setANCLeft(AvneraManager.getAvenraManager(context).getAudioManager(), rawSteps);
            Logger.d(TAG, "SendCommand setLeftAwarenessPresetValue");
        }
    }

    /**
     * Set right Awareness seek bar value.
     *
     * @param rightANCvalue int value to set seek bar
     */
    public void setRightAwarenessPresetValue(int rightANCvalue) {
        int rawSteps = ancValueConverter(rightANCvalue);
        Logger.d(TAG, "Send right rawstep = " + rawSteps);
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppWithUInt32Argument(Command.AppAwarenessRawRight, (long) rawSteps);
            PreferenceUtils.setInt(RIGHTANC, rightANCvalue, context);
        } else {
            Cmd150Manager.getInstance().setANCRight(AvneraManager.getAvenraManager(context).getAudioManager(), rawSteps);
            Logger.d(TAG, "SendCommand setRightAwarenessPresetValue");
        }

    }

    /**
     * Convert ANC (0-100 ) range to ANC steps (0-9).
     *
     * @param ancValue int ANC awareness seek bar value.
     * @return ANC steps
     */
    private int ancValueConverter(int ancValue) {
//        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
//            return (ancValue * 7) / 100;
//        }else{
//            return (ancValue * mRawsteps) /100;
//        }

        if (ancValue > 95) {
            return mRawsteps;
        }
        return (ancValue * mRawsteps) / 100;
    }

    /**
     * Get left ANC value from headphone.
     *
     */
    public void getLeftANCvalue() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppAwarenessRawLeft();
        } else {
            Cmd150Manager.getInstance().getANCLeft(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getLeftANCvalue");
        }
    }

    /**
     * Get right ANC value from headphone.
     *
     */
    public void getRightANCvalue() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppAwarenessRawRight();
        } else {
            Cmd150Manager.getInstance().getANCRight(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getRightANCvalue");
        }
    }

    public void getRawStepsByCmd() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
            Logger.e(TAG, "getRawStepsByCmd ");
             AvneraManager.getAvenraManager(context).getLightX().readAppAwarenessRawSteps();
        } else {
            Cmd150Manager.getInstance().getRawSteps(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getRawStepsByCmd");
        }
    }

    private int mRawsteps = 7;

    public void setRawSteps(int rawSteps) {
        mRawsteps = rawSteps;
    }

    /**
     * Get ambient leveling
     */
    public void getAmbientLeveling() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppANCAwarenessPreset();
        } else {
            Cmd150Manager.getInstance().getAmbientLeveling(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getAmbientLeveling");
        }
    }


    /**
     * Set ambient leveling
     */
    public void setAmbientLeveling(ANCAwarenessPreset value) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppANCAwarenessPreset(value);
        } else {
            int ambientLeveling = 0;
            switch (value) {
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

            Cmd150Manager.getInstance().setAmbientLeveling(AvneraManager.getAvenraManager(context).getAudioManager(), ambientLeveling);
            Logger.d(TAG, "SendCommand setAmbientLeveling ambientLeveling=" + ambientLeveling);
        }
    }

    public void getBatterLeverl() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readApp(Command.AppBatteryLevel);
        } else {
            Cmd150Manager.getInstance().getBatteryLevel(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getBatterLevel");
        }
    }

    public void getFirmwareVersion() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppFirmwareVersion();
        } else {
            Cmd150Manager.getInstance().getFirmwareVersion(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getFirmwareVersion");
        }
    }

    public void getFirmwareInfo() {
        if (AvneraManager.getAvenraManager(context).getLightX() != null) {
            Cmd150Manager.getInstance().getFWInfo(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getFirmwareInfo");
        }
    }

    public void getAutoOffFeature() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppOnEarDetectionWithAutoOff();
        } else {
            Cmd150Manager.getInstance().getAutoOff(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getAutoOffFeature");
        }
    }

    public void setAutoOffFeature(boolean autoOff) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppOnEarDetectionWithAutoOff(autoOff);
        } else {
            Cmd150Manager.getInstance().setAutoOff(AvneraManager.getAvenraManager(context).getAudioManager(), autoOff);
            Logger.d(TAG, "SendCommand setAutoOffFeature autoOff =" + autoOff);
        }
    }

    public void getVoicePrompt() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppVoicePromptEnable();
        } else {
            Cmd150Manager.getInstance().getVoicePrompt(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getVoicePrompt");
        }
    }

    public void setVoicePrompt(boolean voicePrompt) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppVoicePromptEnable(voicePrompt);
        } else {
            Cmd150Manager.getInstance().setVoicePrompt(AvneraManager.getAvenraManager(context).getAudioManager(), voicePrompt);
            Logger.d(TAG, "SendCommand setVoicePrompt voicePrompt =" + voicePrompt);
        }
    }

    public void getBootVersionFileResource() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readBootVersionFileResource();
        } else {
            Logger.e(TAG, "getBootVersionFileResource else ");
        }
    }


    public void getSmartButton() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppSmartButtonFeatureIndex();
        } else {
            Cmd150Manager.getInstance().getSmartButtion(AvneraManager.getAvenraManager(context).getAudioManager());
            Logger.d(TAG, "SendCommand getSmartButton");
        }
    }

    public void setSmartButton(boolean noise) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().writeAppSmartButtonFeatureIndex(noise);
        } else {
            Cmd150Manager.getInstance().setSmartButton(AvneraManager.getAvenraManager(context).getAudioManager(), noise);
            Logger.d(TAG, "SendCommand setSmartButton noise = " + noise);
        }
    }

    public void readBootImageType() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readBootImageType();
        }
    }

    public void getCurrentPreset() {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
             AvneraManager.getAvenraManager(context).getLightX().readAppGraphicEQCurrentPreset();
        else {
            Cmd150Manager.getInstance().getGeqCurrentPreset(AvneraManager.getAvenraManager(context).getAudioManager());
        }
    }

    public void applyPresetsWithBand(GraphicEQPreset presets, int[] values) {
        try {
            applyPresetWithoutBand(presets);
//            if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
            int band = 0;
            for (int one : values) {
                setAppGraphicEQBand(presets, band, one);
                ++band;
            }
//            } else
//                Logger.error(TAG, context.getResources().getString(R.string.plsConnect));
        } catch (IllegalArgumentException e) {
            AlertsDialog.showSimpleDialogWithOKButton(null, e.getMessage(), context);
        }
    }

    public void applyPresetWithoutBand(GraphicEQPreset presets) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
             AvneraManager.getAvenraManager(context).getLightX().writeAppGraphicEQCurrentPreset(presets);
        else {
            //Logger.d(TAG, context.getResources().getString(R.string.plsConnect));
            Cmd150Manager.getInstance().setGeqCurrentPreset(AvneraManager.getAvenraManager(context).getAudioManager(), eqPresetToInt(presets));
        }
    }

    public void setAppGraphicEQBand(GraphicEQPreset presetType, int band, int one) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
             AvneraManager.getAvenraManager(context).getLightX().writeAppGraphicEQBand(presetType, band, one);
        else {
            Cmd150Manager.getInstance().sendSetEqBandGains(AvneraManager.getAvenraManager(context).getAudioManager(), eqPresetToInt(presetType), band, one);
        }
    }

    public void getAppGraphicEQBand(GraphicEQPreset presetType) {
        for (int i = 0; i < kGraphicEQNumBands; i++) {
            if ( AvneraManager.getAvenraManager(context).getLightX() != null)
                 AvneraManager.getAvenraManager(context).getLightX().readAppGraphicEQBand(presetType, i);
            else {
                Cmd150Manager.getInstance().getEqBandGains(AvneraManager.getAvenraManager(context).getAudioManager(), eqPresetToInt(presetType), i);
            }
        }

        /*if ( AvneraManager.getAvenraManager(context).getLightX() != null)
             AvneraManager.getAvenraManager(context).getLightX().readAppGraphicEQBand(presetType,0);
        else {
            Cmd150Manager.getInstance().getEqBandGains(AvneraManager.getAvenraManager(context).getAudioManager(), eqPresetToInt(presetType), 0);
        }*/

    }

    public void getAppGraphicEQPresetBandSettings(GraphicEQPreset preset, int count) {
        if ( AvneraManager.getAvenraManager(context).getLightX() != null) {
             AvneraManager.getAvenraManager(context).getLightX().readAppGraphicEQPresetBandSettings(preset);
        } else {
            Cmd150Manager.getInstance().getAppGraphicEQPresetBandSettings(AvneraManager.getAvenraManager(context).getAudioManager(), eqPresetToInt(preset), 9);
        }
    }

    private int eqPresetToInt(GraphicEQPreset preset) {
        int presetType = 0;
        switch (preset) {
            case Off: {
                presetType = 0;
                break;
            }
            case Jazz: {
                presetType = 1;
                break;
            }
            case Vocal: {
                presetType = 2;
                break;
            }
            case Bass: {
                presetType = 3;
                break;
            }
            case User: {
                presetType = 4;
                break;
            }
            case First: {
                presetType = 1;
                break;
            }
            case Last: {
                presetType = 4;
                break;
            }
            case NumPresets: {
                presetType = 4;
                break;
            }
        }
        return presetType;
    }

    public void readConfigModelNumber(){
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
            AvneraManager.getAvenraManager(context).getLightX().readConfigModelNumber();
    }

    public void readConfigProductName(){
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
            AvneraManager.getAvenraManager(context).getLightX().readConfigProductName();
    }

    public void readBootVersionFileResource(){
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
            AvneraManager.getAvenraManager(context).getLightX().readBootVersionFileResource();
    }

    public void enterApplication(){
        if ( AvneraManager.getAvenraManager(context).getLightX() != null)
            AvneraManager.getAvenraManager(context).getLightX().enterApplication();
    }
}
