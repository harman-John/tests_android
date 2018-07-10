package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;

public class ProductsListFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = ProductsListFragment.class.getSimpleName();
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products_list,
                container, false);
        view.findViewById(R.id.image_view_pro_white_menu).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_650).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_500).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_400).setOnClickListener(this);

        view.findViewById(R.id.image_view_pro_everest_750).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_reflect_aware).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_150).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_700).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_100).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_300).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()){
            case R.id.image_view_pro_white_menu:{
                switchFragment(new InfoFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                return;
            }
            case R.id.image_view_pro_live_650:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_650BTNC);
                break;
            }
            case R.id.image_view_pro_live_500:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_500BT);
                break;
            }
            case R.id.image_view_pro_live_400:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_400BT);
                break;
            }

            case R.id.image_view_pro_everest_750:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_750NC);
                break;
            }
            case R.id.image_view_pro_reflect_aware:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_REFLECT_AWARE);
                break;
            }
            case R.id.image_view_pro_everest_150:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_150NC);
                break;
            }
            case R.id.image_view_pro_everest_700:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_700);
                break;
            }
            case R.id.image_view_pro_everest_100:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_100);
                break;
            }
            case R.id.image_view_pro_everest_300:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_300);
                break;
            }
        }
        UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
        unableConnectFragment.setArguments(bundle);
        switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
    }
}
