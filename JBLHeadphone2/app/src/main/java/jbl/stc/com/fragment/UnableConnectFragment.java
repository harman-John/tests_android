package jbl.stc.com.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.utils.BreathLight;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.ImageViewLR;
import jbl.stc.com.view.RelativeLayoutImage;

public class UnableConnectFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = UnableConnectFragment.class.getSimpleName();
    private LinearLayout linearLayoutTips;
    private RelativeLayoutImage relativeLayoutDeviceIcon;
    private LinearLayout linear_layout_unable_device;
    String deviceModelName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unable_connect,
                container, false);
        view.findViewById(R.id.image_view_unable_back).setOnClickListener(this);
        ImageViewLR imageViewDeviceIcon = view.findViewById(R.id.image_view_unable_device_icon);
        relativeLayoutDeviceIcon = view.findViewById(R.id.relative_layout_unable_breathing_lamp);
        linear_layout_unable_device = view.findViewById(R.id.linear_layout_unable_device);
        setDeviceImageHeight();
        linearLayoutTips = view.findViewById(R.id.linear_layout_unable_tips);
        linearLayoutTips.setVisibility(View.INVISIBLE);
        relativeLayoutDeviceIcon.setOnClickListener(this);
        TextView textViewDeviceName = view.findViewById(R.id.text_view_unable_device_name);
        TextView textViewTipsTwo = view.findViewById(R.id.text_view_unable_advice_two);
        TextView textViewTipsFour = view.findViewById(R.id.text_view_unable_advice_four);
        TextView textViewTipsFive = view.findViewById(R.id.text_view_unable_advice_five);
        deviceModelName = getArguments().getString(JBLConstant.DEVICE_MODEL_NAME);

        if (deviceModelName != null) {
            if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_650BTNC)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_650BTNC);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_400BT)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_400BT);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_500BT)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_LIVE_500BT);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_750NC);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_750nc_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_REFLECT_AWARE);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.reflect_aware_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_150NC);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_150nc_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_700);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_100);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_100_icon));
            } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
                textViewDeviceName.setText(JBLConstant.DEVICE_EVEREST_ELITE_300);
                imageViewDeviceIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_300_icon));
            }
        }
        if (textViewDeviceName.getText().equals(JBLConstant.DEVICE_REFLECT_AWARE)) {
            textViewTipsTwo.setText(R.string.advice_reflect_aware);
            textViewTipsFour.setVisibility(View.GONE);
            textViewTipsFive.setVisibility(View.GONE);
            view.findViewById(R.id.linear_layout_three_in_fuc).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.linear_layout_three_in_fuc).setVisibility(View.VISIBLE);
            TextView textViewTipsThree = view.findViewById(R.id.text_view_unable_advice_three);
            textViewTipsThree.getPaint().setUnderlineText(true);
            textViewTipsThree.setOnClickListener(this);
        }

        breathLight = new BreathLight(getActivity(),
                relativeLayoutDeviceIcon,
                R.anim.breathing_lamp_fade_in,
                R.anim.breathing_lamp_fade_out);

        breathLight.startBreathing(0);

        return view;
    }

    private void setDeviceImageHeight() {
        int height = UiUtils.getDashboardDeviceImageHeight(mContext);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) relativeLayoutDeviceIcon.getLayoutParams();
        if (params != null) {
            params.height = height;
            params.width = height;
            relativeLayoutDeviceIcon.setLayoutParams(params);
        }
        int marginTop = UiUtils.getDeviceNameMarginTop(getActivity());
        linear_layout_unable_device.setPadding(0, marginTop, 0, UiUtils.dip2px(getActivity(), 20));
    }

    private BreathLight breathLight;
    private Handler handler = new Handler();

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayoutTips.setVisibility(View.VISIBLE);
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        breathLight.stopBreathing();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_unable_back: {
                removeAllFragment();
                break;
            }
            case R.id.relative_layout_unable_breathing_lamp: {

                break;
            }
            case R.id.text_view_unable_advice_three: {
                HowToPairFragment howToPairFragment = new HowToPairFragment();
                Bundle bundle = new Bundle();
                bundle.putString(JBLConstant.DEVICE_MODEL_NAME, deviceModelName);
                howToPairFragment.setArguments(bundle);
                DashboardActivity.getDashboardActivity().switchFragment(howToPairFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }

    }

}
