package jbl.stc.com.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ImageViewLR extends AppCompatImageView {
    public ImageViewLR(Context context) {
        super(context);
    }

    public ImageViewLR(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewLR(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
