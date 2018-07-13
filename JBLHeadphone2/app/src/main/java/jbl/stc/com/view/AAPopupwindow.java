package jbl.stc.com.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.LightX;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.listener.AwarenessChangeListener;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Wayne on 6/26/18.
 */
public class AAPopupwindow extends PopupWindow implements View.OnClickListener, AwarenessChangeListener,ANCController.OnSeekArcChangeListener{

    private static final String TAG = AAPopupwindow.class.getSimpleName();
    private ANCController ancController;
    private CircularInsideLayout circularInsideLayout;
    private View offBtn, closeBtn;
    private LightX lightX;
    private ANCAwarenessPreset lastsavedAwarenessState;
    private boolean isRequestingLeftANC, isRequestingRightANC;
    public AAPopupwindow(Context context, LightX lightX) {
        super(context);
        this.lightX = lightX;
        init(context);

    }

    private void init(Context context){
        View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.popup_window_anc, null,
                false);
        setContentView(popupWindow_view);
        setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setAnimationStyle(R.style.style_down_to_top);
        ancController = popupWindow_view.findViewById(R.id.circularSeekBar);
        circularInsideLayout = popupWindow_view.findViewById(R.id.imageContainer);
        offBtn = popupWindow_view.findViewById(R.id.noiseText);
        closeBtn = popupWindow_view.findViewById(R.id.aa_popup_close_arrow);

        circularInsideLayout.setonAwarenesChangeListener(this);
        ancController.setCircularInsideLayout(circularInsideLayout);
        ancController.setOnSeekArcChangeListener(this);
        offBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.noiseText:
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(lightX, ANCAwarenessPreset.None);
                ancController.setSwitchOff(false);
                break;
            case R.id.aa_popup_close_arrow:
                if (isShowing()) {
                    dismiss();
                    showNextTutorialTips();
                }
                break;
        }
    }
    public void setAAOff(){
        ancController.setSwitchOff(false);
    }
    public void updateAALeft(int value){
        PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, value ,JBLApplication.getJBLApplicationContext());
        isRequestingLeftANC = false;
        if(!isRequestingLeftANC && !isRequestingRightANC){
//            ancController.initProgress();
        }
        ancController.initLeftProgress(value);

//        ancController.initProgress(value, PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, getActivity()), value);
    }
    public void updateAARight(int value){
        PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, value ,JBLApplication.getJBLApplicationContext());
        isRequestingRightANC = false;
        if(!isRequestingLeftANC && !isRequestingRightANC){

        }
        ancController.initRightProgress(value);
//        ancController.initProgress(PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, getActivity()), value, value);
    }
    public void updateAAUI(int aaLevelingValue){
        boolean is150NC = AppUtils.is150NC(JBLApplication.getJBLApplicationContext());
        Log.d(TAG, "updateAmbientLevel: " + aaLevelingValue + "," + PreferenceUtils.getInt(PreferenceKeys.AWARENESS, JBLApplication.getJBLApplicationContext()) + ",is150NC=" + is150NC);
        switch (aaLevelingValue) {
            case 0:
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, 0, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, 0, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.None;
                break;
            case 1: //ANCAwarenessPreset.Low
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 28 : 25, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 28 : 25, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.Low;
                break;
            case 2: //ANCAwarenessPreset.Medium
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 58 : 55, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 58 : 55, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.Medium;
                break;
            case 3://ANCAwarenessPreset.High
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 86 : 100, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 86 : 100, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.High;
                break;
        }
        ancController.initProgress(PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, JBLApplication.getJBLApplicationContext()),
                PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, JBLApplication.getJBLApplicationContext()), aaLevelingValue);
        PreferenceUtils.setInt(PreferenceKeys.AWARENESS, aaLevelingValue, JBLApplication.getJBLApplicationContext());

        isRequestingLeftANC = true;
        isRequestingRightANC = true;
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getLeftANCvalue(lightX);
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRightANCvalue(lightX);
    }
    @Override
    public void onMedium() {
        //on AA medium checked
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(lightX, ANCAwarenessPreset.Medium);
    }

    @Override
    public void onLow() {
        //on AA low checked
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(lightX, ANCAwarenessPreset.Low);
    }

    @Override
    public void onHigh() {
        //on AA high checked
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(lightX, ANCAwarenessPreset.High);
    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {
        //controller progress

        if (fromUser) {
            // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
            //Set animation to false and presetValue to -1
            int savedLeft = PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST,JBLApplication.getJBLApplicationContext());
            int savedRight = PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, JBLApplication.getJBLApplicationContext());

            PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, leftProgress, JBLApplication.getJBLApplicationContext());
            PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, rightProgress, JBLApplication.getJBLApplicationContext());
//            AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelChanged(mLeftProgress, true);
//            AnalyticsManager.getInstance(getActivity()).reportAwarenessLevelChanged(mRightProgress, false);
            lastsavedAwarenessState = null;
//            promptSeekAbuse.removeCallbacks(runnablepromptSeekAbuse);
//            promptSeekAbuse.postDelayed(runnablepromptSeekAbuse, 300);
            if (leftProgress != savedLeft) {
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setLeftAwarenessPresetValue(lightX, leftProgress);
            }
            if (rightProgress != savedRight) {
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRightAwarenessPresetValue(lightX, rightProgress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onBothThumbsTouched(ANCController seekArc, boolean touched) {
//        showNextTutorialTips();
    }

    private void showNextTutorialTips(){
        if (DashboardActivity.getDashboardActivity().tutorialAncDialog != null){
            dismiss();
            DashboardActivity.getDashboardActivity().tutorialAncDialog.setTextViewTips(R.string.tutorial_tips_three);
        }
    }
}
