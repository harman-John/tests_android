package jbl.stc.com.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 7/20/18.
 */

public class EqGridView extends GridView {
    public static final String TAG = EqGridView.class.getSimpleName();
    private CustomScrollView mScrollView;
    private boolean isDrag = false;
    private int mDownX;
    private int mDownY;
    private int moveX;
    private int moveY;
    private int mDragPosition;
    private View mStartDragItemView = null;
    private Button mDragImageView;
    private TextView mDragTextView;
    private FrameLayout mDragLayout;
    private int mDragLayoutSize = 140;
    private Vibrator mVibrator;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private Bitmap mDragBitmap;
    private int mPoint2ItemTop;
    private int mPoint2ItemLeft;
    private int mOffset2Top;
    private int mOffset2Left;
    private int mStatusHeight;
    private int mDownScrollBorder;
    private int mUpScrollBorder;
    private static final int speed = 20;
    private boolean mAnimationEnd = true;
    private DragGridBaseAdapter mDragAdapter;
    private int mNumColumns;
    private int mColumnWidth;
    private boolean mNumColumnsSet;
    private int mHorizontalSpacing;
    private int mViewHeight;
    private float mDragScale = 1.0f;
    private int mScaleMill = 200;
    private boolean mDragLastPosition = false;
    private int mDragStartPosition = 7;
    private boolean mIsScaleAnima = false;
    private boolean mIsVibrator = false;
    private int screenHeight = 0;
    private int screenWidth = 0;
    private int mRawX, mRawY;
    private View mEqArcView;
    private View mdragImage;

    public EqGridView(Context context) {
        this(context, null);
    }

