package jbl.stc.com.listener;

/**
 * OnEqChangeListener
 * Created by darren.lu on 2017/8/9.
 */

public interface OnEqChangeListener {
    void onEqValueChanged(int eqIndex, float value);
    void onEqDragFinished(float[] pointX, float[] pointY);
}
