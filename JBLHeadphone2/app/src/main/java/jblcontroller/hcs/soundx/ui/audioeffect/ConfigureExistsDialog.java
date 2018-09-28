package jblcontroller.hcs.soundx.ui.audioeffect;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.ui.dashboard.DashboardDemo;
import jblcontroller.hcs.soundx.ui.noise.NoiseMeasureActivity;

import static jblcontroller.hcs.soundx.utils.AppConstants.BASS_KEY;
import static jblcontroller.hcs.soundx.utils.AppConstants.IS_CONFIGURATION_FOUND;
import static jblcontroller.hcs.soundx.utils.AppConstants.TREBLE_KEY;

public class ConfigureExistsDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    @BindView(R.id.proceed)
    RelativeLayout mSetupBtn;

    @BindView(R.id.cancel)
    RelativeLayout mCancelBtn;

    private int mPreferredBass;
    private int mPreferredTreble;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.message)
    TextView mMessage;

    @BindView(R.id.setup_txt)
    TextView mSetupText;

    @BindView(R.id.use_old_data_txt)
    TextView mCancelText;

    AudioEffectActivity audioEffectActivity;

    public ConfigureExistsDialog() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Transparent theme
        setStyle(STYLE_NO_TITLE, R.style.Theme_Transparent);


        if (getArguments() != null) {
            mPreferredBass = getArguments().getInt(BASS_KEY);
            mPreferredTreble = getArguments().getInt(TREBLE_KEY);
        }

        audioEffectActivity = (AudioEffectActivity) getActivity();


    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.BottomDialogStyle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.custom_bottom_sheet, container, false);
        ButterKnife.bind(this, view);

        mTitle.setTypeface(audioEffectActivity.getBoldFont());
        mMessage.setTypeface(audioEffectActivity.getRegularFont());
        mSetupText.setTypeface(audioEffectActivity.getBoldFont());
        mCancelText.setTypeface(audioEffectActivity.getBoldFont());

        mCancelBtn.setOnClickListener(this);
        mSetupBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.proceed) {
//            dismiss();
            launchNoiseCheckActivity();


        } else if (v.getId() == R.id.cancel) {
//            dismiss();
            launchDashBord();

        }
    }

    private void launchDashBord() {
        Intent intent = new Intent(getActivity(), DashboardDemo.class);
        intent.putExtra(BASS_KEY, mPreferredBass);
        intent.putExtra(TREBLE_KEY, mPreferredTreble);
        intent.putExtra(IS_CONFIGURATION_FOUND, true);
        startActivity(intent);
        getActivity().finish();
    }

    private void launchNoiseCheckActivity() {
        Intent intent = new Intent(getActivity(), NoiseMeasureActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}