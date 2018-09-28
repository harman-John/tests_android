package jblcontroller.hcs.soundx.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.base.BaseActivity;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.data.local.model.UserData;
import jblcontroller.hcs.soundx.ui.audioeffect.AudioEffectActivity;
import jblcontroller.hcs.soundx.ui.login.LoginActivity;
import retrofit2.Response;

public class SignupActivity extends BaseActivity implements SignUpView, View.OnClickListener {
    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_mobile)
    EditText _mobileText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @BindView(R.id.btn_create_account)
    TextView _signupButton;

    @BindView(R.id.link_login)
    TextView _loginLink;

    @BindView(R.id.progressBar)
    ProgressBar progressDialog;

    private SignUpPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mPresenter = new SignUpPresenter(this);
        setPresenter(mPresenter);

        _nameText.setTypeface(getRegularFont());
        _emailText.setTypeface(getRegularFont());
        _mobileText.setTypeface(getRegularFont());
        _passwordText.setTypeface(getRegularFont());
        _reEnterPasswordText.setTypeface(getRegularFont());
        _loginLink.setTypeface(getBoldFont());
        _signupButton.setTypeface(getBoldFont());

        _signupButton.setOnClickListener(this);
        _loginLink.setOnClickListener(this);

    }

    @Override
    public void showNameError(String message) {
        _nameText.setError("at least 3 characters");
    }

    @Override
    public void showEmailError(String message) {
        _emailText.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        _passwordText.setError(message);
    }

    @Override
    public void showPasswordMismatchError(String message) {

        _reEnterPasswordText.setError(message);
    }

    @Override
    public void showMobileNumberError(String message) {
        _mobileText.setError(message);
    }

    @Override
    public void onSigningSuccess(Object response) {
        try {
            Response mResponse = (Response) response;
            if (!TextUtils.isEmpty(mResponse.headers().get("x-auth-token")))
                SoundXSharedPreferences.setAuthToken(this, mResponse.headers().get("x-auth-token"));

        } catch (Exception e) {
        } finally {
            startActivity(new Intent(SignupActivity.this, AudioEffectActivity.class));
            finish();
        }


    }

    @Override
    public void onSigningFail(String error) {
        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgress(String message) {
        progressDialog.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressDialog.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_create_account:
//                startActivity(new Intent(SignupActivity.this, AudioEffectActivity.class));
//                finish();

                if (isOnline(this))
                    createAccount();
                else {
                    Toast.makeText(this, "Please enable internet", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.link_login:
                launchLoginActivity();
                break;
        }
    }

    private void createAccount() {
        UserData data = new UserData();
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        data.setName(name);
        data.setEmail(email);
        data.setMobileNumber(mobile);
        data.setPassword(password);
        data.setReEnterPassword(reEnterPassword);
        SoundXSharedPreferences.setUserName(this, email);
        SoundXSharedPreferences.setPassword(this, password);
//Offline support
        if (SoundXSharedPreferences.isOfflineEnabled(this)) {
            startActivity(new Intent(SignupActivity.this, AudioEffectActivity.class));
            finish();
        } else
            mPresenter.signUp(data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        launchLoginActivity();
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}