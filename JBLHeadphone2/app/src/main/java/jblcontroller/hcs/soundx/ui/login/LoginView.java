package jblcontroller.hcs.soundx.ui.login;


import java.util.List;

import jblcontroller.hcs.soundx.base.BaseView;
import jblcontroller.hcs.soundx.data.remote.model.GetAudioProfile;

public interface LoginView extends BaseView {

    void setEmailError(String message);

    void setPasswordError(String message);

    void onLoginSuccess(String token);

    void onLoginFailed(String message);

    void onGetAudioProfileSuccess(List<GetAudioProfile> profile);

    void onGetAudioProfileFailed(String message);
}
