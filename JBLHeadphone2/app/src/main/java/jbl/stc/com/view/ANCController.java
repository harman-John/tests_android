package jbl.stc.com.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceView;

import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.manager.ANCDrawManager;
import jbl.stc.com.R;

public class ANCController extends SurfaceView {
    private static final String TAG = ANCController.class.getSimpleName();
    private boolean intialSetup = true;
    public static boolean mReCalculcate = false;
    private static int INVALID_PROGRESS_VALUE = -1;
    // The initial rotational offset -90 means we start at 12 o'clock
    private final int mAngleOffset = -90;
    private Handler mHandler1;
    int mdeviceWidth;
    int mdeviceHeight;
    float density;
    CircularInsideLayout circularInsideLayout;
    double dis1 = 1000, dis2 = 1000;
    boolean wasThumbTouched = false;
    int leftFactor, rightFactor;
    private Paint mPointerHaloPaint1, mPointerHaloPaint2;
    private Paint mPointerHaloBorderPaint1, mPointerHaloBorderPaint2;
    private int mHeight, mWidth;
    private Paint mPointerPaint1, mPointerPaint2, mHaloPaint;
    private Paint mPointerTextPaint1, mPointerTextPaint2;
    private boolean mIsClicked = false;
    private int mViewPosition;
    /**
     * The Drawable for the seek arc thumbnail
     */
    private Drawable mThumb1, mThumb2;
    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mMax = 100;
    /**
     * The Current value that the SeekArc is set to
     */
    private int mProgress = 0;
    /**
     * The width of the progress line for this SeekArc
     */
    private int mProgressWidth = 4;
    /**
     * The Width of the background arc for the SeekArc
     */
    private int mArcWidth = 2;
    private boolean isOff;
    /**
     * The Angle to start drawing this Arc from
     */
    private int mStartAngle = 0;
    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private int mSweepAngle = 360;
    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private int mRotation = 0;
    /**
     * Give the SeekArc rounded edges
     */
    private boolean mRoundedEdges = false;
    /**
     * Enable touch inside the SeekArc
     */
    private boolean mTouchInside = false;
    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    private boolean mArcClockwise = true;
    // Internal variables
    private int mArcRadius = 0;
    private float mProgressSweep = 0;
    private float mPreviousClockwiseProgressSweep = 1;
    private float mPreviousAntiClockwiseProgressSweep = 1;
    private int leftProgress = 0;
    private int rightProgress = 0;
    private RectF mArcRect = new RectF();
    private Paint mArcPaint;
    private Paint mProgressPaint;
    private int mTranslateX;
    private int mTranslateY;
    private int mThumb1XPos;
    private int mThumb1YPos;
    private int mThumb1StartAngle = 0;
    private int mThumb2StartAngle = 0;

    private int mThumb1Radius = 50;
    private int mThumb2Radius = 50;

    private int mThumb1Text = 50;
    private int mThumb2Text = 50;

//    private int mThumb1TutoriaRadius;
//    private int mThumb2TutoriaRadius;

//    private ANCFragment ancAwarehome;
    private boolean mClockwise = true;

    private int mThumb2XPos;
    private int mThumb2YPos;
    private double mTouchAngle;
    private OnSeekArcChangeListener mOnSeekArcChangeListener;

