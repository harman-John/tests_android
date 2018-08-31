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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;

public class HowToPairFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = HowToPairFragment.class.getSimpleName();
    private View view;
    private TextView textViewLink;
    private ImageView imageView;
    private TextView textViewTips;
    private LinearLayout linearLayoutVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_how_to_pair,
                container, false);
        imageView = view.findViewById(R.id.image_view_htp);
        view.findViewById(R.id.text_view_how_to_pair_next).setOnClickListener(this);
        linearLayoutVideo = view.findViewById(R.id.linear_layout_video_in_htp);
        textViewTips = view.findViewById(R.id.text_view_tips_one_in_htp);
        view.findViewById(R.id.image_view_how_to_pair_back).setOnClickListener(this);
        view.findViewById(R.id.image_view_how_to_pair_close).setOnClickListener(this);

        textViewLink = view.findViewById(R.id.text_view_how_to_pair_link);
        textViewLink.getPaint().setUnderlineText(true);
        textViewLink.setOnClickListener(this);
        initImageView();
        return view;
    }

    void initImageView() {
        String deviceModelName = getArguments().getString(JBLConstant.DEVICE_MODEL_NAME);
        if (deviceModelName == null) {
            Logger.d(TAG,"init image view, device mode name is null");
            return;
        }
        if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_650BTNC)) {
            textViewTips.setText(getString(R.string.how_to_pair_650_500_400));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_650));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_400BT)) {
            textViewTips.setText(getString(R.string.how_to_pair_650_500_400));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_live_500bt_400bt));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_LIVE_500BT)) {
            textViewTips.setText(getString(R.string.how_to_pair_650_500_400));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_live_500bt_400bt));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
            textViewTips.setText(getString(R.string.how_to_pair_750_150));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_750));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
            textViewTips.setText(getString(R.string.how_to_pair_750_150));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_150nc));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
            textViewTips.setText(getString(R.string.how_to_pair_700_300_100));
            linearLayoutVideo.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_700_300));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
            textViewTips.setText(getString(R.string.how_to_pair_700_300_100));
            linearLayoutVideo.setVisibility(View.GONE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_100));
        } else if (deviceModelName.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
            textViewTips.setText(getString(R.string.how_to_pair_700_300_100));
            linearLayoutVideo.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.outline_elite_700_300));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_how_to_pair_next: {
                DashboardActivity.getDashboardActivity().switchFragment(new HowToPairNextFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_how_to_pair_back:
                getActivity().onBackPressed();
                break;
            case R.id.image_view_how_to_pair_close: {
                removeAllFragment();
                break;
            }
            case R.id.text_view_how_to_pair_link: {
                DashboardActivity.getDashboardActivity().switchFragment(new WebViewFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }
}
