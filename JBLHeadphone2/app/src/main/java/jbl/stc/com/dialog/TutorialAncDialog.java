package jbl.stc.com.dialog;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.GraphicEQPreset;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.listener.OnEqChangeListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.AppShaderView;
import jbl.stc.com.view.EqualizerAddView;
import jbl.stc.com.view.KeyboardLayout;
import jbl.stc.com.view.SaPopupWindow;


public class TutorialAncDialog extends Dialog implements View.OnClickListener, SaPopupWindow.OnSmartAmbientStatusReceivedListener {
    private OnDialogListener onDialogListener;

    private CheckBox checkBoxANC;
    private FragmentActivity mActivity;
    private RelativeLayout relativeLayout;
    private TextView textViewTips;
    private FrameLayout frameLayoutEqInfo;
    private RelativeLayout relativeLayoutEqGrey;
    private LinearLayout linearLayoutAnc;
    private TextView textViewEqName;
    private TextView textViewSkip;
    private RelativeLayout relativeLayoutAdd;
    private RelativeLayout relativeLayoutNoiceCancel;
    private RelativeLayout relativeLayoutAmbientAware;
    private ImageView imageViewAbientAware;
    private TextView textViewAmbientAware;
    private View viewDivider;
    private AppImageView imageViewArrowUp;
    private TextView textViewEq;
    private TextView textViewEqGrey;
    private String modelNumber;
    private EqualizerAddView mEqAddView;
    private KeyboardLayout mKeyboardLayout;
    private FrameLayout tutorialEqualizerLayout;
    private RelativeLayout mTutorialEqSystemParentLayout;
    private AppImageView tripleUpArrow;

    private final static String TAG =  TutorialAncDialog.class.getSimpleName();

    public void setOnDialogListener(OnDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
    }

    public TutorialAncDialog(FragmentActivity context) {
        super(context, R.style.AppDialog);
        mActivity = context;
        modelNumber = AppUtils.getModelNumber(getContext());
        initUI();
    }

