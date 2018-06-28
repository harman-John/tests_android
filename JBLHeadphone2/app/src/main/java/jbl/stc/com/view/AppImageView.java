package jbl.stc.com.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * AppImageView
 * Created by darren.lu on 2017/8/26.
 */

public class AppImageView extends AppCompatImageView {
    public AppImageView(Context context) {
        super(context);
    }

    public AppImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("AppImageView", "onTouchEvent " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                setAlpha(0.5f);
                break;
            case MotionEvent.ACTION_UP:
                setAlpha(1f);
                break;
            case MotionEvent.ACTION_CANCEL:
                setAlpha(1f);
                break;
        }
        return super.onTouchEvent(event);
    }
}
