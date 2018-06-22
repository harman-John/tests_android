package jbl.stc.com.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.Arrays;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.controller.AnalyticsManager;
import jbl.stc.com.controller.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.entity.GraphicEQPreset;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqChangeListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.LogUtil;
import jbl.stc.com.utils.ToastUtil;
import jbl.stc.com.view.EqualizerAddView;
import jbl.stc.com.view.KeyboardLayout;


/**
 * Custom Eq screen
 * Created by darren.lu on 08/11/2017.
 */
public class EqCustomFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_IS_ADD = "IS_ADD";
    public static final String EXTRA_EQ_MODEL = "EQ_MODEL";
    private EqualizerAddView equalizerView;
    private ImageView addEqOkImage;
    private EditText eqNameEdit;
    private ImageView closeImageView;
    private TextView firstTimeAddEqTipText;
    private View eqEditLayout;
    private View equalizerLayout;
    private RelativeLayout.LayoutParams equalizerParams;
    private RelativeLayout.LayoutParams eqEditLayoutParams;
    private KeyboardLayout viewKeyboardLayout;

    private int[] eqValueArray;
    private OnCustomEqListener onCustomEqListener;

    private boolean isAddOperate = true;//true is add EQ, false is modify EQ
    private EQModel currSelectedEq;
    private EQModel srcSelectedEq;
    private Handler mHandler = new Handler();
    private String defaultEqName;
    private List<EQModel> eqModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_eq_custom, container, false);
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_Edit);
        return rootView;
    }

    private void initView() {
        application.isAddEqFragment = true;
        equalizerView = (EqualizerAddView) rootView.findViewById(R.id.equalizerView);
        addEqOkImage = (ImageView) rootView.findViewById(R.id.addEqOkImage);
        closeImageView = (ImageView) rootView.findViewById(R.id.closeImageView);
        eqNameEdit = (EditText) rootView.findViewById(R.id.eqNameEdit);
        firstTimeAddEqTipText = (TextView) rootView.findViewById(R.id.firstTimeAddEqTipText);
        eqEditLayout = rootView.findViewById(R.id.eqEditLayout);
        equalizerLayout = rootView.findViewById(R.id.equalizerLayout);
        eqEditLayoutParams = (RelativeLayout.LayoutParams) eqEditLayout.getLayoutParams();
        viewKeyboardLayout = (KeyboardLayout) rootView.findViewById(R.id.viewKeyboardLayout);
        viewKeyboardLayout.setOnKeyboardStateChangedListener(onKeyboardStateChangedListener);
    }

    private void initEvent() {
        addEqOkImage.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
        equalizerView.setOnEqChangeListener(new OnEqChangeListener() {
            @Override
            public void onEqValueChanged(int eqIndex, float value) {
                if (eqIndex >= 0 && eqIndex < eqValueArray.length) {
                    eqValueArray[eqIndex] = (int)value;
                    LogUtil.d(TAG, "onEqValueChanged eqIndex=" + eqIndex + ",value=" + value);
                    if (firstTimeAddEqTipText.getVisibility() == View.VISIBLE) {
                        firstTimeAddEqTipText.setVisibility(View.GONE);
                        mHandler.removeCallbacks(textUpRunnable);
                        mHandler.removeCallbacks(textDownRunnable);
                    }
                }
            }

            @Override
            public void onEqDragFinished(float[] pointX, float[] pointY) {
                LogUtil.d(TAG, "onEqDragFinished pointX=" + Arrays.toString(pointX) + ",pointY=" + Arrays.toString(pointY));
                currSelectedEq.setPointX(pointX);
                currSelectedEq.setPointY(pointY);
                //CommandManager.get().setGrEqBandGains(currSelectedEq.id, eqValueArray);
//                currSelectedEq = EQSettingManager.get().getEQModelFromValues(currSelectedEq, eqValueArray);
//                EQSettingManager.get().updateCustomEQ(currSelectedEq, currSelectedEq.eqName, mContext);
            }
        });

    }

    private void initValue() {
        defaultEqName = getString(R.string.create_eq_default_name);
        eqModelList = EQSettingManager.get().getCompleteEQList(mContext);
        Bundle bundle = getArguments();
        if (bundle != null) {
            isAddOperate = bundle.getBoolean(EXTRA_IS_ADD);
            if (!isAddOperate) {
                currSelectedEq = (EQModel) bundle.getSerializable(EXTRA_EQ_MODEL);
            }
        }
        LogUtil.d(TAG, "isAddOperate=" + isAddOperate + ",currSelectedEq=" + currSelectedEq);
        if (currSelectedEq == null) {
            currSelectedEq = new EQModel();
            currSelectedEq.eqType = GraphicEQPreset.User.value();
        }
        firstTimeAddEqTipText.setVisibility(View.GONE);
        if (isAddOperate) {
            if (!application.deviceInfo.hasEq) {
                firstTimeAddEqTipText.setVisibility(View.VISIBLE);
                mHandler.postDelayed(textUpRunnable, 1500);
            }
            currSelectedEq.eqName = getNewEqName();
            eqNameEdit.setText(currSelectedEq.eqName);
            if (isAddOperate) {
                currSelectedEq.id = application.deviceInfo.maxEqId + 1;
                currSelectedEq.index = application.deviceInfo.maxEqId + 1;
                EQSettingManager.get().addCustomEQ(currSelectedEq, mContext);
            }
        } else {
            srcSelectedEq = currSelectedEq.clone();
            eqNameEdit.setText(currSelectedEq.eqName);
        }

        eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.main_color);

    }

    private KeyboardLayout.OnKeyboardStateChangedListener onKeyboardStateChangedListener = new KeyboardLayout.OnKeyboardStateChangedListener() {
        @Override
        public void onKeyboardStateChanged(int state, int height) {
            switch (state) {
                case KeyboardLayout.KEYBOARD_STATE_INIT:
                    int eqViewHeight = (int) (height - (getResources().getDimensionPixelSize(R.dimen.eq_name_edit_layout_height)
                            + equalizerView.getMarginTop() + equalizerView.getMarginBottom()));
                    equalizerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            eqViewHeight);
                    LogUtil.d(TAG, "KEYBOARD_STATE_INIT:height=" + eqViewHeight);
                    equalizerLayout.setLayoutParams(equalizerParams);
                    equalizerView.setCustomHeight(eqViewHeight);
                    break;
                case KeyboardLayout.KEYBOARD_STATE_HIDE:
                    LogUtil.d(TAG, "KEYBOARD_STATE_HIDE");
                    eqEditLayoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.eq_name_edit_bottom_margin);
                    equalizerLayout.setLayoutParams(equalizerParams);
                    equalizerView.setSupportDrag(true);
                    viewKeyboardLayout.setBackgroundResource(R.color.transparent);
                    break;
                case KeyboardLayout.KEYBOARD_STATE_SHOW:
                    LogUtil.d(TAG, "KEYBOARD_STATE_SHOW");
                    eqEditLayoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.eq_name_edit_bottom_margin_show);
                    equalizerLayout.setLayoutParams(equalizerParams);
                    equalizerView.setSupportDrag(false);
                    viewKeyboardLayout.setBackgroundResource(R.color.app_dialog_bg);
                    break;
            }
        }
    };

    private Runnable textUpRunnable = new Runnable() {
        @Override
        public void run() {
            firstTimeAddEqTipText.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.anim_first_time_text_up));
            mHandler.postDelayed(textDownRunnable, 600);
        }
    };

    private Runnable textDownRunnable = new Runnable() {
        @Override
        public void run() {
            firstTimeAddEqTipText.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.anim_first_time_text_down));
            mHandler.postDelayed(textUpRunnable, 3000);
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addEqOkImage:
                handlerAddOrModifyEq();
                break;
            case R.id.closeImageView:
                hideSoftKeyBoard();
                if (isAddOperate) {
                    EQSettingManager.get().deleteEQ(currSelectedEq.eqName, getActivity());
                    if (application.deviceInfo.eqOn) {
                        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getActivity(),
                                ""), getActivity());
                        if (currSelectedEq == null) {
                            currSelectedEq = new EQModel();
                            application.deviceInfo.eqOn = false;
                        }
                    } else {
                        currSelectedEq = new EQModel();
                    }
                    eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
                    //CommandManager.get().setGrEqBandGains(currSelectedEq.id, eqValueArray);
                } else {
                    eqValueArray = EQSettingManager.get().getValuesFromEQModel(srcSelectedEq);
                    //CommandManager.get().setGrEqBandGains(srcSelectedEq.id, eqValueArray);
                }
                application.isAddEqFragment = false;
                getOnMainAppListener().getMainActivity().onBackPressed();
                break;
        }
    }

    private void handlerAddOrModifyEq() {
        String eqName = getNewEqName();
        String updateEqName = currSelectedEq.eqName;
        currSelectedEq = EQSettingManager.get().getEQModelFromValues(currSelectedEq, eqValueArray);
        currSelectedEq.eqName = eqName;
        EQSettingManager.OperationStatus operationStatus;
        operationStatus = EQSettingManager.get().updateCustomEQ(currSelectedEq, updateEqName, mContext);
        if (isAddOperate) {
            //ToastUtil.ToastLong(mContext, "add custom eq success!");
            application.deviceInfo.maxEqId++;
            //AnalyticsManager.getInstance(getActivity()).reportNewEQ(eqName);
            setResultOk();
        } else if (operationStatus == EQSettingManager.OperationStatus.UPDATED) {
            //ToastUtil.ToastLong(mContext, "update eq success!");
            //AnalyticsManager.getInstance(getActivity()).reportModifyEQ(eqName);
            setResultOk();
        } else {
            ToastUtil.ToastLong(mContext, "An unknown anomaly has occurred");
        }

    }

    private String getNewEqName() {
        String eqName = eqNameEdit.getText().toString().trim();
        String updateEqName = currSelectedEq.eqName;
        if (TextUtils.isEmpty(eqName)) {
            if (isAddOperate) {
                eqName = defaultEqName;
            } else {
                eqName = updateEqName;
            }
        }
        return EQSettingManager.getNewEqName(eqModelList, eqName, updateEqName);
    }

    private void setResultOk() {
        hideSoftKeyBoard();
        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, mContext);
        if (isAddOperate) {
            application.deviceInfo.eqOn = true;
        }
        if (application.deviceInfo.hasEq) {
            if (onCustomEqListener != null) {
                onCustomEqListener.onCustomEqResult(currSelectedEq, isAddOperate);
            }
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            application.deviceInfo.hasEq = true;
            //CommandManager.get().setGrEqBandGains(currSelectedEq.id, eqValueArray);
            getActivity().getSupportFragmentManager().popBackStack();
            getOnMainAppListener().getMainActivity().switchFragment(new EqSettingFragment());
        }
        getOnMainAppListener().refreshPage();
    }



    public void setOnCustomEqListener(OnCustomEqListener onCustomEqListener) {
        this.onCustomEqListener = onCustomEqListener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        application.isAddEqFragment = false;
        mHandler.removeCallbacks(textUpRunnable);
        mHandler.removeCallbacks(textDownRunnable);
    }
}