    private void initUI() {
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setContentView(R.layout.dialog_tutorial_anc);
        mKeyboardLayout = findViewById(R.id.tutorial_viewKeyboardLayout);
        mKeyboardLayout.setOnKeyboardStateChangedListener(onKeyboardStateChangedListener);
        tutorialEqualizerLayout = findViewById(R.id.tutorial_equalizerLayout);
        mTutorialEqSystemParentLayout = findViewById(R.id.tutorial_eq_system_parent_layout);
        relativeLayout = findViewById(R.id.relative_layout_tutorial_dialog);
        textViewTips = findViewById(R.id.text_view_tutorial_dialog_tips);
        frameLayoutEqInfo = findViewById(R.id.frame_layout_tutorial_dialog_eq_info);
        frameLayoutEqInfo.setOnClickListener(this);
        linearLayoutAnc = findViewById(R.id.linear_layout_tutorial_dialog_anc);
        textViewEqName = findViewById(R.id.text_view_tutorial_dialog_eq_name);
        textViewSkip = findViewById(R.id.text_view_tutorial_dialog_skip);
        textViewSkip.setOnClickListener(this);
        relativeLayoutAdd = findViewById(R.id.relative_layout_tutorial_dialog_add);
        relativeLayoutAdd.setOnClickListener(this);
        viewDivider = findViewById(R.id.view_dialog_eq_divider);
        imageViewArrowUp = findViewById(R.id.image_view_tutorial_dialog_arrow_up);
        textViewEq = findViewById(R.id.text_view_tutorial_dialog_eq);
        relativeLayoutEqGrey = findViewById(R.id.relative_layout_tutorial_dialog_eq_grey);
        textViewEqGrey = findViewById(R.id.text_view_tutorial_dialog_eq_grey);
        textViewEqGrey.setOnClickListener(this);
        mEqAddView = findViewById(R.id.tutorial_equalizerView);
        tripleUpArrow = findViewById(R.id.eq_tutorial_triple_up_arrows_img);
        mEqAddView.setOnEqChangeListener(new OnEqChangeListener() {
            @Override
            public void onEqValueChanged(int eqIndex, float value) {
                dismiss();
            }

            @Override
            public void onEqDragFinished(float[] pointX, float[] pointY) {
                dismiss();
            }
        });
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        Window window = getWindow();
//        window.setWindowAnimations(R.style.style_down_to_top);
        window.setAttributes(lp);

        relativeLayoutNoiceCancel = findViewById(R.id.relative_layout_tutorial_dialog_noise_cancel);
        if (!DeviceFeatureMap.isFeatureSupported(modelNumber, Feature.ENABLE_NOISE_CANCEL)) {
            relativeLayoutNoiceCancel.setVisibility(View.GONE);
        } else {
            relativeLayoutNoiceCancel.setVisibility(View.VISIBLE);
            checkBoxANC = findViewById(R.id.image_view_tutorial_dialog_noise_cancel);
            checkBoxANC.setOnClickListener(this);
        }

        relativeLayoutAmbientAware = findViewById(R.id.relative_layout_tutorial_dialog_ambient_aware);
        if (!DeviceFeatureMap.isFeatureSupported(modelNumber, Feature.ENABLE_AMBIENT_AWARE) || isShowOnlyNoiceCancelingType()) {
            relativeLayoutAmbientAware.setVisibility(View.GONE);
        } else {
            relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
            imageViewAbientAware = findViewById(R.id.image_view_tutorial_dialog_ambient_aware);
            imageViewAbientAware.setOnClickListener(this);
            textViewAmbientAware = findViewById(R.id.text_view_tutorial_dialog_ambient_aware);
            if (isShowOnlySmartAmbientType()) {
                textViewAmbientAware.setText(R.string.smart_ambient);
            }
//            relativeLayoutAmbientAware.setVisibility(View.INVISIBLE);
        }

        setDeviceImageHeight();
    }
    private void setDeviceImageHeight() {
        AppImageView imageViewDevice=findViewById(R.id.image_view_tutorial_dialog_device_image);
        DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
        int screenheigth = dm.heightPixels;
        int screenwidth = dm.widthPixels;
        int statusHeight = UiUtils.getStatusHeight(mActivity);
        int height = (int) (screenheigth - UiUtils.dip2px(mActivity, 200) - statusHeight) / 2 -UiUtils.dip2px(mActivity, 10);
        if (height>UiUtils.dip2px(mActivity,240)){
            height=UiUtils.dip2px(mActivity,240);
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageViewDevice.getLayoutParams();
        params.height = height;
        params.width = height;
        imageViewDevice.setLayoutParams(params);

    }
    private KeyboardLayout.OnKeyboardStateChangedListener onKeyboardStateChangedListener = new KeyboardLayout.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state, int height) {
            switch (state) {
                case KeyboardLayout.KEYBOARD_STATE_INIT:
                    int eqViewHeight = (int) (height - (mActivity.getResources().getDimensionPixelSize(R.dimen.eq_name_edit_layout_height)
                            + mEqAddView.getMarginTop() + mEqAddView.getMarginBottom()));
                    RelativeLayout.LayoutParams equalizerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            eqViewHeight);
                    Log.d(TAG, "KEYBOARD_STATE_INIT:height=" + eqViewHeight);
                    tutorialEqualizerLayout.setLayoutParams(equalizerParams);
                    mEqAddView.setCustomHeight(eqViewHeight);
                    break;
                case KeyboardLayout.KEYBOARD_STATE_HIDE:

                    break;
                case KeyboardLayout.KEYBOARD_STATE_SHOW:

                    break;
            }
        }
    };
    private boolean isShowOnlySmartAmbientType(){
        return (modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                || modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                || modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA));
    }
    private boolean isShowOnlyNoiceCancelingType(){
        return (modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC));
    }
    public void setChecked(boolean isChecked){
        if (checkBoxANC!= null){
            checkBoxANC.setChecked(isChecked);
        }
        if (isChecked) {
            if (relativeLayoutAmbientAware != null){
                relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
            }
            setTextViewTips(R.string.tutorial_tips_one);
        }else{
            if (relativeLayoutAmbientAware != null){
                relativeLayoutAmbientAware.setVisibility(View.INVISIBLE);
            }
            setTextViewTips(R.string.tutorial_tips_zero);
        }
    }

    public void setTextViewTips(int restId){
        if (textViewTips!= null){
            textViewTips.setText(restId);
        }
    }

    public void showEqInfo(){
        Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (fr != null && fr instanceof HomeFragment){
            ((HomeFragment)fr).setEqMenuColor(false);
        }
        if (textViewEqName != null){
            textViewEqName.setVisibility(View.INVISIBLE);
        }
        if (frameLayoutEqInfo != null){
            frameLayoutEqInfo.setVisibility(View.INVISIBLE);
        }
        if (relativeLayoutEqGrey != null){
            relativeLayoutEqGrey.setVisibility(View.VISIBLE);
            relativeLayoutEqGrey.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.shape_circle_off_eq_name_bg_normal));
        }
        if (textViewEqGrey != null){
            textViewEqGrey.setVisibility(View.VISIBLE);
        }
        if (linearLayoutAnc != null){
            linearLayoutAnc.setVisibility(View.GONE);
        }
    }

    public void hideEqInfo(){
        if (frameLayoutEqInfo != null){
            frameLayoutEqInfo.setVisibility(View.GONE);
        }
        if (linearLayoutAnc != null){
            linearLayoutAnc.setVisibility(View.GONE);
        }
    }

    private void hideSkip(){
        if (textViewSkip != null){
            textViewSkip.setVisibility(View.GONE);
        }
    }

    private void showAdd(){
        if (relativeLayoutAdd != null){
            relativeLayoutAdd.setVisibility(View.VISIBLE);
        }
    }

    public void updateCurrentEQ(int index) {
        switch (index) {
            case 0: {
                textViewEqName.setText(R.string.off);
                frameLayoutEqInfo.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.gray_aa_bg));
                break;
            }
            case 1: {
                ((JBLApplication)DashboardActivity.getDashboardActivity().getApplication()).deviceInfo.eqOn = true;
                textViewEqName.setText(R.string.jazz);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                ((JBLApplication)DashboardActivity.getDashboardActivity().getApplication()).deviceInfo.eqOn = true;
                textViewEqName.setText(R.string.vocal);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                ((JBLApplication)DashboardActivity.getDashboardActivity().getApplication()).deviceInfo.eqOn = true;
                textViewEqName.setText(R.string.bass);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                ((JBLApplication)DashboardActivity.getDashboardActivity().getApplication()).deviceInfo.eqOn = true;
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getContext(), null);
                if (name != null) {
                    textViewEqName.setText(name);
                    if (textViewEqName.getText().length() >= JBLConstant.MAX_MARQUEE_LEN) {
                        textViewEqName.setSelected(true);
                        textViewEqName.setMarqueeRepeatLimit(-1);
                    }
                } else {
                    textViewEqName.setText(R.string.custom_eq);
                }
                break;
            }
            default:
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getContext(), null);
                textViewEqName.setText(name != null ? name : getContext().getString(R.string.off));
                break;
        }
    }

    @Override
    public void onSaStatusReceived(boolean isDaEnable, boolean isTtEnable) {
        if(isDaEnable){
            textViewTips.setText(JBLApplication.getJBLApplicationContext().getString(R.string.tutorial_tips_one_live));
        }else{
            textViewTips.setText(JBLApplication.getJBLApplicationContext().getString(R.string.tutorial_tips_two_live));
        }
    }

    private void showTripleArrowsAnimation(){
        final Animation tripleArrowsAnim = AnimationUtils.loadAnimation(JBLApplication.getJBLApplicationContext(), R.anim.anim_triple_up_arrow);
        tripleUpArrow.setAnimation(tripleArrowsAnim);
        tripleUpArrow.setVisibility(View.VISIBLE);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_tutorial_dialog_noise_cancel:{
                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr != null && fr instanceof HomeFragment){
                    ((HomeFragment)fr).setANC();
                    setChecked(checkBoxANC.isChecked());
                }
                break;
            }
            case R.id.image_view_tutorial_dialog_ambient_aware:{
                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if(!isShowOnlySmartAmbientType()) {
                    if (fr != null && fr instanceof HomeFragment) {
                        setTextViewTips(R.string.tutorial_tips_two);
                        ((HomeFragment) fr).showAncPopupWindow(relativeLayout);
                    }
                }else{
                    if (fr != null && fr instanceof HomeFragment) {
//                        setTextViewTips(R.string.tutorial_tips_two);
                        ((HomeFragment) fr).showSaPopupWindow(relativeLayout,this);
                    }
                }
                break;
            }
            case R.id.frame_layout_tutorial_dialog_eq_info:{
//                repeatTrippleArrowAnim = false;
                tripleUpArrow.clearAnimation();
                tripleUpArrow.setVisibility(View.GONE);
                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null ){
                    Logger.i(TAG,"fr is null");
                    return;
                }
                if  ( fr instanceof EqSettingFragment) {
                    Logger.i(TAG,"fr is already showed");
                    return;
                }
                DashboardActivity.getDashboardActivity().switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                hideEqInfo();
                setTextViewTips(R.string.tutorial_tips_five);
                hideSkip();
                showAdd();
                break;
            }
            case R.id.relative_layout_tutorial_dialog_add:{
//                dismiss();
                if (relativeLayoutAdd!=null){
                    relativeLayoutAdd.setVisibility(View.GONE);
                }

                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null ){
                    Logger.i(TAG,"fr is null");
                    return;
                }
                if  ( fr instanceof EqSettingFragment) {
                    Logger.i(TAG,"fr is already showed");
                    ((EqSettingFragment)fr).onAddCustomEq(true, false);
                }
                setTextViewTips(R.string.tutorial_tips_six);
                EQModel currSelectedEq  = new EQModel();
                JBLApplication application = (JBLApplication)mActivity.getApplication();
                currSelectedEq.id = application.deviceInfo.maxEqId + 1;
                currSelectedEq.index = application.deviceInfo.maxEqId + 1;
                currSelectedEq.eqType = GraphicEQPreset.User.value();
                mEqAddView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.white);
                mTutorialEqSystemParentLayout.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.text_view_tutorial_dialog_skip:{
                dismiss();
                break;
            }
            case R.id.text_view_tutorial_dialog_eq_grey:{
                if (relativeLayoutEqGrey!= null) {
                    relativeLayoutEqGrey.setVisibility(View.GONE);
                }
                if (frameLayoutEqInfo!= null) {
                    frameLayoutEqInfo.setVisibility(View.VISIBLE);
                }
                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr != null && fr instanceof HomeFragment){
                    ((HomeFragment)fr).setEqMenuColor(true);
                }
                setTextViewTips(R.string.tutorial_tips_four);
                showTripleArrowsAnimation();
                break;
            }
        }
    }
}
