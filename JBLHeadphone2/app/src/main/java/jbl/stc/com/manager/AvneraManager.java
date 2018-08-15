package jbl.stc.com.manager;

import android.content.Context;

import com.avnera.audiomanager.audioManager;
import com.avnera.smartdigitalheadset.LightX;

/**
 * AvneraManager
 * Created by intahmad on 9/16/2015.
 */
public class AvneraManager {
    private static AvneraManager avneraManager;
    private audioManager mAudioManager = null;
    private LightX lightX;

    public static AvneraManager getAvenraManager() {
        if (avneraManager == null)
            avneraManager = new AvneraManager();
        return avneraManager;
    }

    public LightX getLightX() {
        return lightX;
    }

    public void setLightX(LightX lightX) {
        this.lightX = lightX;
    }

    public audioManager getAudioManager() {
        return mAudioManager;
    }

    public void setAudioManager(audioManager audioManager) {
        this.mAudioManager = audioManager;
    }

}
