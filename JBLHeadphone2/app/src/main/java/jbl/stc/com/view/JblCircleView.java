package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;

public class JblCircleView extends View {


    private int mColor = getResources().getColor(R.color.white);

    private int mImageRadius = 100;

    private int mWidth = 600;

    private Integer mMaxRadius = 300;

    private boolean mIsWave = true;

    private List<Integer> mAlphas = new ArrayList<>();

    private List<Integer> mRadius = new ArrayList<>();
    private Paint mPaint;
    private Paint mPaintCircle;

    private boolean isFill = false;

    private Canvas mCanvas;
    private final static String TAG = JblCircleView.class.getSimpleName();

    public JblCircleView(Context context) {
        this(context, null);
    }

    public JblCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JblCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Logger.i(TAG, "JblCircleView");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView, defStyleAttr, 0);
        mColor = a.getColor(R.styleable.WaveView_wave_color, mColor);
        mWidth = a.getInt(R.styleable.WaveView_wave_width, mWidth);
        mImageRadius = a.getInt(R.styleable.WaveView_wave_coreImageRadius, mImageRadius);
        a.recycle();
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Logger.i(TAG, "onWindowFocusChanged");

        mMaxRadius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
        invalidate();
    }

    @Override
    public void invalidate() {
        if (hasWindowFocus()) {
//            Logger.i(TAG,"invalidate");
            super.invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        mCanvas = canvas;

        doAnimation();
    }

    int des = 0;
    public void doAnimation() {
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);

        mPaintCircle.setColor(mColor);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setStrokeWidth(18);
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, 300, mPaintCircle);
        for (int i = 0; i < mAlphas.size(); i++) {

            Integer alpha = mAlphas.get(i);
            mPaint.setAlpha(alpha);

            Integer radius = mRadius.get(i);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius, mPaint);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius +100, mPaint);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius +200, mPaint);

            if (alpha > 0 && mImageRadius + radius < mMaxRadius) {
                alpha = (int) (255.0F * (1.0F - (mImageRadius + radius) * 1.0f / mMaxRadius));
                Logger.i(TAG, "mAlphas size =" + mAlphas.size()
                        + ",i = " + i
                        + ",alpha = " + alpha
                        + ",mImageRadius = " + mImageRadius
                        + ",radius = " + radius
                        + ",des = " + des
                        + ",radius size = " + mRadius.get(mRadius.size() - 1)
                        + ",mWidth = " + mWidth);
                if (alpha > 148/2) {
                    des = des +1;
                    mAlphas.set(i, des );
                }else{
                    des = des -1;
                    mAlphas.set(i, des);
                }
                mRadius.set(i, radius + 3);
            } else if (alpha < 0 && mImageRadius + radius > mMaxRadius) {

                Logger.i(TAG, "remove i = " + i + ",alpha = " + alpha);
                mRadius.remove(i);
                mAlphas.remove(i);
            }

        }

        if (mRadius.get(mRadius.size() - 1) >= mWidth) {
            des = 1;
            mAlphas.clear();
            mRadius.clear();
            mAlphas.add(1);
            mRadius.add(200);
            mPaint.reset();
        }

//        Logger.i(TAG,"mIsWave = "+mIsWave);
        if (mIsWave) {
            invalidate();
        }
    }

    public void circle() {
        Logger.i(TAG, "circle");
        mIsWave = true;
        mAlphas.add(1);
        mRadius.add(200);
        mMaxRadius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
        invalidate();
    }

    public void stop() {
        Logger.i(TAG, "stop");
        mPaint.reset();
        mAlphas.clear();
        mRadius.clear();
        mMaxRadius = 300;
        mIsWave = false;
    }


    public boolean isWave() {
        return mIsWave;
    }


    public void setColor(int colorId) {
        mColor = colorId;
    }


    public void setWidth(int width) {
        mWidth = width;
    }

    public boolean isFill() {
        return isFill;
    }

    public void setFill(boolean fill) {
        isFill = fill;
    }


}
