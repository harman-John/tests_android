package jbl.stc.com.manager;

public interface BaseManager {

    void getANC();

    void setANC(boolean anc);

    void getAmbientLeveling();

    void getANCLeft();

    void getANCRight();

    void setANCLeft(int ancLeft);

    void setANCRight(int ancRight);

    void getBatteryLevel();

    void getFirmwareVersion();

    void getAutoOff();

    void setAutoOff(boolean autoOff);

    void getVoicePrompt();

    void setVoicePrompt(boolean voicePrompt);

    void getSmartButtion();

    void setSmartButton(boolean smartButton);

    void getGeqCurrentPreset();

    void getGeqBandFreq(int preset, int band);

}
