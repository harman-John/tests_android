package com.example.testcase.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.testcase.R;


public class TestShowFragmentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = TestShowFragmentActivity.class.getSimpleName();
    private float lastX = 0, startDownX=0;
    private float lastY = 0,startDownY =0;
    private int screenWidth;
    private int screenHeight;

    private boolean isIntercept = false;
    private View viewDelete;
    private Button button;
//    private Button,buttonAdd, buttonRemove,buttonStart;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_show_fragment);
        setTitle(R.string.test_show_fragment);
        viewDelete = findViewById(R.id.eqArcView);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        screenHeight = windowManager.getDefaultDisplay().getHeight();

        button.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //获取点击时x y 轴的数据
                        startDownX = lastX = event.getRawX();
                        startDownY = lastY = event.getRawY();
                        viewDelete.setVisibility(View.VISIBLE);
                        Animation scaleOn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_on);
                        viewDelete.setAnimation(scaleOn);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("test", "up" + event.getX() + ":" + event.getY());
                        //触摸弹起的时候来一个小动画
                        Animation scaleOff = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_off);
                        viewDelete.setAnimation(scaleOff);
                        viewDelete.setVisibility(View.GONE);
                        int lastMoveDx = Math.abs((int) event.getRawX() - (int)startDownX);
                        int lastMoveDy = Math.abs((int) event.getRawY() -(int) startDownY);
                        if (0 != lastMoveDx || 0 != lastMoveDy) {
                            isIntercept = true;
                        } else {
                            isIntercept = false;
                        }
                        // 每次移动都要设置其layout，不然由于父布局可能嵌套listview，当父布局发生改变冲毁（如下拉刷新时）则移动的view会回到原来的位置
                        RelativeLayout.LayoutParams lpFeedback = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lpFeedback.leftMargin = v.getLeft();
                        lpFeedback.topMargin = v.getTop();
                        lpFeedback.setMargins(v.getLeft(), v.getTop(), 0, 0);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        //在move中直接把得到的坐标设置为控件的坐标..果然天真单纯
                        // v.setX(event.getX());
                        // v.setY(event.getY());


                        //获得x y轴的偏移量
                        int dx = (int) (event.getRawX() - lastX);
                        int dy = (int) (event.getRawY() - lastY);

                        //获得控件上下左右的位置信息,加上我们的偏移量,新得到的位置就是我们
                        //控件将要出现的位置
                        int l = v.getLeft() + dx;
                        int b = v.getBottom() + dy;
                        int r = v.getRight() + dx;
                        int t = v.getTop() + dy;

                        //然后使用我们view的layout重新在布局中把我们的控件画出来
                        v.layout(l, t, r, b);
                        //并把现在的x y设置给lastx lasty
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        v.postInvalidate();//绘画

                        break;
                    default:
                        break;
                }
                return isIntercept;
            }
        });

//        buttonAdd = findViewById(R.id.button2);
//        buttonAdd.setOnClickListener(this);
//        buttonRemove = findViewById(R.id.button3);
//        buttonRemove.setOnClickListener(this);
//        buttonStart = findViewById(R.id.button4);
//        buttonStart.setOnClickListener(this);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public void startAnimation() {
        //先获取当前控件的x坐标
        //然后向右五次,向左一次回来
        float x = button.getX();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(button,
                "X", x-100,x+100);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(button,
                "X", x+100,x);
        objectAnimator1.setRepeatCount(4);
        objectAnimator2.setRepeatCount(0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator2).after(objectAnimator1);
        animatorSet.setDuration(100);
        animatorSet.start();
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:{
//                switchFragment(new TestFragment());
                break;
            }

        }
    }

    private void switchFragment(TestFragment baseFragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(TestFragment.class.getSimpleName());
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
        if (fragment != null) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.replace(R.id.container,baseFragment, baseFragment.getTag());
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();

    }

}
