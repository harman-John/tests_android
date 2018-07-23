package jbl.stc.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.adapter.EqGridViewAdapter;
import jbl.stc.com.adapter.EqNameGridAdapter;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.view.CustomScrollView;
import jbl.stc.com.view.DragGridView;
import jbl.stc.com.view.EqArcView;
import jbl.stc.com.view.EqGridView;
/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 7/20/18.
 */
public class NewEqMoreSettingFragment extends BaseFragment implements View.OnClickListener{

    private CustomScrollView mScrollView;
    private ImageView closeImageView;
    private ImageView iv_remove;
    private EqGridView eqGridView;
    private EqGridViewAdapter adapter;
    private List<EQModel> eqModelList=new ArrayList<>();
    private EqArcView mEqArcView;
    private TextView tv_jazz,tv_vocal,tv_bass;
    private LinearLayout ll_view;
    private LightX lightX;
    private static final String TAG = NewEqMoreSettingFragment.class.getSimpleName();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_eq_more_setting_new, container, false);
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_MORE);
        return rootView;
    }
    private void initView() {
        String curEqName=PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, "");
        tv_jazz=(TextView)rootView.findViewById(R.id.tv_jazz);
        tv_vocal=(TextView)rootView.findViewById(R.id.tv_vocal);
        tv_bass=(TextView) rootView.findViewById(R.id.tv_bass);
        if (!TextUtils.isEmpty(curEqName)&&curEqName.equals(getResources().getString(R.string.jazz))){
            tv_jazz.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        }else if (!TextUtils.isEmpty(curEqName)&&curEqName.equals(getResources().getString(R.string.vocal))){
            tv_vocal.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        }else if (!TextUtils.isEmpty(curEqName)&&curEqName.equals(getResources().getString(R.string.bass))){
            tv_bass.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        }
        closeImageView = (ImageView) rootView.findViewById(R.id.closeImageView);
        iv_remove=(ImageView)rootView.findViewById(R.id.iv_remove);
        eqGridView=(EqGridView) rootView.findViewById(R.id.eqGridView);
        mScrollView = rootView.findViewById(R.id.scrollview);
        mEqArcView =rootView.findViewById(R.id.eqArcView);
        eqGridView.setmEqArcView(mEqArcView);
        eqGridView.setScrollView(mScrollView);
        ll_view=(LinearLayout)rootView.findViewById(R.id.ll_view);
        /*DisplayMetrics dm = getResources().getDisplayMetrics();
        int heigth = dm.heightPixels;
        int width = dm.widthPixels;
        Logger.d(TAG,"height:"+String.valueOf(heigth))；*/

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
        adapter=new EqGridViewAdapter();
        adapter.setEqModels(eqModelList,lightX);
        eqGridView.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
        }

    }

}
