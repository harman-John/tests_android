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

public class HowToPairFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = HowToPairFragment.class.getSimpleName();
    private View view;
    private TextView textViewLink;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_how_to_pair,
                container, false);
        view.findViewById(R.id.text_view_how_to_pair_next).setOnClickListener(this);
        textViewLink = view.findViewById(R.id.text_view_how_to_pair_link);
        SpannableString spannableString = new SpannableString(getString(R.string.how_to_pair_tips_two));
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View arg0) {
                DashboardActivity.getDashboardActivity().switchFragment(new WebViewFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.black));
                ds.setUnderlineText(true);
                ds.clearShadowLayer();
            }

        }, 0, textViewLink.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewLink.setText(spannableString);
        textViewLink.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
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
            case R.id.image_view_how_to_pair_close:{
                getActivity().onBackPressed();
                break;
            }
        }
    }
}
