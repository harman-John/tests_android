package jbl.stc.com.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * MyGridLayoutManager
 * Created by ludm1 on 2018/3/30.
 */

public class MyGridLayoutManager extends GridLayoutManager {
    private float MILLISECONDS_PER_INCH = 0.03f;
    private Context context;

    public MyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        this.context = context;
        setSpeedSlow();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return MyGridLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }

                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.density;
                    }

                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public void setSpeedSlow() {
        MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.3f;
    }

    public void setSpeedFast() {
        MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.03f;
    }
}
