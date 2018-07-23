package jbl.stc.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqNameGridAdapter;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnDeleteEqListener;
import jbl.stc.com.listener.OnEqIndexChangeListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.view.DragGridView;
import jbl.stc.com.view.EqGridView;


public class EqMoreSettingFragment extends BaseFragment implements View.OnClickListener{
    private ImageView closeImageView;
    private DragGridView eqGridView;

    private List<EQModel> eqModelList;
    private EqNameGridAdapter eqNameGridAdapter;
    private EQModel currSelectedEq;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_eq_more_setting, container, false);
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_MORE);
        return rootView;
    }

    private void initView() {
        closeImageView = (ImageView) rootView.findViewById(R.id.closeImageView);
        eqGridView = (DragGridView) rootView.findViewById(R.id.eqGridView);

        eqNameGridAdapter = new EqNameGridAdapter();
    }

    private void initEvent() {
        closeImageView.setOnClickListener(this);
        eqGridView.setOnChangeListener(new OnEqIndexChangeListener() {

            @Override
            public void onIndexChange(int originalPosition, int nowPosition) {
                Log.d(TAG, "onIndexChange originalPosition=" + originalPosition + ",nowPosition=" + nowPosition);
                //从前向后拖动，其他item依次前移
                if (originalPosition < nowPosition) {
                    eqModelList.add(nowPosition + 1, eqModelList.get(originalPosition));
                    eqModelList.remove(originalPosition);
                }
                //从后向前拖动，其他item依次后移
                else if (originalPosition > nowPosition) {
                    eqModelList.add(nowPosition, eqModelList.get(originalPosition));
                    eqModelList.remove(originalPosition + 1);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int index = 0; index < eqModelList.size(); index++) {
                            EQSettingManager.get().updateEqIndexByName(index, eqModelList.get(index).eqName, mContext);
                        }
                    }
                }).start();
            }
        });

        eqNameGridAdapter.setOnDeleteEqListener(new OnDeleteEqListener() {
            @Override
            public void onDeleted(int index) {
                EQModel removeEq = eqModelList.remove(index);
                EQSettingManager.OperationStatus operationStatus = EQSettingManager.get().deleteEQ(removeEq.eqName, mContext);
                if (operationStatus == EQSettingManager.OperationStatus.DELETED) {
                    //delete successful
                    if (currSelectedEq != null && currSelectedEq.eqName != null && currSelectedEq.eqName.equals(removeEq.eqName) && !eqModelList.isEmpty()) {
                        if (index < eqModelList.size()) {//not last one
                            currSelectedEq = eqModelList.get(index);
                        } else {//delete last one,
                            currSelectedEq = eqModelList.get(index - 1);
                        }
                        if (application.deviceInfo.eqOn) {
                            currSelectedEq.isSelected = true;
                            //CommandManager.get().setGrEqBandGains(currSelectedEq.id, EQSettingManager.get().getValuesFromEQModel(currSelectedEq));
                        }
                        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, mContext);
                    } else if (eqModelList.isEmpty()) {
                        if (application.deviceInfo.eqOn) {
                            EQModel tempEqModel = new EQModel();
                            //CommandManager.get().setGrEqBandGains(tempEqModel.id, EQSettingManager.get().getValuesFromEQModel(tempEqModel));
                        }
                    }
                    Log.d(TAG, "onDeleted successful removeEq=" + removeEq.eqName);
                } else {
                    //delete failed
                    eqModelList.add(index, removeEq);
                }
                eqNameGridAdapter.setEqModels(eqModelList);
            }
        });

    }

    private void initValue() {
        eqModelList = EQSettingManager.get().getCompleteEQList(mContext);
        int index=0;
        if (eqModelList!=null&&eqModelList.size()>0){
            for (int i=0;i<eqModelList.size();i++){
                if (eqModelList.get(i).eqName.equals(getContext().getString(R.string.off))){
                    index=i;
                }
            }
        }
        eqModelList.remove(index);
        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, ""), mContext);
        if (eqModelList == null) {
            eqModelList = new ArrayList<>();
        }
        if (currSelectedEq != null && currSelectedEq.eqName != null) {
            if (application.deviceInfo.eqOn) {
                for (int i = 0; i < eqModelList.size(); i++) {
                    if (currSelectedEq.eqName.equals(eqModelList.get(i).eqName)) {
                        eqModelList.get(i).isSelected = true;
                    } else {
                        eqModelList.get(i).isSelected = false;
                    }
                }
            }
        }
        for (int i = 0; i < eqModelList.size(); i++) {
            Log.d(TAG, "i=" + i + "," + eqModelList.get(i));
        }
        eqNameGridAdapter.setCanRemove(true);
        eqNameGridAdapter.setEqModels(eqModelList);
        eqGridView.setAdapter(eqNameGridAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
        }
    }

}
