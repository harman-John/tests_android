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

public class JblCircleView extends View {


    private int mColor = getResources().getColor(R.color.white);

    private int mImageRadius = 100;

    private int mWidth = 578;

    private Integer mMaxRadius = 300;

    private boolean mIsWave = true;

    private List<Integer> mAlphas = new ArrayList<>();
    private List<Integer> mAlphas1 = new ArrayList<>();
    private List<Integer> mAlphas2 = new ArrayList<>();

    private List<Integer> mRadius = new ArrayList<>();
    private Paint mPaint;
    private Paint mPaint1;
    private Paint mPaint2;
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

        mPaint1 = new Paint();
        mPaint1.setAntiAlias(true);

        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);

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

        mPaint1.setColor(mColor);
        mPaint1.setStyle(Paint.Style.STROKE);
        mPaint1.setStrokeWidth(5);

        mPaint2.setColor(mColor);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(5);

        mPaintCircle.setColor(mColor);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setStrokeWidth(18);
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, 300, mPaintCircle);
        for (int i = 0; i < mAlphas.size(); i++) {

            Integer alpha = mAlphas.get(i);
            mPaint.setAlpha(alpha);
            Integer alpha1 = mAlphas1.get(i);
            mPaint1.setAlpha(alpha1);
            Integer alpha2 = mAlphas2.get(i);
            mPaint2.setAlpha(alpha2);

            Integer radius = mRadius.get(i);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius, mPaint);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius +180, mPaint1);

            mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mImageRadius + radius +320, mPaint2);

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
                if (alpha > 110) {
                    des = des +5;
                    if (des >0) {
                        mAlphas.set(i, des);
                    }
                    if (des -30 >0){
                        mAlphas1.set(i, des-30);
                    }
                    if (des -60 >0){
                        mAlphas2.set(i, des-60);
                    }
                }else{
                    des = des -2;
                    if (des >0) {
                        mAlphas.set(i, des);
                    }
                    if (des -30 >0){
                        mAlphas1.set(i, des-30);
                    }
                    if (des -60 >0){
                        mAlphas2.set(i, des-60);
                    }
                }
                mRadius.set(i, radius + 3);
            }

        }

        if (mRadius.get(mRadius.size() - 1) >= mWidth) {
            des = 1;
            mAlphas.clear();
            mRadius.clear();
            mAlphas.add(1);
            mAlphas1.add(1);
            mAlphas2.add(1);
            mRadius.add(200);
            mPaint.reset();
            mPaint1.reset();
            mPaint2.reset();
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
        mAlphas1.add(1);
        mAlphas2.add(1);
        mRadius.add(200);
        mMaxRadius = getWidth() > getHeight() ? getHeight() / 2 : getWidth() / 2;
        invalidate();
    }

    public void stop() {
        Logger.i(TAG, "stop");
        mPaint.reset();
        mPaint1.reset();
        mPaint2.reset();
        mAlphas.clear();
        mAlphas1.clear();
        mAlphas2.clear();
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
