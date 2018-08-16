package jbl.stc.com.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jbl.stc.com.logger.Logger;

/**
 * AppImageView
 * Created by darren.lu on 2017/8/26.
 */

public class AppImageView extends AppCompatImageView {
    private static final String TAG = AppImageView.class.getSimpleName();

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
        //Logger.d("AppImageView", "onTouchEvent " + event.getAction());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha((float) 0.5);
                break;
            case MotionEvent.ACTION_MOVE:
                setAlpha((float) 0.5);
                break;
            case MotionEvent.ACTION_UP:
                setAlpha((float) 1);
                break;
            case MotionEvent.ACTION_CANCEL:
                setAlpha((float) 1);
                break;
        }
        return super.onTouchEvent(event);
    }
}
