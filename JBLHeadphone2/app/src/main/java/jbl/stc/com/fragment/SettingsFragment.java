package jbl.stc.com.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;
import jbl.stc.com.activity.Calibration;
import jbl.stc.com.constant.JBLConstant;

public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings,
                container, false);
        view.findViewById(R.id.text_firmware).setOnClickListener(this);
        view.findViewById(R.id.text_true_note).setOnClickListener(this);
        view.findViewById(R.id.image_view_settings_back).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_firmware:{
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_settings_back:{
                getActivity().onBackPressed();
                break;

            }
            case R.id.text_true_note: {
                Log.d(TAG, "truenote clicked");
                Intent intent = new Intent(getActivity(), Calibration.class);
                intent.putExtra(Calibration.TAG, GlobalCalibration.class.getSimpleName());
                startActivity(intent);
                break;
            }
            case R.id.text_smartButton:{
                switchFragment(new SmartButtonFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }

        }

    }
}
