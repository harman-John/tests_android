package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import jbl.stc.com.utils.StatusBarUtil;

public class ProductsListFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = ProductsListFragment.class.getSimpleName();
    private View view;
    private TextView textViewTipsOne;
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
        textViewTipsOne = view.findViewById(R.id.text_view_product_tips_one);
        SpannableString spannableString = new SpannableString(getString(R.string.my_products_tips_one));
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View arg0) {
                DashboardActivity.getDashboardActivity().switchFragment(new HowToPairNextFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.black));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
                ds.clearShadowLayer();
            }

        }, 67, textViewTipsOne.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewTipsOne.setText(spannableString);
        textViewTipsOne.setMovementMethod(LinkMovementMethod.getInstance());
        view.findViewById(R.id.image_view_pro_white_back).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_650).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_500).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_live_400).setOnClickListener(this);

        view.findViewById(R.id.image_view_pro_everest_750).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_reflect_aware).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_150).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_700).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_100).setOnClickListener(this);
        view.findViewById(R.id.image_view_pro_everest_300).setOnClickListener(this);

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
            case R.id.image_view_pro_white_back:{
                getActivity().onBackPressed();
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
