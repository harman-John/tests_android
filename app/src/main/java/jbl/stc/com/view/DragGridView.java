package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;


import jbl.stc.com.adapter.EqNameGridAdapter;
import jbl.stc.com.listener.OnEqIndexChangeListener;
import jbl.stc.com.utils.LogUtil;

public class DragGridView extends GridView {
    private static final int DRAG_IMG_SHOW = 1;
    private static final int DRAG_IMG_NOT_SHOW = 0;
    private static final String TAG = DragGridView.class.getSimpleName();

    /**
     * 被拖动的视图
     */
    private ImageView dragImageView;
    private WindowManager.LayoutParams dragImageViewParams;
    private WindowManager windowManager;
    private boolean isViewOnDrag = false;
    private OnEqIndexChangeListener onChangeListener;

    /**
     * 前一次拖动的位置
     */
    private int preDraggedOverPosition = AdapterView.INVALID_POSITION;
    private int currDraggedPosition;
    private int downRawX;
    private int downRawY;

    /**
     * 长按选中拖动item
     */
    private OnItemLongClickListener onLongClickListener = new OnItemLongClickListener() {

        @Override
        //长按item开始拖动
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //记录长按item位置
            preDraggedOverPosition = position;

            //获取被长按item的drawing cache
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            //通过被长按item，获取拖动item的bitmap
            Bitmap dragBitmap = Bitmap.createBitmap(view.getDrawingCache());

            //设置拖动item的参数
            dragImageViewParams.gravity = Gravity.TOP | Gravity.LEFT;
            dragImageViewParams.width = (dragBitmap.getWidth());
            dragImageViewParams.height = (dragBitmap.getHeight());
            //设置触摸点为绘制拖动item的中心
            dragImageViewParams.x = (downRawX - dragImageViewParams.width / 2);
            dragImageViewParams.y = (downRawY - dragImageViewParams.height / 2);
            dragImageViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            dragImageViewParams.format = PixelFormat.TRANSLUCENT;
            dragImageViewParams.windowAnimations = 0;

            //dragImageView为被拖动item的容器，清空上一次的显示
            if ((int) dragImageView.getTag() == DRAG_IMG_SHOW) {
                windowManager.removeView(dragImageView);
                dragImageView.setTag(DRAG_IMG_NOT_SHOW);
            }

            //设置本次被长按的item
            dragImageView.setImageBitmap(dragBitmap);

            //添加拖动item到屏幕
            windowManager.addView(dragImageView, dragImageViewParams);
            dragImageView.setTag(DRAG_IMG_SHOW);
            isViewOnDrag = true;

            //设置被长按item不显示
            ((EqNameGridAdapter) getAdapter()).hideView(position);
            return true;
        }
    };

    public DragGridView(Context context) {
        super(context);
        initView();
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        setOnItemLongClickListener(onLongClickListener);
        //初始化显示被拖动item的image view
        dragImageView = new ImageView(getContext());
        dragImageView.setTag(DRAG_IMG_NOT_SHOW);
        //初始化用于设置dragImageView的参数对象
        dragImageViewParams = new WindowManager.LayoutParams();
        //获取窗口管理对象，用于后面向窗口中添加dragImageView
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downRawX = (int) ev.getRawX();
                downRawY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isViewOnDrag) {
                    //设置触摸点为dragImageView中心
                    dragImageViewParams.x = (int) (ev.getRawX() - dragImageView.getWidth() / 2);
                    dragImageViewParams.y = (int) (ev.getRawY() - dragImageView.getHeight() / 2);
                    //更新窗口显示
                    windowManager.updateViewLayout(dragImageView, dragImageViewParams);
                    //获取当前触摸点的item position
                    currDraggedPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
                    //如果当前停留位置item不等于上次停留位置的item，交换本次和上次停留的item
                    if ((currDraggedPosition != AdapterView.INVALID_POSITION) && (currDraggedPosition != preDraggedOverPosition)) {
                        ((EqNameGridAdapter) getAdapter()).swapView(preDraggedOverPosition, currDraggedPosition);
                        if (onChangeListener != null) {
                            onChangeListener.onIndexChange(preDraggedOverPosition, currDraggedPosition);
                        }
                        preDraggedOverPosition = currDraggedPosition;
                        //itemMove(currDraggedPosition);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isViewOnDrag) {
                    ((EqNameGridAdapter) getAdapter()).showHideView();
                    if ((int) dragImageView.getTag() == DRAG_IMG_SHOW) {
                        windowManager.removeView(dragImageView);
                        dragImageView.setTag(DRAG_IMG_NOT_SHOW);
                    }
                    isViewOnDrag = false;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void itemMove(int dropPosition) {
        TranslateAnimation translateAnimation;
        LogUtil.d(TAG, "itemMove preDraggedOverPosition=" + preDraggedOverPosition + ",dropPosition=" + dropPosition);
        if (dropPosition < preDraggedOverPosition) {
            for (int i = dropPosition; i < preDraggedOverPosition; i++) {
                View view = getChildAt(i - getFirstVisiblePosition());
                View nextView = getChildAt(i + 1 - getFirstVisiblePosition());
                LogUtil.d(TAG, "itemMove i1=" + i + ",nextView=" + nextView);
                if (view != null && nextView != null) {
                    LogUtil.d(TAG, "itemMove i2=" + i);
                    float xValue = (nextView.getLeft() - view.getLeft()) * 1f / view.getWidth();
                    float yValue = (nextView.getTop() - view.getTop()) * 1f / view.getHeight();
                    translateAnimation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, xValue, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, yValue);
                    translateAnimation.setInterpolator(new LinearInterpolator());
                    translateAnimation.setFillAfter(true);
                    translateAnimation.setDuration(300);
                    if (i == preDraggedOverPosition - 1) {
                        translateAnimation.setAnimationListener(animationListener);
                    }
                    view.startAnimation(translateAnimation);
                }
            }
        } else {
            for (int i = preDraggedOverPosition + 1; i <= dropPosition; i++) {
                View view = getChildAt(i - getFirstVisiblePosition());
                View prevView = getChildAt(i - 1 - getFirstVisiblePosition());
                LogUtil.d(TAG, "itemMove i3=" + i + ",prevView=" + prevView + ",view=" + view);
                if (view != null && prevView != null) {
                    float xValue = (prevView.getLeft() - view.getLeft()) * 1f / view.getWidth();
                    float yValue = (prevView.getTop() - view.getTop()) * 1f / view.getHeight();
                    translateAnimation =
                            new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, xValue, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, yValue);
                    translateAnimation.setInterpolator(new LinearInterpolator());
                    translateAnimation.setFillAfter(true);
                    translateAnimation.setDuration(300);
                    if (i == dropPosition) {
                        translateAnimation.setAnimationListener(animationListener);
                    }
                    view.startAnimation(translateAnimation);
                }
            }
        }
    }

    /**
     * 动画监听器
     */
    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // 在动画完成时将adapter里的数据交换位置
            ListAdapter adapter = getAdapter();
            if (adapter != null && adapter instanceof EqNameGridAdapter) {
                ((EqNameGridAdapter) getAdapter()).swapView(preDraggedOverPosition, currDraggedPosition);
            }
            preDraggedOverPosition = currDraggedPosition;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public void setOnChangeListener(OnEqIndexChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }
}