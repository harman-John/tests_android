package jbl.stc.com.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * font face Poppins-Medium TextView
 * Created by darren.lu on 2017/8/25.
 */

public class PoppinsMediumTextView extends AppCompatTextView {

    public PoppinsMediumTextView(Context context) {
        super(context);
        init(context);
    }

    public PoppinsMediumTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PoppinsMediumTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        AssetManager assetManager = context.getAssets();
        Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/Poppins-Medium.ttf");
        setTypeface(typeface);
    }

}
