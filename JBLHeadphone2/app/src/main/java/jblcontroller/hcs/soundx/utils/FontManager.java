package jblcontroller.hcs.soundx.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * This singleton class has methods to create different types of fonts from the ttf files regular
 * font, medium font, bold fonts are supported
 */
public class FontManager {

    private static volatile FontManager sFontManager = null;
    private Context mAppContext = null;
    private Typeface mRegularTypeface = null;
    private Typeface mMediumTypeface = null;
    private Typeface mBoldTypeface = null;

    private FontManager(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static FontManager getInstance(Context context) {
        if (sFontManager == null) {
            synchronized (FontManager.class) {
                if (sFontManager == null) {
                    sFontManager = new FontManager(context);
                }
            }
        }
        return sFontManager;
    }

    public Typeface getRegularFont() {
        if (mRegularTypeface == null) {
            mRegularTypeface = Typeface.createFromAsset(mAppContext.getAssets(),
                    "OpenSans-Regular.ttf");
        }
        return mRegularTypeface;
    }

    public Typeface getMediumFont() {
        if (mMediumTypeface == null) {
            mMediumTypeface = Typeface.createFromAsset(mAppContext.getAssets(),
                    "OpenSans-Semibold.ttf");
        }
        return mMediumTypeface;
    }

    public Typeface getBoldFont() {
        if (mBoldTypeface == null) {
            mBoldTypeface = Typeface.createFromAsset(mAppContext.getAssets(),
                    "OpenSans-Bold.ttf");
        }
        return mBoldTypeface;
    }
}
