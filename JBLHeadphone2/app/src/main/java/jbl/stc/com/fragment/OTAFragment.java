package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;

public class OTAFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = OTAFragment.class.getSimpleName();
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ota,
                container, false);
        view.setOnClickListener(this);
        view.findViewById(R.id.image_view_ota_back).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_ota_back:{
                getActivity().onBackPressed();
                break;
            }
        }

    }
}
