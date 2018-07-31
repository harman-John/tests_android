package jbl.stc.com.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.listener.AwarenessChangeListener;


/**
 * CircularInsideLayout
 * Created by inkkashy02 on 7/15/2015.
 */
public class CircularInsideLayout extends LinearLayout {

    private boolean isLowSelected, isMediumSelected, isHighSelected; // Flags to check the selected view (High/Low/Medium)

    View hView = null, lView = null, mView = null;

    /**
     * Constructor
     */
    public CircularInsideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
    }

    /**
     * <p>Checks if the point(x,y) is inside this view or not.</p>
     */
    public int checkReside(float x, float y) {

        int num = getChildCount();
        int c = 0;


        for (int i = 0; i < num; i++) {

            final View view = getChildAt(i);
            if (view instanceof RelativeLayout) {
                if (isResideInsideThisRect(new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()), new Point((int) x, (int) y))) {
                    // Touched break;
                    switch (view.getId()) {

                        case R.id.high:
                            if (!isHighSelected) {
                                setSelectedViewStatus(R.id.high);
                                hView = view;
                                final TextView text1;
                                text1 = (TextView) ((RelativeLayout) view).getChildAt(0);
                                text1.setTextColor(ContextCompat.getColor(getContext(), R.color.background));
//                                text1.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.mipmap.waves_high_l_pressed), null, ContextCompat.getDrawable(getContext(), R.mipmap.waves_high_r_pressed), null);
                                hView.setAlpha((float) 0.5);
                                setBackgroundResource(R.mipmap.ambient_awareness_circle_h);
                            }
                            return R.id.high;

                        case R.id.medium:
                            if (!isMediumSelected) {
                                setSelectedViewStatus(R.id.medium);
                                mView = view;
                                final TextView text2;
                                text2 = (TextView) ((RelativeLayout) view).getChildAt(0);
                                text2.setTextColor(ContextCompat.getColor(getContext(), R.color.background));
//                                text2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.mipmap.waves_medium_l_pressed), null, ContextCompat.getDrawable(getContext(), R.mipmap.waves_medium_r_pressed), null);
                                mView.setAlpha((float) 0.5);
                                setBackgroundResource(R.mipmap.ambient_awareness_circle_m);
                            }
                            return R.id.medium;
                        case R.id.low:
                            if (!isLowSelected) {
                                setSelectedViewStatus(R.id.low);
                                lView = view;
                                final TextView text3;
                                text3 = (TextView) ((RelativeLayout) view).getChildAt(0);
                                text3.setTextColor(ContextCompat.getColor(getContext(), R.color.background));
//                                text3.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.mipmap.waves_low_l_pressed), null, ContextCompat.getDrawable(getContext(), R.mipmap.waves_low_r_pressed), null);
                                lView.setAlpha((float) 0.5);
                                setBackgroundResource(R.mipmap.ambient_awareness_circle_l);
                            }
                            return R.id.low;
                    }
                }

            }
        }
        return 0;
    }


    /**
     * <p>Checks if the point(x,y) is inside this view or not.</p>
     */
    public int checkResideAndSendCommand(float x, float y) {

        int num = getChildCount();
        int c = 0;
        for (int i = 0; i < num; i++) {
            final View view = getChildAt(i);
            if (view instanceof RelativeLayout) {
                if (isResideInsideThisRect(new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()), new Point((int) x, (int) y))) {
                    // Touched break;
                    switch (view.getId()) {

                        case R.id.high:
                            onAwarenesChangeListener.onHigh();
                            return R.id.high;
                        case R.id.medium:
                            onAwarenesChangeListener.onMedium();
                            return R.id.medium;
                        case R.id.low:
                            onAwarenesChangeListener.onLow();
                            return R.id.low;
                    }
                }

            }
        }
        return 0;
    }

    /**
     * <p>Applys the effects for the selected state i.e HIGH,LOW or MEDIUM</p>
     */
    public void setSelectedViewStatus(int id) {

        for (int i = 0; i < getChildCount(); i++) {
            switch (getChildAt(i).getId()) {
                case R.id.high:
                    hView = getChildAt(i);
                    break;
                case R.id.medium:
                    mView = getChildAt(i);
                    break;
                case R.id.low:
                    lView = getChildAt(i);
                    break;

            }
        }

        TextView text1, text2, text3;
//        Drawable dLeft, dRight;


        switch (id) {
            case R.id.low://LOW
                isHighSelected = false;
                isMediumSelected = false;
                isLowSelected = true;
                mView.setAlpha(1);
                hView.setAlpha(1);

                text1 = (TextView) ((RelativeLayout) hView).getChildAt(0);
                text1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text1.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_r), null);


                text2 = (TextView) ((RelativeLayout) mView).getChildAt(0);
                text2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_r), null);