    private boolean isAnimationRunning = false;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isAnimationRunning = true;
            switch (mViewPosition) {

                case 0:
                    if (leftProgress == 0 && rightProgress == 0) {
                        mIsClicked = false;
                        //Don't forget to turn isAnimationRunning = false before return;
                        isAnimationRunning = false;
                        return;
                    }
                    setDecrementDemo();
                    setProgress(leftProgress, true);
                    setProgress(rightProgress, false);
                    break;
                case 1:
                    if (leftProgress < 25) {
                        setProgress(++leftProgress, true);
                    } else if (leftProgress > 25) {
                        setProgress(--leftProgress, true);
                    }

                    if (rightProgress < 25) {
                        setProgress(++rightProgress, false);
                    } else if (rightProgress > 25) {
                        setProgress(--rightProgress, false);
                    }

                    if (leftProgress == 25 && rightProgress == 25) {
                        mIsClicked = false;
                        //Don't forget to turn isAnimationRunning = false before return;
                        isAnimationRunning = false;
                        return;

                    }
                    break;

                case 2:
                    if (leftProgress < 55) {
                        setProgress(++leftProgress, true);
                    } else if (leftProgress > 55) {
                        setProgress(--leftProgress, true);
                    }

                    if (rightProgress < 55) {
                        setProgress(++rightProgress, false);
                    } else if (rightProgress > 55) {
                        setProgress(--rightProgress, false);
                    } else if (leftProgress == 55 && rightProgress == 55) {
                        mIsClicked = false;
                        //Don't forget to turn isAnimationRunning = false before return;
                        isAnimationRunning = false;
                        return;

                    }
                    break;
                case 3:
                    leftProgress += 2;
                    rightProgress += 2;

//                    boolean is150NC = AppUtils.is150NC(getAppActivity());
                    setProgress(leftProgress, true);
                    setProgress(rightProgress, false);
                    int maxProgress = 100;
                    if (leftProgress >= maxProgress && rightProgress >= maxProgress) {
                        mIsClicked = false;
                        //Don't forget to turn isAnimationRunning = false before return;
                        isAnimationRunning = false;
                        return;
                    }
                    break;
                case -1:
                    if (leftFactor == 0 && rightFactor == 0) {
                        setProgress(leftProgress, true);
                        setProgress(rightProgress, false);
                    }
                    if (leftProgress < leftFactor) {
                        leftProgress += 1;
                        setProgress(leftProgress, true);
                    }
                    if (rightProgress < rightFactor) {
                        rightProgress += 1;
                        setProgress(rightProgress, false);
                    }
                    if (rightProgress == rightFactor && leftProgress == leftFactor) {
                        //Don't forget to turn isAnimationRunning = false before return;
                        isAnimationRunning = false;
                        return;
                    }
                    break;
            }
            mHandler1.postDelayed(runnable, 10);
        }
    };

    /**
     * Check if animation is running currently, if it is then wait for other callbacks.
     */
    public boolean isAnimationRunning() {
        return isAnimationRunning;
    }

    /**
     * Constructor
     */
    public ANCController(Context context) {
        super(context);
        init(context, null, 0);
    }

    /**
     * Constructor with Attribute set
     */
    public ANCController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    /**
     * Constructor
     */

    public ANCController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public CircularInsideLayout getCircularInsideLayout() {
        return circularInsideLayout;
    }

    public void setCircularInsideLayout(CircularInsideLayout circularInsideLayout) {
        this.circularInsideLayout = circularInsideLayout;
    }

    /**
     * <p>Intialize the resources</p>
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {

//        Log.d(TAG, "Initialising SeekArc");
        initializeDimensones(context);
        int thumbHalfheight = 0, thumbHalfheight1 = 0;
        int thumbHalfWidth = 0, thumbHalfWidth1 = 0;
        mThumb1 = getResources().getDrawable(R.drawable.mobile_portrait_l);
        mThumb2 = getResources().getDrawable(R.drawable.mobile_portrait_l);
        // Convert progress width to pixels for current density
        mProgressWidth = (int) (mProgressWidth * density);

        mHandler1 = new Handler();

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.ANCController, defStyle, 0);


            thumbHalfheight = (int) mThumb1.getIntrinsicHeight() / 10;
            thumbHalfWidth = (int) mThumb1.getIntrinsicWidth() / 10;
            mThumb1.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);
            thumbHalfheight1 = (int) mThumb2.getIntrinsicHeight() / 10;
            thumbHalfWidth1 = (int) mThumb2.getIntrinsicWidth() / 10;
            mThumb2.setBounds(-thumbHalfWidth1, -thumbHalfheight1, thumbHalfWidth1,
                    thumbHalfheight1);

            mMax = a.getInteger(R.styleable.ANCController_max, mMax);
            mProgress = a.getInteger(R.styleable.ANCController_progress, mProgress);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.ANCController_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.ANCController_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.ANCController_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.ANCController_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.ANCController_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.ANCController_roundEdges,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.ANCController_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.ANCController_clockwise,
                    mClockwise);
            mThumb1StartAngle = a.getInteger(R.styleable.ANCController_thumb1_start, mThumb1StartAngle);
            mThumb2StartAngle = a.getInteger(R.styleable.ANCController_thumb2_start, mThumb2StartAngle);

            mThumb1Radius = (int) a.getDimension(R.styleable.ANCController_thumb1_radius, mThumb1Radius);
            mThumb2Radius = (int) a.getDimension(R.styleable.ANCController_thumb2_radius, mThumb2Radius);


            mThumb1Text = (int) a.getDimension(R.styleable.ANCController_thumb1_text, mThumb1Text);
            mThumb2Text = (int) a.getDimension(R.styleable.ANCController_thumb2_text, mThumb2Text);


            int arcColor = a.getColor(R.styleable.ANCController_arcColor, Color.BLACK);
            int progressColor = a.getColor(R.styleable.ANCController_progressColor,
                    Color.BLUE);
            initilizePaint(arcColor, progressColor, context);
            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;


        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    public void initializeDimensones(Context context) {
        ANCDrawManager ancDrawManager = ANCDrawManager.getInsManager();
        if (ancDrawManager.getDeviceHeight() == -1 || ancDrawManager.getDeviceWidth() == -1 || ancDrawManager.getDesnity() == 10000) {
            Resources res = getResources();
            density = res.getDisplayMetrics().density;
            DisplayMetrics metrics = res.getDisplayMetrics();
            mdeviceWidth = metrics.widthPixels;
            mdeviceHeight = metrics.heightPixels;
            ancDrawManager.setDeviceHeight(mdeviceHeight);
            ancDrawManager.setDeviceWidth(mdeviceWidth);
            ancDrawManager.setDesnity(density);
        } else {
            density = ancDrawManager.getDesnity();
            mdeviceWidth = ancDrawManager.getDeviceWidth();
            mdeviceHeight = ancDrawManager.getDeviceHeight();
        }
    }

    /**
     * Method is used to avoid creating instance so that memory can be for better performance on LOW memory device
     *
     * @param arcColor
     * @param progressColor
     * @param context
     */
    public void initilizePaint(int arcColor, int progressColor, Context context) {
        ANCDrawManager ancDrawManager = ANCDrawManager.getInsManager();
        if (ancDrawManager.getmArcPaint() != null) {
            mArcPaint = ancDrawManager.getmArcPaint();
        } else {
            mArcPaint = new Paint();
            mArcPaint.setColor(arcColor);
            mArcPaint.setAntiAlias(true);
            mArcPaint.setStyle(Paint.Style.STROKE);
            mArcPaint.setStrokeWidth(mArcWidth);
            ancDrawManager.setmArcPaint(mArcPaint);
        }

        if (ancDrawManager.getmPointerPaint1() != null) {
            mPointerPaint1 = ancDrawManager.getmPointerPaint1();
        } else {
            mPointerPaint1 = new Paint();
            mPointerPaint1.setAntiAlias(true);
            mPointerPaint1.setDither(true);
            mPointerPaint1.setStyle(Paint.Style.FILL);
            mPointerPaint1.setColor(Color.BLACK);
            mPointerPaint1.setStrokeWidth(14);
            ancDrawManager.setmPointerPaint1(mArcPaint);
        }

        if (ancDrawManager.getmPointerPaint2() != null) {
            mPointerPaint2 = ancDrawManager.getmPointerPaint2();
        } else {
            mPointerPaint2 = new Paint();
            mPointerPaint2.setAntiAlias(true);
            mPointerPaint2.setDither(true);
            mPointerPaint2.setStyle(Paint.Style.FILL);
            mPointerPaint2.setColor(Color.BLACK);
            mPointerPaint2.setStrokeWidth(14);
            //mArcPaint.setAlpha(45);
            ancDrawManager.setmPointerPaint2(mPointerPaint2);
        }
        if (ancDrawManager.getmProgressPaint() != null) {
            mProgressPaint = ancDrawManager.getmProgressPaint();
        } else {
            mProgressPaint = new Paint();
            mProgressPaint.setColor(progressColor);
            mProgressPaint.setAntiAlias(true);
            mProgressPaint.setStyle(Paint.Style.STROKE);
            mProgressPaint.setStrokeWidth(mProgressWidth);
            ancDrawManager.setmProgressPaint(mProgressPaint);
        }

        if (mdeviceWidth <= 720) { /** Addition for smaller device**/
            mThumb1Radius = mThumb1Radius - 4;
            mThumb2Radius = mThumb2Radius - 4;
            mThumb1Text = mThumb1Text - 3;
            mThumb2Text = mThumb2Text - 3;
        }

        if (ancDrawManager.getmPointerTextPaint1() != null) {
            mPointerTextPaint1 = ancDrawManager.getmPointerTextPaint1();
        } else {
            mPointerTextPaint1 = new Paint();
            mPointerTextPaint1.setTextAlign(Paint.Align.CENTER);
            mPointerTextPaint1.setColor(Color.WHITE);
            mPointerTextPaint1.setTextSize(mThumb1Text);
            ancDrawManager.setmPointerTextPaint1(mPointerTextPaint1);
        }

        if (ancDrawManager.getmPointerTextPaint2() != null) {
            mPointerTextPaint2 = ancDrawManager.getmPointerTextPaint2();
        } else {
            mPointerTextPaint2 = new Paint();
            mPointerTextPaint2.setTextAlign(Paint.Align.CENTER);
            mPointerTextPaint2.setColor(Color.WHITE);
            mPointerTextPaint2.setTextSize(mThumb2Text);
            ancDrawManager.setmPointerTextPaint2(mPointerTextPaint2);
        }

        if (ancDrawManager.getmPointerHaloPaint1() != null) {
            mPointerHaloPaint1 = ancDrawManager.getmPointerHaloPaint1();
        } else {
            mPointerHaloPaint1 = new Paint();
            mPointerHaloPaint1.set(mPointerPaint1);
            mPointerHaloPaint1.setColor(getResources().getColor(R.color.background));
            mPointerHaloPaint1.setAlpha(100);
            mPointerHaloPaint1.setStrokeWidth(52);
            ancDrawManager.setmPointerHaloPaint1(mPointerHaloPaint1);
        }

        if (ancDrawManager.getmPointerHaloPaint2() != null) {
            mPointerHaloPaint2 = ancDrawManager.getmPointerHaloPaint2();
        } else {
            mPointerHaloPaint2 = new Paint();
            mPointerHaloPaint2.set(mPointerPaint2);
            mPointerHaloPaint2.setColor(getResources().getColor(R.color.background));
            mPointerHaloPaint2.setAlpha(100);
            mPointerHaloPaint2.setStrokeWidth(52);
            ancDrawManager.setmPointerHaloPaint2(mPointerHaloPaint2);
        }

        if (ancDrawManager.getmPointerHaloBorderPaint1() != null) {
            mPointerHaloBorderPaint1 = ancDrawManager.getmPointerHaloBorderPaint1();
        } else {
            mPointerHaloBorderPaint1 = new Paint();
            mPointerHaloBorderPaint1.set(mPointerPaint1);
            mPointerHaloBorderPaint1.setStrokeWidth(2);
            mPointerHaloBorderPaint1.setColor(getResources().getColor(R.color.background));
            mPointerHaloBorderPaint1.setStyle(Paint.Style.STROKE);
            ancDrawManager.setmPointerHaloBorderPaint1(mPointerHaloBorderPaint1);
        }
        if (ancDrawManager.getmPointerHaloBorderPaint2() != null) {
            mPointerHaloBorderPaint2 = ancDrawManager.getmPointerHaloBorderPaint2();
        } else {
            mPointerHaloBorderPaint2 = new Paint();
            mPointerHaloBorderPaint2.set(mPointerPaint2);
            mPointerHaloBorderPaint2.setStrokeWidth(2);
            mPointerHaloBorderPaint2.setColor(getResources().getColor(R.color.background));
            mPointerHaloBorderPaint2.setStyle(Paint.Style.STROKE);
            ancDrawManager.setmPointerHaloBorderPaint2(mPointerHaloBorderPaint2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Logger.error("Order, onDraw");
        if (mReCalculcate) {
            canvas.drawArc(mArcRect, mThumb1StartAngle + mAngleOffset, mPreviousClockwiseProgressSweep, false,
                    mProgressPaint);
            canvas.drawArc(mArcRect, mThumb2StartAngle + mAngleOffset, -mPreviousAntiClockwiseProgressSweep, false,
                    mProgressPaint);
            canvas.drawCircle(mTranslateX - mThumb1XPos, mTranslateY - mThumb1YPos, mThumb1Radius, mPointerPaint1);
            canvas.drawText("L", mTranslateX - mThumb1XPos, mTranslateY - mThumb1YPos + 5, mPointerTextPaint1);
            canvas.drawCircle(mTranslateX - mThumb2XPos, mTranslateY - mThumb2YPos, mThumb1Radius, mPointerPaint2);
            canvas.drawText("R", mTranslateX - mThumb2XPos, mTranslateY - mThumb2YPos + 5, mPointerTextPaint2);
            mReCalculcate = false;
            return;

        }


        if (mArcClockwise && mProgressSweep != 360 && mProgressSweep != 0 && !mIsClicked) {
            canvas.drawArc(mArcRect, mThumb1StartAngle + mAngleOffset, mPreviousClockwiseProgressSweep, false,
                    mProgressPaint);

            canvas.drawArc(mArcRect, mThumb2StartAngle + mAngleOffset, -mPreviousAntiClockwiseProgressSweep, false,
                    mProgressPaint);
        } else if (!mArcClockwise && mProgressSweep != 360 && mProgressSweep != 0 && !mIsClicked) {

            canvas.drawArc(mArcRect, mThumb2StartAngle + mAngleOffset, -mPreviousAntiClockwiseProgressSweep, false,
                    mProgressPaint);

            canvas.drawArc(mArcRect, mThumb1StartAngle + mAngleOffset, mPreviousClockwiseProgressSweep, false,
                    mProgressPaint);
        }

        if (mIsClicked) {
            canvas.drawArc(mArcRect, mThumb1StartAngle + mAngleOffset, mPreviousClockwiseProgressSweep, false,
                    mProgressPaint);

            canvas.drawArc(mArcRect, mThumb2StartAngle + mAngleOffset, -mPreviousAntiClockwiseProgressSweep, false,
                    mProgressPaint);
        }
        canvas.drawCircle(mTranslateX - mThumb1XPos, mTranslateY - mThumb1YPos, mThumb1Radius, mPointerPaint1);
        canvas.drawText("L", mTranslateX - mThumb1XPos, mTranslateY - mThumb1YPos + 10, mPointerTextPaint1);
        canvas.drawCircle(mTranslateX - mThumb2XPos, mTranslateY - mThumb2YPos, mThumb1Radius, mPointerPaint2);
        canvas.drawText("R", mTranslateX - mThumb2XPos, mTranslateY - mThumb2YPos + 10, mPointerTextPaint2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Logger.error("Order, onMeasure");
        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        mHeight = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        mWidth = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);

        final int min = Math.min(width, height);
        float top = 0;
        float left = 0;
        int arcDiameter = 0;

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        arcDiameter = min - getPaddingLeft();
        mArcRadius = arcDiameter / 2;
        top = height / 2 - (arcDiameter / 2);
        left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        if (intialSetup && !mReCalculcate) {
            int arcStart1 = mThumb1StartAngle + mRotation + 90;

            mThumb1XPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart1)));
            mThumb1YPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart1)));

            int arcStart2 = mThumb2StartAngle + mRotation + 90;

            mThumb2XPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart2)));
            mThumb2YPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart2)));
            intialSetup = false;

        }

        setTouchInSide(mTouchInside);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * check if ANC is on
     */
    public boolean isOff() {
        return isOff;
    }

    /**
     * Set ANC flag
     */

    public void setIsOff(boolean isOff) {
        this.isOff = isOff;
    }

    /**
     * Get the calculation flag.
     */
    public boolean getreCalculcate() {
        return mReCalculcate;
    }

    /**
     * Set the  calculation flag
     */

    public void setreCalculcate(boolean mReCalculcate) {
        this.mReCalculcate = mReCalculcate;
    }

    public void checkTouchDown(MotionEvent event) {
        int factor = JBLConstant.THUMB_TOUCH_FACTOR;
        RectF leftThumbRect = new RectF();
        leftThumbRect.set(mTranslateX - mThumb1XPos - mThumb1Radius - (factor / 2), mTranslateY - mThumb1YPos - mThumb1Radius - (factor / 2), mTranslateX - mThumb1XPos + mThumb1Radius + (factor / 2), mTranslateY - mThumb1YPos + mThumb1Radius + (factor / 2));

        RectF rightThumbRect = new RectF();
        rightThumbRect.set(mTranslateX - mThumb2XPos - mThumb2Radius - (factor / 2), mTranslateY - mThumb2YPos - mThumb1Radius - (factor / 2), mTranslateX - mThumb2XPos + mThumb1Radius + (factor / 2), mTranslateY - mThumb2YPos + mThumb1Radius + (factor / 2));

        if (rightThumbRect.contains(event.getX(), event.getY()) || leftThumbRect.contains(event.getX(), event.getY())) {
            wasThumbTouched = true;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isOff)
            return true;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkTouchDown(event);
                if (!(Math.sqrt(Math.pow((mArcRect.centerX() - event.getX()), 2) + Math.pow((mArcRect.centerY() - event.getY()), 2)) > mArcRect.width() / 2) || wasThumbTouched) {
                    dis1 = Math.sqrt(((mTranslateX - mThumb1XPos - event.getX()) * (mTranslateX - mThumb1XPos - event.getX()) + (mTranslateY - mThumb1YPos - event.getY()) * (mTranslateY - mThumb1YPos - event.getY())));
                    dis2 = Math.sqrt(((mTranslateX - mThumb2XPos - event.getX()) * (mTranslateX - mThumb2XPos - event.getX()) + (mTranslateY - mThumb2YPos - event.getY()) * (mTranslateY - mThumb2YPos - event.getY())));
                    onStartTrackingTouch();
                    updateOnTouch(event, true);
                }
//                if (ancAwarehome != null) {
//                    ancAwarehome.removePolling();
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouchOnMove(event);
                break;
            case MotionEvent.ACTION_UP:
                dis1 = mThumb1Radius + 999;
                dis2 = mThumb2Radius + 999;
                // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
                //Just skip the updateOnTouch() when wasThumTouch is TRUE
                if (!wasThumbTouched) {
                    if (!(Math.sqrt(Math.pow((mArcRect.centerX() - event.getX()), 2) + Math.pow((mArcRect.centerY() - event.getY()), 2)) > mArcRect.width() / 2) || wasThumbTouched) {
                        dis1 = Math.sqrt(((mTranslateX - mThumb1XPos - event.getX()) * (mTranslateX - mThumb1XPos - event.getX()) + (mTranslateY - mThumb1YPos - event.getY()) * (mTranslateY - mThumb1YPos - event.getY())));
                        dis2 = Math.sqrt(((mTranslateX - mThumb2XPos - event.getX()) * (mTranslateX - mThumb2XPos - event.getX()) + (mTranslateY - mThumb2YPos - event.getY()) * (mTranslateY - mThumb2YPos - event.getY())));
                        onStartTrackingTouch();
                        updateOnTouch(event, false);
                    }
                }
                wasThumbTouched = false;


                onStopTrackingTouch();
                setPressed(false);
                getCircularInsideLayout().setSelectedViewStatus(-1);
//                if (ancAwarehome != null) {
//                    ancAwarehome.remove_addPollingAgain();
//                }
                break;
            case MotionEvent.ACTION_CANCEL:
                dis1 = mThumb1Radius + 999;
                dis2 = mThumb2Radius + 999;
                wasThumbTouched = false;
                onStopTrackingTouch();
                setPressed(false);
//                if (ancAwarehome != null) {
//                    ancAwarehome.remove_addPollingAgain();
//                }
                break;
        }

        return true;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb1 != null && mThumb1.isStateful()) {
            int[] state = getDrawableState();
            mThumb1.setState(state);
        }
        invalidate();
    }

    /**
     * <p>Called when user is moving the thumbs i.e LEFT or RIGHT awareness</p>
     */
    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
        }
    }

    /**
     * <p>Called when user has stopped  moving the thumbs i.e LEFT or RIGHT awareness</p>
     */

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
        }
    }

    /**
     * <p>Moves thumbs to 0 positions</p>
     */
    private void setDecrementDemo() {
        int decFactor = 0;
        if (leftProgress > 0) {
            decFactor = (leftProgress * 10) / 100;
            if (decFactor == 0) {
                if (leftProgress >= 2) {
                    decFactor = 2;
                } else {
                    decFactor = 1;
                }
            }
            leftProgress -= decFactor;
            decFactor = 0;
        }

        if (rightProgress > 0) {
            decFactor = (rightProgress * 10) / 100;
            if (decFactor == 0) {
                if (rightProgress >= 2) {
                    decFactor = 2;
                } else {
                    decFactor = 1;
                }
            }
            rightProgress -= decFactor;

        }


    }

    /**
     * @deprecated <p>Moves thumbs to 0 positions</p>
     */
    private void setDecrement() {
        if (leftProgress >= 2) {
            leftProgress -= 2;
        } else if (leftProgress > 0)
            --leftProgress;
        if (rightProgress >= 2) {
            rightProgress -= 2;
        } else if (rightProgress > 0)
            --rightProgress;

    }

    /**
     * <p>Set the value of the thumb position i.e LOW(1) , MEDIUM(2) , HIGH(3) or OFF(0)</p>
     */
    public void setmViewPosition(int value) {
        this.mViewPosition = value;
    }

    /**
     * @param leftFactor  position to move for left
     * @param rightFactor position to move for right
     * @param caseValue   Thumb position i.e LOW(1) , MEDIUM(2) , HIGH(3) or OFF(0)
     */
    public void initProgress(int leftFactor, int rightFactor, int caseValue) {
        mViewPosition = caseValue;
        this.leftFactor = leftFactor;
        this.rightFactor = rightFactor;
        if (caseValue == -1) {
            leftProgress = 0;
            rightProgress = 0;
        }
        mHandler1.removeCallbacks(runnable);
        mHandler1.postDelayed(runnable, 10);
    }

    public int getTouchRectIdDown() {
        return touchRectIdDown;
    }

    public void setTouchRectIdDown(int touchRectId) {
        this.touchRectIdDown = touchRectId;
    }


    /**
     * Update the view on touch
     *
     * @param event
     */
    int touchRectIdDown = -1;

    public int getTouchRectUp() {
        return touchRectUp;
    }

    public void setTouchRectUp(int touchRectUp) {
        this.touchRectUp = touchRectUp;
    }

    int touchRectUp = -1;

    private void updateOnTouch(MotionEvent event, boolean isDownTouch) {
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());


        float x = event.getX();
        float y = event.getY();
        if (dis1 <= mThumb1Radius * 2) {
            // Touch Left thumb
            mHandler1.removeCallbacks(runnable);
            mArcClockwise = true;
        } else if (dis2 <= mThumb2Radius * 2) {
            // Touch Right thumb
            mHandler1.removeCallbacks(runnable);
            mArcClockwise = false;
        } else {
            mIsClicked = true;

            // Nothing
            int diffHeight = (mHeight - getCircularInsideLayout().getMeasuredHeight()) / 2;
            int diffWidth = (mWidth - getCircularInsideLayout().getMeasuredWidth()) / 2;
            if (mIsClicked) {
                if (getCircularInsideLayout() != null) {

                    if (!ignoreTouch(x, y)) {
                        int id = getCircularInsideLayout().checkReside(event.getX() - diffWidth, event.getY() - diffHeight);
                        switch (id) {

                            case R.id.high:
                                mViewPosition = 3;
                                break;
                            case R.id.medium:
                                mViewPosition = 2;
                                break;
                            case R.id.low:
                                mViewPosition = 1;

                        }
                        if (isDownTouch)
                            setTouchRectIdDown(id);
                        else
                            setTouchRectUp(id);
                    }
                }
            }
            if (!isDownTouch && getTouchRectUp() == getTouchRectIdDown()) {
                int id = getCircularInsideLayout().checkResideAndSendCommand(event.getX() - diffWidth, event.getY() - diffHeight);
                switch (id) {

                    case R.id.high:
                        mViewPosition = 3;
                        break;
                    case R.id.medium:
                        mViewPosition = 2;
                        break;
                    case R.id.low:
                        mViewPosition = 1;

                }
                mHandler1.postDelayed(runnable, 10);
            }
            return;
        }

        if (mArcClockwise)
            mTouchAngle = mTouchAngle - mThumb1StartAngle;
        else
            mTouchAngle = mThumb2StartAngle - mTouchAngle;
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true, mArcClockwise);
    }

    /**
     * Update the view on touch
     *
     * @param event
     */

    private void updateOnTouchOnMove(MotionEvent event) {
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        if (dis1 <= mThumb1Radius * 2) {
            // Touch Left thumb
            mHandler1.removeCallbacks(runnable);
            mArcClockwise = true;
        } else if (dis2 <= mThumb2Radius * 2) {
            // Touch Right thumb
            mHandler1.removeCallbacks(runnable);
            mArcClockwise = false;
        } else {
            return;
        }

        if (mArcClockwise)
            mTouchAngle = mTouchAngle - mThumb1StartAngle;
        else
            mTouchAngle = mThumb2StartAngle - mTouchAngle;
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true, mArcClockwise);
    }

    /**
     * Ignore the touch
     *
     * @param xPos
     * @param yPos
     * @return
     */
    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        //mTouchIgnoreRadius = mArcRadius -14 ;
        if (touchRadius > mArcRadius) {
            ignore = true;
        }
