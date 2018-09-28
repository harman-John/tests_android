package jblcontroller.hcs.soundx.ui.login;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jbl.stc.com.logger.Logger;
import jblcontroller.hcs.soundx.base.BaseActivity;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.data.remote.model.GetAudioProfile;
import jblcontroller.hcs.soundx.ui.audioeffect.AudioEffectActivity;
import jblcontroller.hcs.soundx.ui.signup.SignupActivity;
import jblcontroller.hcs.soundx.ui.splash.HeadsetConnectionDialog;

import static jblcontroller.hcs.soundx.utils.AppConstants.BASS_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_CONFIGURATION_FOUND;
import static jblcontroller.hcs.soundx.utils.AppConstants.TREBLE_KEY;


public class LoginActivity extends BaseActivity implements LoginView, View.OnClickListener {
    private final String TAG = LoginActivity.class.getSimpleName();
    int bass = 0;
    int treble = 0;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    TextView mLoginButton;
    @BindView(R.id.link_signup)
    TextView mSignupText;
    @BindView(R.id.forgot_password)
    TextView mForgotPosstword;
    @BindView(R.id.progressBar)
    ProgressBar progressDialog;
    private boolean isFound = false;
    private int position = 0;
    private BluetoothProfile proxy;
    HeadsetConnectionDialog mDialog;
    private Handler mHandler = new Handler();

    @BindView(R.id.offline_check)
    CheckBox offlineCheck;

    private LoginPresenter mPresenter;
    List<GetAudioProfile> profileList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mDialog = new HeadsetConnectionDialog();
        mDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);

        _emailText.setTypeface(getRegularFont());
        _passwordText.setTypeface(getRegularFont());
        mLoginButton.setTypeface(getBoldFont());
        mSignupText.setTypeface(getBoldFont());
        offlineCheck.setTypeface(getMediumFont());
        mForgotPosstword.setTypeface(getBoldFont());

        mPresenter = new LoginPresenter(this);
        setPresenter(mPresenter);

        offlineCheck.setChecked(SoundXSharedPreferences.isOfflineEnabled(this));
        offlineCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked())
                    SoundXSharedPreferences.setWorkOffline(true, LoginActivity.this);
                else
                    SoundXSharedPreferences.setWorkOffline(false, LoginActivity.this);

            }
        });

        mLoginButton.setOnClickListener(this);
        mSignupText.setOnClickListener(this);
        _passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    mLoginButton.performClick();
                }
                return false;
            }
        });

    }


    @Override
    public void setEmailError(String message) {
        _emailText.setError(message);
    }

    @Override
    public void setPasswordError(String message) {
        _passwordText.setError("between 4 and 10 alphanumeric characters");
    }


    @Override
    public void onLoginSuccess(String token) {
        // Launch Profile Activity
        SoundXSharedPreferences.setAuthToken(this, token);
        mPresenter.getUserAudioProfile();

    }

    @Override
    public void onLoginFailed(String error) {
        SoundXSharedPreferences.setAuthToken(this, "");
        Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
        hideProgress();
        mLoginButton.setEnabled(true);
    }

    @Override
    public void onGetAudioProfileSuccess(List<GetAudioProfile> profilelist) {
        this.profileList = profilelist;
        discoverBT();
    }

    private void discoverBT() {
        showProgress("");
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.enable();
        } catch (Exception e) {
        }
        mHandler.postDelayed(a2dpRunable, 0);
    }

    @Override
    public void onGetAudioProfileFailed(String message) {
        discoverBT();
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
            case R.id.btn_login:
                //Offline support
                if (SoundXSharedPreferences.isOfflineEnabled(this)) {
                    discoverBT();

                } else {
                    if (isOnline(this)) {

                        mPresenter.login(_emailText.getText().toString(), _passwordText.getText().toString());
                    } else {
                        Toast.makeText(this, "Please enable internet", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.link_signup:
                launchSignUpActivity();
                break;
        }
    }

    /**
     * This will launch the SignUpActivity
     */
    private void launchSignUpActivity() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchAudioEffectActivity() {
        boolean isConfigured;
        hideProgress();
        if (profileList != null && profileList.size() > 0) {
            try {
                bass = Integer.parseInt(profileList.get(profileList.size() - 1).getBass());
                treble = Integer.parseInt(profileList.get(profileList.size() - 1).getTreble());
            } catch (ClassCastException e) {
            }
            isConfigured = true;
        } else {
            isConfigured = false;
        }
        Intent newIntent = new Intent(this, AudioEffectActivity.class);
        newIntent.putExtra(BASS_KEY, bass);
        newIntent.putExtra(TREBLE_KEY, treble);
        newIntent.putExtra(IS_CONFIGURATION_FOUND, isConfigured);
        startActivity(newIntent);
        this.overridePendingTransition(0, 0);
        finish();
    }

    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                List<BluetoothDevice> deviceList = proxy.getConnectedDevices();
                Logger.d(TAG, " A2DP connected deviceList = " + deviceList + ",size = " + deviceList.size() + ",position =" + position);
                if (deviceList.size() > 0
                        && position < deviceList.size()
                        && deviceList.get(position).getName().toUpperCase().contains("JBL Everest".toUpperCase())) {
                    mHandler.removeCallbacks(a2dpRunable);
                    isFound = true;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isFound) {
                                hideDialog();
                                launchAudioEffectActivity();
                            } else {
                                showDialog();
                            }
                        }
                    }, 2000);

                    Logger.d(TAG, " A2DP connected first device = " + deviceList + ",position =" + position);

                } else {
                    showDialog();
                    if (position < deviceList.size() - 1) {
                        position++;
                    } else position = 0;

                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };
    private Runnable a2dpRunable = new Runnable() {
        @Override
        public void run() {
            startA2DPCheck();
            mHandler.postDelayed(a2dpRunable, 2000);
        }
    };

    private void startA2DPCheck() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            try {
                mBtAdapter.getProfileProxy(this, mListener, BluetoothProfile.A2DP);
            } catch (Exception e) {

            }
        }
    }

    private void showDialog() {
        if (mDialog != null && !mDialog.isAdded() && !mDialog.isVisible()) {
            mDialog.show(getSupportFragmentManager(), mDialog.getTag());
        }
    }

    private void hideDialog() {
        try {
            if (mDialog != null && mDialog.isVisible()) {
                mDialog.dismissAllowingStateLoss();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP, proxy);
        } catch (Exception e) {
        }
    }
}
