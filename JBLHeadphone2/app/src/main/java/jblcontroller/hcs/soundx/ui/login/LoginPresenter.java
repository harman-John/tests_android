package jblcontroller.hcs.soundx.ui.login;


import java.util.List;

import jblcontroller.hcs.soundx.base.BasePresenter;
import jblcontroller.hcs.soundx.data.remote.api.ResponseListener;
import jblcontroller.hcs.soundx.data.remote.api.RestClientManager;
import jblcontroller.hcs.soundx.data.remote.model.GetAudioProfile;
import retrofit2.Response;

public class LoginPresenter extends BasePresenter {

    private LoginView mView;

    public LoginPresenter(LoginView view) {
        mView = view;
    }

    /**
     * This method handles the user login
     */
    public void login(String email, String password) {
        if (validateUserInput(email, password)) {

            mView.showProgress("Authenticating");
            //TODO- call api here and call respective view methods based on api result
            RestClientManager restClientManager = new RestClientManager(new ResponseListener() {
                @Override
                public void onSuccess(Object response) {
                    Response mResponse = (Response) response;
                    mView.onLoginSuccess(mResponse.headers().get("x-auth-token"));
                    mView.hideProgress();
                }

                @Override
                public void onFailure(String message) {
                    mView.onLoginFailed(message);
                    mView.hideProgress();
                }
            });
            restClientManager.login(email, password);

        }
    }

    private boolean validateUserInput(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mView.setEmailError("enter a valid email address");
            isValid = false;
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mView.setPasswordError("between 4 and 10 alphanumeric characters");
            isValid = false;
        }

        return isValid;
    }

    public void getUserAudioProfile() {
        //TODO- call api here and call respective view methods based on api result
        RestClientManager restClientManager = new RestClientManager(new ResponseListener() {
            @Override
            public void onSuccess(Object response) {
                List<GetAudioProfile> audioProfile = (List<GetAudioProfile>) response;
                mView.onGetAudioProfileSuccess(audioProfile);
            }

            @Override
            public void onFailure(String message) {
                mView.onGetAudioProfileFailed("no data");
            }
        });
        restClientManager.getUserAudioProfile();

    }
}