//        if (xPos > mTranslateX-60   && xPos < 60 + mTranslateX)
//            ignore = true;
        return ignore;
    }

    /**
     * Get the touch position in degree
     *
     * @param xPos
     * @param yPos
     * @return
     */
    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        //angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        if (mArcClockwise)
            return (float) mMax / (mStartAngle + mSweepAngle - mThumb1StartAngle);

        return
                (float) mMax / (mThumb2StartAngle - mStartAngle);
    }

    private void onProgressRefresh(int progress, boolean fromUser, boolean clockwise) {
        updateProgress(progress, fromUser, clockwise);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("PARENT", superState);
        state.putInt("MAX", mMax);
        state.putInt("thumb1X", mThumb1XPos);
        state.putInt("thumb1Y", mThumb1YPos);
        state.putInt("thumb2X", mThumb2XPos);
        state.putInt("thumb2Y", mThumb2YPos);
        state.putFloat("sweepClockwise", mPreviousClockwiseProgressSweep);
        state.putFloat("sweepAntiClockwise", mPreviousAntiClockwiseProgressSweep);
        state.putInt("leftProgress", leftProgress);
        state.putInt("rightProgress", rightProgress);
        state.putInt("arcRadius", mArcRadius);

        mHandler1.removeCallbacks(runnable);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);
        mMax = savedState.getInt("MAX");
        mThumb1XPos = savedState.getInt("thumb1X");
        mThumb1YPos = savedState.getInt("thumb1Y");
        mThumb2XPos = savedState.getInt("thumb2X");
        mThumb2YPos = savedState.getInt("thumb2Y");
        mPreviousClockwiseProgressSweep = savedState.getFloat("sweepClockwise");
        mPreviousAntiClockwiseProgressSweep = savedState.getFloat("sweepAntiClockwise");
        leftProgress = savedState.getInt("leftProgress");
        rightProgress = savedState.getInt("rightProgress");
        mArcRadius = savedState.getInt("arcRadius");
        invalidate();


    }

    /**
     * Update the thumb to new position
     *
     * @param clockwise
     * @param mProgressSweep
     */
    private void updateThumbPosition(boolean clockwise, float mProgressSweep) {


        if (clockwise) {
            int thumbAngle = (int) (mThumb1StartAngle + mProgressSweep + 90);
            mThumb1XPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
            mThumb1YPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
        } else {

            int thumbAngle = (int) (mThumb2StartAngle - mProgressSweep + mRotation + 90);
            mThumb2XPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
            mThumb2YPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));

        }
    }

    /***
     * Update the progress to new position
     *
     * @param progressLeft
     * @param progressRight
     * @param fromUser
     */
    public void updateProgressAtOnce(int progressLeft, int progressRight, boolean fromUser) {

        if (progressLeft == INVALID_PROGRESS_VALUE || progressRight == INVALID_PROGRESS_VALUE) {
            return;
        }

        /** Calculate for Left Thumb **/
        mProgressSweep = (float) progressLeft / mMax * (mStartAngle + mSweepAngle - mThumb1StartAngle) + (float) .1;
        mPreviousClockwiseProgressSweep = mProgressSweep;
        leftProgress = progressLeft;
        updateThumbPosition(true, mProgressSweep);
        /** Calculate for Right Thumb**/
        mProgressSweep = (float) progressRight / mMax * (mThumb2StartAngle - mStartAngle) + (float) .1;
        mPreviousAntiClockwiseProgressSweep = mProgressSweep;
        rightProgress = progressRight;
        updateThumbPosition(false, mProgressSweep);
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, leftProgress, rightProgress, fromUser);
        }
        invalidate();
    }

    /**
     * Update the progress to new position
     *
     * @param progress
     * @param fromUser
     * @param clockwise
     */
    private void updateProgress(int progress, boolean fromUser, boolean clockwise) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }


        progress = (progress > mMax) ? mMax : progress;
        progress = (mProgress < 0) ? 0 : progress;

        mProgress = progress;

        if (clockwise) {
            mProgressSweep = (float) progress / mMax * (mStartAngle + mSweepAngle - mThumb1StartAngle) + (float) .1;
            mPreviousClockwiseProgressSweep = mProgressSweep;
            leftProgress = progress;
        } else {
            mProgressSweep = (float) progress / mMax * (mThumb2StartAngle - mStartAngle) + (float) .1;
            mPreviousAntiClockwiseProgressSweep = mProgressSweep;
            rightProgress = progress;
        }

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, leftProgress, rightProgress, fromUser);
        }

        updateThumbPosition(clockwise, mProgressSweep);

        invalidate();
    }


    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l The seek bar notification listener
     * @see android.widget.SeekBar.OnSeekBarChangeListener
     */
    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
    }

    public void setProgress(int progress, boolean clockwise) {
        updateProgress(progress, false, clockwise);
    }

    public void setSwitchOff(boolean off) {


        mViewPosition = 0;
        mIsClicked = true;
        mHandler1.removeCallbacks(runnable);
        mHandler1.postDelayed(runnable, 10);
    }


    public void setTouchInSide(boolean isEnabled) {
        mTouchInside = isEnabled;
    }

//    public ANCFragment getAncAwarehome() {
//        return ancAwarehome;
//    }
//
//    public void setAncAwarehome(ANCFragment ancAwarehome) {
//        this.ancAwarehome = ancAwarehome;
//    }


    /**
     * <p> Listener for progress change</p>
     */
    public interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param ANCController The SeekArc whose progress has changed
         * @param leftProgress  The current progress level of the Left Seek Bar. This will be in the range
         *                      0..max where max was set by
         * @param fromUser      True if the progress change was initiated by the user.
         * @link ProgressArc#setMax(int)}. (The default value for
         * max is 100.)
         */
        void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param ANCController The SeekArc in which the touch gesture began
         */
        void onStartTrackingTouch(ANCController ANCController);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param ANCController The SeekArc in which the touch gesture began
         */
        void onStopTrackingTouch(ANCController ANCController);
    }
}
