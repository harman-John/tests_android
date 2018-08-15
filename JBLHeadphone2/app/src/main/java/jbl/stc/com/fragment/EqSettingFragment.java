package jbl.stc.com.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqRecyclerAdapter;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.CircleModel;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.FastClickHelper;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.EqualizerShowView;
import jbl.stc.com.view.MyGridLayoutManager;


public class EqSettingFragment extends BaseFragment implements View.OnClickListener {
    private EqualizerShowView equalizerView;
    private ImageView eqEditImage;
    private View titleBar;
    private TextView eqNameText;
    private ImageView closeImageView;
    private ImageView moreImageView;
    private ImageView addImageView;
    private RecyclerView eqRecycleView;
    private EqRecyclerAdapter eqAdapter;
    private RelativeLayout rl_eq_view;
    private LinearLayout linearLayout;


    private List<EQModel> eqModelList = new ArrayList<>();
    private EQModel currSelectedEq;
    private int currSelectedEqIndex;
    private LightX lightX;
    private Handler mHandler = new Handler();
    private float mPosX = 0, mCurPosX;
    private int screenHeght;
    private boolean isDynamicDrawCurve = false;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d("EqSettingFragment:", "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_eq_settings, container, false);
        lightX = AvneraManager.getAvenraManager().getLightX();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeght = dm.heightPixels;
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_SETTINGS_PANEL);
        return rootView;
    }


    private void initView() {
        titleBar = rootView.findViewById(R.id.titleBar);
        equalizerView = rootView.findViewById(R.id.equalizerView);
        eqEditImage = rootView.findViewById(R.id.eqEditImage);
        closeImageView = rootView.findViewById(R.id.closeImageView);
        moreImageView = rootView.findViewById(R.id.moreImageView);
        addImageView = rootView.findViewById(R.id.addImageView);
        eqNameText = rootView.findViewById(R.id.text_view_eq_settings_eq_name);
        eqRecycleView = rootView.findViewById(R.id.eqRecycleView);
        eqRecycleView.setLayoutManager(new MyGridLayoutManager(getActivity(), 2));
        eqAdapter = new EqRecyclerAdapter();
        eqRecycleView.setAdapter(eqAdapter);
        rl_eq_view = (RelativeLayout) rootView.findViewById(R.id.rl_eq_view);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
    }

    private void initEvent() {
        eqEditImage.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
        moreImageView.setOnClickListener(this);
        addImageView.setOnClickListener(this);
        eqAdapter.setOnEqSelectedListener(new OnEqItemSelectedListener() {
            @Override
            public void onSelected(int position) {
            onEqNameSelected(position, true);
            if (isDynamicDrawCurve) {
                pos = 0;
                myHandler.removeMessages(MSG_SHOW_LINE);
                equalizerView.clearAllPointCircles();
                myHandler.sendEmptyMessage(MSG_SHOW_LINE);
                Logger.d(TAG, "aaaaa onEqNameSelected "); //dynamic draw curve
            }

            }
        });
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                createVelocityTracker(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        yDown = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        yMove = event.getRawY();
                        int ySpeed = getScrollVelocity();
//                        if (distanceY > Y_DISTANCE_MIN && ySpeed > Y_SPEED_MIN) {
//                            if (FastClickHelper.isFastClick()) {
//                                return true;
//                            }
//                            getActivity().onBackPressed();
//                        }
                        rootView.setTranslationY(yMove);

                        break;
                    case MotionEvent.ACTION_UP:
                        recycleVelocityTracker();
                        if (yMove < screenHeght - UiUtils.dip2px(getActivity(), 70)) {
                            rootView.setTranslationY(0);
                        } else {
                            getActivity().onBackPressed();

                        }

                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        equalizerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mCurPosX - mPosX > 0
                                && (Math.abs(mCurPosX - mPosX) > 25)) {
                            //scroll to right
                            if (currSelectedEqIndex >= 1) {
                                eqAdapter.setSelectedIndex(currSelectedEqIndex - 1);
                            } else {
                                eqAdapter.setSelectedIndex(eqModelList.size() - 1);
                            }


                        } else if (mCurPosX - mPosX < 0
                                && (Math.abs(mCurPosX - mPosX) > 25)) {
                            //scroll to left
                            if (currSelectedEqIndex < eqModelList.size() - 1) {
                                eqAdapter.setSelectedIndex(currSelectedEqIndex + 1);
                            } else {
                                eqAdapter.setSelectedIndex(0);
                            }
                        }

                        break;
                }
                return true;
            }

        });
    }

    private void initValue() {
        List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(mContext);
        eqModelList.clear();
        eqModelList.addAll(eqModels);
        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""), mContext);
        //LogUtil.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""));

        /*if (eqModelList == null || eqModelList.isEmpty()) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }*/
        Logger.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, getResources().getString(R.string.off)));
        if (currSelectedEq != null && currSelectedEq.eqName != null) {
            if (application.deviceInfo.eqOn) {
                for (int i = 0; i < eqModelList.size(); i++) {
                    if (currSelectedEq.eqName.equals(eqModelList.get(i).eqName)) {
                        eqModelList.get(i).isSelected = true;
                        currSelectedEqIndex = i;
                    } else {
                        eqModelList.get(i).isSelected = false;
                    }
                }
            }
            if (application.deviceInfo.eqOn) {
                eqNameText.setText(currSelectedEq.eqName);
                eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            } else {
                eqNameText.setText(R.string.off);
                eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
            }
        } else {
            eqNameText.setText(R.string.off);
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
            for (EQModel model : eqModelList) {
                model.isSelected = false;
            }
            eqModelList.get(0).isSelected = true;
        }
        for (int i = 0; i < eqModelList.size(); i++) {
            Logger.d(TAG, "i=" + i + "," + eqModelList.get(i));
        }

        eqAdapter.setEqModels(eqModelList);

        //float[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        if (currSelectedEq != null) {
            if (isDynamicDrawCurve) {
                pos = 0;
                equalizerView.clearAllPointCircles();
                myHandler.removeMessages(MSG_SHOW_LINE);
                myHandler.sendEmptyMessageDelayed(MSG_SHOW_LINE, 500);
                Logger.d(TAG, "aaaaa initValue");
            } else {
                equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
            }

        }
        if (currSelectedEqIndex == 0) {
            linearLayout.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            eqEditImage.setClickable(false);
        } else {
            linearLayout.setBackgroundResource(R.drawable.shape_gradient_eq);
            eqEditImage.setClickable(true);
        }
        smoothToPosition();
    }

    private static int pos = 0;
    private MyHandler myHandler = new MyHandler();
    private final static int MSG_SHOW_LINE = 0;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_LINE: {
                    List<CircleModel> listsBak = equalizerView.getAllPointCircles(currSelectedEq.getPointX(), currSelectedEq.getPointY());
                    if (pos >= listsBak.size()) {
                        pos = 0;
                        break;
                    }

                    if (pos < listsBak.size() && pos + 5 >= listsBak.size()) {
                        equalizerView.setCurveData(listsBak.subList(pos, listsBak.size() - 1), R.color.text_white_80, isDynamicDrawCurve);
                    } else {
                        equalizerView.setCurveData(listsBak.subList(pos, pos + 5), R.color.text_white_80, isDynamicDrawCurve);
                    }
                    myHandler.sendEmptyMessage(MSG_SHOW_LINE);
                    pos = pos + 5;
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    ;

    private void smoothToPosition() {
        if (currSelectedEqIndex > 1) {
            Logger.d(TAG, "smoothToPosition currSelectedEqIndex=" + currSelectedEqIndex);
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
    }

    private void onEqNameSelected(int eqIndex, boolean fromUser) {
        Logger.d(TAG, "onEqNameSelected eqIndex is " + eqIndex);
        currSelectedEq = eqModelList.get(eqIndex);
        currSelectedEqIndex = eqIndex;
        eqNameText.setText(currSelectedEq.eqName);
        eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        application.deviceInfo.eqOn = true;
        for (EQModel model : eqModelList) {
            model.isSelected = false;
        }
        currSelectedEq.isSelected = true;
        eqAdapter.setEqModels(eqModelList);
        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, mContext);
        if (!currSelectedEq.eqName.equals(getResources().getString(R.string.off))) {
            PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, currSelectedEq.eqName, mContext);
        }
        //int[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        if (!isDynamicDrawCurve) {
            equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
        }
        AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
        if (fromUser) {
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
        if (eqIndex == 0) {
            linearLayout.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            eqEditImage.setClickable(false);
        } else {
            linearLayout.setBackgroundResource(R.drawable.shape_gradient_eq);
            eqEditImage.setClickable(true);
        }
        mHandler.removeCallbacks(applyRunnable);
        mHandler.postDelayed(applyRunnable, 300);
    }

    /**
     * Stop user to abuse app and send all command only after 300 milliseconds
     */
    Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {
            switch (currSelectedEqIndex) {
                case 0:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Off);
                    break;
                case 1:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                    break;
                case 2:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Vocal);
                    break;
                case 3:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Bass);
                    break;
                default:
                    ANCControlManager.getANCManager(getContext()).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(currSelectedEq));
                    break;
            }
            AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
            PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, getActivity());
            Logger.d(TAG, "select eq position is " + String.valueOf(currSelectedEqIndex));
        }
    };

    public void onAddCustomEq(boolean isAdd, boolean isPreset) {
        Logger.d(TAG, "onAddCustomEq()");
        EqCustomFragment fragment = new EqCustomFragment();
        fragment.setOnCustomEqListener(onCustomEqListener);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_ADD, isAdd);
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_PRESET, isPreset);
        if (!isAdd || isPreset) {
            bundle.putSerializable(EqCustomFragment.EXTRA_EQ_MODEL, currSelectedEq);
        }
        fragment.setArguments(bundle);
        switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
        eqRecycleView.smoothScrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eqEditImage:
                if (currSelectedEqIndex < 4) {
                    onAddCustomEq(true, true);
                } else {
                    onAddCustomEq(false, false);
                }
                break;
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
            case R.id.moreImageView:
                /*EqMoreSettingFragment fragment = new EqMoreSettingFragment();
                switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);*/
                NewEqMoreSettingFragment fragment = new NewEqMoreSettingFragment();
                switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            case R.id.addImageView:
                onAddCustomEq(true, false);
                break;
        }
    }


    private OnCustomEqListener onCustomEqListener = new OnCustomEqListener() {
        @Override
        public void onCustomEqResult(EQModel model, boolean isAdd) {
            if (model != null) {
                if (!isAdd) {//modify
                    currSelectedEq = eqModelList.get(currSelectedEqIndex);
                    currSelectedEq.eqName = model.eqName;
                    currSelectedEq.value_32 = model.value_32;
                    currSelectedEq.value_64 = model.value_64;
                    currSelectedEq.value_125 = model.value_125;
                    currSelectedEq.value_250 = model.value_250;
                    currSelectedEq.value_500 = model.value_500;
                    currSelectedEq.value_1000 = model.value_1000;
                    currSelectedEq.value_2000 = model.value_2000;
                    currSelectedEq.value_4000 = model.value_4000;
                    currSelectedEq.value_8000 = model.value_8000;
                    currSelectedEq.value_16000 = model.value_16000;
                    onEqNameSelected(currSelectedEqIndex, false);
                } else {//add new
                    int addIndex = eqModelList.size() - 1;
                    eqModelList.add(addIndex, model);
                    onEqNameSelected(addIndex, false);
                }
                smoothToPosition();
            }
        }
    };
}
