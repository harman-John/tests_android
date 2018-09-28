package jblcontroller.hcs.soundx.ui.audioeffect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.base.BaseActivity;
import jblcontroller.hcs.soundx.ui.noise.NoiseMeasureActivity;
import jblcontroller.hcs.soundx.ui.signup.SignupActivity;

import static jblcontroller.hcs.soundx.utils.AppConstants.BASS_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_CONFIGURATION_FOUND;
import static jblcontroller.hcs.soundx.utils.AppConstants.TREBLE_KEY;

public class AudioEffectActivity extends BaseActivity {

    @BindView(R.id.ready_text)
    TextView mReadyText;
    @BindView(R.id.description)
    TextView mDescription;
    @BindView(R.id.ready_layout)
    FrameLayout mReady;
    private ConfigureExistsDialog bottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_effect);
        ButterKnife.bind(this);

        mReadyText.setTypeface(getBoldFont());
        mDescription.setTypeface(getRegularFont());

        if (getIntent() != null) {
            boolean isConfigurationFound = getIntent().getBooleanExtra(IS_CONFIGURATION_FOUND, false);
            if (isConfigurationFound) {

                int bass = getIntent().getIntExtra(BASS_KEY, 0);
                int treble = getIntent().getIntExtra(TREBLE_KEY, 0);

                Bundle data = new Bundle();
                data.putInt(BASS_KEY, bass);
                data.putInt(TREBLE_KEY, treble);

               bottomSheetFragment = new ConfigureExistsDialog();
                bottomSheetFragment.setCancelable(false);
                bottomSheetFragment.setArguments(data);


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if( !bottomSheetFragment.isVisible())
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                    }
                }, 500);
            }
        }


        mReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetFragment!=null && !bottomSheetFragment.isVisible()){
                    bottomSheetFragment.dismiss();
                }
                Intent intent = new Intent(AudioEffectActivity.this, NoiseMeasureActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AudioEffectActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }
}
