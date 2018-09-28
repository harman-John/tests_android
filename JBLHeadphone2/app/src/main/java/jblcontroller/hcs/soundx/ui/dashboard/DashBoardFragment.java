package jblcontroller.hcs.soundx.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jblcontroller.hcs.soundx.base.SoundXBaseFragment;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.data.remote.api.ResponseListener;
import jblcontroller.hcs.soundx.data.remote.api.RestClientManager;
import jblcontroller.hcs.soundx.data.remote.model.ProfileRequest;
import jblcontroller.hcs.soundx.ui.login.LoginActivity;

import static jblcontroller.hcs.soundx.utils.AppConstants.BASS_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_CONFIGURATION_FOUND;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_SESSION_EXISTS;
import static jblcontroller.hcs.soundx.utils.AppConstants.TREBLE_KEY;

public class DashBoardFragment extends SoundXBaseFragment implements View.OnClickListener {

    private int mPreferredBass;
    private int mPreferredTreble;

    @BindView(R.id.soundeffect_icon)
    ImageView mSoundEffect;

    @BindView(R.id.illustration_icon)
    ImageView mIllustrationIcon;

    @BindView(R.id.enable_device_title)
    TextView mEnableTitle;

    @BindView(R.id.enable_soundx)
    TextView mEnaleSoundxTitle;

    @BindView(R.id.logout)
    TextView mLogout;


    private boolean isPresetFound;

    private MediaPlayer player;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
//	int[] a = {10,10,10,10,10,10,10,10,10,10};
//    int[] b = {-10,-10,-10,-10,-10,-10,-10,-10,-10,-10};

    int[] a = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};    // default

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SoundXSharedPreferences.setConfigurationDone(true, getActivity());

        playSampleMusic();
        registerAudioFocusListener();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_soundeffect, container, false);
        ButterKnife.bind(this, view);
        mEnableTitle.setTypeface(getBoldFont());
        mEnaleSoundxTitle.setTypeface(getRegularFont());
        mLogout.setTypeface(getRegularFont());
        mSoundEffect.setOnClickListener(this);
        mLogout.setOnClickListener(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            isPresetFound = bundle.getBoolean(IS_CONFIGURATION_FOUND);
            boolean isSessionExists = bundle.getBoolean(IS_SESSION_EXISTS);

            if (isSessionExists) {
                mPreferredBass = SoundXSharedPreferences.getPrefBass(getActivity());
                mPreferredTreble = SoundXSharedPreferences.getPrefTreble(getActivity());
                applyPresetEffect(mPreferredBass, mPreferredTreble);
            } else if (isPresetFound) {
                mPreferredBass = bundle.getInt(BASS_KEY, 0);
                mPreferredTreble = bundle.getInt(TREBLE_KEY, 0);
                SoundXSharedPreferences.enablePreference(true, getActivity());

                SoundXSharedPreferences.setPreferredBass(getActivity(), mPreferredBass);
                SoundXSharedPreferences.setPreferredTreble(getActivity(), mPreferredTreble);

                applyPresetEffect(mPreferredBass, mPreferredTreble);
            } else {
                SoundXSharedPreferences.enablePreference(false, getActivity());
                setDefault();

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!SoundXSharedPreferences.isOfflineEnabled(getActivity())) {
//            if (isPresetFound)
                createAudioProfile();
        }
    }

    private void createAudioProfile() {
        ProfileRequest request = new ProfileRequest();
        request.setBass(String.valueOf(mPreferredBass));
        request.setTreble(String.valueOf(mPreferredTreble));
        String[] deviceType = {"Everest Elite 750NC"}; // TODO need to change hardcode value
        request.setDeviceType(deviceType);
        request.setListningExp(String.valueOf(SoundXSharedPreferences.getListeningExp(JBLApplication.getJBLApplicationContext())));
        request.setYearOfBirth(String.valueOf(SoundXSharedPreferences.getYob(JBLApplication.getJBLApplicationContext())));
        request.setGender(String.valueOf(SoundXSharedPreferences.getGender(JBLApplication.getJBLApplicationContext())));
        RestClientManager restClientManager = new RestClientManager(new ResponseListener() {
            @Override
            public void onSuccess(Object response) {
            }

            @Override
            public void onFailure(String message) {
            }
        });
        restClientManager.createAudioProfile(request);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.soundeffect_icon:

                if (SoundXSharedPreferences.isPreferenceEnabled(getActivity())) {
                    setDefault();
                    Log.d("SOUNDX", "disabled --------");
                    SoundXSharedPreferences.enablePreference(false, getActivity());
                    mIllustrationIcon.setVisibility(View.INVISIBLE);
                    mSoundEffect.setImageResource(R.mipmap.right_x_left_x_mask);


                } else {
                    Log.d("SOUNDX", "enabled --------");
                    applyPresetEffect(mPreferredBass, mPreferredTreble);
                    SoundXSharedPreferences.enablePreference(true, getActivity());
                    mIllustrationIcon.setVisibility(View.VISIBLE);
                    mSoundEffect.setImageResource(R.mipmap.right_x_left_x_mask_selected);
                }
                break;

            case R.id.logout:
                logout();
                break;


        }
    }


    private void playSampleMusic() {
        try {
            AssetFileDescriptor afd = getActivity().getAssets().openFd("sample_1.wav");
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
            player.setLooping(true);
        } catch (IOException e) {

        }

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        // Set volume level to desired levels
                        playSampleMusic();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        // You have audio focus for a short time
                        playSampleMusic();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        // Play over existing audio
                        playSampleMusic();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        try {
                            if (player != null)
                                player.stop();
                        } catch (Exception e) {

                        }
//                        stop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        // Temporary loss of audio focus - expect to get it back - you can keep your resources around
//                        pause();
                        try {
                            player.pause();
                        } catch (Exception e) {

                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        // Lower the volume
                        break;
                }
            }
        };
    }

    private void logout() {
        try {
            if (player != null) {
                player.stop();
                player.release();
            }
        } catch (Exception e) {

        }
        SoundXSharedPreferences.clearUserData(getActivity());
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();

    }

    private void registerAudioFocusListener() {
        AudioManager am = (AudioManager) getActivity()
                .getSystemService(Context.AUDIO_SERVICE);
        // Request audio focus for play back
        int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (player != null) {
            try {
                player.stop();
                player.release();
            } catch (Exception e) {

            }
        }
    }
}
