package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private int mShaderColor = getResources().getColor(R.color.drawable_shader);
    private String mText;
    private Paint mPaint;

    public AppShaderView(Context context) {
        this(context, null);
        init();
    }

    public AppShaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getAttrs(context, attrs, 0);
        init();
    }

    public AppShaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getAttrs(context, attrs, defStyleAttr);
        init();

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
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw the shader
        mPaint.setShadowLayer(10F, 15F, 15F, mShaderColor);
        RectF rectF = new RectF(0, 0, 260, 60);
        canvas.drawRoundRect(rectF, 75, 75, mPaint);
    }
}
