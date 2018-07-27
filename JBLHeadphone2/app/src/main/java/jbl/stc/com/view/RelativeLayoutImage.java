package jbl.stc.com.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeLayoutImage extends RelativeLayout {
    public RelativeLayoutImage(Context context) {
        super(context);
    }

    public RelativeLayoutImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RelativeLayoutImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mWidth = getMeasuredWidth();
        int mHeight = getMeasuredHeight();

        if (mWidth > mHeight){
            mWidth = mHeight;
        }else if (mWidth < mHeight){
            mHeight = mWidth;
        }

        setMeasuredDimension(mWidth, mHeight);
    }
}
