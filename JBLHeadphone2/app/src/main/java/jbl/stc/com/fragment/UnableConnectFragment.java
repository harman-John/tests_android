package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;

public class UnableConnectFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = UnableConnectFragment.class.getSimpleName();
    private View view;
    private ImageView imageViewDeviceIcon;
    private TextView textViewDeviceName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_unable_connect,
                container, false);
        view.findViewById(R.id.image_view_unable_white_menu).setOnClickListener(this);
        imageViewDeviceIcon = view.findViewById(R.id.image_view_unable_device_icon);
        textViewDeviceName = view.findViewById(R.id.text_view_unable_device_name);
        String deviceModelName = getArguments().getString(JBLConstant.DEVICE_MODEL_NAME);
        if (deviceModelName != null){
            switch (deviceModelName){
                case JBLConstant.DEVICE_LIVE_650BTNC:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_650BTNC);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_700_icon));
                    break;
                }
                case JBLConstant.DEVICE_LIVE_400BT:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_400BT);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_700_icon));
                    break;
                }
                case JBLConstant.DEVICE_LIVE_500BT:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_500BT);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_700_icon));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_750NC:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_750NC);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_750nc_icon));
                    break;
                }
                case JBLConstant.DEVICE_REFLECT_AWARE:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_REFLECT_AWARE);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.reflect_aware_icon));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_150NC:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_150NC);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_150nc_icon));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_700:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_700);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_700_icon));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_100:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_100);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_100_icon));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_300:{
                    textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_300);
                    imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.mipmap.everest_elite_300_icon));
                    break;
                }
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_unable_white_menu:{
                switchFragment(new InfoFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }

    }
}
