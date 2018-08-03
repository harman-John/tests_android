package jbl.stc.com.manager;

import android.util.Log;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;

import jbl.stc.com.logger.Logger;

public class CmdSDHManager implements BaseManager {


    private static final String TAG = CmdSDHManager.class.getSimpleName();
    private LightX object;

    private static class InstanceHolder {
        public static final CmdSDHManager instance = new CmdSDHManager();
    }

    public static CmdSDHManager getInstance() {
        return InstanceHolder.instance;
    }

    public void setManager(LightX lightX) {
        object = lightX;
    }

    @Override
    public void getANC(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppANCEnable();
    }

    @Override
    public void setANC(Object object, boolean anc) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppANCEnable(anc);
    }

    @Override
    public void getAmbientLeveling(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppANCAwarenessPreset();
    }

    @Override
    public void getANCLeft(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppAwarenessRawLeft();
    }

    @Override
    public void getANCRight(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppAwarenessRawRight();
    }

    @Override
    public void setANCLeft(Object object, int ancLeft) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppWithUInt32Argument(Command.AppAwarenessRawLeft, (long) ancLeft);
    }

    @Override
    public void setANCRight(Object object, int ancRight) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppWithUInt32Argument(Command.AppAwarenessRawRight, (long) ancRight);
    }

    public void setAmbientLeveling(Object object, ANCAwarenessPreset value) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppANCAwarenessPreset(value);
    }

    @Override
    public void getBatteryLevel(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readApp(Command.AppBatteryLevel);
    }

    @Override
    public void getFirmwareVersion(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppFirmwareVersion();
    }

    @Override
    public void getAutoOff(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppOnEarDetectionWithAutoOff();
    }

    @Override
    public void setAutoOff(Object object, boolean autoOff) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppOnEarDetectionWithAutoOff(autoOff);
    }

    @Override
    public void getVoicePrompt(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppVoicePromptEnable();
    }

    @Override
    public void setVoicePrompt(Object object, boolean voicePrompt) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppVoicePromptEnable(voicePrompt);
    }

    @Override
    public void getSmartButtion(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppSmartButtonFeatureIndex();
    }

    @Override
    public void setSmartButton(Object object, boolean smartButton) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeAppSmartButtonFeatureIndex(smartButton);
    }

    @Override
    public void getGeqCurrentPreset(Object object) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppGraphicEQCurrentPreset();
    }

    @Override
    public void getGeqBandFreq(Object object, int preset, int band) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).readAppGraphicEQBandFreq();
    }

    public void updateImage(Object object, LightX.FirmwareRegion region, byte[] data) {
        if (object == null) {
            Logger.d(TAG, "object is null, call setManager first");
            return;
        }
        ((LightX) object).writeFirmware(region, data);
    }

}
