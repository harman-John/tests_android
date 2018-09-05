package jbl.stc.com.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import java.util.LinkedList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.logger.Logger;


public class MyDragGridView extends GridView {
    public static final String TAG = MyDragGridView.class.getSimpleName();
    private long dragResponseMS = 1000;
    private boolean isDrag = false;
    private int mDownX;
    private int mDownY;
    private int moveX;
    private int moveY;
    private int mDragPosition;
    private View mStartDragItemView = null;
    private View mGridViewItem = null;
    private ImageView mDragImageView;
    private FrameLayout mDragLayout;
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
    private float mDragScale = 1.2f;
    private int mScaleMill = 200;
    private boolean mDragLastPosition = false;
    private int mDragStartPosition = 7;
    private boolean mIsScaleAnima = false;

    private boolean mIsVibrator = false;

    public MyDragGridView(Context context) {
        this(context, null);
    }

    public MyDragGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragGridView);
        mDragScale = typedArray.getFloat(R.styleable.DragGridView_drag_scale, 1.2f);
        mScaleMill = typedArray.getInteger(R.styleable.DragGridView_scale_mill, 200);
        mDragStartPosition = typedArray.getInteger(R.styleable.DragGridView_drag_start_position, 7);
        mDragLastPosition = typedArray.getBoolean(R.styleable.DragGridView_drag_last_position, false);
        mIsVibrator = typedArray.getBoolean(R.styleable.DragGridView_vibrator, false);
        typedArray.recycle();

        if (!isInEditMode()) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mStatusHeight = getStatusHeight(context); // 获取状态栏的高度

        }
        if (!mNumColumnsSet) {
            mNumColumns = AUTO_FIT;
        }

    }

    private View mDeleteView;

    public void setDeleteView(View deleteView) {
        mDeleteView = deleteView;
    }

    private RelativeLayout relativeLayoutMenu;

    public void setMenuBar(RelativeLayout relativeLayout) {
        relativeLayoutMenu = relativeLayout;
    }

    private void showDeleteView() {
        mDeleteView.setVisibility(View.VISIBLE);
        Animation scaleOn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_on);
        mDeleteView.setAnimation(scaleOn);
    }

    private void hideDeleteView() {
        Animation scaleOff = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_off);
        mDeleteView.setAnimation(scaleOff);
        mDeleteView.setVisibility(View.GONE);
    }

    private Handler mHandler = new Handler();

    private Runnable mLongClickRunnable = new Runnable() {

        @Override
        public void run() {
            isDrag = true;
            if (mIsVibrator) mVibrator.vibrate(50);
            showDeleteView();
            mDragAdapter.setHideItem(mDragPosition);
            mStartDragItemView.setVisibility(View.INVISIBLE);
            createDragImage(mDragBitmap, mDownX, mDownY);
        }
    };

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
                int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec)
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

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mViewHeight = b - t;
    }

    public void setDragResponseMS(long dragResponseMS) {
        this.dragResponseMS = dragResponseMS;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();

                mDragPosition = pointToPosition(mDownX, mDownY);
                if (mDragPosition < mDragStartPosition) {
                    return super.dispatchTouchEvent(ev);
                }
                if (null != getAdapter() && mDragPosition == (getAdapter().getCount() - 1) && !mDragLastPosition) {
                    return super.dispatchTouchEvent(ev);
                }

                if (mDragPosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(ev);
                }

                if (mDragPosition == getLastVisiblePosition()) {
                    return super.dispatchTouchEvent(ev);
                }
                mHandler.postDelayed(mLongClickRunnable, dragResponseMS);

                mGridViewItem = getChildAt(mDragPosition - getFirstVisiblePosition());

                mStartDragItemView = mGridViewItem.findViewById(R.id.relative_layout_item_connected_before_breathing_icon);

                int[] location = new int[2];
                mStartDragItemView.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1] - relativeLayoutMenu.getMeasuredHeight() - dip2px(getContext(), 20);
                int right = left + mGridViewItem.getMeasuredWidth();
                int bottom = top + mGridViewItem.getMeasuredHeight();

                mPoint2ItemTop = mDownY - top;
                mPoint2ItemLeft = mDownX - left;

                mOffset2Top = (int) (ev.getRawY() - mDownY);
                mOffset2Left = (int) (ev.getRawX() - mDownX);

                mDownScrollBorder = getHeight() / 5;
                mUpScrollBorder = getHeight() * 4 / 5;


//                mStartDragItemView.setDrawingCacheEnabled(true);
                mStartDragItemView.setDrawingCacheEnabled(true);
                Bitmap drawingCache = mStartDragItemView.getDrawingCache();
