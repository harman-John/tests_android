package jbl.stc.com.manager;

public interface BaseManager {

    void getANC(Object object);

    void setANC(Object object,boolean anc);

    void getAmbientLeveling(Object object);

    void getANCLeft(Object object);

    void getANCRight(Object object);

    void setANCLeft(Object object,int ancLeft);

    void setANCRight(Object object,int ancRight);

    void getBatteryLevel(Object object);

    void getFirmwareVersion(Object object);

    void getAutoOff(Object object);

    void setAutoOff(Object object,boolean autoOff);

    void getVoicePrompt(Object object);

    void setVoicePrompt(Object object,boolean voicePrompt);

    void getSmartButtion(Object object);

    void setSmartButton(Object object,boolean smartButton);

    void getGeqCurrentPreset(Object object);

    void getGeqBandFreq(Object object,int preset, int band);

}