//                dLeft = ContextCompat.getDrawable(getContext(), R.mipmap.waves_low_l_pressed);
//                dRight = ContextCompat.getDrawable(getContext(), R.mipmap.waves_low_r_pressed);
//                dLeft.setAlpha(255);
//                dRight.setAlpha(255);
                setBackgroundResource(R.mipmap.ambient_awareness_circle);

                break;

            case R.id.medium://Medium
                isHighSelected = false;
                isMediumSelected = true;
                isLowSelected = false;
                lView.setAlpha(1);
                hView.setAlpha(1);
                text1 = (TextView) ((RelativeLayout) hView).getChildAt(0);
                text1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text1.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_r), null);

                text3 = (TextView) ((RelativeLayout) lView).getChildAt(0);
                text3.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text3.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_r), null);

//                dLeft = ContextCompat.getDrawable(getContext(), R.mipmap.waves_medium_l_pressed);
//                dRight = ContextCompat.getDrawable(getContext(), R.mipmap.waves_medium_r_pressed);
//                dLeft.setAlpha(255);
//                dRight.setAlpha(255);
                setBackgroundResource(R.mipmap.ambient_awareness_circle);
                break;

            case R.id.high://High
                isHighSelected = true;
                isMediumSelected = false;
                isLowSelected = false;
                lView.setAlpha(1);
                mView.setAlpha(1);


                text2 = (TextView) ((RelativeLayout) mView).getChildAt(0);
                text2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_r), null);


                text3 = (TextView) ((RelativeLayout) lView).getChildAt(0);
                text3.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text3.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_r), null);

//                dLeft = ContextCompat.getDrawable(getContext(), R.mipmap.waves_high_l_pressed);
//                dRight = ContextCompat.getDrawable(getContext(), R.mipmap.waves_high_r_pressed);
//                dLeft.setAlpha(255);
//                dRight.setAlpha(255);
                setBackgroundResource(R.mipmap.ambient_awareness_circle);
                break;

            case -1://Reset

                lView.setAlpha(1);
                mView.setAlpha(1);
                hView.setAlpha(1);


                text1 = (TextView) ((RelativeLayout) hView).getChildAt(0);
                text1.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text1.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_high_r), null);


                text2 = (TextView) ((RelativeLayout) mView).getChildAt(0);
                text2.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_medium_r), null);


                text3 = (TextView) ((RelativeLayout) lView).getChildAt(0);
                text3.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_bold_black));
//                text3.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_l), null, ContextCompat.getDrawable(getContext(),R.mipmap.waves_low_r), null);

                lView = null;
                hView = null;
                mView = null;
                isHighSelected = false;
                isMediumSelected = false;
                isLowSelected = false;
                setBackgroundResource(R.mipmap.ambient_awareness_circle);

                break;


        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                checkReside(event.getX(), event.getY());

        }
        return true;
    }

    /**
     * Checks if point is inside the provided rectangle.
     *
     * @return TRUE if the point is inside the rect else FALSE
     */

    private boolean isResideInsideThisRect(Rect rect, Point point) {

        return rect.contains(point.x, point.y);
    }


    AwarenessChangeListener onAwarenesChangeListener;

    /**
     * Sets the awareness change listener
     */
    public void setonAwarenesChangeListener(AwarenessChangeListener onAwarenesChangeListener) {
        this.onAwarenesChangeListener = onAwarenesChangeListener;
    }
}
