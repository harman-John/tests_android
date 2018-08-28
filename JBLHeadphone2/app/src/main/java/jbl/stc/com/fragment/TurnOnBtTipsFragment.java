package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.view.BlurringView;
import jbl.stc.com.view.BtTipPopupWindow;

public class TurnOnBtTipsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = TurnOnBtTipsFragment.class.getSimpleName();
    private View view;
    private TextView tv_stillsee;
    private BlurringView mBlurView;
    private View bluredView;
    private BtTipPopupWindow btTipPopupWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_turn_on_bt,
                container, false);
        //tv_stillsee = view.findViewById(R.id.tv_stillsee);
        //tv_stillsee.setOnClickListener(this);
        bluredView = view.findViewById(R.id.relative_layout_splash_turn_on_bt);
        mBlurView = view.findViewById(R.id.view_blur);
        btTipPopupWindow = new BtTipPopupWindow(getActivity());
        btTipPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mBlurView != null) {
                    mBlurView.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        btTipPopupWindow.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.tv_stillsee: {

                mBlurView.setBlurredView(bluredView);
                mBlurView.invalidate();
                mBlurView.setVisibility(View.VISIBLE);
                mBlurView.setAlpha(0f);
                mBlurView.animate().alpha(0.5f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mBlurView.setVisibility(View.VISIBLE);
                        //OR
                        mBlurView.setAlpha(0.5f);
                    }
                });
                btTipPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
                break;
            }*/
        }

    }
}
