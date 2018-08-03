package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import jbl.stc.com.R;
/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 7/27/18.
 */
public class AppShaderView extends View {

    private int mDrawableColor = getResources().getColor(R.color.orange);
    private int mShaderColor = Color.parseColor("#9298A2");
    private String mText;
    private Paint mPaint;
    private Paint mShaderPaint;
    private int width;
    private int height;
    private Bitmap bitmap;

    public AppShaderView(Context context) {
        this(context, null);
    }

    public AppShaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        //this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getAttrs(context, attrs, 0);
    }

    public AppShaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //getAttrs(context, attrs, defStyleAttr);

    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShaderView, defStyleAttr, 0);
            mDrawableColor = a.getColor(R.styleable.ShaderView_drawableColor, mDrawableColor);
            mShaderColor = a.getColor(R.styleable.ShaderView_shaderColor, mShaderColor);
            mText = a.getString(R.styleable.ShaderView_text);
            a.recycle();
        }
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(mDrawableColor);
        mPaint.setStyle(Paint.Style.FILL);
        mShaderPaint=new Paint();
        mShaderPaint.setColor(mShaderColor);
        //mPaint.setShadowLayer(20F, -5, 20, mShaderColor);
        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.rectangle_with_round_corner_view_page_orange);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw the shader
        //RectF rectF = new RectF(10, 10, width-10, height-10);
        //canvas.drawRoundRect(rectF, 75, 75, mPaint);
        canvas.drawBitmap(bitmap,0,0,mPaint);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingBottom = 0;
        this.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

    }
}
