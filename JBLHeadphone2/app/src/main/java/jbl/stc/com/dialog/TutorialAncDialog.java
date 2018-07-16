package jbl.stc.com.dialog;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;


public class TutorialAncDialog extends Dialog implements View.OnClickListener {
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
    private ImageView imageViewArrowUp;
    private TextView textViewEq;
    private TextView textViewEqGrey;

    private final static String TAG =  TutorialAncDialog.class.getSimpleName();

    public void setOnDialogListener(OnDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
    }

    public TutorialAncDialog(FragmentActivity context) {
        super(context, R.style.AppDialog);
        mActivity = context;
        initUI();
    }

    private void initUI() {
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setContentView(R.layout.dialog_tutorial_anc);
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
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        Window window = getWindow();
//        window.setWindowAnimations(R.style.style_down_to_top);
        window.setAttributes(lp);

        relativeLayoutNoiceCancel = findViewById(R.id.relative_layout_tutorial_dialog_noise_cancel);
        String modelNumber = AppUtils.getModelNumber(getContext());
        if (!DeviceFeatureMap.isFeatureSupported(modelNumber, Feature.ENABLE_NOISE_CANCEL)) {
            relativeLayoutNoiceCancel.setVisibility(View.GONE);
        } else {
            relativeLayoutNoiceCancel.setVisibility(View.VISIBLE);
            checkBoxANC = findViewById(R.id.image_view_tutorial_dialog_noise_cancel);
            checkBoxANC.setOnClickListener(this);
        }

        relativeLayoutAmbientAware = findViewById(R.id.relative_layout_tutorial_dialog_ambient_aware);
        if (!DeviceFeatureMap.isFeatureSupported(modelNumber, Feature.ENABLE_AMBIENT_AWARE)) {
            relativeLayoutAmbientAware.setVisibility(View.GONE);
        } else {
            relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
            imageViewAbientAware = findViewById(R.id.image_view_tutorial_dialog_ambient_aware);
            imageViewAbientAware.setOnClickListener(this);
            textViewAmbientAware = findViewById(R.id.text_view_tutorial_dialog_ambient_aware);
            if (modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                    || modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                    || modelNumber.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA)) {
                textViewAmbientAware.setText(R.string.smart_ambient);
            }
            relativeLayoutAmbientAware.setVisibility(View.INVISIBLE);
        }

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
                if (fr != null && fr instanceof HomeFragment){
                    setTextViewTips(R.string.tutorial_tips_two);
                    ((HomeFragment)fr).showAncPopupWindow(relativeLayout);
                }
                break;
            }
            case R.id.frame_layout_tutorial_dialog_eq_info:{
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
                break;
            }
        }
    }
}
