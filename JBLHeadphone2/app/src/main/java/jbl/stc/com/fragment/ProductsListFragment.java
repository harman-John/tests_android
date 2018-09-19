package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;

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
        Logger.d(TAG,"onCreateView");
        view = inflater.inflate(R.layout.fragment_products_list,
                container, false);
        TextView linkHowToPairTv = view.findViewById(R.id.text_view_product_tips_how_to_pair);
        linkHowToPairTv.setOnClickListener(this);
        linkHowToPairTv.getPaint().setUnderlineText(true);

        view.findViewById(R.id.image_view_close_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_400_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_500_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_650_in_pl).setOnClickListener(this);

        view.findViewById(R.id.image_view_100_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_150_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_300_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_700_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_750_in_pl).setOnClickListener(this);
        view.findViewById(R.id.image_view_reflect_in_pl).setOnClickListener(this);

//        StatusBarUtil.setColor(getActivity(), ContextCompat.getColor(getContext(),R.color.gray_dee2e6));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume");
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()){
            case R.id.image_view_close_in_pl:{
                removeAllFragment();
                return;
            }
            case R.id.image_view_400_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_400BT);
                break;
            }
            case R.id.image_view_500_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_500BT);
                break;
            }
            case R.id.image_view_650_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_LIVE_650BTNC);
                break;
            }

            case R.id.image_view_100_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_100);
                break;
            }
            case R.id.image_view_150_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_150NC);
                break;
            }
            case R.id.image_view_300_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_300);
                break;
            }
            case R.id.image_view_700_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_700);
                break;
            }
            case R.id.image_view_750_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_EVEREST_ELITE_750NC);
                break;
            }
            case R.id.image_view_reflect_in_pl:{
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME,JBLConstant.DEVICE_REFLECT_AWARE);
                break;
            }
            case R.id.text_view_product_tips_how_to_pair:{
                DashboardActivity.getDashboardActivity().switchFragment(new HowToPairNextFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                return;
            }
        }
        UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
        unableConnectFragment.setArguments(bundle);
        switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(TAG,"onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG,"onDestroy");
    }
}
