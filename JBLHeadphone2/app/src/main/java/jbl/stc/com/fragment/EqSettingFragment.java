package jbl.stc.com.fragment;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqRecyclerAdapter;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.entity.GraphicEQPreset;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.FastClickHelper;
import jbl.stc.com.utils.ToastUtil;
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
    private ImageView addImageView;
    private RecyclerView eqRecycleView;
    private EqRecyclerAdapter eqAdapter;

    private List<EQModel> eqModelList=new ArrayList<>();
    private EQModel currSelectedEq;
    private int currSelectedEqIndex;
    private int eqType;


        @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("EqSettingFragment:","onCreateView");
        rootView = inflater.inflate(R.layout.fragment_eq_settings, container, false);
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
        addImageView=rootView.findViewById(R.id.addImageView);
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
        addImageView.setOnClickListener(this);
        eqAdapter.setOnEqSelectedListener(new OnEqItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                onEqNameSelected(position, true);
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
                            getActivity().onBackPressed();
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
        List<EQModel> eqTypeModels=new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            EQModel eqModel = new EQModel();
            eqModel.eqType = i;
            eqTypeModels.add(eqModel);
        }
        List<EQModel> eqModels=EQSettingManager.get().getCompleteEQList(mContext);
        eqModelList.clear();
        eqModelList.addAll(eqTypeModels);
        eqModelList.addAll(eqModels);
        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""), mContext);


        //LogUtil.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""));

        /*if (eqModelList == null || eqModelList.isEmpty()) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }*/
        Log.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""));
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
                eqNameText.setText(R.string.eq_off_name_text);
                eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
            }
        }else{
            eqNameText.setText(R.string.eq_off_name_text);
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
            for (EQModel model : eqModelList) {
                model.isSelected = false;
            }
            eqModelList.get(0).isSelected=true;
        }
        for (int i = 0; i < eqModelList.size(); i++) {
            Log.d(TAG, "i=" + i + "," + eqModelList.get(i));
        }


        eqAdapter.setEqModels(eqModelList);

        //float[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        if (currSelectedEq!=null){
           // LogUtil.d("TAG","setCurveData");
            equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80);
        }
        smoothToPosition();
    }

    private void smoothToPosition() {
        if (currSelectedEqIndex > 1) {
            Log.d(TAG, "smoothToPosition currSelectedEqIndex=" + currSelectedEqIndex);
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
    }

    private void onEqNameSelected(int eqIndex, boolean fromUser) {

        if (eqIndex>3) {
            //LogUtil.d(TAG, "onEqNameSelected eqIndex is " + eqIndex);
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
            AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
            if (fromUser) {
                eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
            }
        }else {
            //select eqType
            eqType = eqIndex;
            if (eqIndex == 0) {
                eqNameText.setText("OFF");
            } else if (eqIndex == 1) {
                eqNameText.setText("JAZZ");
            } else if (eqIndex == 2) {
                eqNameText.setText("VOCAL");
            } else if (eqIndex == 3) {
                eqNameText.setText("BASS");
            }
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            for (EQModel model : eqModelList) {
                model.isSelected = false;
            }
            eqModelList.get(eqIndex).isSelected = true;
            eqAdapter.setEqModels(eqModelList);
        }
        Log.d(TAG, "onEqNameSelected eqIndex is " + eqIndex);
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
        AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
        if (fromUser) {
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }

    }

    private void onAddCustomEq(boolean isAdd) {
        Log.d(TAG, "onAddCustomEq()");
        EqCustomFragment fragment = new EqCustomFragment();
        fragment.setOnCustomEqListener(onCustomEqListener);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_ADD, isAdd);
        if (!isAdd) {
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
                List<EQModel> eqModels=EQSettingManager.get().getCompleteEQList(mContext);
                if (eqModels!=null&&eqModels.size()>0){
                    if (eqNameText.getText().equals("OFF")||eqNameText.getText().equals("JAZZ")
                            ||eqNameText.getText().equals("VOCAL")||eqNameText.getText().equals("BASS")){
                        ToastUtil.ToastLong(getActivity(),"Please select a EQ first.");
                        return;
                    }
                }else{
                    ToastUtil.ToastLong(getActivity(),"Please add a EQ first.");
                    return;
                }

                onAddCustomEq(false);
                break;
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
            case R.id.moreImageView:
                EqMoreSettingFragment fragment = new EqMoreSettingFragment();
                switchFragment(fragment,JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            case R.id.addImageView:
                onAddCustomEq(true);
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
