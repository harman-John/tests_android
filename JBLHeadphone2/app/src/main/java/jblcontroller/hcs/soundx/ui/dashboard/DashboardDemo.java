package jblcontroller.hcs.soundx.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;

import jbl.stc.com.R;
import jbl.stc.com.activity.BaseActivity;

import static jblcontroller.hcs.soundx.utils.AppConstants.BASS_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_CONFIGURATION_FOUND;
import static jblcontroller.hcs.soundx.utils.AppConstants.TREBLE_KEY;

public class DashboardDemo extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        DashBoardFragment fragment = new DashBoardFragment();
        if (getIntent() != null) {
            boolean isConfigurationFound = getIntent().getBooleanExtra(IS_CONFIGURATION_FOUND, false);
            if (isConfigurationFound) {


                int bass = getIntent().getIntExtra(BASS_KEY, 0);
                int treble = getIntent().getIntExtra(TREBLE_KEY, 0);

                Bundle data = new Bundle();
                data.putInt(BASS_KEY, bass);
                data.putInt(TREBLE_KEY, treble);
                data.putBoolean(IS_CONFIGURATION_FOUND, true);
                fragment.setArguments(data);

            }


            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

}