    public EqGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragGridView);
        mDragScale = typedArray.getFloat(R.styleable.DragGridView_drag_scale, 1.0f);
        mScaleMill = typedArray.getInteger(R.styleable.DragGridView_scale_mill, 200);
        mDragStartPosition = typedArray.getInteger(R.styleable.DragGridView_drag_start_position, 7);
        mDragLastPosition = typedArray.getBoolean(R.styleable.DragGridView_drag_last_position, false);
        mIsVibrator = typedArray.getBoolean(R.styleable.DragGridView_vibrator, false);
        typedArray.recycle();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        if (!isInEditMode()) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mStatusHeight = getStatusHeight(context);
        }
        if (!mNumColumnsSet) {
            mNumColumns = AUTO_FIT;
        }

    }

    public void setScrollView(CustomScrollView scrollView) {
        mScrollView = scrollView;
    }

    public void setmEqArcView(View eqArcView) {
        mEqArcView = eqArcView;
    }

    public void setmTVDragImage(View tv_dragImage) {
        mdragImage = tv_dragImage;
    }

    private Handler mHandler = new Handler();

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if (!isInEditMode()) {
            if (adapter instanceof DragGridBaseAdapter) {
                mDragAdapter = (DragGridBaseAdapter) adapter;
            } else {
                throw new IllegalStateException(
                        "the adapter must be implements DragGridAdapter");
            }
        }
    }

    public void setDragStartPosition(int dragStartPosition) {
        mDragStartPosition = dragStartPosition;
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        mNumColumnsSet = true;
        this.mNumColumns = numColumns;
    }

    @Override
    public void setColumnWidth(int columnWidth) {
        super.setColumnWidth(columnWidth);
        mColumnWidth = columnWidth;
    }

    @Override
    public void setHorizontalSpacing(int horizontalSpacing) {
        super.setHorizontalSpacing(horizontalSpacing);
        this.mHorizontalSpacing = horizontalSpacing;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mNumColumns == AUTO_FIT) {
            int numFittedColumns;
            if (mColumnWidth > 0) {
                int gridWidth = Math.max(View.MeasureSpec.getSize(widthMeasureSpec)
                        - getPaddingLeft() - getPaddingRight(), 0);
                numFittedColumns = gridWidth / mColumnWidth;
                if (numFittedColumns > 0) {
                    while (numFittedColumns != 1) {
                        if (numFittedColumns * mColumnWidth
                                + (numFittedColumns - 1) * mHorizontalSpacing > gridWidth) {
                            numFittedColumns--;
                        } else {
                            break;
                        }
                    }
                } else {
                    numFittedColumns = 1;
                }
            } else {
                numFittedColumns = 2;
            }
            mNumColumns = numFittedColumns;
        }

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //mViewHeight = b - t;
    }

    public boolean setOnItemLongClickListener(final MotionEvent ev) {
        //Logger.d(TAG,"onItemLongClick");
        this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                Logger.d(TAG, "FirstVisiblePosition:" + getFirstVisiblePosition());

                mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
                mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
                mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
                Logger.d(TAG, "mPoint2ItemTop:" + mPoint2ItemTop + "mPoint2ItemLeft:" + mPoint2ItemLeft + "mStartDragItemView.Top:" + mStartDragItemView.getTop() + "mStartDragItemView.getLeft()" + mStartDragItemView.getLeft());
                mOffset2Top = (int) (ev.getRawY() - mDownY);
                mOffset2Left = (int) (ev.getRawX() - mDownX);
                Logger.d(TAG, "mOffset2Top:" + mOffset2Top + "mOffset2Left:" + mOffset2Left);
                Logger.d(TAG, "getHeight():" + getHeight() + "screenHeigth:" + screenHeight + "screenWidth:" + screenWidth);

                mDownScrollBorder = screenHeight / 6;
                mUpScrollBorder = screenHeight * 5 / 6;
                Logger.d(TAG, "mDownScrollBorder:" + mDownScrollBorder + "mUpScrollBorder:" + mUpScrollBorder);

                //mStartDragItemView.setDrawingCacheEnabled(true);
                //mDragBitmap = Bitmap.createScaledBitmap(drawingCache, (int) (drawingCache.getWidth() * mDragScale), (int) (drawingCache.getHeight() * mDragScale), true);

                //Bitmap drawingCache = mStartDragItemView.getDrawingCache();
                //mDragBitmap = Bitmap.createBitmap(drawingCache);
                //mStartDragItemView.destroyDrawingCache();

                isDrag = true;
                if (mIsVibrator) mVibrator.vibrate(50);
                mStartDragItemView.setVisibility(View.INVISIBLE);
                mScrollView.setScrollStop(true);
                createDragImage(mDragBitmap, mDownX, mDownY, mRawX, mRawY);
                mEqArcView.setVisibility(View.VISIBLE);
                Animation scaleOn = AnimationUtils.loadAnimation(JBLApplication.getJBLApplicationContext(), R.anim.anim_scale_on);
                mEqArcView.setAnimation(scaleOn);

                return false;
            }

            ;
        });
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return setOnItemLongClickListener(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mRawX = (int) ev.getRawX();
                mRawY = (int) ev.getRawY();
                Logger.d(TAG, "mDownX:" + String.valueOf(mDownX) + "mDownY:" + String.valueOf(mDownY));
                Logger.d(TAG, "mRawX:" + String.valueOf(ev.getRawX()) + "mRawY:" + String.valueOf(ev.getRawY()));
                mDragPosition = pointToPosition(mDownX, mDownY);
                Logger.d(TAG, "mDragPosition:" + mDragPosition);

                if (mDragPosition < mDragStartPosition) {
                    return super.dispatchTouchEvent(ev);
                }
                if (null != getAdapter() && mDragPosition == (getAdapter().getCount() - 1) && !mDragLastPosition) {
                    return super.dispatchTouchEvent(ev);
                }

                if (mDragPosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(ev);
                }


                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();
                if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
                    //    Logger.d(TAG,"notInTouch");
                } else {
                    //    Logger.d(TAG,"InTouch");
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mScrollRunnable);
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    /**
     * isTouchInItem
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchInItem(View dragView, int x, int y) {
        if (dragView == null) {
            return false;
        }
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop();
        if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
            return false;
        }

        if (y < topOffset || y > topOffset + dragView.getHeight()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDrag && mDragImageView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    moveX = (int) ev.getX();
                    moveY = (int) ev.getY();

                    mRawX = (int) ev.getRawX();
                    mRawY = (int) ev.getRawY();

                    Logger.d(TAG, "TouchEvent rawX:" + mRawX + "TouchEvent rawY:" + mRawY);
                    onDragItem(moveX, moveY, mRawX, mRawY);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopDrag((int) ev.getX(), (int) ev.getY());
                    isDrag = false;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * createDragImage
     *
     * @param bitmap
     * @param downX
     * @param downY
     */
    private void createDragImage(Bitmap bitmap, int downX, int downY, int rawX, int rawY) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = (int) (mRawX - UiUtils.dip2px(getContext(), mDragLayoutSize) / 2);
        mWindowLayoutParams.y = (int) (mRawY - UiUtils.dip2px(getContext(), mDragLayoutSize) / 2 - mStatusHeight);
        Logger.d(TAG, "createDragImage.x:" + mWindowLayoutParams.x + "createDragImage.y" + mWindowLayoutParams.y + "rawY:" + rawY);
        mWindowLayoutParams.alpha = 1.0f;
        mWindowLayoutParams.width = UiUtils.dip2px(getContext(), mDragLayoutSize);
        mWindowLayoutParams.height = UiUtils.dip2px(getContext(), mDragLayoutSize);
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        TextView textView = (mStartDragItemView.findViewById(R.id.tv_eqname));
        /*mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mDragImageView.setBackgroundResource(R.drawable.drag_bubble);
        mDragImageView.setLayoutParams(layoutParams);
        mDragTextView = new TextView(getContext());
        mDragTextView.setText(textView.getText().toString());
        mDragTextView.setGravity(Gravity.CENTER);
        mDragTextView.setPadding(UiUtils.dip2px(getContext(), 15), 0, UiUtils.dip2px(getContext(), 15), 0);
        mDragTextView.setLayoutParams(layoutParams);
        mDragLayout = new FrameLayout(getContext());
        mDragLayout.addView(mDragImageView);
        mDragLayout.addView(mDragTextView);
        mWindowManager.addView(mDragLayout, mWindowLayoutParams);*/

        mDragImageView = new Button(getContext());
        mDragImageView.setBackgroundResource(R.drawable.drag_bubble);
        mDragImageView.setText(textView.getText().toString());
        mDragImageView.setGravity(Gravity.CENTER);
        mDragImageView.setLayoutParams(layoutParams);
        mDragLayout = new FrameLayout(getContext());
        mDragLayout.addView(mDragImageView);
        mWindowManager.addView(mDragLayout, mWindowLayoutParams);


        /*mdragImage.setVisibility(View.VISIBLE);
        int marginTop=(int) (mRawY - mdragImage.getHeight()/2 - mStatusHeight);
        int marginBottom=(int) (screenHeight-marginTop-mdragImage.getHeight());
        int marginLeft=(int) (mRawX  - mdragImage.getWidth()/2);
        int marginRight=(int) (screenWidth-marginLeft-mdragImage.getWidth());
        mdragImage.setPadding(marginLeft,marginTop,marginRight,marginBottom);*/
    }


    private void removeDragImage() {
        if (mDragImageView != null) {
            mWindowManager.removeView(mDragLayout);
            mDragLayout = null;
            mDragImageView = null;
        }
    }

    private boolean IsDeleteArea(int x, int y) {

        int deleteIconSize = (int) mEqArcView.getWidth() / 2;
        Logger.d(TAG, "DeleteArea CurrentX:" + x + "DeleteArea CurrentY:" + y + "deleteIconSize:" + deleteIconSize + "screenHeight:" + screenHeight + "screenWidth:" + screenWidth);
        if (y > (screenHeight - deleteIconSize) && x > (screenWidth - deleteIconSize)) {
            return true;
        }
        return false;

    }

    /**
     * drag  item;swap item ;and scrollview scroll
     */
    private void onDragItem(int moveX, int moveY, int rawX, int rawY) {
        mWindowLayoutParams.x = (int) (mRawX - UiUtils.dip2px(getContext(), mDragLayoutSize) / 2);
        mWindowLayoutParams.y = (int) (mRawY - UiUtils.dip2px(getContext(), mDragLayoutSize) / 2 - mStatusHeight);
        mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams);

        Logger.d(TAG, "onDragItem  x:" + mWindowLayoutParams.x + "onDragItem y:" + mWindowLayoutParams.y);

        onSwapItem(moveX, moveY);


        int currentY = mRawY;
        int currentX = mRawX;
        System.out.println("CurrentY" + currentY + "CurrentX" + currentX);
        if (IsDeleteArea(currentX, currentY)) {
            Logger.d(TAG, "IsDeleteArea:True");
            startScaleAnimation();
        } else {
            Logger.d(TAG, "IsDeleteArea:false");
            // ScrollView scroll
            mHandler.post(mScrollRunnable);
            mIsScaleAnima = false;
            mDragImageView.clearAnimation();
            mWindowLayoutParams.width = UiUtils.dip2px(getContext(), mDragLayoutSize);
            mWindowLayoutParams.height = UiUtils.dip2px(getContext(), mDragLayoutSize);
            mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams);
        }
    }

    private void startScaleAnimation() {
        if (!mIsScaleAnima) {
            mIsScaleAnima = true;
            ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.25f);
            AnimationSet anim = new AnimationSet(false);
            anim.addAnimation(scaleAnim);
            anim.addAnimation(translateAnim);
            anim.setDuration(mScaleMill);
            anim.setFillAfter(true);
            mDragImageView.clearAnimation();
            mDragImageView.startAnimation(anim);
            mWindowLayoutParams.width = UiUtils.dip2px(getContext(), mDragLayoutSize * 6 / 10);
            mWindowLayoutParams.height = UiUtils.dip2px(getContext(), mDragLayoutSize * 6 / 10);
            mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams);

        }
    }

    /**
     * scrollview scroll
     */
    private Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            int scrollY;
            Logger.d(TAG, "mScrollView Y:" + mScrollView.getScrollY());
            if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
                mHandler.removeCallbacks(mScrollRunnable);
            }

            //int currentY = mRawY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
            int currentY = mRawY;
            if (currentY > mUpScrollBorder) {
                scrollY = speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            } else if (currentY < mDownScrollBorder) {
                scrollY = -speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            } else {
                scrollY = 0;
                mHandler.removeCallbacks(mScrollRunnable);
            }

            Logger.d(TAG, "currentY:" + currentY + "---scrollY:" + scrollY + "-----mUpScrollBorder:" + mUpScrollBorder + "-----mDownScrollBorder:" + mDownScrollBorder);
            mScrollView.smoothScrollBy(0, scrollY);
        }
    };


    /**
     * onSwapItem
     *
     * @param moveX
     * @param moveY
     */
    private void onSwapItem(int moveX, int moveY) {
        final int tempPosition = pointToPosition(moveX, moveY);
        Logger.d(TAG, "tempPosition:" + tempPosition + "mDragStartPosition:" + mDragStartPosition);

        if (tempPosition < mDragStartPosition) {
            return;
        }

        if (null != getAdapter() && tempPosition == (getAdapter().getCount() - 1) && !mDragLastPosition) {
            return;
        }

        if (tempPosition != mDragPosition
                && tempPosition != AdapterView.INVALID_POSITION
                && mAnimationEnd
                ) {

            mDragAdapter.reorderItems(mDragPosition, tempPosition);
            mDragAdapter.setHideItem(tempPosition);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    //animateReorder(mDragPosition, tempPosition);
                    mDragPosition = tempPosition;
                    return true;
                }
            });

        }
    }

    /**
     * createTranslationAnimations
     *
     * @param view
     * @param startX
     * @param endX
     * @param startY
     * @param endY
     * @return
     */
    private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }

    /**
     * animateReorder
     *
     * @param oldPosition
     * @param newPosition
     */
    private void animateReorder(final int oldPosition, final int newPosition) {
        boolean isForward = newPosition > oldPosition;
        List<Animator> resultList = new LinkedList<Animator>();
        if (isForward) {
            for (int pos = oldPosition; pos < newPosition; pos++) {
                View view = getChildAt(pos - getFirstVisiblePosition());

                if (null == view) return;

                if ((pos + 1) % mNumColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            -view.getWidth() * (mNumColumns - 1), 0,
                            view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth(), 0, 0, 0));
                }
            }
        } else {
            for (int pos = oldPosition; pos > newPosition; pos--) {
                View view = getChildAt(pos - getFirstVisiblePosition());

                if (null == view) return;

                if ((pos + mNumColumns) % mNumColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth() * (mNumColumns - 1), 0,
                            -view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            -view.getWidth(), 0, 0, 0));
                }
            }
        }

        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(300);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimationEnd = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationEnd = true;
            }
        });
        resultSet.start();
    }

    /**
     * onStopDrag
     */
    private void onStopDrag(int moveX, int moveY) {
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        mDragAdapter.setHideItem(-1);
        removeDragImage();
        Logger.d(TAG, "moveY:" + String.valueOf(moveY) + "mViewHeight:" + String.valueOf(mViewHeight));
        int currentY = mRawY;
        int currentX = mRawX;
        if (IsDeleteArea(currentX, currentY)) {
            if (mIsVibrator) mVibrator.vibrate(50);
            mIsScaleAnima = false;
            mDragAdapter.deleteItem(mDragPosition);
        }
        mScrollView.setScrollStop(false);
        mStartDragItemView.findViewById(R.id.image_view_select).setVisibility(View.GONE);
        mStartDragItemView.findViewById(R.id.image_view).setVisibility(View.VISIBLE);
        Animation scaleOff = AnimationUtils.loadAnimation(JBLApplication.getJBLApplicationContext(), R.anim.anim_scale_off);
        mEqArcView.setAnimation(scaleOff);
        mEqArcView.setVisibility(View.GONE);
    }

    /**
     * getStatusHeight
     *
     * @param context
     * @return
     */
    private static int getStatusHeight(Context context) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass
                        .getField("status_bar_height").get(localObject)
                        .toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    public interface DragGridBaseAdapter {

        /**
         * reorderItems
         *
         * @param oldPosition
         * @param newPosition
         */
        void reorderItems(int oldPosition, int newPosition);


        /**
         * setHideItem
         *
         * @param hidePosition
         */
        void setHideItem(int hidePosition);


        /**
         * deleteItem
         *
         * @param deletePosition
         */
        void deleteItem(int deletePosition);
    }


}
