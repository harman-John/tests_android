package jbl.stc.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqRecyclerAdapter;
import jbl.stc.com.controller.AnalyticsManager;
import jbl.stc.com.controller.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.entity.GraphicEQPreset;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.FastClickHelper;
import jbl.stc.com.utils.LogUtil;
import jbl.stc.com.view.EqualizerShowView;
import jbl.stc.com.view.MyGridLayoutManager;


/**
 * Setting Eq screen
 * Created by darren.lu on 08/06/2017.
 */

public class EqSettingFragment extends BaseFragment implements View.OnClickListener {
    private EqualizerShowView equalizerView;
    private ImageView eqEditImage;
    private View titleBar;
    private TextView eqNameText;
    private ImageView closeImageView;
    private ImageView moreImageView;
    private RecyclerView eqRecycleView;
    private EqRecyclerAdapter eqAdapter;

    private List<EQModel> eqModelList;
    private EQModel currSelectedEq;
    private int currSelectedEqIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_eq_settings, container, false);
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_SETTING);
        return rootView;
    }

    private void initView() {
        titleBar = rootView.findViewById(R.id.titleBar);
        equalizerView = rootView.findViewById(R.id.equalizerView);
        eqEditImage = rootView.findViewById(R.id.eqEditImage);
        closeImageView = rootView.findViewById(R.id.closeImageView);
        moreImageView = rootView.findViewById(R.id.moreImageView);
        eqNameText = rootView.findViewById(R.id.eqNameText);
        eqRecycleView = rootView.findViewById(R.id.eqRecycleView);
        eqRecycleView.setLayoutManager(new MyGridLayoutManager(getActivity(), 2));
        eqAdapter = new EqRecyclerAdapter();
        eqRecycleView.setAdapter(eqAdapter);
    }

    private void initEvent() {
        eqEditImage.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
        moreImageView.setOnClickListener(this);
        eqAdapter.setOnEqSelectedListener(new OnEqItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                if (position < eqModelList.size()) {
                    if (eqModelList.get(position).isPlusItem) {//last item is plus button
                        onAddCustomEq(true);
                    } else {
                        onEqNameSelected(position, true);
                    }
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
                        int distanceY = (int) (yMove - yDown);
                        int ySpeed = getScrollVelocity();
                        if (distanceY > Y_DISTANCE_MIN && ySpeed > Y_SPEED_MIN) {
                            if (FastClickHelper.isFastClick()) {
                                return true;
                            }
                            getOnMainAppListener().getMainActivity().onBackPressed();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        recycleVelocityTracker();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initValue() {
        eqModelList = EQSettingManager.get().getCompleteEQList(mContext);
        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""), mContext);
        if (eqModelList == null || eqModelList.isEmpty()) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }
        LogUtil.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""));
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
            } else {
                currSelectedEq = new EQModel(GraphicEQPreset.Off);
            }
        } else {
            currSelectedEq = new EQModel(GraphicEQPreset.Off);
        }
        if (application.deviceInfo.eqOn) {
            eqNameText.setText(currSelectedEq.eqName);
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        } else {
            eqNameText.setText(R.string.eq_off_name_text);
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
        }
        for (int i = 0; i < eqModelList.size(); i++) {
            LogUtil.d(TAG, "i=" + i + "," + eqModelList.get(i));
        }
        eqModelList.add(new EQModel(true));
        eqAdapter.setEqModels(eqModelList);

        //float[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80);
        smoothToPosition();
    }

    private void smoothToPosition() {
        if (currSelectedEqIndex > 1) {
            LogUtil.d(TAG, "smoothToPosition currSelectedEqIndex=" + currSelectedEqIndex);
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
    }

    private void onEqNameSelected(int eqIndex, boolean fromUser) {
        LogUtil.d(TAG, "onEqNameSelected eqIndex is " + eqIndex);
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
        int[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80);
        //CommandManager.get().setGrEqBandGains(currSelectedEq.id, eqValueArray);
        getOnMainAppListener().refreshPage();
        AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
        if (fromUser) {
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
    }

    private void onAddCustomEq(boolean isAdd) {
        LogUtil.d(TAG, "onAddCustomEq()");
        EqCustomFragment fragment = new EqCustomFragment();
        fragment.setOnCustomEqListener(onCustomEqListener);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_ADD, isAdd);
        if (!isAdd) {
            bundle.putSerializable(EqCustomFragment.EXTRA_EQ_MODEL, currSelectedEq);
        }
        fragment.setArguments(bundle);
        getOnMainAppListener().getMainActivity().switchFragment(fragment);
        eqRecycleView.smoothScrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eqEditImage:
                if (currSelectedEq.eqType != GraphicEQPreset.Off.value()) {
                    onAddCustomEq(false);
                } else {
                    onAddCustomEq(true);
                }
                break;
            case R.id.closeImageView:
                getOnMainAppListener().getMainActivity().onBackPressed();
                break;
            case R.id.moreImageView:
                EqMoreSettingFragment fragment = new EqMoreSettingFragment();
                getOnMainAppListener().getMainActivity().switchFragment(fragment);
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
