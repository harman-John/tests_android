package jbl.stc.com.fragment;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.entity.FirmwareModel;

public class OTAFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = OTAFragment.class.getSimpleName();
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
    public void setIsUpdateAvailable(boolean isUpdateAvailable, CopyOnWriteArrayList<FirmwareModel> fwList) {
        if (getActivity()== null){
            Log.e(TAG,"Activity is null");
            return;
        }

        if (!isAdded()){
            Log.e(TAG,"This fragment is not added");
            return;
        }
    }
}
