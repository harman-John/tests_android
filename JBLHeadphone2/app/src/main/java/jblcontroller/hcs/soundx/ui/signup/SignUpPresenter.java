package jblcontroller.hcs.soundx.ui.signup;


import android.text.TextUtils;

import com.avnera.smartdigitalheadset.Log;

import jblcontroller.hcs.soundx.base.BasePresenter;
import jblcontroller.hcs.soundx.data.local.model.UserData;
import jblcontroller.hcs.soundx.data.remote.api.ResponseListener;
import jblcontroller.hcs.soundx.data.remote.api.RestClientManager;
import jblcontroller.hcs.soundx.data.remote.model.SignupRequest;
import retrofit2.Response;

public class SignUpPresenter extends BasePresenter {

    private SignUpView mView;

    public SignUpPresenter(SignUpView view) {
        mView = view;
    }


    public void signUp(UserData user) {
        if (validateUserInput(user)) {

            SignupRequest data = new SignupRequest();

            data.setName(user.getName());
            data.setEmail(user.getEmail());
            data.setMobile(user.getMobileNumber());
            data.setPassword(user.getPassword());
            mView.showProgress("Please wait..");
            RestClientManager restClientManager = new RestClientManager(new ResponseListener() {
                @Override
                public void onSuccess(Object response) {
                    mView.onSigningSuccess(response);
                    mView.hideProgress();
                }

                @Override
                public void onFailure(String message) {
                    mView.onSigningFail(message);
                    mView.hideProgress();
                }
            });
            restClientManager.signUp(data);

        }

    }

    private boolean validateUserInput(UserData userData) {

        boolean valid = true;


        String name = userData.getName();
        String email = userData.getEmail();
        String mobile = userData.getMobileNumber();
        String password = userData.getPassword();
        String reEnterPassword = userData.getReEnterPassword();

        if (name.isEmpty() || name.length() < 3) {
            valid = false;
            mView.showNameError("enter a valid name");
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid = false;
            mView.showEmailError("enter a valid email address");
        }

        if (mobile.isEmpty() /*|| mobile.length() != 10*/) {
              mView.showMobileNumberError("enter valid mobile number");
            valid = false;
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 15) {
            valid = false;
            mView.showPasswordError("enter valid password");
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 15 || !(reEnterPassword.equals(password))) {
             mView.showPasswordMismatchError("Password mismatch");
            valid = false;
        }

        return valid;
    }
}
