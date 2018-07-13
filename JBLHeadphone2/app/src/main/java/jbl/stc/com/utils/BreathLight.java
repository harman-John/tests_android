package jbl.stc.com.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.util.Timer;


public class BreathLight {

    private RelativeLayout relativeLayoutbreath;
    private boolean isOpen = true;
    private final int BREATH_INTERVAL_TIME = 1000;
    private int mResFadeIn;
    private int mResFadeOut;
    private Context mContext;
    private int mPosition;

    public BreathLight(Context context, RelativeLayout relativeLayout, int resFadeIn, int resFadeOut){
        mContext = context;
        relativeLayoutbreath = relativeLayout;
        mResFadeIn = resFadeIn;
        mResFadeOut = resFadeOut;
    }


    public int getPosition(){
        return mPosition;
    }

    public void startBreathing(int position) {
        mPosition = position;
        mHandler.sendEmptyMessage(MSG_BREATHING_FADE_OUT);
    }

    public void stopBreathing(){
        isOpen = false;
    }


    private MHandler mHandler = new MHandler(Looper.getMainLooper());
    private final static int MSG_BREATHING_FADE_IN = 1;
    private final static int MSG_BREATHING_FADE_OUT = 2;
    private class MHandler extends Handler {
        public MHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            if (!isOpen){
                mHandler.removeMessages(MSG_BREATHING_FADE_IN);
                mHandler.removeMessages(MSG_BREATHING_FADE_OUT);
                return;
            }
            switch (msg.what) {
                case MSG_BREATHING_FADE_IN:
                    relativeLayoutbreath.clearAnimation();
                    relativeLayoutbreath.setAnimation(getFadeIn());
                    mHandler.sendEmptyMessageDelayed(MSG_BREATHING_FADE_OUT,BREATH_INTERVAL_TIME);
                    break;
                case MSG_BREATHING_FADE_OUT:
                    relativeLayoutbreath.clearAnimation();
                    relativeLayoutbreath.setAnimation(getFadeOut());
                    mHandler.sendEmptyMessageDelayed(MSG_BREATHING_FADE_IN,BREATH_INTERVAL_TIME);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private Animation getFadeIn() {
        Animation fadeIn = AnimationUtils.loadAnimation(mContext,mResFadeIn);
        fadeIn.setDuration(BREATH_INTERVAL_TIME);
        return fadeIn;
    }

    private Animation getFadeOut() {
        Animation fadeOut = AnimationUtils.loadAnimation(mContext,mResFadeOut);
        fadeOut.setDuration(BREATH_INTERVAL_TIME);
        return fadeOut;
    }
}
