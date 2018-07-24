package jbl.stc.com.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * font face Poppins-Regular EditText
 * Created by darren.lu on 2017/8/25.
 */

public class PoppinsRegularEditText extends AppCompatEditText {

    public PoppinsRegularEditText(Context context) {
        super(context);
        init(context);
    }

    public PoppinsRegularEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PoppinsRegularEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        AssetManager assetManager = context.getAssets();
        Typeface typeface = Typeface.createFromAsset(assetManager, "OpenSans-Regular.ttf");
        setTypeface(typeface);
    }

}
