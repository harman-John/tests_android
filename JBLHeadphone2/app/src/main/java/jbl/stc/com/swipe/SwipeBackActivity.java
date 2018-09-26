package jbl.stc.com.swipe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.ViewDragHelper;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.logger.Logger;
import jbl.stc.com.lifecycle.ActivityLifecycleMgr;


public class SwipeBackActivity extends FragmentActivity {
    private SwipeBackLayout mSwipeBackLayout;
    private final static String TAG = SwipeBackActivity.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        mSwipeBackLayout = new SwipeBackLayout(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSwipeBackLayout.setLayoutParams(params);
        mSwipeBackLayout.setBackgroundColor(Color.TRANSPARENT);
        mSwipeBackLayout.setEdgeOrientation(SwipeBackLayout.EDGE_RIGHT);
        mSwipeBackLayout.attachToActivity(this);
        mSwipeBackLayout.setEdgeLevel(SwipeBackLayout.EdgeLevel.MAX);
        mSwipeBackLayout.setEnableGesture(true);
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.OnSwipeListener() {
            @Override
            public void onDragStateChange(int state) {
                if (state == ViewDragHelper.STATE_SETTLING){
                    Activity activity = ActivityLifecycleMgr.getInstance().getPenultimateActivity();
                    if (activity != null && !activity.isFinishing()) {
                        View decorView = activity.getWindow().getDecorView();
                        decorView.setTranslationX(0);
                    }
                }
            }

            @Override
            public void onEdgeTouch(int oritentationEdgeFlag) {

            }

            @Override
            public void onDragScrolled(float scrollPercent) {

                Logger.i(TAG,"scrollPercent = "+scrollPercent);
                Activity activity = ActivityLifecycleMgr.getInstance().getPenultimateActivity();
                if (activity != null && !activity.isFinishing()) {
                    View decorView = activity.getWindow().getDecorView();
                    if (scrollPercent >= 1) {
                        decorView.setTranslationX(0);
                    } else {
                        decorView.setTranslationX((decorView.getMeasuredWidth() / 1.0f) * (1 - scrollPercent));
                    }
                }
            }
        });
    }

}
