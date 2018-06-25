package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;
import jbl.stc.com.legal.LegalApi;

public class InfoFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = InfoFragment.class.getSimpleName();
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info,
                container, false);
        view.findViewById(R.id.text_view_open_source_license).setOnClickListener(this);
        view.findViewById(R.id.text_view_eula).setOnClickListener(this);
        view.findViewById(R.id.text_view_harman_privacy_policy).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_open_source_license:{
                LegalApi.INSTANCE.showOpenSource(getActivity());
                break;
            }
            case R.id.text_view_eula:{
                LegalApi.INSTANCE.showEula(getActivity());
                break;
            }
            case R.id.text_view_harman_privacy_policy:{
                LegalApi.INSTANCE.showPrivacyPolicy(getActivity());
                break;
            }
        }

    }
}
