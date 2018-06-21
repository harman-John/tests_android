package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.CircleModel;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.LogUtil;
import jbl.stc.com.utils.UiUtils;

/**
 * EqualizerView
 * <p>
 * Created by darren.lu on 08/09/2017.
 */
public class EqualizerShowView extends View {
    private static final String TAG = EqualizerShowView.class.getSimpleName();
    private Context mContext;
    private Paint mPointPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private Paint mLinePaint = new Paint();
    private Paint mCurvePaint = new Paint();

    private int mWidth, mHeight;
    private int[] mEqFreqArray = new int[]{32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
    //private int[] mHorizontalName = new int[]{20, 10, 0, -10, -20};
    private int[] mVerticalLineArray = new int[]{32, 250, 2000, 16000};

    private float[] mEqPointX, mEqPointY;

    // x、y轴坐标值
    private List<Float> pointX, pointY;
    private int curveColor;
    private Path curvePath = new Path();

    private float marginLeft = 0f;
    private float marginRight = 0f;
    private float marginTop;
    private float marginBottom;
    private float textMarginBottom;

    private int maxFrequency = mEqFreqArray[mEqFreqArray.length - 1];
    private int minFrequency = mEqFreqArray[0];

    private int minValue = -10;
    private int maxValue = 10;

    private List<CircleModel> controlCircles = new ArrayList<>();
    private List<CircleModel> allPointCircles = new ArrayList<>();
    private final int CIRCLE_R = dp2px(5);

    private final int STEP = AppUtils.EQ_VIEW_DEFAULT_STEP;

    public EqualizerShowView(Context context) {
        this(context, null);
    }

    public EqualizerShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initView(context);
    }

