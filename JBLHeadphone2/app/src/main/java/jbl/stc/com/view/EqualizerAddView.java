package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.CircleModel;
import jbl.stc.com.listener.OnEqChangeListener;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.StringUtils;
import jbl.stc.com.utils.UiUtils;


public class EqualizerAddView extends View {
    private static final String TAG = EqualizerAddView.class.getSimpleName();
    private Context mContext;
    private Paint mPointPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private Paint mLinePaint = new Paint();
    private Paint mCurvePaint = new Paint();
    private Paint touchLinePaint = new Paint();

    private int mWidth, mHeight;
    private int customHeight = 0;
    private int[] mEqFreqArray = new int[]{32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
    private int[] mVerticalLineArray = new int[]{32, 250, 2000, 16000};
    private int lowTextStart = 80;
    private int midTextStart = 600;
    private int highTextStart = 5000;
    private int[] mHorizontalName = new int[]{20, 10, 0, -10, -20};

    private float[] mEqPointX, mEqPointY;

    // x、y轴坐标值
    private List<Float> pointX, pointY;
    private int curveColor;
    private boolean supportDrag = false;

    private int where = -2;
    private Path curvePath = new Path();
    private Bitmap controlBitmap;
    private Bitmap controlBitmapCurr;

    private float marginLeft = 0f;
    private float marginRight = 0f;
    private float marginTop;
    private float marginBottom;
    private float textMarginBottom;
    private float freqTextWidth;
    private float dbTextWidth;
    private float nearPointFreqX;
    private String freqText;
    private String dbText;
    private float touchLineWidth;

    private int maxFrequency = mEqFreqArray[mEqFreqArray.length - 1];
    private int minFrequency = mEqFreqArray[0];

    private int minValue = -10;
    private int maxValue = 10;

    private float touchX;
    private float touchY;

    private List<CircleModel> controlCircles = new ArrayList<>();
    private List<CircleModel> allPointCircles = new ArrayList<>();
    private final int CIRCLE_TOUCH_DIS = dp2px(15);
    private final int CIRCLE_R = dp2px(5);

    private OnEqChangeListener mOnEqChangeListener;
    private long startTime = 0;
    //拖动原点时每隔100ms刷新一次曲线
    public static final int DRAW_INTERVAL = 250;
    private int STEP = AppUtils.EQ_VIEW_DEFAULT_STEP;
    private boolean hasDrag = false;

    public void setOnEqChangeListener(OnEqChangeListener onEqChangeListener) {
        this.mOnEqChangeListener = onEqChangeListener;
    }

    public EqualizerAddView(Context context) {
        this(context, null);
    }

    public EqualizerAddView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerAddView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initView(context);
    }

