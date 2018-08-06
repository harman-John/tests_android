package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.ToastUtil;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 2018/08/01
 */
public class AppButton extends AppCompatButton {
    private final static String TAG = CustomFontTextView.class.getSimpleName();
    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";
    private static final int SEMI_BOLD = 4;
    private Context mContext;

    public AppButton(Context context) {
        super(context);
    }

    public AppButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        applyCustomFont(context, attrs);
    }

    public AppButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(0.8f);
                setScaleX(0.97f);
                setScaleY(0.97f);
                //setOutlineProvider(viewOutlineProvider1);
                //setOutlineProvider(null);
                break;
            case MotionEvent.ACTION_MOVE:
                setAlpha(0.8f);
                setScaleX(0.97f);
                setScaleY(0.97f);
                break;
            case MotionEvent.ACTION_UP:
                setAlpha(1.0f);
                setScaleX(1.0f);
                setScaleY(1.0f);
                //setOutlineProvider(null);
                //setOutlineProvider(viewOutlineProvider);
                break;
            case MotionEvent.ACTION_CANCEL:
                setAlpha(1.0f);
                setScaleX(1.0f);
                setScaleY(1.0f);
                //setOutlineProvider(null);
                //setOutlineProvider(viewOutlineProvider);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void applyCustomFont(Context context, AttributeSet attrs) {
        int textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", SEMI_BOLD);
        Typeface customFont = selectTypeface(context, textStyle);
        setTypeface(customFont);
    }

    ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            rect.top = 20;
            rect.left = -5;
            rect.right = view.getWidth() + 5;
            rect.bottom = view.getHeight() + 15;
            outline.setRoundRect(rect, 75);
            outline.setAlpha(0.65f);

            //outline.setOval(-5, -5,view.getWidth()+5, view.getHeight()+5);   //设置圆形（还有圆角矩形，矩形，path
        }
    };

    ViewOutlineProvider viewOutlineProvider1 = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            rect.top = 20;
            rect.left = -5;
            rect.right = view.getWidth() + 5;
            rect.bottom = view.getHeight() + 5;
            outline.setRoundRect(rect, 75);
            outline.setAlpha(0.95f);

            //outline.setOval(-5, -5,view.getWidth()+5, view.getHeight()+5);   //设置圆形（还有圆角矩形，矩形，path
        }
    };

    /**
     * Select a particular font face as per UI design
     *
     * @param context   Context
     * @param textStyle bold or normal etc
     * @return Typeface
     */
    private Typeface selectTypeface(Context context, int textStyle) {
        switch (textStyle) {
            case Typeface.BOLD:
                return getOpenSansTypeface(JBLConstant.OPEN_SANS_BOLD);
            case Typeface.NORMAL:
                return getOpenSansTypeface(JBLConstant.OPEN_SANS_REGULAR);
            case Typeface.ITALIC:
                return getOpenSansTypeface(JBLConstant.OPEN_SANS_ITALIC);
            case SEMI_BOLD:
                return getOpenSansTypeface(JBLConstant.OPEN_SANS_SEMI_BOLD);
        }
        return getOpenSansTypeface(JBLConstant.OPEN_SANS_REGULAR);
    }

    public Typeface getOpenSansTypeface(String fonFile) {
        try {
            return Typeface.createFromAsset(mContext.getAssets(), fonFile);
        } catch (Exception e) {
            Logger.e(TAG, " RuntimeException: Font asset not found, file = " + fonFile);
        }
        return null;
    }


}
