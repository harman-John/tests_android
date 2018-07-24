package jbl.stc.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;


public class CustomFontTextView extends AppCompatTextView {
    private final static String TAG = CustomFontTextView.class.getSimpleName();
    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";
    private static final int SEMI_BOLD = 4;
    private Context mContext;
    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        applyCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context) {
        super(context);
    }

    private void applyCustomFont(Context context, AttributeSet attrs) {
        int textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", SEMI_BOLD);
        Typeface customFont = selectTypeface(context, textStyle);
        setTypeface(customFont);
    }

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

    public Typeface getOpenSansTypeface(String fonFile){
        try {
            return Typeface.createFromAsset(mContext.getAssets(), fonFile);
        }catch (Exception e){
            Logger.e(TAG," RuntimeException: Font asset not found, file = " + fonFile);
        }
        return null;
    }
}
