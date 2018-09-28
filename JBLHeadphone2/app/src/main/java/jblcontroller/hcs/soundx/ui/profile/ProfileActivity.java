package jblcontroller.hcs.soundx.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.wefika.horizontalpicker.HorizontalPicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.base.BaseActivity;
import jblcontroller.hcs.soundx.ui.noise.NoiseMeasureActivity;
import jblcontroller.hcs.soundx.ui.preference.PreferenceEditorActivity;
import jblcontroller.hcs.soundx.utils.AppConstants;

import static jblcontroller.hcs.soundx.utils.AppConstants.EXPERIENCE_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.GENDER_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.YEAR_KEY;

public class ProfileActivity extends BaseActivity implements ProfileView, View.OnClickListener {

    @BindView(R.id.hpick)
    HorizontalPicker horizontalPicker;

    @BindView(R.id.bubble)
    IndicatorSeekBar bubbleSeekBar;

    @BindView(R.id.female)
    ImageView mFemaleImage;

    @BindView(R.id.male)
    ImageView maleImage;

    @BindView(R.id.back_btn)
    ImageView mBackBtn;

    @BindView(R.id.next_btn)
    RelativeLayout mNextBtn;

    @BindView(R.id.help_image)
    ImageView mHelpImage;

    @BindView(R.id.others)
    ImageView mOthers;

    @BindView(R.id.tv_title_pt)
    TextView ptTitle;

    @BindView(R.id.tv_gendertext)
    TextView genderText;

    @BindView(R.id.tv_lsnexptext)
    TextView listneningExpText;


    @BindView(R.id.tv_yobtext)
    TextView yob;

    private int GENDER_CODE;
    private int YEAR;
    private int LISTENING_EXPERIENCE = AppConstants.BEGINNER;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mBackBtn.setOnClickListener(this);
        mFemaleImage.setOnClickListener(this);
        maleImage.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mHelpImage.setOnClickListener(this);
        mOthers.setOnClickListener(this);
        ptTitle.setTypeface(getBoldFont());
        genderText.setTypeface(getBoldFont());
        listneningExpText.setTypeface(getBoldFont());
        yob.setTypeface(getBoldFont());
        horizontalPicker.setSelectedItem(42);
        horizontalPicker.setOnItemSelectedListener(new HorizontalPicker.OnItemSelected() {
            @Override
            public void onItemSelected(int index) {

                YEAR = index + 1938;

            }
        });

        bubbleSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                android.util.Log.d("TEST", "Progress" + seekBar.getProgress());
                if (seekBar.getProgress() >= 0 && seekBar.getProgress() <= 16) {
                    LISTENING_EXPERIENCE = AppConstants.BEGINNER;
                    android.util.Log.d("TEST", "BASS" + LISTENING_EXPERIENCE);

                } else if (seekBar.getProgress() >= 17 && seekBar.getProgress() <= 50) {
                    LISTENING_EXPERIENCE = AppConstants.AVERAGE;
                    android.util.Log.d("TEST", "BASS" + LISTENING_EXPERIENCE);

                } else if (seekBar.getProgress() >= 51 && seekBar.getProgress() <= 83) {
                    LISTENING_EXPERIENCE = AppConstants.A_LOT;
                    android.util.Log.d("TEST", "BASS" + LISTENING_EXPERIENCE);

                } else if (seekBar.getProgress() >= 84 && seekBar.getProgress() <= 100) {
                    LISTENING_EXPERIENCE = AppConstants.TRAINED;
                    android.util.Log.d("TEST", "BASS" + LISTENING_EXPERIENCE);
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.back_btn:
                finish();

                break;
            case R.id.female:
                GENDER_CODE = AppConstants.FEMALE_CODE;
                setFemaleSelection(true);
                break;
            case R.id.male:
                GENDER_CODE = AppConstants.MALE_CODE;
                setMaleSelection(true);
                break;

            case R.id.others:
                GENDER_CODE = AppConstants.OTHERS;
                setOtherSelection(true);
                break;

            case R.id.next_btn:
                Intent intent = new Intent(this, PreferenceEditorActivity.class);
                intent.putExtra(GENDER_KEY, GENDER_CODE);
                if (YEAR == 0) {
                    YEAR = 1980;
                }
                intent.putExtra(YEAR_KEY, YEAR);
                intent.putExtra(EXPERIENCE_KEY, LISTENING_EXPERIENCE);
                startActivity(intent);
                finish();
                break;


            case R.id.help_image:
                Intent intent1 = new Intent(this, HelpActivity.class);
                startActivity(intent1);

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, NoiseMeasureActivity.class);
        startActivity(intent);
        finish();
    }

    private void setFemaleSelection(boolean selection) {
        if (selection) {
            mFemaleImage.setImageDrawable(getResources().getDrawable(R.mipmap.girl_active));
            maleImage.setImageDrawable(getResources().getDrawable(R.mipmap.boy_inactivate));
            mOthers.setImageDrawable(getResources().getDrawable(R.mipmap.no_answer_inactivate));
        } else {
            mFemaleImage.setImageDrawable(getResources().getDrawable(R.mipmap.girl_inactivate));
        }

    }

    private void setMaleSelection(boolean selection) {
        if (selection) {
            maleImage.setImageDrawable(getResources().getDrawable(R.mipmap.boy_active));
            mFemaleImage.setImageDrawable(getResources().getDrawable(R.mipmap.girl_inactivate));
            mOthers.setImageDrawable(getResources().getDrawable(R.mipmap.no_answer_inactivate));
        } else {
            maleImage.setImageDrawable(getResources().getDrawable(R.mipmap.boy_inactivate));
        }

    }

    private void setOtherSelection(boolean selection) {
        if (selection) {
            mOthers.setImageDrawable(getResources().getDrawable(R.mipmap.no_answer_active));
            mFemaleImage.setImageDrawable(getResources().getDrawable(R.mipmap.girl_inactivate));
            maleImage.setImageDrawable(getResources().getDrawable(R.mipmap.boy_inactivate));
        } else {
            mOthers.setImageDrawable(getResources().getDrawable(R.mipmap.no_answer_inactivate));
        }

    }

}

