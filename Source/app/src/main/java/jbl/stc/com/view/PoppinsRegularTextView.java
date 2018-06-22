package jbl.stc.com.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * font face Poppins-Regular TextView
 * Created by darren.lu on 2017/8/25.
 */

public class PoppinsRegularTextView extends AppCompatTextView {

    public PoppinsRegularTextView(Context context) {
        super(context);
        init(context);
    }

    public PoppinsRegularTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PoppinsRegularTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        AssetManager assetManager = context.getAssets();
        Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/Poppins-Regular.ttf");
        setTypeface(typeface);
    }

}
