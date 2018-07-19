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
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;


public class EqGridView extends GridView {
    public static final String TAG = EqGridView.class.getSimpleName();
    /**
     * DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置
     */
    private long dragResponseMS = 3000;

    private CustomScrollView mScrollView;

    /**
     * 是否可以拖拽，默认不可以
     */
    private boolean isDrag = false;

    private int mDownX;
    private int mDownY;
    private int moveX;
    private int moveY;
    /**
     * 正在拖拽的position
     */
    private int mDragPosition;

    /**
     * 刚开始拖拽的item对应的View
     */
    private View mStartDragItemView = null;

    /**
     * 用于拖拽的镜像，这里直接用一个ImageView
     */
    private ImageView mDragImageView;

    private TextView mDragTextView;

    /**
     * 用于拖拽的镜像，这里直接放入拖动的 Image
     */
    private FrameLayout mDragLayout;

    /**
     * 震动器
     */
    private Vibrator mVibrator;

    private WindowManager mWindowManager;
    /**
     * item镜像的布局参数
     */
    private WindowManager.LayoutParams mWindowLayoutParams;

    /**
     * 我们拖拽的item对应的Bitmap
     */
    private Bitmap mDragBitmap;

    /**
     * 按下的点到所在item的上边缘的距离
     */
    private int mPoint2ItemTop;

    /**
     * 按下的点到所在item的左边缘的距离
     */
    private int mPoint2ItemLeft;

    /**
     * DragGridView距离屏幕顶部的偏移量
     */
    private int mOffset2Top;

    /**
     * DragGridView距离屏幕左边的偏移量
     */
    private int mOffset2Left;

    /**
     * 状态栏的高度
     */
    private int mStatusHeight;

    /**
     * DragGridView自动向下滚动的边界值
     */
    private int mDownScrollBorder;

    /**
     * DragGridView自动向上滚动的边界值
     */
    private int mUpScrollBorder;

    /**
     * DragGridView自动滚动的速度
     */
    private static final int speed = 20;

    private boolean mAnimationEnd = true;

    private DragGridBaseAdapter mDragAdapter;
    private int mNumColumns;
    private int mColumnWidth;
    private boolean mNumColumnsSet;
    private int mHorizontalSpacing;

    private int mViewHeight;
    /**
     * 拖动时景象放大倍数
     */
    private float mDragScale = 1.0f;
    /**
     * 大小变化时间单位毫秒
     */
    private int mScaleMill = 200;
    /**
     * 最后一个 position是否能够移动
     */
    private boolean mDragLastPosition = false;
    /**
     * position 位置的可以开始拖动
     */
    private int mDragStartPosition = 7;
    /**
     * 是否正在执行缩放动画
     */
    private boolean mIsScaleAnima = false;

    private boolean mIsVibrator = false;

    private int marginTopHeight=0;
    private int screenHeight=0;
    private int screenWidth=0;
    private int mRawX,mRawY;
    private ImageView mIvRemove;
    private List<EQModel> mEqModels=new ArrayList<>();

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

        marginTopHeight=UiUtils.dip2px(getContext(),340);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth =dm.widthPixels;

