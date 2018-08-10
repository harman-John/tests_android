package jbl.stc.com.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.HomeActivity;
import jbl.stc.com.activity.JBLApplication;


public class SaPopupWindow extends PopupWindow implements View.OnClickListener {

    private OnSmartAmbientStatusReceivedListener mListener;
    private ImageView mDaImg, mTtImg;
    private TextView mDaTvTitle, mTtTvTitle, mDaTvDetail, mTtTvDetail;
    private Activity mActivity;

    public SaPopupWindow(Activity activity) {
        super(activity);
        mActivity = activity;
        init(activity);
    }

    private void init(Context context) {
        View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.popup_window_sa, null,
                false);
        setContentView(popupWindow_view);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setAnimationStyle(R.style.style_down_to_top);
        mDaImg = popupWindow_view.findViewById(R.id.iv_ambient_aware);
        mTtImg = popupWindow_view.findViewById(R.id.iv_talk_through);
        mDaTvTitle = popupWindow_view.findViewById(R.id.daylight_ambient_tv_title);
        mDaTvDetail = popupWindow_view.findViewById(R.id.daylight_ambient_tv_detail);
        mTtTvTitle = popupWindow_view.findViewById(R.id.talk_through_tv_title);
        mTtTvDetail = popupWindow_view.findViewById(R.id.talk_through_tv_detail);
        mDaImg.setOnClickListener(this);
        mTtImg.setOnClickListener(this);
        popupWindow_view.findViewById(R.id.aa_popup_close_arrow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aa_popup_close_arrow:
                if (isShowing()) {
                    dismiss();
                    showNextTutorialTips();
                }
                break;
            case R.id.iv_ambient_aware:
                doDaEnable();
                break;
            case R.id.iv_talk_through:
                doTtEnable();
                break;
        }

    }

    private void doDaEnable(){

        mDaImg.setSelected(true);
        mTtImg.setSelected(false);
        mDaTvTitle.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color1));
        mDaTvDetail.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color1));
        mTtTvTitle.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color2));
        mTtTvDetail.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color2));
        if(mListener != null){
            mListener.onSaStatusReceived(true, false);
        }
    }
    private void doTtEnable(){
        mDaImg.setSelected(false);
        mTtImg.setSelected(true);
        mDaTvTitle.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color2));
        mDaTvDetail.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color2));
        mTtTvTitle.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color1));
        mTtTvDetail.setTextColor(ContextCompat.getColor(JBLApplication.getJBLApplicationContext(), R.color.smart_audio_popup_text_color1));
        if(mListener != null){
            mListener.onSaStatusReceived(false, true);
        }
    }

    private void showNextTutorialTips(){
        if (mActivity instanceof HomeActivity) {
            if (((HomeActivity) mActivity).getTutorialAncDialog() != null) {
                dismiss();
                ((HomeActivity) mActivity).getTutorialAncDialog().setTextViewTips(R.string.tutorial_tips_three);
            }
        }
    }
    public void setOnSmartAmbientStatusReceivedListener(OnSmartAmbientStatusReceivedListener listener) {
        this.mListener = listener;
    }

    public interface OnSmartAmbientStatusReceivedListener {
        /**
         * A call back method
         *
         * @param isDaEnable indicate if Daylight Ambient feature enable
         * @param isTtEnable indicate if Talk Through feature enable
         */
        void onSaStatusReceived(boolean isDaEnable, boolean isTtEnable);
    }
}
