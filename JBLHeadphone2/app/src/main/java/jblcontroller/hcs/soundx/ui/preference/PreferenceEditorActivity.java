package jblcontroller.hcs.soundx.ui.preference;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jbl.stc.com.activity.BaseActivity;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;
import jblcontroller.hcs.soundx.ui.profile.ProfileActivity;

import static jblcontroller.hcs.soundx.utils.AppConstants.EXPERIENCE_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.GENDER_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.YEAR_KEY;

public class PreferenceEditorActivity extends BaseActivity {

    @BindView(R.id.fragment_container)
    FrameLayout frameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            int year = getIntent().getIntExtra(YEAR_KEY, 0);
            int gender = getIntent().getIntExtra(GENDER_KEY, 0);
            int experience = getIntent().getIntExtra(EXPERIENCE_KEY, 0);
            Bundle data = new Bundle();
            data.putInt(YEAR_KEY, year);
            data.putInt(GENDER_KEY, gender);
            data.putInt(EXPERIENCE_KEY, experience);
            SoundXSharedPreferences.setListeningExp(this,experience);
            SoundXSharedPreferences.setGender(this,gender);
            SoundXSharedPreferences.setYob(this,year);
            PreferenceFragment fragment = new PreferenceFragment();
            fragment.setArguments(data);

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();

        }


//        mDone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PreferenceEditorActivity.this, DashboardDemo.class);
//                PreferenceEditorActivity.this.startActivity(intent);
//                finish();
//            }
//        });
//
//        mBackBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }



}
