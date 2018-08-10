package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import jbl.stc.com.R;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;
/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 2018/08/02
 */
public class ShadowLayout extends RelativeLayout {
    public static final int ALL = 0x1111;
    public static final int LEFT = 0x0001;
    public static final int TOP = 0x0010;
    public static final int RIGHT = 0x0100;
    public static final int BOTTOM = 0x1000;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRectF = new RectF();
    /**
     * shader color
     */
    private int mShadowColor = Color.parseColor("#66000000");
    /**
     * shader size
     */
    private float mShadowRadius = UiUtils.dip2px(getContext(),5);
    /**
     * shader x offset
     */
    private float mShadowDx = 0;
    /**
     * shader y offset
     */
    private float mShadowDy = 0;
    /**
     * shader size
     */
    private int mShadowSide = ALL;
    private int width;
    private int height;
    private String mShape = "rectangle";
    public ShadowLayout(Context context) {
        this(context, null);
    }
    public ShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setShape(String shape){
        mShape = shape;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        /*super.onLayout(changed, left, top, right, bottom);
        float effect = mShadowRadius + UiUtils.dip2px(getContext(),5);
        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = this.getWidth();
        float rectBottom = this.getHeight();
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingBottom = 0;
        if (((mShadowSide & LEFT) == LEFT)) {
            rectLeft = effect;
            paddingLeft = (int) effect;
        }
        if (((mShadowSide & TOP) == TOP)) {
            rectTop = effect;
            paddingTop = (int) effect;
        }
        if (((mShadowSide & RIGHT) == RIGHT)) {
            rectRight = this.getWidth() - effect;
            paddingRight = (int) effect;
        }
        if (((mShadowSide & BOTTOM) == BOTTOM)) {
            rectBottom = this.getHeight() - effect;
            paddingBottom = (int) effect;
        }
        if (mShadowDy != 0.0f) {
            rectBottom = rectBottom - mShadowDy;
            paddingBottom = paddingBottom + (int) mShadowDy;
        }
        if (mShadowDx != 0.0f) {
            rectRight = rectRight - mShadowDx;
            paddingRight = paddingRight + (int) mShadowDx;
        }
        mRectF.left = rectLeft;
        mRectF.top = rectTop;
        mRectF.right = rectRight;
        mRectF.bottom = rectBottom;
        this.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);*/

        super.onLayout(changed, left, top, right, bottom);
        float effect = mShadowRadius;
        float rectLeft = effect;
        float rectTop = effect;
        float rectRight = width-effect;
        float rectBottom = height-effect;
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingBottom = 0;
        mRectF.left = rectLeft;
        mRectF.top = rectTop;
        mRectF.right = rectRight;
        mRectF.bottom = rectBottom;
        this.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        Logger.d("ShadowLayout","width:"+width+"height:"+height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawRect(mRectF, mPaint);
        if (mShape.equals("rectangle")){
            canvas.drawRoundRect(mRectF,75,75,mPaint);
        }else if (mShape.equals("circle")){
            canvas.drawCircle(width/2,height/2,width/2-mShadowRadius,mPaint);
        }
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        mShape=typedArray.getString(R.styleable.ShadowLayout_shape);
        if (TextUtils.isEmpty(mShape)){
            mShape="rectangle";
        }
        typedArray.recycle();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.setWillNotDraw(false);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }

}

