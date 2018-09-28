package jblcontroller.hcs.soundx.ui.signup;


import jblcontroller.hcs.soundx.base.BaseView;

public interface SignUpView extends BaseView {

    void showNameError(String message);

    void showEmailError(String message);

    void showPasswordError(String message);

    void showPasswordMismatchError(String message);

    void showMobileNumberError(String message);

    void onSigningSuccess(Object response);

    void onSigningFail(String error);
}

