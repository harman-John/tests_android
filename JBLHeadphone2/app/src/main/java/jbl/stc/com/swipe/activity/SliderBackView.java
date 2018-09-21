package jbl.stc.com.swipe.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class SliderBackView extends SwipeBackVg {
    private static final String TAG = "SliderBackView";

    public SliderBackView(@NonNull Context context) {
        this(context, null);
    }

    public SliderBackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderBackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSwipeBackListener(defaultSwipeBackListener);
    }

    @Override
    public void setDirectionMode(int direction) {
        super.setDirectionMode(direction);
    }

    private OnSwipeBackListener defaultSwipeBackListener = new OnSwipeBackListener() {
        @Override
        public void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor) {
            invalidate();
            SlideUtil.onPanelSlide(swipeBackFraction);
        }

        @Override
        public void onViewSwipeFinished(View mView, boolean isEnd) {
            if (isEnd) {
                finish();
            }
            SlideUtil.onPanelReset();
        }
    };
}
