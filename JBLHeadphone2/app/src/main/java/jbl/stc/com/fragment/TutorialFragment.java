package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.ANCAwarenessPreset;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.listener.AwarenessChangeListener;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.view.ANCController;
import jbl.stc.com.view.CircularInsideLayout;

public class TutorialFragment extends BaseFragment implements View.OnClickListener,AwarenessChangeListener,ANCController.OnSeekArcChangeListener {
    public static final String TAG = TutorialFragment.class.getSimpleName();
    private View view;
    private ViewPager viewPager;
    private List<View> views;
    private View view1, view2;
    private TextView textViewOffButton;

    private ANCController ancController;
    private CircularInsideLayout circularInsideLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tutorial,
                container, false);

        viewPager = view.findViewById(R.id.view_pager);
        views = new ArrayList<>();
        view1 = inflater.inflate(R.layout.view_page_one, null);
        view2 = inflater.inflate(R.layout.view_page_two, null);
        views.add(view1);
        views.add(view2);

        PagerAdapter adapter = new ViewAdapter(views);
        viewPager.setAdapter(adapter);

        ancController = view2.findViewById(R.id.circularSeekBar_anc_circle);
        circularInsideLayout = view2.findViewById(R.id.imageContainer_anc_circle);
        textViewOffButton = view2.findViewById(R.id.text_view_page_two_off);
        textViewOffButton.setOnClickListener(this);
        view2.findViewById(R.id.text_view_page_two_get_started).setOnClickListener(this);
        circularInsideLayout.setonAwarenesChangeListener(this);
        ancController.setCircularInsideLayout(circularInsideLayout);
        ancController.setOnSeekArcChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_page_two_off: {
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX(), ANCAwarenessPreset.None);
                ancController.setSwitchOff(false);
                break;
            }
            case R.id.text_view_page_two_get_started:{
                Fragment fr = getActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr != null && fr instanceof HomeFragment) {
                    switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }else{
                    switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }
                break;
            }
        }

    }

    @Override
    public void onMedium() {

    }

    @Override
    public void onLow() {

    }

    @Override
    public void onHigh() {

    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {

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