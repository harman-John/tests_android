package jbl.stc.com.unused;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avnera.smartdigitalheadset.Logger;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.fragment.CalibrationFragment;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.utils.AppUtils;

public class NewTutorialFragment extends BaseFragment implements  View.OnClickListener{

    private View view;
    private ViewPager viewPager;
    private List<View> views;
    private View view1, view2,view3,view4;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tutorial,
                container, false);
        Logger.d(TAG,"onCreate");
        viewPager = view.findViewById(R.id.view_pager);
        views = new ArrayList<>();
        view1 = inflater.inflate(R.layout.new_tutorial_one, null);
        view2 = inflater.inflate(R.layout.new_tutorial_two, null);
        view3 = inflater.inflate(R.layout.new_tutorial_three, null);
        view4 = inflater.inflate(R.layout.new_tutorial_four, null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);

        PagerAdapter adapter = new ViewAdapter(views);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Logger.d(TAG,"onPageScrolled position "+position
                        +",positionOffset "+positionOffset
                        +",positionOffsetPixels "+positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                Logger.d(TAG,"onPageSelected position = "+position);
                if (position == 1){
                    ANCControlManager.getANCManager(getContext()).getAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX());
                }else if (position == 0){
                    ANCControlManager.getANCManager(getContext()).getANCValue(AvneraManager.getAvenraManager(getActivity()).getLightX());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Logger.d(TAG,"onPageScrollStateChanged");
            }
        });

        view1.findViewById(R.id.iv_close_one).setOnClickListener(this);
        view2.findViewById(R.id.iv_close_two).setOnClickListener(this);
        view3.findViewById(R.id.iv_close_three).setOnClickListener(this);
        view4.findViewById(R.id.iv_close_four).setOnClickListener(this);
        view4.findViewById(R.id.text_view_get_started).setOnClickListener(this);
        view2.findViewById(R.id.iv_back_two).setOnClickListener(this);
        view3.findViewById(R.id.iv_back_three).setOnClickListener(this);
        view4.findViewById(R.id.iv_back_four).setOnClickListener(this);
        return  view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.iv_close_one:
            case R.id.iv_close_two:
            case R.id.iv_close_three:
            case R.id.iv_close_four:
            case R.id.text_view_get_started:{
                Logger.d(TAG,"model number = "+ AppUtils.getModelNumber(getActivity()));
                if(AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)
                        ||AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)
                        ||AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)){
                    switchFragment(new CalibrationFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }else{
                    DashboardActivity.getDashboardActivity().goHomeFragment(DashboardActivity.getDashboardActivity().getMyDeviceConnected());
                }
                break;
            }

            case R.id.iv_back_two:
                viewPager.setCurrentItem(0,true);
                break;
            case R.id.iv_back_three:
                viewPager.setCurrentItem(1,true);
                break;
            case R.id.iv_back_four:
                viewPager.setCurrentItem(2,true);
                break;

        }

    }

    class ViewAdapter extends PagerAdapter {
        private List<View> datas;

        public ViewAdapter(List<View> list) {
            datas = list;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            Logger.d(TAG,"getItemPosition");
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = datas.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(datas.get(position));
        }
    }
}
