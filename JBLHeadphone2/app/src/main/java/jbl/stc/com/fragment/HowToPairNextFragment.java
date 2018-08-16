package jbl.stc.com.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;

public class HowToPairNextFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = HowToPairNextFragment.class.getSimpleName();
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_how_to_pair_next,
                container, false);
        view.findViewById(R.id.text_view_how_to_pair_next_go_bt_settings).setOnClickListener(this);
        view.findViewById(R.id.image_view_how_to_pair_next_back).setOnClickListener(this);
        view.findViewById(R.id.image_view_how_to_pair_next_close).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_how_to_pair_next_go_bt_settings:{
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            }
            case R.id.image_view_how_to_pair_next_back:{
                getActivity().onBackPressed();
                break;
            }
            case R.id.image_view_how_to_pair_next_close:{
                removeAllFragment();
                break;
            }
        }
    }
}
