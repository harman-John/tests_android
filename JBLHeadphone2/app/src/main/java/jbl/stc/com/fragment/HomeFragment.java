package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.listener.OnDialogListener;

public class HomeFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private View view;
    private CreateEqTipsDialog createEqTipsDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,
                container, false);
        view.findViewById(R.id.image_view_settings).setOnClickListener(this);
        view.findViewById(R.id.image_view_info).setOnClickListener(this);
        view.findViewById(R.id.image_view_ambient_aware);
        view.findViewById(R.id.deviceImageView);
        view.findViewById(R.id.eqSwitchLayout);
        view.findViewById(R.id.eqInfoLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.eqInfoLayout).setOnClickListener(this);
        view.findViewById(R.id.image_view_noise_cancel);
        view.findViewById(R.id.eqNameText);
        view.findViewById(R.id.titleEqText);
        view.findViewById(R.id.eqDividerView);
        view.findViewById(R.id.batteryProgressBar);
        view.findViewById(R.id.batteryLevelText);
        view.findViewById(R.id.text_view_ambient_aware);
        createEqTipsDialog = new CreateEqTipsDialog(getActivity());
        createEqTipsDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment(),JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
            }

            @Override
            public void onCancel() {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_ambient_aware:

                break;
            case R.id.eqInfoLayout: {
                switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_info:{
                switchFragment(new InfoFragment(),JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                break;
            }
            case R.id.image_view_settings:{
                switchFragment(new SettingsFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }
}
