package jbl.stc.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqGridViewAdapter;
import jbl.stc.com.adapter.EqNameGridAdapter;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.view.EqGridView;

public class NewEqMoreSettingFragment extends BaseFragment implements View.OnClickListener{


    private ImageView closeImageView;
    private ImageView iv_remove;
    private EqGridView eqGridView;
    private EqGridViewAdapter adapter;
    private List<EQModel> eqModelList=new ArrayList<>();
    private static final String TAG = EqNameGridAdapter.class.getSimpleName();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_eq_more_setting_new, container, false);
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_MORE);
        return rootView;
    }
    private void initView() {
        closeImageView = (ImageView) rootView.findViewById(R.id.closeImageView);
        iv_remove=(ImageView)rootView.findViewById(R.id.iv_remove);
        eqGridView=(EqGridView)rootView.findViewById(R.id.eqGridView);
        adapter=new EqGridViewAdapter();
    }
    private void initEvent() {
        iv_remove.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
    }
    private void initValue() {
        eqModelList= EQSettingManager.get().getCompleteEQList(mContext);
        if (eqModelList!=null&&eqModelList.size()>=4){
            for(int i=0;i<4;i++){
                eqModelList.remove(0);
            }
        }
        Logger.d(TAG,"size:"+eqModelList.size());
        adapter.setEqModels(eqModelList);
        eqGridView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
            case R.id.iv_remove:
                List<String> eqIndexs=adapter.getEqIndexs();
                if (eqIndexs!=null&&eqIndexs.size()>0){
                    for (int i=0;i<eqIndexs.size();i++){
                        int index=Integer.valueOf(eqIndexs.get(i));
                        EQSettingManager.get().deleteEQ(eqModelList.get(index).eqName,mContext);
                    }
                    initValue();
                }
                break;
        }


    }
}