        if (!isInEditMode()) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mStatusHeight = getStatusHeight(context); // 获取状态栏的高度

        }
        if (!mNumColumnsSet) {
            mNumColumns = AUTO_FIT;
        }

    }

    public void setScrollView(CustomScrollView scrollView){
        mScrollView = scrollView;
    }

    public void setMIvRemove(ImageView ivRemove){
            mIvRemove=ivRemove;
    }

    public void setMEqModels(List<EQModel> eqModels){
        mEqModels=eqModels;
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

    /**
     * 若设置为AUTO_FIT，计算有多少列
     */
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
        mViewHeight = b - t;
    }

    /**
     * 设置响应拖拽的毫秒数，默认是1000毫秒
     *
     * @param dragResponseMS
     */
    public void setDragResponseMS(long dragResponseMS) {
        this.dragResponseMS = dragResponseMS;
    }

    public boolean setOnItemLongClickListener(final MotionEvent ev) {
        //Logger.d(TAG,"onItemLongClick");
        this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                Logger.d(TAG,"FirstVisiblePosition:"+getFirstVisiblePosition());
                // 根据position获取该item所对应的View
                mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
                //mStartDragItemView.findViewById(R.id.image_view_select).setVisibility(View.VISIBLE);
                //mStartDragItemView.findViewById(R.id.image_view).setVisibility(View.GONE);

                // 下面这几个距离大家可以参考我的博客上面的图来理解下
                //mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
                //mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
                mPoint2ItemTop = mRawY - mStartDragItemView.getTop();
                mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

                Logger.d(TAG,"mPoint2ItemTop:"+mPoint2ItemTop+"mPoint2ItemLeft:"+mOffset2Left+"mStartDragItemView.Top:"+mStartDragItemView.getTop());

                mOffset2Top = (int) (ev.getRawY() - mDownY);
                mOffset2Left = (int) (ev.getRawX() - mDownX);

                Logger.d(TAG,"mOffset2Top:"+mOffset2Top+"mOffset2Left:"+mOffset2Left);


                Logger.d(TAG,"getHeight():"+getHeight()+"screenHeigth:"+screenHeight+"screenWidth:"+screenWidth);

                // 获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
                //mDownScrollBorder = getHeight() / 5;
                // 获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
                mDownScrollBorder = 50;
                mUpScrollBorder = screenHeight * 4 / 5 - 95 ;
                Logger.d(TAG,"mDownScrollBorder:"+mDownScrollBorder+"mUpScrollBorder:"+mUpScrollBorder);

                // 开启mDragItemView绘图缓存
                //mStartDragItemView.setDrawingCacheEnabled(true);
                // 获取mDragItemView在缓存中的Bitmap对象
                //mDragBitmap = Bitmap.createScaledBitmap(drawingCache, (int) (drawingCache.getWidth() * mDragScale), (int) (drawingCache.getHeight() * mDragScale), true);

                //Bitmap drawingCache = mStartDragItemView.getDrawingCache();
                //mDragBitmap = Bitmap.createBitmap(drawingCache);
                // 这一步很关键，释放绘图缓存，避免出现重复的镜像
                //mStartDragItemView.destroyDrawingCache();




                isDrag = true; // 设置可以拖拽
                if (mIsVibrator) mVibrator.vibrate(50); // 震动一下
                mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item
                mScrollView.setScrollStop(true);
                // 根据我们按下的点显示item镜像
                createDragImage(mDragBitmap, mDownX, mDownY,mRawX,mRawY,140);
                mIvRemove.setVisibility(View.VISIBLE);
                mIvRemove.setImageResource(R.mipmap.eq_delete);
                return false;
            };
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
                mRawY =(int) ev.getRawY();
                Logger.d(TAG,"mDownX:"+String.valueOf(mDownX)+"mDownY:"+String.valueOf(mDownY));
                Logger.d(TAG,"mRawX:"+String.valueOf(ev.getRawX())+"mRawY:"+String.valueOf(ev.getRawY()));
                // 根据按下的X,Y坐标获取所点击item的position
                mDragPosition = pointToPosition(mDownX, mDownY);
                Logger.d(TAG,"mDragPosition:"+mDragPosition);
                //如果是前mDragStartPosition不执行长按
                if (mDragPosition < mDragStartPosition) {
                    return super.dispatchTouchEvent(ev);
                }
                //如果是最后一位不交换
                if (null != getAdapter() && mDragPosition == (getAdapter().getCount() - 1) && !mDragLastPosition) {
                    return super.dispatchTouchEvent(ev);
                }

                if (mDragPosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(ev);
                }

                // 使用Handler延迟dragResponseMS执行mLongClickRunnable
                //mHandler.postDelayed(mLongClickRunnable, dragResponseMS);


                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();

                // 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
                if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
                //    Logger.d(TAG,"notInTouch");
                }else{
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
     * 是否点击在GridView的item上面
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

                    Logger.d(TAG,"TouchEvent rawX:"+mRawX+"TouchEvent rawY:"+mRawY);
                    // 拖动item
                    onDragItem(moveX, moveY ,mRawX ,mRawY);
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
     * 创建拖动的镜像
     *
     * @param bitmap
     * @param downX  按下的点相对父控件的X坐标
     * @param downY  按下的点相对父控件的X坐标
     */
    private void createDragImage(Bitmap bitmap, int downX, int downY ,int rawX ,int rawY,int size) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        //mWindowLayoutParams.x = (int) (downX - mPoint2ItemLeft * mDragScale + mOffset2Left);
        //mWindowLayoutParams.y = (int) (downY - mPoint2ItemTop * mDragScale + mOffset2Top - mStatusHeight);

        mWindowLayoutParams.x = (int) (downX - mPoint2ItemLeft * mDragScale + mOffset2Left);
        mWindowLayoutParams.y = (int) (rawY- mPoint2ItemTop * mDragScale + mOffset2Top - mStatusHeight);

        Logger.d(TAG,"createDragImage.x:"+mWindowLayoutParams.x+"createDragImage.y"+mWindowLayoutParams.y+"rawY:"+rawY);

        mWindowLayoutParams.alpha = 1.0f; // 透明度
        //mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.width = UiUtils.dip2px(getContext(),size);
        mWindowLayoutParams.height = UiUtils.dip2px(getContext(),size);
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
        layoutParams.gravity=Gravity.CENTER;
        TextView textView=(mStartDragItemView.findViewById(R.id.tv_eqname));
        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mDragImageView.setBackgroundResource(R.drawable.drag_bubble);
        mDragImageView.setLayoutParams(layoutParams);
        mDragTextView =new TextView(getContext());
        mDragTextView.setText(textView.getText().toString());
        mDragTextView.setGravity(Gravity.CENTER);
        mDragTextView.setLayoutParams(layoutParams);
        mDragLayout = new FrameLayout(getContext());
        mDragLayout.addView(mDragImageView);
        mDragLayout.addView(mDragTextView);

        mWindowManager.addView(mDragLayout, mWindowLayoutParams);

    }

    /**
     * 从界面上面移动拖动镜像
     */
    private void removeDragImage() {
        if (mDragImageView != null) {
            mWindowManager.removeView(mDragLayout);
            mDragLayout = null;
            mDragImageView = null;
            mDragTextView =null;
        }
    }

    private boolean IsDeleteArea(int x, int y){

        int deleteIconSize = UiUtils.dip2px(getContext(),25);
        int eqIconSize = UiUtils.dip2px(getContext(), 110);
        Logger.d(TAG,"deleteIconSize:"+ (screenWidth - deleteIconSize - eqIconSize ) +"removeIcon centerY:"+(screenHeight-UiUtils.dip2px(getContext(),25) - eqIconSize));
        if (y > (screenHeight - deleteIconSize - eqIconSize + 130) &&
                x > screenWidth - deleteIconSize - eqIconSize + 130){
            return true;
        }
        return false;
        /*int deleteIconSize = UiUtils.dip2px(getContext(),25);

        Logger.d(TAG,"deleteIconSize:"+ (deleteIconSize) +"removeIcon centerY:"+(screenHeight-UiUtils.dip2px(getContext(),25)));
        if (y > (screenHeight - deleteIconSize ) && x > (screenWidth - deleteIconSize)){
            return true;
        }
        return false;*/

    }

    /**
     * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
     */
    private void onDragItem(int moveX, int moveY ,int rawX, int rawY) {
        /*mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;

        mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams); // 更新镜像的位置*/

        mWindowLayoutParams.x = (int) (moveX - mPoint2ItemLeft + mOffset2Left);
        mWindowLayoutParams.y = (int) (rawY- mPoint2ItemTop + mOffset2Top - mStatusHeight);
        mWindowManager.updateViewLayout(mDragLayout, mWindowLayoutParams); // 更新镜像的位置

        Logger.d(TAG,"onDragItem  x:"+mWindowLayoutParams.x+"onDragItem y:"+mWindowLayoutParams.y);

        onSwapItem(moveX, moveY);

        //如果拖拽到删除区域删除

        int currentY = mRawY;
        int currentX = mRawX;
        int deleteIconSize = UiUtils.dip2px(getContext(),25);
        int eqIconSize = UiUtils.dip2px(getContext(), 110);
        if (!(currentX > screenWidth - deleteIconSize - eqIconSize + 130)){
            // GridView自动滚动
            mHandler.post(mScrollRunnable);

        }

        System.out.println("--Bright---RY-->" + currentY + "---RX---->" + currentX);
        if (IsDeleteArea(currentX,currentY)){

            Logger.d(TAG,"IsDeleteArea:True");

            mIvRemove.setVisibility(View.VISIBLE);
            mIvRemove.setImageResource(R.mipmap.icon_remove_eq);
            //startScaleAnimation();

        }else {
            Logger.d(TAG,"IsDeleteArea:false");
            mIvRemove.setVisibility(View.VISIBLE);
            mIvRemove.setImageResource(R.mipmap.eq_delete);
            mDragImageView.clearAnimation();
            mIsScaleAnima=false;
            mWindowLayoutParams.width = UiUtils.dip2px(getContext(),140);
            mWindowLayoutParams.height = UiUtils.dip2px(getContext(),140);
            mDragLayout.setLayoutParams(mWindowLayoutParams);
        }

        /*if (moveY > (mViewHeight)) {
            if (mIsVibrator) mVibrator.vibrate(50);
            //缩小
            if (!mIsScaleAnima) {
                ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, .5f, 1.0f, .5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.25f);
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
        }*/
    }

    private void startScaleAnimation() {
        if (!mIsScaleAnima){
            ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.25f);
            AnimationSet anim = new AnimationSet(false);
            anim.addAnimation(scaleAnim);
            anim.addAnimation(translateAnim);
            anim.setDuration(mScaleMill);
            anim.setFillAfter(true);
            mDragImageView.clearAnimation();
            mDragImageView.startAnimation(anim);
            mIsScaleAnima=true;
            mWindowLayoutParams.width = UiUtils.dip2px(getContext(),70);
            mWindowLayoutParams.height = UiUtils.dip2px(getContext(),70);
            mDragLayout.setLayoutParams(mWindowLayoutParams);
        }
    }



    /**
     * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
     * 否则不进行滚动
     */
    private Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            int scrollY;
            Logger.d(TAG,"mScrollView Y:"+mScrollView.getScrollY());
            if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
                mHandler.removeCallbacks(mScrollRunnable);
            }

            int currentY= mRawY - mPoint2ItemTop + mOffset2Top - mStatusHeight;

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

            Logger.d(TAG,"currentY:"+currentY+"---scrollY:"+scrollY+"-----mUpScrollBorder:"+mUpScrollBorder+"-----mDownScrollBorder:"+mDownScrollBorder);
            mScrollView.smoothScrollBy(0, scrollY);
        }
    };



    /**
     * 交换item,并且控制item之间的显示与隐藏效果
     *
     * @param moveX
     * @param moveY
     */
    private void onSwapItem(int moveX, int moveY) {
        // 获取我们手指移动到的那个item的position
        final int tempPosition = pointToPosition(moveX, moveY);
        Logger.d(TAG,"tempPosition:"+tempPosition+"mDragStartPosition:"+mDragStartPosition);

        //如果是前mDragStartPosition位不交换
        if (tempPosition < mDragStartPosition) {
            return;
        }

        //如果是最后一位不交换
        if (null != getAdapter() && tempPosition == (getAdapter().getCount() - 1) && !mDragLastPosition) {
            return;
        }

        // 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
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
     * 创建移动动画
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
     * item的交换动画效果
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
     * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
     */
    private void onStopDrag(int moveX, int moveY) {
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        mDragAdapter.setHideItem(-1);
        removeDragImage();
        Logger.d(TAG,"moveY:"+String.valueOf(moveY)+"mViewHeight:"+String.valueOf(mViewHeight));
        /*if (moveY > (mViewHeight)) {
            mIsScaleAnima = false;
            mDragAdapter.deleteItem(mDragPosition);
            mIvRemove.setVisibility(View.GONE);
        }*/
        int currentY=mRawY;
        int currentX=mRawX;
        if (IsDeleteArea(currentX,currentY)){
            if (mIsVibrator) mVibrator.vibrate(50);
            mIsScaleAnima = false;
            mDragAdapter.deleteItem(mDragPosition);
        }
        mScrollView.setScrollStop(false);
        mIvRemove.setVisibility(View.GONE);
        mStartDragItemView.findViewById(R.id.image_view_select).setVisibility(View.GONE);
        mStartDragItemView.findViewById(R.id.image_view).setVisibility(View.VISIBLE);
    }

    /**
     * 获取状态栏的高度
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
         * 重新排列数据
         *
         * @param oldPosition
         * @param newPosition
         */
        void reorderItems(int oldPosition, int newPosition);


        /**
         * 设置某个item隐藏
         *
         * @param hidePosition
         */
        void setHideItem(int hidePosition);


        /**
         * 删除某个 item
         *
         * @param deletePosition
         */
        void deleteItem(int deletePosition);
    }


}
