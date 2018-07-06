package jbl.stc.com.fragment;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jbl.stc.com.R;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;

public class AutoOffTimeFragment extends BaseFragment implements View.OnClickListener {

    private ImageView iv_check_five, iv_check_ten, iv_check_thirty, iv_check_never;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_auto_off_timer,
                container, false);
        iv_check_five = (ImageView) view.findViewById(R.id.iv_check_five);
        iv_check_ten = (ImageView) view.findViewById(R.id.iv_check_ten);
        iv_check_thirty = (ImageView) view.findViewById(R.id.iv_check_thirty);
        iv_check_never = (ImageView) view.findViewById(R.id.iv_check_never);
        view.findViewById(R.id.image_view_back).setOnClickListener(this);
        view.findViewById(R.id.fiveminutesLayout).setOnClickListener(this);
        view.findViewById(R.id.tenminutesLayout).setOnClickListener(this);
        view.findViewById(R.id.thirtyminutesLayout).setOnClickListener(this);
        view.findViewById(R.id.neverLayout).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_back:
                getActivity().onBackPressed();
                break;
            case R.id.fiveminutesLayout:
                PreferenceUtils.setString(PreferenceKeys.AUTOOFFTIMER,getContext().getString(R.string.five_minute),getActivity());
                iv_check_five.setVisibility(View.VISIBLE);
                iv_check_ten.setVisibility(View.GONE);
                iv_check_thirty.setVisibility(View.GONE);
                iv_check_never.setVisibility(View.GONE);
                iv_check_five.setImageResource(R.mipmap.selected_orange);
                break;
            case R.id.tenminutesLayout:
                PreferenceUtils.setString(PreferenceKeys.AUTOOFFTIMER,getContext().getString(R.string.ten_minute),getActivity());
                iv_check_five.setVisibility(View.GONE);
                iv_check_ten.setVisibility(View.VISIBLE);
                iv_check_thirty.setVisibility(View.GONE);
                iv_check_never.setVisibility(View.GONE);
                iv_check_ten.setImageResource(R.mipmap.selected_orange);
                break;
            case R.id.thirtyminutesLayout:
                PreferenceUtils.setString(PreferenceKeys.AUTOOFFTIMER,getContext().getString(R.string.thirty_minute),getActivity());
                iv_check_five.setVisibility(View.GONE);
                iv_check_ten.setVisibility(View.GONE);
                iv_check_thirty.setVisibility(View.VISIBLE);
                iv_check_never.setVisibility(View.GONE);
                iv_check_thirty.setImageResource(R.mipmap.selected_orange);
                break;
            case R.id.neverLayout:
                PreferenceUtils.setString(PreferenceKeys.AUTOOFFTIMER,getContext().getString(R.string.never),getActivity());
                iv_check_five.setVisibility(View.GONE);
                iv_check_ten.setVisibility(View.GONE);
                iv_check_thirty.setVisibility(View.GONE);
                iv_check_never.setVisibility(View.VISIBLE);
                iv_check_never.setImageResource(R.mipmap.selected_orange);
                break;


        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
