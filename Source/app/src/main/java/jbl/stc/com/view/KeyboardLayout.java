package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import jbl.stc.com.utils.LogUtil;


public class KeyboardLayout extends RelativeLayout {

    public static final byte KEYBOARD_STATE_SHOW = -3;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_INIT = -1;

    private boolean mHasInit = false;
    private boolean mHasKeyboard = false;
    private int heightPrevious;
    private OnKeyboardStateChangedListener mListener;
    private Rect rect = new Rect();

    private int mJudgeKeyboardHeight;

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardLayout(Context context) {
        super(context);
    }

    /***
     * set keyboard state listener
     */
    public void setOnKeyboardStateChangedListener(OnKeyboardStateChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getWindowVisibleDisplayFrame(rect);
        int heightNow = rect.height();
        if (!mHasInit) {
            DisplayMetrics dm = getScreenDisplayMetrics(getContext());
            mHasInit = true;
            heightPrevious = heightNow;
            mJudgeKeyboardHeight = dm.heightPixels / 4;
            notifyStateChanged(KEYBOARD_STATE_INIT, heightPrevious);
        } else {
            heightPrevious = heightNow > heightPrevious ? heightNow : heightPrevious;
        }

        if (mHasInit) {
            //只能是adjustResize的情况
            LogUtil.d("keyboard", "keyboard height = " + Math.abs(heightPrevious - heightNow) + ",heightPrevious=" + heightPrevious + ",heightNow=" + heightNow);
            if (!mHasKeyboard && Math.abs(heightPrevious - heightNow) > mJudgeKeyboardHeight) {
                mHasKeyboard = true;
                notifyStateChanged(KEYBOARD_STATE_SHOW, Math.abs(heightPrevious - heightNow));
            } else if (mHasKeyboard && Math.abs(heightPrevious - heightNow) < mJudgeKeyboardHeight) {
                mHasKeyboard = false;
                notifyStateChanged(KEYBOARD_STATE_HIDE, 0);
            }

        }
    }

    private DisplayMetrics getScreenDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    private void notifyStateChanged(final int state, final int height) {
        if (mListener == null)
            return;

        post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null)
                    mListener.onKeyboardStateChanged(state, height);
            }
        });
    }

    public interface OnKeyboardStateChangedListener {
        void onKeyboardStateChanged(int state, int height);
    }

}