    public void initView(Context context) {
        mContext = context;
        pointX = new ArrayList<>();
        pointY = new ArrayList<>();
        controlBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.eq_control_point);
        controlBitmapCurr = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.eq_control_point_curr);

        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(UiUtils.dip2px(mContext, 11));

        mPointPaint.setColor(ContextCompat.getColor(mContext, R.color.main_color));
        mPointPaint.setAntiAlias(true);

        curveColor = R.color.text_white_80;
        mCurvePaint.setColor(ContextCompat.getColor(mContext, curveColor));
        mCurvePaint.setAntiAlias(true);
        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurvePaint.setStrokeWidth(dp2px(2));

        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.light_white));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        //mLinePaint.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));
        mLinePaint.setStrokeWidth(1);

        touchLineWidth = dp2px(45);
        touchLinePaint.setColor(ContextCompat.getColor(mContext, R.color.equalizer_view_touch_line));
        touchLinePaint.setAntiAlias(true);
        touchLinePaint.setStyle(Paint.Style.STROKE);
        touchLinePaint.setStrokeWidth(touchLineWidth);

        marginTop = dp2px(10);
        marginBottom = dp2px(45);
        marginLeft = dp2px(20);
        marginRight = dp2px(20);
        textMarginBottom = dp2px(35);
        nearPointFreqX = dp2px(15);
        setSupportDrag(true);
    }

    public void setCustomHeight(int height) {
        customHeight = height;
    }

    public void setSupportDrag(boolean supportDrag) {
        this.supportDrag = supportDrag;
        if (!this.supportDrag) {
            setOnTouchListener(null);
        } else {
            setOnTouchListener(onTouchListener);
        }
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setCurveData(float[] eqPointX, float[] eqPointY, int curveColor) {
        Log.d(TAG, "setCurveData size=" + eqPointX.length + ",pointX=" + Arrays.toString(eqPointX) + ",pointY=" + Arrays.toString(eqPointY));
        this.curveColor = curveColor;
        pointX.clear();
        pointY.clear();
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
        drawLine(canvas);
        drawText(canvas);

        //画当前的曲线
        if (controlCircles == null) return;
        curvePath.reset();
        allPointCircles.clear();

        //十字背景
        if (touchX > 0 && touchY > 0) {
            canvas.drawLine(touchX, 0, touchX, mHeight, touchLinePaint);
            canvas.drawLine(0, touchY, mWidth, touchY, touchLinePaint);
        }

        drawCurveChart(canvas);

        if (touchX > 0 && touchY > 0) {
            freqText = getFreqFromX(touchX) + "Hz";
            int db = (int) (getDbValueFromY(touchY));
            dbText = db + "dB";
            if (db > 0) {
                dbText = "+" + dbText;
            }
            freqTextWidth = mTextPaint.measureText(freqText);
            dbTextWidth = mTextPaint.measureText(dbText);

            canvas.drawText(freqText, touchX - freqTextWidth / 2, dp2px(20), mTextPaint);
            if (touchX >= (mWidth / 2)) {
                canvas.drawText(dbText, 25, touchY, mTextPaint);
            } else {
                canvas.drawText(dbText, mWidth - dbTextWidth - 25, touchY, mTextPaint);
            }
        }

        for (int i = 0; i < controlCircles.size(); i++) {
            //canvas.drawCircle(controlCircles.get(i).getX(), controlCircles.get(i).getY(), controlCircles.get(i).getR(), mPointPaint);
            if (touchX > 0 && touchY > 0 && isTouchInCircle(touchX, touchY, controlCircles.get(i))) {
                canvas.drawBitmap(controlBitmapCurr, controlCircles.get(i).getX() - controlBitmapCurr.getHeight() / 2,
                        controlCircles.get(i).getY() - controlBitmapCurr.getHeight() / 2, mPointPaint);
            } else {
                canvas.drawBitmap(controlBitmap, controlCircles.get(i).getX() - controlBitmap.getHeight() / 2,
                        controlCircles.get(i).getY() - controlBitmap.getHeight() / 2, mPointPaint);
            }
        }
    }

    private void drawLine(Canvas canvas) {
        Log.d(TAG, "CenterHeight:" + String.valueOf((mHeight - marginTop - marginBottom) / 2));
        //draw center horizontal line
        mLinePaint.reset();
        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.light_white));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{dp2px(5), dp2px(5)}, 0));
        mLinePaint.setStrokeWidth(dp2px(1));
        float startHorizontalY = getRelativelyY((maxValue + minValue) / 2);
        //canvas.drawLine(0, startHorizontalY- marginBottom/2, mWidth, startHorizontalY- marginBottom/2, mLinePaint);
        canvas.drawLine(marginLeft, (mHeight - marginBottom + marginTop) / 2, mWidth-marginRight, (mHeight - marginBottom + marginTop) / 2, mLinePaint);

        //draw vertical line
        mLinePaint.reset();
        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.light_white));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(1);
        for (int i = 0; i < mVerticalLineArray.length; i++) {
            float startVerticalX = getRelativelyX(mVerticalLineArray[i]);
            canvas.drawLine(startVerticalX, marginTop, startVerticalX, mHeight - marginBottom, mLinePaint);
        }

        //draw horizontal line
        mLinePaint.reset();
        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.light_white));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(dp2px(1));
        canvas.drawLine(0, marginTop, mWidth, marginTop, mLinePaint);
        canvas.drawLine(0, mHeight - marginBottom, mWidth, mHeight - marginBottom, mLinePaint);
    }

    private void drawText(Canvas canvas) {
        if (touchX > 0 && touchY > 0) {
            mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text_white_10));
        } else {
            mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text_white_80));
        }
        //draw text
        for (int i = 0; i < mEqFreqArray.length; i++) {
            float startVerticalX = getRelativelyX(mEqFreqArray[i]);
            //canvas.drawText(getShowName(i), startVerticalX - dp2px(6), dp2px(20), mTextPaint);
            if (i == mEqFreqArray.length - 1) {
                canvas.drawText(getShowName(i), startVerticalX - dp2px(20), mHeight - marginBottom / 2, mTextPaint);
            } else {
                canvas.drawText(getShowName(i), startVerticalX - dp2px(6), mHeight - marginBottom / 2, mTextPaint);
            }
        }

        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text_white_80));
        float lowStartX = getRelativelyX(lowTextStart);
        canvas.drawText("LOW", lowStartX, textMarginBottom, mTextPaint);
        float midStartX = getRelativelyX(midTextStart);
        canvas.drawText("MID", midStartX, textMarginBottom, mTextPaint);
        float highStartX = getRelativelyX(highTextStart);
        canvas.drawText("HIGH", highStartX, textMarginBottom, mTextPaint);
    }

    /**
     * Draw Curve
     */
    private void drawCurveChart(Canvas canvas) {
        Log.d(TAG, "drawCurve");
        mCurvePaint.setColor(ContextCompat.getColor(mContext, curveColor));
        curvePath.reset();
        pointX.clear();
        pointY.clear();
        for (int i = 0; i < controlCircles.size(); i++) {
            pointX.add(controlCircles.get(i).getX());
            pointY.add(controlCircles.get(i).getY());
        }
        Log.d(TAG, "controlCircles size" + String.valueOf(controlCircles.size()));
        if (controlCircles.size() > 2) {
            STEP = (8 * AppUtils.EQ_VIEW_DEFAULT_STEP) / (controlCircles.size() - 1);
        }

        List<Cubic> calculate_x = calculateCurve(pointX);
        List<Cubic> calculate_y = calculateCurve(pointY);
        Log.d(TAG, "calculate_x size" + String.valueOf(calculate_x.size()));
        float nearX;
        float lastX;
        float lastY;
        if (calculate_x != null && calculate_y != null && calculate_y.size() >= calculate_x.size()) {
            curvePath.moveTo(calculate_x.get(0).evaluate(0), calculate_y.get(0).evaluate(0));
            allPointCircles.add(produceCirce(calculate_x.get(0).evaluate(0), calculate_y.get(0).evaluate(0), CIRCLE_R));
            nearX = calculate_x.get(0).evaluate(0);
            for (int i = 0; i < calculate_x.size(); i++) {
                lastX = calculate_x.get(i).evaluate(1);
                lastY = calculate_y.get(i).evaluate(1);
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
                    if (lineToX > lastX - 5f) {
                        lineToX = lastX - 5f;
                    }
                    if (lineToX > lastX) {
                        lineToX = lastX;
                    }
                    if (lineToX < nearX) {
                        lineToX = nearX + 0.5f + (j - STEP / 2 + 1) * 0.02f;
                    }
                    //Log.d(TAG, "moreHalf=" + moreHalf + ",j=" + j + ",STEP=" + STEP + ",lastX=" + lastX);
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

    private float getDbValueFromY(float Y) {
        //get one point float
        float value = (maxValue - (Y - marginTop) / getViewHeight() * (maxValue - minValue));
        Log.d(TAG, "Y:" + String.valueOf(Y) + "ViewHeight:" + String.valueOf(getViewHeight()) + "value:" + String.valueOf(value));
        return Float.valueOf(StringUtils.getOnePointFloat(value));
    }

    private String getShowName(int point) {
        int value = mEqFreqArray[point];
        if (value >= 1000) {
            if (value >= 16000) {
                return String.valueOf(value).substring(0, String.valueOf(value).length() - 3) + "k HZ";
            } else {
                return String.valueOf(value).substring(0, String.valueOf(value).length() - 3) + "k";
            }

        }
        return String.valueOf(value);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            mWidth = getSuggestedMinimumWidth();
            mWidth = mWidth == 0 ? getDefaultWidth() : mWidth;
            mHeight = getSuggestedMinimumHeight();
            mHeight = mHeight == 0 ? getDefaultHeight() : mHeight;
        } else {
            mWidth = width;
            mHeight = height;
        }
        //Log.d(TAG, "onMeasure mWidth=" + mWidth + ",mHeight=" + mHeight + ",hasDrag=" + hasDrag);
        if (!hasDrag) {
            if (mEqPointX != null) {
                controlCircles.clear();
                for (int i = 0; i < mEqPointX.length; i++) {
                    controlCircles.add(produceCirce(getRelativelyX(mEqPointX[i]), getRelativelyY(mEqPointY[i]), CIRCLE_R));
                }
            }
        }
        if (customHeight > 0) {
            mHeight = customHeight;
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
        return Math.max(outMetrics.widthPixels, outMetrics.heightPixels) - 2 * dp2px(20);
    }

    private int getDefaultHeight() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels) -
                mContext.getResources().getDimensionPixelSize(R.dimen.equalizer_button_layout_height) - 2 * dp2px(10);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (supportDrag) {
                        touchX = event.getX();
                        touchY = event.getY();
                        where = touchWhere(touchX, touchY);
                        Log.d(TAG, "ACTION_DOWN touchWhere = " + where + " touchX=" + touchX + ",touchY=" + touchY + ",STEP=" + STEP);
                        hasDrag = true;
                        if (where == -1) {
                            where = handleAddPoint(touchX, touchY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (supportDrag && where >= 0 && where < controlCircles.size()) {
                        float value = getDbValueFromY(event.getY());
                        int freq = getFreqFromX(event.getX());
                        //Log.d(TAG, "ACTION_MOVE touchX=" + event.getX() + ",touchY=" + event.getY() + ",freq=" + freq + ",where=" + where + ",size=" + controlCircles.size());

                        if (freq > maxFrequency) {
                            freq = maxFrequency;
                        }

                        if (freq < minFrequency) {
                            freq = minFrequency;
                        }
                        if (where == 0) {
                            freq = minFrequency;
                        }
                        if (where == controlCircles.size() - 1) {
                            freq = maxFrequency;
                        }
                        touchX = getRelativelyX(freq);
                        if (value >= minValue && value <= maxValue) {
                            //not first point and not last point
                            if (where > 0 && where < controlCircles.size() - 1) {
                                handleCrossover(where, touchX);
                            }
                            if (where >= 0 && where < controlCircles.size()) {
                                controlCircles.get(where).setX(touchX);
                                touchY = getRelativelyY(value);
                                controlCircles.get(where).setY(touchY);
                            } else {
                                touchY = 0;
                                touchX = 0;
                            }
                            if (mOnEqChangeListener != null && System.currentTimeMillis() - startTime > DRAW_INTERVAL) {
                                for (int i = 0; i < mEqFreqArray.length; i++) {
                                    mOnEqChangeListener.onEqValueChanged(i, getValueFromFreq(mEqFreqArray[i]));
                                }
                                startTime = System.currentTimeMillis();
                            }
                            invalidate();
                        }
                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    touchX = 0;
                    touchY = 0;
                    invalidate();
                    mEqPointX = new float[controlCircles.size()];
                    mEqPointY = new float[controlCircles.size()];
                    for (int i = 0; i < controlCircles.size(); i++) {
                        mEqPointX[i] = getFreqFromX(controlCircles.get(i).getX());
                        mEqPointY[i] = getDbValueFromY(controlCircles.get(i).getY());
                    }
                    mOnEqChangeListener.onEqDragFinished(mEqPointX, mEqPointY);
                    break;
            }
            return true;
        }
    };

    private void handleCrossover(int currIndex, float currX) {
        int size = controlCircles.size();
        if (size > 2) {
            if (currIndex > 0 && currIndex < size - 1) {
                float preX = controlCircles.get(currIndex - 1).getX();
                float nextX = controlCircles.get(currIndex + 1).getX();
                //Log.d(TAG,"currX="+currX+",preX="+preX+",rearX="+rearX);
                if (currX <= preX + nearPointFreqX || currX >= nextX - nearPointFreqX) {
                    controlCircles.remove(currIndex);
                    where = -1;
                }
            }
        }
    }

    private int handleAddPoint(float currX, float currY) {
        int addIndex = -1;
        int addWhere = -1;
        for (int i = 0; i < allPointCircles.size(); i++) {
            CircleModel circe = allPointCircles.get(i);
            if (isTouchInCircle(currX, currY, circe)) {
                addIndex = i;
            }
        }

        if (addIndex != -1) {
            CircleModel addCirce = allPointCircles.get(addIndex);
            for (int i = 0; i < controlCircles.size(); i++) {
                CircleModel circe = controlCircles.get(i);
                if (circe.getX() > addCirce.getX()) {
                    addWhere = i;
                    break;
                }
            }

            if (addWhere != -1) {
                controlCircles.add(addWhere, produceCirce(addCirce.getX(), addCirce.getY(), CIRCLE_R));
            }
        }
        //Log.d(TAG, "handleAddPoint addIndex=" + addIndex + ",addWhere=" + addWhere + ",SIZE=" + controlCircles.size());
        return addWhere;
    }

    private boolean isTouchInCircle(float x, float y, CircleModel circle) {
        return Math.pow(x - circle.getX(), 2) + Math.pow(y - circle.getY(), 2) <= Math.pow(circle.getR() + CIRCLE_TOUCH_DIS, 2);
    }

    private int touchWhere(float x, float y) {
        for (int i = 0; i < controlCircles.size(); i++) {
            CircleModel circe = controlCircles.get(i);
            if (isTouchInCircle(x, y, circe)) {
                return i;
            }
        }
        return -1;
    }

    private float getValueFromFreq(float freq) {
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
        float dbValue = getDbValueFromY(allPointCircles.get(nearIndex).getY());
        //Log.d(TAG, "freq=" + freq + ",ReX=" + relativelyX +
        //        ",nearX=" + allPointCircles.get(nearIndex).getX() + ",nearY=" + allPointCircles.get(nearIndex).getY() + ",dbValue=" + dbValue);
        return dbValue;
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


