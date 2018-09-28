package jblcontroller.hcs.soundx.ui.noise;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.base.BaseActivity;
import jblcontroller.hcs.soundx.ui.audioeffect.AudioEffectActivity;
import jblcontroller.hcs.soundx.ui.profile.ProfileActivity;
import jblcontroller.hcs.soundx.utils.NoiseTrackListener;
import jblcontroller.hcs.soundx.utils.NoiseUtils;

public class NoiseMeasureActivity extends BaseActivity implements NoiseMeasureView, NoiseTrackListener, View.OnClickListener {
    @BindView(R.id.next_layout)
    RelativeLayout mNextButton;

    @BindView(R.id.warning_layout)
    RelativeLayout mWarningLayout;

    @BindView(R.id.noise_normal_layout)
    LinearLayout mNormalLayout;

    @BindView((R.id.next))
    TextView moveToNext;

    @BindView((R.id.nxt_btn))
    TextView mNext;

    @BindView((R.id.noise_title))
    TextView mNoiseTitle;

    @BindView((R.id.noise_msg))
    TextView mMessage;

    @BindView((R.id.warning_msg))
    TextView mWaringMessage;

    @BindView((R.id.not_recommend))
    TextView notRecommendText;

    private NoiseMeasurePresenter mPresenter;
    private NoiseUtils mNoiseUtils;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        ButterKnife.bind(this);
        mPresenter = new NoiseMeasurePresenter(this);
        setPresenter(mPresenter);

        mNoiseTitle.setTypeface(getBoldFont());
        mWaringMessage.setTypeface(getBoldFont());
        mNext.setTypeface(getBoldFont());
        moveToNext.setTypeface(getRegularFont());
        notRecommendText.setTypeface(getRegularFont());
        mNextButton.setOnClickListener(this);
        moveToNext.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRecordPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mNoiseUtils.stopRecord();
        } catch (Exception e) {
        }
    }

    private void checkRecordPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        } else {
            initNoiseUitls();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initNoiseUitls();
        } else {
            launchProfileActivity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(NoiseMeasureActivity.this, AudioEffectActivity.class);
        startActivity(intent);
        finish();
    }

    private void initNoiseUitls() {
        mNoiseUtils = new NoiseUtils(NoiseMeasureActivity.this, this);
        mNoiseUtils.startRecord();
    }

    @Override
    public void onNoiseDetect() {
        mWarningLayout.setVisibility(View.VISIBLE);
        mNormalLayout.setVisibility(View.GONE);
    }

    @Override
    public void onQuietEnvironment() {
        mWarningLayout.setVisibility(View.GONE);
        mNormalLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.next_layout:
            case R.id.next:
                launchProfileActivity();
                break;
        }
    }

    private void launchProfileActivity() {
        Intent intent = new Intent(NoiseMeasureActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
