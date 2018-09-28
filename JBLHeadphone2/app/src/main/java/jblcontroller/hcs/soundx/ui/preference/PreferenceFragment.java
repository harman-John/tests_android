package jblcontroller.hcs.soundx.ui.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.harman.bluetooth.constants.EnumEqPresetIdx;
import com.harman.bluetooth.req.CmdEqPresetSet;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jblcontroller.hcs.soundx.base.SoundXBaseFragment;
import jblcontroller.hcs.soundx.ui.customui.FadePageTransfomer;
import jblcontroller.hcs.soundx.ui.dashboard.DashboardDemo;

import static jblcontroller.hcs.soundx.utils.AppConstants.EXPERIENCE_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.GENDER_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.YEAR_KEY;

public class PreferenceFragment extends SoundXBaseFragment implements PreferenceView {
    public static final String TAG = PreferenceFragment.class.getSimpleName();
    //    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
    private MediaPlayer player;

    @BindView(R.id.sound_shade_pager)
    ViewPager mPreferencePager;

    @BindView(R.id.rl_done_btn)
    RelativeLayout mDone;

    @BindView(R.id.back_btn)
    ImageView mBackBtn;

    @BindView(R.id.tv_pref_title)
    TextView prefTitle;

    @BindView(R.id.tv_pref_instr)
    TextView prefInstruction;

    @BindView(R.id.tv_pref_done)
    TextView prefDone;

    private PreferencePager mShadePageAdapter;
    private double mPreferredBass;
    private double mPreferredTreble;
    private PreferenceEditorPresenter mPresenter;

    private int[] bassSettings = {0, 0, 3, 3, -3, 0}; // Bass HF
    private int[] trebleSettings = {0, 3, 0, 3, 0, 6};// Treble LF

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate");


        mPresenter = new PreferenceEditorPresenter(this);
        setPresenter(mPresenter);

        playSampleMusic();

        registerAudioFocusListener();

        Bundle bundle = getArguments();
        if (bundle != null) {
            int year = bundle.getInt(YEAR_KEY, 0);
            int gender = bundle.getInt(GENDER_KEY, 0);
            int experience = bundle.getInt(EXPERIENCE_KEY, 0);
            mPresenter.calculatePreferredBass(year, experience);
            mPresenter.calculatePreferredTreble(year, experience, gender);
//            if (lightX != null) {
//                lightX.writeAppGraphicEQCurrentPreset(GraphicEQPreset.User);
            CmdEqPresetSet cmdEqPresetSet = new CmdEqPresetSet(EnumEqPresetIdx.USER);
            LiveManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).mac, cmdEqPresetSet);
            applyPresetEffect(mPreferredBass, mPreferredTreble);
//            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_shade, container, false);

        ButterKnife.bind(this, view);
        initViews();

        return view;
    }

    private void initViews() {

        mShadePageAdapter = new PreferencePager(getActivity());

        mPreferencePager.setAdapter(mShadePageAdapter);
        mPreferencePager.setClipToPadding(false);
        mPreferencePager.setPageMargin(-400);
        mPreferencePager.setPageTransformer(false, new FadePageTransfomer());

//@TODO - position 0 wee need to calculation

        mPreferencePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                double bass = mPreferredBass + bassSettings[position];
                double treble = mPreferredTreble + trebleSettings[position];

                // ---- High eq settings for Demo ----------
//                applyPresetEffect(bass, treble,position);
                applyPresetEffect(bass, treble);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DashboardDemo.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefTitle.setTypeface(getBoldFont());
        prefInstruction.setTypeface(getBoldFont());
        prefDone.setTypeface(getBoldFont());
        PreferenceEditorActivity mActivity = (PreferenceEditorActivity) getActivity();
    }

    @Override
    public void applyPresetEffect(double mPreferredBass, double mPreferredTreble) {
        super.applyPresetEffect(mPreferredBass, mPreferredTreble);
    }


    @Override
    public void getPreferredBass(double bass) {
        mPreferredBass = bass;
        android.util.Log.d("TEST", "BASS" + mPreferredBass);

    }

    @Override
    public void getPreferredTreble(double treble) {

        mPreferredTreble = treble;
        android.util.Log.d("TEST", "mPreferredTreble" + mPreferredTreble);

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

            Log.d("playing", "playing---- ");
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
                            player.stop();
                        }
                        catch (Exception e){

                        }
//                        stop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        // Temporary loss of audio focus - expect to get it back - you can keep your resources around
//                        pause();
                        try {
                            player.pause();
                        }catch (Exception e){

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

    @Override
    public void onDetach() {
        super.onDetach();
        if (player != null) {
            player.stop();
            player.release();
        }
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
}
