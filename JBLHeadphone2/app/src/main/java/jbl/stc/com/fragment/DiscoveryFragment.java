package jbl.stc.com.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.view.JblCircleView;

public class DiscoveryFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = DiscoveryFragment.class.getSimpleName();
    private View view;
    private final static int MSG_SHOW_PRODUCT_LIST_FRAGMENT = 0;

    private DHandler dHandler = new DHandler(Looper.getMainLooper());
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_discovery,
                container, false);

        relativeLayoutAnimation = view.findViewById(R.id.relative_layout_discovery_animation);
        view.findViewById(R.id.image_view_discovery_back).setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showProductLIst();
    }

    @Override
    public void onPause() {
        super.onPause();
        dHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_discovery_back:{
                getActivity().onBackPressed();
                break;
            }
        }

    }

    private void showProductLIst() {
        startCircle();
        dHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
        dHandler.sendEmptyMessageDelayed(MSG_SHOW_PRODUCT_LIST_FRAGMENT, 5000);
    }

    private JblCircleView jblCircleView;
    private RelativeLayout relativeLayoutAnimation;
    private void startCircle() {
        relativeLayoutAnimation.setVisibility(View.VISIBLE);
        if (jblCircleView == null) {
            jblCircleView = view.findViewById(R.id.jbl_circle_view_dashboard);
            jblCircleView.setVisibility(View.VISIBLE);
            jblCircleView.circle();
        }
    }

    private void stopCircle() {
        if (jblCircleView != null) {
            jblCircleView.stop();
            jblCircleView.setVisibility(View.GONE);
            jblCircleView = null;
        }
        relativeLayoutAnimation.setVisibility(View.GONE);
    }

    private class DHandler extends Handler {

        DHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_PRODUCT_LIST_FRAGMENT: {
                    dHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
                    Log.i(TAG, "MSG_SHOW_PRODUCT_LIST_FRAGMENT");
                    Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new ProductsListFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else if (!(fr instanceof ProductsListFragment)) {
                        switchFragment(new ProductsListFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                    stopCircle();
                    break;
                }
            }
        }
    }
}
