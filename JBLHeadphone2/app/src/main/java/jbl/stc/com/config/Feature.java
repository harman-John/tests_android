package jbl.stc.com.config;

import java.util.HashMap;


public enum Feature {
    NONE,
    ENABLE_NOISE_CANCEL,
    ENABLE_AMBIENT_AWARE,
    ENABLE_AUTO_OFF_TIMER,
    ENABLE_AUTO_OFF_TIMER_SWITCH,
    ENABLE_SMART_BUTTON,
    ENABLE_TRUE_NOTE,
    ENABLE_SOUND_X_SETUP,
    ENABLE_SMART_ASSISTANT;


    public static Feature getEnum(String feature) {
        switch (feature){
            case "NONE": return NONE;
            case "ENABLE_NOISE_CANCEL": return ENABLE_NOISE_CANCEL;
            case "ENABLE_AMBIENT_AWARE": return ENABLE_AMBIENT_AWARE;
            case "ENABLE_AUTO_OFF_TIMER": return ENABLE_AUTO_OFF_TIMER;
            case "ENABLE_AUTO_OFF_TIMER_SWITCH": return ENABLE_AUTO_OFF_TIMER_SWITCH;
            case "ENABLE_SMART_BUTTON": return ENABLE_SMART_BUTTON;
            case "ENABLE_TRUE_NOTE": return ENABLE_TRUE_NOTE;
            case "ENABLE_SOUND_X_SETUP": return ENABLE_SOUND_X_SETUP;
            case "ENABLE_SMART_ASSISTANT": return ENABLE_SMART_ASSISTANT;
            default: return null;
        }

    }
}