//                Bitmap drawingCache = mStartDragItemView.getDrawingCache();
                // mDragBitmap = Bitmap.createBitmap(drawingCache);
                mDragBitmap = Bitmap.createScaledBitmap(drawingCache, (int) (drawingCache.getWidth() * mDragScale), (int) (drawingCache.getHeight() * mDragScale), true);
                mStartDragItemView.destroyDrawingCache();
//                mStartDragItemView.destroyDrawingCache();

                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();
                if (!isInViewArea(mStartDragItemView, moveX, moveY)) {
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mLongClickRunnable);
                mHandler.removeCallbacks(mScrollRunnable);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public static int dip2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);

    }

    private boolean isTouchInItem(View dragView, int x, int y) {
        if (dragView == null) {
            return false;
        }
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop() + dragView.findViewById(R.id.text_view_item_connected_before_device_name).getHeight();
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

                    // move item
                    onDragItem(moveX, moveY);
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

    private synchronized void createDragImage(Bitmap bitmap, int downX, int downY) {
        removeDragImage();
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = (int) (downX - mPoint2ItemLeft * mDragScale + mOffset2Left);
        mWindowLayoutParams.y = (int) (downY - mPoint2ItemTop * mDragScale + mOffset2Top - mStatusHeight);
        mWindowLayoutParams.alpha = 1.0f; // 透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
//        mDragImageView.setAlpha((float) 0.5);
//        mDragImageView.setBackgroundResource(R.drawable.shape_product_circle);


        mDragLayout = new FrameLayout(getContext());
        mDragLayout.addView(mDragImageView);

        mWindowManager.addView(mDragLayout, mWindowLayoutParams);
    }

    private void removeDragImage() {
        if (mDragImageView != null) {
            mWindowManager.removeView(mDragLayout);
            mDragLayout = null;
            mDragImageView = null;
            mWindowLayoutParams = null;
        }
    }

    private void onDragItem(int moveX, int moveY) {
        mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;

        mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams); // 更新镜像的位置

        mDragAdapter.setHideItem(mDragPosition);
//        onSwapItem(moveX, moveY);

        mHandler.post(mScrollRunnable);


        //drag to this area and loose, then delete
        if (isInViewArea(mDeleteView, moveX, moveY)) {
//            if (mIsVibrator) mVibrator.vibrate(50);
            //shrink
            if (!mIsScaleAnima) {
                ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, .5f, 1.0f, .5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.25f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.25f);
                AnimationSet anim = new AnimationSet(false);
                anim.addAnimation(scaleAnim);
                anim.addAnimation(translateAnim);
                anim.setDuration(mScaleMill);
                anim.setFillAfter(true);

                mDragImageView.clearAnimation();
                mDragImageView.startAnimation(anim);
                mIsScaleAnima = true;
            }
        } else {
            if (mIsScaleAnima) {

                mDragImageView.clearAnimation();
                mIsScaleAnima = false;
            }
        }
    }


    private boolean isInViewArea(View view, float x, float y) {
        Logger.i(TAG, "moveX = " + x + ",moveY = " + y + ",mMenuHeight = " + relativeLayoutMenu.getMeasuredHeight());
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1] - relativeLayoutMenu.getMeasuredHeight() - dip2px(getContext(), 20);
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        Logger.i(TAG, "left = " + left + ",top = " + top + "right = " + right + ",bottom = " + bottom);
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }

    private Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            int scrollY;
            if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
                mHandler.removeCallbacks(mScrollRunnable);
            }

            if (moveY > mUpScrollBorder) {
                scrollY = speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            } else if (moveY < mDownScrollBorder) {
                scrollY = -speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            } else {
                scrollY = 0;
                mHandler.removeCallbacks(mScrollRunnable);
            }

            smoothScrollBy(scrollY, 10);
        }
    };

    private void onSwapItem(int moveX, int moveY) {
        final int tempPosition = pointToPosition(moveX, moveY);

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
            observer.addOnPreDrawListener(new OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    animateReorder(mDragPosition, tempPosition);
                    mDragPosition = tempPosition;
                    return true;
                }
            });

        }
    }

    private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }

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

    private void onStopDrag(int moveX, int moveY) {
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }

        hideDeleteView();
        mDragAdapter.setHideItem(-1);
        removeDragImage();

        if (isInViewArea(mDeleteView, moveX, moveY)) {
            mIsScaleAnima = false;
            mDragAdapter.deleteItem(mDragPosition);
        }
    }

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
         * @param oldPosition
         * @param newPosition
         */
        void reorderItems(int oldPosition, int newPosition);


        /**
         * @param hidePosition
         */
        void setHideItem(int hidePosition);


        /**
         * @param deletePosition
         */
        void deleteItem(int deletePosition);
    }
}
