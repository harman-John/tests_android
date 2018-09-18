package jbl.stc.com.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqChangeListener;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.ToastUtil;
import jbl.stc.com.view.EqualizerAddView;
import jbl.stc.com.view.KeyboardLayout;

public class EqCustomFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_IS_ADD = "IS_ADD";
    public static final String EXTRA_EQ_MODEL = "EQ_MODEL";
    public static final String EXTRA_IS_PRESET = "IS_PRESET";
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
    private LightX lightX;
    private boolean isPreset = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_eq_custom, container, false);
        lightX = AvneraManager.getAvenraManager().getLightX();
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance().setScreenName(AnalyticsManager.SCREEN_CUSTOM_EQ);
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
                    eqValueArray[eqIndex] = (int) value;
                    Logger.d(TAG, "onEqValueChanged eqIndex=" + eqIndex + ",value=" + value);
                    if (firstTimeAddEqTipText.getVisibility() == View.VISIBLE) {
                        firstTimeAddEqTipText.setVisibility(View.GONE);
                        mHandler.removeCallbacks(textUpRunnable);
                        mHandler.removeCallbacks(textDownRunnable);
                    }
                }
            }

            @Override
            public void onEqDragFinished(float[] pointX, float[] pointY) {
                Logger.d(TAG, "onEqDragFinished pointX=" + Arrays.toString(pointX) + ",pointY=" + Arrays.toString(pointY));
                currSelectedEq.setPointX(pointX);
                currSelectedEq.setPointY(pointY);
                //ANCControlManager.getANCManager(getContext()).applyPresetsWithBand(GraphicEQPreset.User, eqValueArray);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Logger.d(TAG, "click back");
                    if (isAddOperate) {
                        EQSettingManager.get().deleteEQ(currSelectedEq.eqName, getActivity());
                        if (application.globalEqInfo.eqOn) {
                            currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getActivity(),
                                    ""), getActivity());
                            if (currSelectedEq == null) {
                                currSelectedEq = new EQModel();
                                application.globalEqInfo.eqOn = false;
                            }
                        }
                    }
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });

    }

    private void initValue() {
        defaultEqName = getString(R.string.create_eq_default_name);
        eqModelList = EQSettingManager.get().getCompleteEQList(mContext);
        Bundle bundle = getArguments();
        if (bundle != null) {
            isAddOperate = bundle.getBoolean(EXTRA_IS_ADD);
            currSelectedEq = (EQModel) bundle.getSerializable(EXTRA_EQ_MODEL);
            isPreset = bundle.getBoolean(EXTRA_IS_PRESET);
        }
        if (isPreset) {
            defaultEqName = currSelectedEq.eqName;
        }
        Logger.d(TAG, "isAddOperate=" + isAddOperate + ",currSelectedEq=" + currSelectedEq);
        if (currSelectedEq == null) {
            currSelectedEq = new EQModel();
            currSelectedEq.eqType = GraphicEQPreset.User.value();
        }
        firstTimeAddEqTipText.setVisibility(View.GONE);
        if (isAddOperate) {
            if (!application.globalEqInfo.hasEq) {
                firstTimeAddEqTipText.setVisibility(View.VISIBLE);
                mHandler.postDelayed(textUpRunnable, 1500);
            }
            currSelectedEq.eqName = getNewEqName();
            eqNameEdit.setText(currSelectedEq.eqName);
            if (isAddOperate) {
                currSelectedEq.id = application.globalEqInfo.maxEqId + 1;
                currSelectedEq.index = application.globalEqInfo.maxEqId + 1;
                currSelectedEq.eqType = GraphicEQPreset.User.value();
                EQSettingManager.get().addCustomEQ(currSelectedEq, mContext);
            }
        } else {
            srcSelectedEq = currSelectedEq.clone();
            eqNameEdit.setText(currSelectedEq.eqName);

        }

        eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.white);

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
                    Logger.d(TAG, "KEYBOARD_STATE_INIT:height=" + eqViewHeight);
                    equalizerLayout.setLayoutParams(equalizerParams);
                    equalizerView.setCustomHeight(eqViewHeight);
                    break;
                case KeyboardLayout.KEYBOARD_STATE_HIDE:
                    Logger.d(TAG, "KEYBOARD_STATE_HIDE");
                    eqEditLayoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.eq_name_edit_bottom_margin);
                    equalizerLayout.setLayoutParams(equalizerParams);
                    equalizerView.setSupportDrag(true);
                    viewKeyboardLayout.setBackgroundResource(R.color.transparent);
                    break;
                case KeyboardLayout.KEYBOARD_STATE_SHOW:
                    Logger.d(TAG, "KEYBOARD_STATE_SHOW");
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
                    if (application.globalEqInfo.eqOn) {
                        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getActivity(),
                                ""), getActivity());
                        if (currSelectedEq == null) {
                            currSelectedEq = new EQModel();
                            application.globalEqInfo.eqOn = false;
                        }
                    }
                }
                application.isAddEqFragment = false;
                getActivity().onBackPressed();
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
            application.globalEqInfo.maxEqId++;
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
        /*String eqName = eqNameEdit.getText().toString().trim();
        String updateEqName = currSelectedEq.eqName;
        if (TextUtils.isEmpty(eqName)) {
            if (isAddOperate) {
                eqName = defaultEqName;
            } else {
                eqName = updateEqName;
            }
        }
        return EQSettingManager.getNewEqName(eqModelList, eqName, updateEqName);*/
        String eqName = "";
        if (currSelectedEq != null) {

            if (isAddOperate) {

                boolean isContainDefault = false;
                boolean isEqualDefault = false;
                List<EQModel> eqNameModels = new ArrayList<>();
                for (EQModel eqModel : eqModelList) {
                    if (eqModel.eqName.contains(defaultEqName)) {
                        if (eqModel.eqName.equals(defaultEqName)) {
                            isEqualDefault = true;
                        }
                        isContainDefault = true;
                        if (eqModel.eqName.length() > defaultEqName.length()) {
                            eqNameModels.add(eqModel);
                        }
                    }
                }
                if (isContainDefault) {
                    if (isEqualDefault) {
                        List<Integer> eqNameIndexs = new ArrayList<>();
                        if (eqNameModels.size() > 0) {
                            for (EQModel eqModel : eqNameModels) {
                                String[] strs = eqModel.eqName.split(defaultEqName);
                                eqNameIndexs.add(Integer.valueOf(strs[1]));
                            }
                            Collections.max(eqNameIndexs);
                            int minIndex = Collections.min(eqNameIndexs);
                            int maxIndex = Collections.max(eqNameIndexs);
                            Logger.d(TAG, "EqNamemax:" + Collections.max(eqNameIndexs));
                            Logger.d(TAG, "EqNamemin:" + Collections.min(eqNameIndexs));
                            if (minIndex > 2) {
                                eqName = defaultEqName + String.valueOf(minIndex - 1);
                            } else {
                                eqName = defaultEqName + String.valueOf(maxIndex + 1);
                            }
                        } else {
                            eqName = defaultEqName + "2";
                        }
                    } else {
                        eqName = defaultEqName;
                    }
                } else {
                    if (defaultEqName.equals(getString(R.string.create_eq_default_name))) {
                        eqName = defaultEqName;
                    }
                }
            } else {
                eqName = currSelectedEq.eqName;
            }
        }
        return eqName;
    }

    private void setResultOk() {
        hideSoftKeyBoard();
        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, mContext);
        if (isAddOperate) {
            application.globalEqInfo.eqOn = true;
        }
        if (onCustomEqListener != null) {
            onCustomEqListener.onCustomEqResult(currSelectedEq, isAddOperate);
        } else {
            application.globalEqInfo.hasEq = true;
            int mConnectStatus = PreferenceUtils.getInt(JBLConstant.KEY_CONNECT_STATUS, getActivity());
            if (LiveManager.getInstance().isConnected()) {
                //add the ble user Eq code
                LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, EQSettingManager.get().getBleEqSettingFromEqModel(currSelectedEq));
            } else {
                ANCControlManager.getANCManager(getContext()).applyPresetsWithBand(GraphicEQPreset.User, eqValueArray);
            }
            switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
        }
        getActivity().getSupportFragmentManager().popBackStack();

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
