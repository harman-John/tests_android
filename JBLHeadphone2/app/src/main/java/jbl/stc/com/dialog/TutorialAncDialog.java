package jbl.stc.com.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.GraphicEQPreset;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.HomeActivity;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.listener.OnEqChangeListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.CustomFontTextView;
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
    private RelativeLayout relativeLayoutAmbientAware;
    private ImageView imageViewAmbientAaware;
    private TextView textViewEqGrey;
    private EqualizerAddView mEqAddView;
    private FrameLayout tutorialEqualizerLayout;
    private RelativeLayout mTutorialEqSystemParentLayout;
    private AppImageView tripleUpArrow;
    private String deviceName;
    private final static String TAG = TutorialAncDialog.class.getSimpleName();
    private boolean isDelayed = false;

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
        KeyboardLayout mKeyboardLayout = findViewById(R.id.tutorial_viewKeyboardLayout);
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
        relativeLayoutEqGrey = findViewById(R.id.relative_layout_tutorial_dialog_eq_grey);
        textViewEqGrey = findViewById(R.id.text_view_tutorial_dialog_eq_grey);
        textViewEqGrey.setOnClickListener(this);
        mEqAddView = findViewById(R.id.tutorial_equalizerView);
        tripleUpArrow = findViewById(R.id.eq_tutorial_triple_up_arrows_img);
        checkBoxANC = findViewById(R.id.image_view_tutorial_dialog_noise_cancel);
        imageViewAmbientAaware = findViewById(R.id.image_view_tutorial_dialog_ambient_aware);
        mEqAddView.setOnEqChangeListener(new OnEqChangeListener() {
            @Override
            public void onEqValueChanged(int eqIndex, float value) {
                dismiss();
                onDialogListener.onConfirm();
            }

            @Override
            public void onEqDragFinished(float[] pointX, float[] pointY) {
                dismiss();
                onDialogListener.onConfirm();
            }
        });
        Window window = getWindow();
        WindowManager.LayoutParams lp = null;
        if (window != null)
            lp = getWindow().getAttributes();
        if (lp != null) {
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
//        window.setWindowAnimations(R.style.style_down_to_top);
        if (window != null)
            window.setAttributes(lp);

        deviceName = ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).deviceName;
        Logger.d(TAG, "deviceName" + deviceName);
        RelativeLayout relativeLayoutNoiceCancel = findViewById(R.id.relative_layout_tutorial_dialog_noise_cancel);
        if (!DeviceFeatureMap.isFeatureSupported(deviceName, Feature.ENABLE_NOISE_CANCEL)) {
            relativeLayoutNoiceCancel.setVisibility(View.GONE);
        } else {
            relativeLayoutNoiceCancel.setVisibility(View.VISIBLE);
            checkBoxANC.setOnClickListener(this);
        }

        relativeLayoutAmbientAware = findViewById(R.id.relative_layout_tutorial_dialog_ambient_aware);
        if (!DeviceFeatureMap.isFeatureSupported(deviceName, Feature.ENABLE_AMBIENT_AWARE) || isShowOnlyNoiceCancelingType()) {
            relativeLayoutAmbientAware.setVisibility(View.GONE);
        } else {
            //relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
            ImageView imageViewAbientAware = findViewById(R.id.image_view_tutorial_dialog_ambient_aware);
            imageViewAbientAware.setOnClickListener(this);
        }
        if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)) {
            relativeLayoutNoiceCancel.setVisibility(View.VISIBLE);
            checkBoxANC.setBackgroundResource(R.drawable.checkbox_talk_through_selector);
            checkBoxANC.setOnClickListener(this);
            CustomFontTextView tv_noise_cancle = findViewById(R.id.tv_noise_cancle);
            tv_noise_cancle.setText(R.string.talkthru);
            relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
            //imageViewAmbientAaware.setBackgroundResource(R.mipmap.aa_icon_non_active);
            //imageViewAmbientAaware.setTag("0");
            setTextViewTips(R.string.tutorial_tips_zero_live_400);
        }

        setDeviceImageHeight();
    }

    private void setDeviceImageHeight() {
        AppImageView imageViewDevice = findViewById(R.id.image_view_tutorial_dialog_device_image);
        int height = UiUtils.getDashboardDeviceImageHeight(mActivity);
        Logger.d(TAG, "height:" + height);
        int statusHeight = UiUtils.getStatusHeight(mActivity);
        height = height - statusHeight / 2;
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
                    Logger.d(TAG, "KEYBOARD_STATE_INIT:height=" + eqViewHeight);
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

    private boolean isShowOnlySmartAmbientType() {
        return (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA));
    }

    private boolean isShowOnlyNoiceCancelingType() {
        return (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC));
    }

    public void setChecked(boolean isChecked) {
        if (checkBoxANC != null) {
            checkBoxANC.setChecked(isChecked);
        }
        if (!deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC)) {
            if (isChecked) {
                if (relativeLayoutAmbientAware != null) {
                    relativeLayoutAmbientAware.setVisibility(View.VISIBLE);
                }
                setTextViewTips(R.string.tutorial_tips_one);
            } else {
                if (relativeLayoutAmbientAware != null) {
                    relativeLayoutAmbientAware.setVisibility(View.INVISIBLE);
                }

                setTextViewTips(R.string.tutorial_tips_zero);


            }
        } else {
            if (isChecked) {
                setTextViewTips(R.string.tutorial_tips_zero_live_650);
            } else {
                setTextViewTips(R.string.tutorial_tips_one_live_650);
            }
        }

    }

    public void updateBleAAUI(int aaValue) {
        Logger.d(TAG, "updateAAUI:" + aaValue);
        if (aaValue == 0) {
            checkBoxANC.setChecked(false);
            imageViewAmbientAaware.setTag("0");
            imageViewAmbientAaware.setBackgroundResource(R.mipmap.aa_icon_non_active);
        } else if (aaValue == 1) {
            checkBoxANC.setChecked(true);
            imageViewAmbientAaware.setTag("0");
            imageViewAmbientAaware.setBackgroundResource(R.mipmap.aa_icon_non_active);
        } else if (aaValue == 2) {
            checkBoxANC.setChecked(false);
            imageViewAmbientAaware.setTag("1");
            imageViewAmbientAaware.setBackgroundResource(R.mipmap.aa_icon_active);
        }
    }

    public void setTextViewTips(int restId) {
        if (textViewTips != null) {
            textViewTips.setVisibility(View.VISIBLE);
            textViewTips.setText(restId);
        }
    }

    public void showEqInfo() {
        if (mActivity instanceof HomeActivity) {
            ((HomeActivity) mActivity).setEqMenuColor(false);
        }

        if (textViewEqName != null) {
            textViewEqName.setVisibility(View.INVISIBLE);
        }
        if (frameLayoutEqInfo != null) {
            frameLayoutEqInfo.setVisibility(View.INVISIBLE);
        }
        if (relativeLayoutEqGrey != null) {
            relativeLayoutEqGrey.setVisibility(View.VISIBLE);
            relativeLayoutEqGrey.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_circle_off_eq_name_bg_normal));
        }
        if (textViewEqGrey != null) {
            textViewEqGrey.setVisibility(View.VISIBLE);
        }
        if (linearLayoutAnc != null) {
            linearLayoutAnc.setVisibility(View.GONE);
        }
    }

    private void hideEqInfo() {
        if (frameLayoutEqInfo != null) {
            frameLayoutEqInfo.setVisibility(View.GONE);
        }
        if (linearLayoutAnc != null) {
            linearLayoutAnc.setVisibility(View.GONE);
        }
    }

    private void hideSkip() {
        if (textViewSkip != null) {
            textViewSkip.setVisibility(View.GONE);
        }
    }

    private void showAdd() {
        if (relativeLayoutAdd != null) {
            relativeLayoutAdd.setVisibility(View.VISIBLE);
        }
    }

    public void updateCurrentEQ(int index) {
        textViewEqName.setVisibility(View.VISIBLE);
        switch (index) {
            case 0: {
                textViewEqName.setText(R.string.off);
                frameLayoutEqInfo.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_aa_bg));
                break;
            }
            case 1: {
                ((JBLApplication) DashboardActivity.getDashboardActivity().getApplication()).globalEqInfo.eqOn = true;
                textViewEqName.setText(R.string.jazz);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                ((JBLApplication) DashboardActivity.getDashboardActivity().getApplication()).globalEqInfo.eqOn = true;
                textViewEqName.setText(R.string.vocal);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                ((JBLApplication) DashboardActivity.getDashboardActivity().getApplication()).globalEqInfo.eqOn = true;
                textViewEqName.setText(R.string.bass);
                frameLayoutEqInfo.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                ((JBLApplication) DashboardActivity.getDashboardActivity().getApplication()).globalEqInfo.eqOn = true;
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
        if (isDaEnable) {
            textViewTips.setText(JBLApplication.getJBLApplicationContext().getString(R.string.tutorial_tips_one_live));
        } else {
            textViewTips.setText(JBLApplication.getJBLApplicationContext().getString(R.string.tutorial_tips_two_live));
        }
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private void showTripleArrowsAnimation() {
//        final Animation tripleArrowsAnim = AnimationUtils.loadAnimation(JBLApplication.getJBLApplicationContext(), R.anim.anim_triple_up_arrow);
//        tripleUpArrow.setAnimation(tripleArrowsAnim);
//        tripleUpArrow.setVisibility(View.VISIBLE);
//
//
////        final GestureDetector gestureDetector = new GestureDetector(gestureListener);
//        final GestureDetector gestureDetector = new GestureDetector(mActivity, gestureListener);
//
//        frameLayoutEqInfo.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//
//        });
//
//    }

//    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return false;
//        }
//
//        @Override
//        public void onShowPress(MotionEvent e) {
//
//        }
//
//        @Override
//        public boolean onSingleTapUp(MotionEvent e) {
//            return false;
//        }
//
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            return false;
//        }
//
//        @Override
//        public void onLongPress(MotionEvent e) {
//
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            if (e1.getY() - e2.getY() > 25 && Math.abs(velocityY) > 25) {
//                tripleUpArrow.clearAnimation();
//                tripleUpArrow.setVisibility(View.GONE);
//                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
//                if (fr == null) {
//                    Logger.i(TAG, "fr is null");
//                    return false;
//                }
//                if (fr instanceof EqSettingFragment) {
//                    Logger.i(TAG, "fr is already showed");
//                    return false;
//                }
//                DashboardActivity.getDashboardActivity().switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
//                hideEqInfo();
//                setTextViewTips(R.string.tutorial_tips_five);
//                hideSkip();
//                showAdd();
//            }
//            return false;
//        }
//    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_tutorial_dialog_noise_cancel: {
                if (mActivity instanceof HomeActivity) {
                    if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT) || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)) {
                        if (checkBoxANC.isChecked()) {
                            Logger.d(TAG, "noise cancel  checked");
                            checkBoxANC.setChecked(true);
                            /*if (imageViewAmbientAaware.getTag().equals("1")) {
                                imageViewAmbientAaware.setBackground(mActivity.getResources().getDrawable(R.mipmap.aa_icon_non_active));
                                imageViewAmbientAaware.setTag("0");
                            }*/
                            /*if (!isDelayed) {
                                isDelayed = true;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTextViewTips(R.string.tutorial_tips_three);
                                        showEqInfo();
                                    }
                                }, 1000);
                            }*/
                            setTextViewTips(R.string.tutorial_tips_three);
                            showEqInfo();
                        } else {
                            Logger.d(TAG, "noise cancel unchecked");
                            checkBoxANC.setChecked(false);
                            setTextViewTips(R.string.tutorial_tips_three);
                            showEqInfo();
                        }
                        ((HomeActivity) mActivity).setBleAAComand(checkBoxANC, imageViewAmbientAaware);
                    } else {
                        setChecked(checkBoxANC.isChecked());
                        if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC)) {
                            if (!isDelayed) {
                                isDelayed = true;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showEqInfo();
                                        setTextViewTips(R.string.tutorial_tips_three);
                                    }
                                }, 1000);
                            }
                        }
                        ((HomeActivity) mActivity).tutorialSetANC(checkBoxANC);
                    }
                }
                break;
            }
            case R.id.image_view_tutorial_dialog_ambient_aware: {
                if (mActivity instanceof HomeActivity) {
                    if (!LiveManager.getInstance().isConnected()) {
                        setTextViewTips(R.string.tutorial_tips_two);
                        checkBoxANC.setChecked(true);
                        ((HomeActivity) mActivity).showAncPopupWindow(relativeLayout);
                    } else {
                        if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                                || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)) {
                            Logger.d(TAG, "tag: new device" + imageViewAmbientAaware.getTag());
                            /*if (imageViewAmbientAaware.getTag().equals("1")) {
                                imageViewAmbientAaware.setBackground(mActivity.getResources().getDrawable(R.mipmap.aa_icon_non_active));
                                imageViewAmbientAaware.setTag("0");
                            } else if (imageViewAmbientAaware.getTag().equals("0")) {
                                imageViewAmbientAaware.setBackground(mActivity.getResources().getDrawable(R.mipmap.aa_icon_active));
                                imageViewAmbientAaware.setTag("1");
                                checkBoxANC.setChecked(false);
                            }
                            if (!isDelayed) {
                                isDelayed = true;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        showEqInfo();
                                        setTextViewTips(R.string.tutorial_tips_three);
                                    }
                                }, 1000);
                            }*/
                            showEqInfo();
                            setTextViewTips(R.string.tutorial_tips_three);
                            ((HomeActivity) mActivity).setBleAAComand(checkBoxANC, imageViewAmbientAaware);
                        }
                    }
                }
                break;
            }
            case R.id.frame_layout_tutorial_dialog_eq_info: {
//                repeatTrippleArrowAnim = false;
                tripleUpArrow.clearAnimation();
                tripleUpArrow.setVisibility(View.GONE);
                if (mActivity instanceof HomeActivity) {
                    ((HomeActivity) mActivity).switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                }
                hideEqInfo();
                setTextViewTips(R.string.tutorial_tips_five);
                hideSkip();
                showAdd();
                break;
            }
            case R.id.relative_layout_tutorial_dialog_add: {
//                dismiss();
                if (relativeLayoutAdd != null) {
                    relativeLayoutAdd.setVisibility(View.GONE);
                }

                Fragment fr = mActivity.getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    Logger.i(TAG, "fr is null");
                    return;
                }
                if (fr instanceof EqSettingFragment) {
                    Logger.i(TAG, "fr is already showed");
                    ((EqSettingFragment) fr).onAddCustomEq(true, false);
                }
                setTextViewTips(R.string.tutorial_tips_six);
                EQModel currSelectedEq = new EQModel();
                JBLApplication application = (JBLApplication) mActivity.getApplication();
                currSelectedEq.id = application.globalEqInfo.maxEqId + 1;
                currSelectedEq.index = application.globalEqInfo.maxEqId + 1;
                currSelectedEq.eqType = GraphicEQPreset.User.value();
                mEqAddView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.white);
                mTutorialEqSystemParentLayout.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.text_view_tutorial_dialog_skip: {
                dismiss();
                onDialogListener.onConfirm();
                break;
            }
            case R.id.text_view_tutorial_dialog_eq_grey: {
                if (relativeLayoutEqGrey != null) {
                    relativeLayoutEqGrey.setVisibility(View.GONE);
                }
                if (frameLayoutEqInfo != null) {
                    frameLayoutEqInfo.setVisibility(View.VISIBLE);
                }
                if (mActivity instanceof HomeActivity) {
                    ((HomeActivity) mActivity).setEqMenuColor(true);
                }
                if (textViewEqName != null) {
                    textViewEqName.setVisibility(View.VISIBLE);
                }
                setTextViewTips(R.string.tutorial_tips_four);
                //showTripleArrowsAnimation();
                break;
            }
        }
    }
}
