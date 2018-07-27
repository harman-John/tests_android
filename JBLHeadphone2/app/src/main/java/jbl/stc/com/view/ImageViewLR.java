package jbl.stc.com.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ImageViewLR extends ImageView {
    public ImageViewLR(Context context) {
        super(context);
    }

    public ImageViewLR(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewLR(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageViewLR(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
