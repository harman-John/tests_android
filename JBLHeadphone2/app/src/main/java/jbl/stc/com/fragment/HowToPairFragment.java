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
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;

public class HowToPairFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = HowToPairFragment.class.getSimpleName();
    private View view;
    private TextView textViewLink;
    private ImageView imageView;
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
        initImageView();
        view.findViewById(R.id.text_view_how_to_pair_next).setOnClickListener(this);
        textViewLink = view.findViewById(R.id.text_view_how_to_pair_link);
        view.findViewById(R.id.image_view_how_to_pair_back).setOnClickListener(this);
        view.findViewById(R.id.image_view_how_to_pair_close).setOnClickListener(this);
        SpannableString spannableString = new SpannableString(getString(R.string.how_to_pair_tips_two));
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View arg0) {
                DashboardActivity.getDashboardActivity().switchFragment(new WebViewFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.orange_dark_FF5201));
                ds.setUnderlineText(true);
                ds.clearShadowLayer();
            }

        }, 0, textViewLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewLink.setText(spannableString);
        textViewLink.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    void initImageView(){
        String deviceModelName = getArguments().getString(JBLConstant.DEVICE_MODEL_NAME);
        if (deviceModelName != null) {
            switch (deviceModelName) {
                case JBLConstant.DEVICE_LIVE_650BTNC: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.live_650));
                    break;
                }
                case JBLConstant.DEVICE_LIVE_400BT: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.live_400));
                    break;
                }
                case JBLConstant.DEVICE_LIVE_500BT: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.live_400));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_750NC: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.elite_750));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_150NC: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.elite_150nc));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_700: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.elite_700));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_100: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.elite_100));
                    break;
                }
                case JBLConstant.DEVICE_EVEREST_ELITE_300: {
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.elite_300));
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_how_to_pair_next:{
                DashboardActivity.getDashboardActivity().switchFragment(new HowToPairNextFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_how_to_pair_back:
                getActivity().onBackPressed();
                break;
            case R.id.image_view_how_to_pair_close:{
                removeAllFragment();
                break;
            }
        }
    }
}
