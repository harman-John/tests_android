package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import jbl.stc.com.utils.UiUtils;

public class EqArcView extends View{


    private Paint paint;
    private int screenHeight,screenWidth;
    public EqArcView(Context context) {
        super(context);
        init();
    }

    public EqArcView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        paint=new Paint();
        paint.setColor(Color.parseColor("#F13E2A"));
        paint.setStyle(Paint.Style.FILL);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth =dm.widthPixels;
        Log.d("cyx","screeheight:"+screenHeight+"screenWidth:"+screenWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect1 = new RectF(screenWidth- UiUtils.dip2px(getContext(),80), screenHeight-UiUtils.dip2px(getContext(),80), screenWidth, screenHeight);
        canvas.drawArc(rect1, 0, 90, true, paint);

        //canvas.drawRect(screenWidth- UiUtils.dip2px(getContext(),80), screenHeight-UiUtils.dip2px(getContext(),80), screenWidth, screenHeight,paint);
        Log.d("cyx","StartX:"+(screenWidth- UiUtils.dip2px(getContext(),80))+"StartY:"+(screenHeight-UiUtils.dip2px(getContext(),80)+"EndX:"+screenWidth+"EndY:"+screenHeight));

    }
}