    public void initView(Context context) {
        mContext = context;

        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text_white_80));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(UiUtils.dip2px(mContext, 11));

        mPointPaint.setColor(ContextCompat.getColor(mContext, R.color.main_color));
        mPointPaint.setAntiAlias(true);

        curveColor = R.color.main_color;
        mCurvePaint.setColor(ContextCompat.getColor(mContext, curveColor));
        mCurvePaint.setAntiAlias(true);
        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurvePaint.setStrokeWidth(dp2px(2));

        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.equalizer_view_dash_line));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));
        mLinePaint.setStrokeWidth(dp2px(1));

        marginTop = UiUtils.dip2px(mContext, 15);
        marginBottom = UiUtils.dip2px(mContext, 30);
        marginLeft = UiUtils.dip2px(context, 15);
        marginRight = UiUtils.dip2px(context, 15);
        textMarginBottom = UiUtils.dip2px(context, 15);
    }

    public void setCurveData(float[] eqPointX, float[] eqPointY, int curveColor) {
        LogUtil.d(TAG, "setCurveData size=" + eqPointX.length + ",pointX=" + Arrays.toString(eqPointX) + ",pointY=" + Arrays.toString(eqPointY));
        this.curveColor = curveColor;
        pointX = new ArrayList<>();
        pointY = new ArrayList<>();
        mEqPointX = new float[eqPointX.length];
        mEqPointY = new float[eqPointX.length];
        for (int i = 0; i < eqPointX.length; i++) {
            mEqPointX[i] = eqPointX[i];
            mEqPointY[i] = eqPointY[i];
        }
        controlCircles.clear();
        for (int i = 0; i < mEqPointX.length; i++) {
            controlCircles.add(produceCirce(getRelativelyX(mEqPointX[i]), getRelativelyY(mEqPointY[i]), CIRCLE_R));
        }
        invalidate();
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private float getViewHeight() {
        return mHeight - marginBottom - marginTop;
    }

    private String getHoriName(int i) {
        String temp = i + "";
        if (temp.length() == 1) {
            temp = "  " + temp;
        } else if (temp.length() == 2) {
            temp = " " + temp;
        }
        return temp;
    }

//    public int getFontHeight(Paint paint) {
//        Paint.FontMetrics fm = paint.getFontMetrics();
//        return (int) (Math.ceil(fm.descent - fm.ascent) / 3);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //drawText(canvas);
        drawLine(canvas);
        //画当前的曲线
        if (controlCircles == null) return;
        curvePath.reset();
        allPointCircles.clear();
        drawCurveChart(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i < mEqFreqArray.length; i++) {
            float startVerticalX = getRelativelyX(mEqFreqArray[i]);
            //canvas.drawLine(startVerticalX, marginTop, startVerticalX, mHeight - marginBottom, mBoxPaint);
            canvas.drawText(getShowName(i), startVerticalX - UiUtils.dip2px(mContext, 6), mHeight - marginBottom + textMarginBottom, mTextPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        float startHorizontalY = getRelativelyY((maxValue + minValue) / 2);
        canvas.drawLine(marginLeft - 6, startHorizontalY, mWidth - marginRight, startHorizontalY, mLinePaint);

        //draw vertical line
        for (int i = 0; i < mVerticalLineArray.length; i++) {
            float startVerticalX = getRelativelyX(mVerticalLineArray[i]);
            canvas.drawLine(startVerticalX, 0, startVerticalX, mHeight - marginBottom + textMarginBottom, mLinePaint);
        }
    }

    /**
     * Draw Curve
     */
    private void drawCurveChart(Canvas canvas) {
        if (pointX == null || pointY == null) {
            return;
        }
        mCurvePaint.setColor(ContextCompat.getColor(mContext, curveColor));
        curvePath.reset();
        pointX.clear();
        pointY.clear();
        for (int i = 0; i < controlCircles.size(); i++) {
            pointX.add(controlCircles.get(i).getX());
            pointY.add(controlCircles.get(i).getY());
        }

        List<Cubic> calculate_x = calculateCurve(pointX);
        List<Cubic> calculate_y = calculateCurve(pointY);
        float nearX;
        float lastX;
        if (calculate_x != null && calculate_y != null && calculate_y.size() >= calculate_x.size()) {
            curvePath.moveTo(calculate_x.get(0).evaluate(0), calculate_y.get(0).evaluate(0));
            allPointCircles.add(produceCirce(calculate_x.get(0).evaluate(0), calculate_y.get(0).evaluate(0), CIRCLE_R));
            nearX = calculate_x.get(0).evaluate(0);
            for (int i = 0; i < calculate_x.size(); i++) {
                lastX = calculate_x.get(i).evaluate(1);
                for (int j = 1; j <= STEP; j++) {
                    float u = j / (float) STEP;
                    float lineToX = calculate_x.get(i).evaluate(u);
                    float lineToY = calculate_y.get(i).evaluate(u);
                    if (lineToY < marginTop) {
                        lineToY = marginTop;
                    }
                    if (lineToY > mHeight - marginBottom) {
                        lineToY = mHeight - marginBottom;
                    }

                    if (lineToX < marginLeft) {
                        lineToX = marginLeft;
                    }
                    if (lineToX > mWidth - marginRight) {
                        lineToX = mWidth - marginRight;
                    }
                    if (lineToX > lastX) {
                        lineToX = lastX;
                    }
                    if (lineToX < nearX) {
                        lineToX = nearX + 0.5f + (j - STEP / 2 + 1) * 0.02f;
                    }
                    curvePath.lineTo(lineToX, lineToY);
                    allPointCircles.add(produceCirce(lineToX, lineToY, CIRCLE_R));
                    nearX = lineToX;
                }
            }
        }

        canvas.drawPath(curvePath, mCurvePaint);
    }

    /**
     * Calculate Curve.
     */
    private List<Cubic> calculateCurve(List<Float> pointList) {
        if (null != pointList && pointList.size() > 0) {
            int n = pointList.size() - 1;
            float[] gamma = new float[n + 1];
            float[] delta = new float[n + 1];
            float[] D = new float[n + 1];
            int i;
            gamma[0] = 1.0f / 2.0f;
            for (i = 1; i < n; i++) {
                gamma[i] = 1 / (4 - gamma[i - 1]);
            }
            gamma[n] = 1 / (2 - gamma[n - 1]);

            delta[0] = 3 * (pointList.get(1) - pointList.get(0)) * gamma[0];
            for (i = 1; i < n; i++) {
                delta[i] = (3 * (pointList.get(i + 1) - pointList.get(i - 1)) - delta[i - 1]) * gamma[i];
            }
            delta[n] = (3 * (pointList.get(n) - pointList.get(n - 1)) - delta[n - 1]) * gamma[n];

            D[n] = delta[n];
            for (i = n - 1; i >= 0; i--) {
                D[i] = delta[i] - gamma[i] * D[i + 1];
            }

            /* now compute the coefficients of the cubics */
            List<Cubic> cubicList = new LinkedList<>();
            for (i = 0; i < n; i++) {
                Cubic c = new Cubic(pointList.get(i), D[i], 3 * (pointList.get(i + 1) - pointList.get(i)) - 2 * D[i] - D[i + 1], 2 * (pointList.get(i) - pointList.get(i + 1)) + D[i] + D[i + 1]);
                cubicList.add(c);
            }
            return cubicList;
        }
        return null;
    }

    private class Cubic {
        float a, b, c, d; /* a + b*u + c*u^2 +d*u^3 */

        private Cubic(float a, float b, float c, float d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        /**
         * evaluate cubic
         */
        private float evaluate(float u) {
            return (((d * u) + c) * u + b) * u + a;
        }
    }

    private float getRelativelyX(float x) {
        return (float) ((Math.log10(x) - Math.log10(minFrequency)) * (mWidth - marginLeft - marginRight) / (Math.log10(maxFrequency) - Math.log10(minFrequency)) + marginLeft);
    }

    private int getFreqFromX(float X) {
        double temp = Math.pow(10, ((X - marginLeft) * (Math.log10(maxFrequency) - Math.log10(minFrequency)) / (mWidth - marginLeft - marginRight) + Math.log10(minFrequency)));
        return (int) temp;
    }

    private float getRelativelyY(float yValue) {
        return getViewHeight() * (maxValue - yValue) / (maxValue - minValue) + marginTop;
    }

    private int getValueFromY(float Y) {
        return (int) (maxValue - (Y - marginTop) / getViewHeight() * (maxValue - minValue));
    }

    private String getShowName(int point) {
        int value = mEqFreqArray[point];
        if (value >= 1000) {
            return String.valueOf(value).substring(0, String.valueOf(value).length() - 3) + "k";
        }
        return String.valueOf(value);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 根据传入的参数，分别获取测量模式和测量值
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 如果宽或者高的测量模式非精确值
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            mWidth = getSuggestedMinimumWidth();
            mWidth = mWidth == 0 ? getDefaultWidth() : mWidth;
            mHeight = getSuggestedMinimumHeight();
            mHeight = mHeight == 0 ? getDefaultHeight() : mHeight;
        } else {
            // 若设置为精确值
            mWidth = width;
            mHeight = height;
        }
        if (mEqPointX != null) {
            controlCircles.clear();
            for (int i = 0; i < mEqPointX.length; i++) {
                controlCircles.add(produceCirce(getRelativelyX(mEqPointX[i]), getRelativelyY(mEqPointY[i]), CIRCLE_R));
            }
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.max(outMetrics.widthPixels, outMetrics.heightPixels) - 2 * UiUtils.dip2px(mContext, 20);
    }

    private int getDefaultHeight() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels) -
                mContext.getResources().getDimensionPixelSize(R.dimen.equalizer_button_layout_height) - 2 * UiUtils.dip2px(mContext, 10);
    }

    private int getValueFromFreq(float freq) {
        float nearX = mWidth;
        float absX;
        int nearIndex = 0;
        float relativelyX = getRelativelyX(freq);
        for (int i = 0; i < allPointCircles.size(); i++) {
            absX = Math.abs(allPointCircles.get(i).getX() - relativelyX);
            if (absX < nearX) {
                nearX = absX;
                nearIndex = i;
            }
        }
        //LogUtil.d(TAG, "freq=" + freq + ",ReX=" + relativelyX + ",nearIndex=" + nearIndex + ",XX=" + allPointCircles.get(nearIndex).getX());
        return getValueFromY(allPointCircles.get(nearIndex).getY());
    }

    private CircleModel produceCirce(float x, float y, float r) {
        CircleModel circle = new CircleModel();
        circle.setX(x);
        circle.setY(y);
        circle.setR(r);
        return circle;
    }

    public int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}


