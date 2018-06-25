package jbl.stc.com.manager;

import android.app.Activity;
import android.graphics.Paint;

/**
 * ANCDrawManager
 * Created by intahmad on 12/14/2015.
 */
public class ANCDrawManager {
    private static ANCDrawManager ancDrawManager;

    public synchronized static ANCDrawManager getInsManager() {
        if (ancDrawManager == null)
            return new ANCDrawManager();
        else
            return ancDrawManager;
    }

    private Paint mArcPaint = null, mPointerPaint1 = null, mPointerPaint2 = null, mProgressPaint = null, mPointerTextPaint1 = null, mPointerTextPaint2 = null,
            mPointerHaloPaint1 = null, mPointerHaloBorderPaint1 = null, mPointerHaloPaint2 = null, mPointerHaloBorderPaint2 = null;

    public Paint getmArcPaint() {
        return mArcPaint;
    }

    public void setmArcPaint(Paint mArcPaint) {
        this.mArcPaint = mArcPaint;
    }

    public Paint getmPointerPaint2() {
        return mPointerPaint2;
    }

    public void setmPointerPaint2(Paint mPointerPaint2) {
        this.mPointerPaint2 = mPointerPaint2;
    }

    public Paint getmProgressPaint() {
        return mProgressPaint;
    }

    public void setmProgressPaint(Paint mProgressPaint) {
        this.mProgressPaint = mProgressPaint;
    }

    public Paint getmPointerTextPaint1() {
        return mPointerTextPaint1;
    }

    public void setmPointerTextPaint1(Paint mPointerTextPaint1) {
        this.mPointerTextPaint1 = mPointerTextPaint1;
    }

    public Paint getmPointerTextPaint2() {
        return mPointerTextPaint2;
    }

    public void setmPointerTextPaint2(Paint mPointerTextPaint2) {
        this.mPointerTextPaint2 = mPointerTextPaint2;
    }

    public Paint getmPointerHaloPaint1() {
        return mPointerHaloPaint1;
    }

    public void setmPointerHaloPaint1(Paint mPointerHaloPaint1) {
        this.mPointerHaloPaint1 = mPointerHaloPaint1;
    }

    public Paint getmPointerHaloBorderPaint1() {
        return mPointerHaloBorderPaint1;
    }

    public void setmPointerHaloBorderPaint1(Paint mPointerHaloBorderPaint1) {
        this.mPointerHaloBorderPaint1 = mPointerHaloBorderPaint1;
    }

    public Paint getmPointerHaloPaint2() {
        return mPointerHaloPaint2;
    }

    public void setmPointerHaloPaint2(Paint mPointerHaloPaint2) {
        this.mPointerHaloPaint2 = mPointerHaloPaint2;
    }

    public Paint getmPointerHaloBorderPaint2() {
        return mPointerHaloBorderPaint2;
    }

    public void setmPointerHaloBorderPaint2(Paint mPointerHaloBorderPaint2) {
        this.mPointerHaloBorderPaint2 = mPointerHaloBorderPaint2;
    }

    public Paint getmPointerPaint1() {
        return mPointerPaint1;
    }

    public void setmPointerPaint1(Paint mPointerPaint1) {
        this.mPointerPaint1 = mPointerPaint1;
    }

    int deviceHeight = -1;
    int deviceWidth = -1;

    public float getDesnity() {
        return desnity;
    }

    public void setDesnity(float desnity) {
        this.desnity = desnity;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(int deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public int getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(int deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    float desnity = 10000;
}
