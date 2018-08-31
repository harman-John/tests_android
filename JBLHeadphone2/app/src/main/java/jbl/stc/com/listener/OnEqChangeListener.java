package jbl.stc.com.listener;

public interface OnEqChangeListener {
    void onEqValueChanged(int eqIndex, float value);
    void onEqDragFinished(float[] pointX, float[] pointY);
}
