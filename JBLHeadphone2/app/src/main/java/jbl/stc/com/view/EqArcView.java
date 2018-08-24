package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import jbl.stc.com.R;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 2018/07/20.
 */
public class EqArcView extends View {


    private int radius;
    private Paint paint;
    private int screenHeight, screenWidth;
    private Bitmap bitmap;
    private int mPaintColor = Color.parseColor("#F13E2A");
    private Drawable mDrawable;

    public EqArcView(Context context) {
        super(context);
        init();
    }

    public EqArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs, 0);
        init();
    }

    public EqArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        paint = new Paint();
        paint.setColor(mPaintColor);
        paint.setStyle(Paint.Style.FILL);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        Logger.d("cyx", "screeheight:" + screenHeight + "screenWidth:" + screenWidth);
    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EqArcView, defStyleAttr, 0);
            mPaintColor = a.getColor(R.styleable.EqArcView_color, mPaintColor);
            mDrawable = a.getDrawable(R.styleable.EqArcView_drawable);
            a.recycle();
        }

        if (mDrawable == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.button_delete);
        } else {
            bitmap = ((BitmapDrawable) mDrawable).getBitmap();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        radius = width > height ? width / 2 : height / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //RectF rect1 = new RectF(screenWidth- UiUtils.dip2px(getContext(),80), screenHeight-UiUtils.dip2px(getContext(),80), screenWidth, screenHeight);
        //canvas.drawArc(rect1, 0, 90, true, paint);

        //canvas.drawRect(screenWidth- UiUtils.dip2px(getContext(),80), screenHeight-UiUtils.dip2px(getContext(),80), screenWidth, screenHeight,paint);
        //Logger.d("cyx","StartX:"+(screenWidth- UiUtils.dip2px(getContext(),80))+"StartY:"+(screenHeight-UiUtils.dip2px(getContext(),80)+"EndX:"+screenWidth+"EndY:"+screenHeight));
        //canvas.drawCircle();

        canvas.drawCircle(radius, radius, radius, paint);

        canvas.drawBitmap(bitmap, radius / 2 - bitmap.getWidth() / 4, radius / 2 - bitmap.getHeight() / 4, paint);
    }
}
