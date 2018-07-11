package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.ANCAwarenessPreset;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.activity.JBLApplication;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.listener.AwarenessChangeListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.view.ANCController;
import jbl.stc.com.view.CircularInsideLayout;

public class TutorialFragment extends BaseFragment implements View.OnClickListener,AwarenessChangeListener,ANCController.OnSeekArcChangeListener {
    public static final String TAG = TutorialFragment.class.getSimpleName();
    private View view;
    private ViewPager viewPager;
    private List<View> views;
    private View view1, view2;
    private TextView textViewOffButton;
    private ImageView closeButton;
    private CheckBox checkBoxNoiseCancel;

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
        Logger.d(TAG,"onCreate");
        viewPager = view.findViewById(R.id.view_pager);
        views = new ArrayList<>();
        view1 = inflater.inflate(R.layout.view_page_one, null);
        view2 = inflater.inflate(R.layout.view_page_two, null);
        views.add(view1);
        views.add(view2);

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
        closeButton = view1.findViewById(R.id.image_view_page_one_close);
        closeButton.setOnClickListener(this);
        checkBoxNoiseCancel = view1.findViewById(R.id.image_view_page_one_noise_cancel);
        checkBoxNoiseCancel.setOnClickListener(this);
        ancController = view2.findViewById(R.id.circularSeekBar_anc_circle);
        circularInsideLayout = view2.findViewById(R.id.imageContainer_anc_circle);
        textViewOffButton = view2.findViewById(R.id.text_view_page_two_off);
        textViewOffButton.setOnClickListener(this);
        view2.findViewById(R.id.image_view_page_two_close).setOnClickListener(this);
        view2.findViewById(R.id.image_view_page_two_back).setOnClickListener(this);
        view2.findViewById(R.id.text_view_page_two_get_started).setOnClickListener(this);
        circularInsideLayout.setonAwarenesChangeListener(this);
        ancController.setCircularInsideLayout(circularInsideLayout);
        ancController.setOnSeekArcChangeListener(this);
        getRawSteps();
        return view;
    }
    private void getRawSteps(){
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext())
                .getRawStepsByCmd(AvneraManager.getAvenraManager(JBLApplication.getJBLApplicationContext()).getLightX());//get raw steps count of connected device
    }
    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume");
        ANCControlManager.getANCManager(getContext()).getANCValue(AvneraManager.getAvenraManager(getActivity()).getLightX());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_page_two_off: {
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX(), ANCAwarenessPreset.None);
                ancController.setSwitchOff(false);
                break;
            }
            case R.id.image_view_page_two_close:
            case R.id.image_view_page_one_close:
            case R.id.text_view_page_two_get_started:{
                Logger.d(TAG,"model number = "+AppUtils.getModelNumber(getActivity()));
                if(AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)
                        ||AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)
                        ||AppUtils.getModelNumber(getActivity()).toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)){
                    switchFragment(new CalibrationFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }else{
                    Fragment fr = getActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr != null && fr instanceof HomeFragment) {
                        switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else {
                        switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                }
                break;
            }
            case R.id.image_view_page_two_back:{
                viewPager.setCurrentItem(0,true);
                break;
            }
            case R.id.image_view_page_one_noise_cancel:{
                if (checkBoxNoiseCancel.isChecked()){
                    ANCControlManager.getANCManager(getActivity()).setANCValue(AvneraManager.getAvenraManager(getActivity()).getLightX(),true);
                }else{
                    ANCControlManager.getANCManager(getActivity()).setANCValue(AvneraManager.getAvenraManager(getActivity()).getLightX(),false);
                }
                break;
            }
        }

    }

    @Override
    public void onMedium() {
        Log.d(TAG, "onMedium ");
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX(), ANCAwarenessPreset.Medium);
    }

    @Override
    public void onLow() {
        Log.d(TAG, "onLow ");
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX(), ANCAwarenessPreset.Low);
    }

    @Override
    public void onHigh() {
        Log.d(TAG, "onHigh ");
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX(), ANCAwarenessPreset.High);
    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged fromUser "+fromUser);
        if (fromUser) {
            // Check added to fix Bug :Bug 64517 - Sometimes Awareness adjustment is disordered when left and right AA have different level.
            //Set animation to false and presetValue to -1
            mFromUser = fromUser;
            mLeftProgress = leftProgress;
            mRightProgress = rightProgress;
//            int savedLeft = PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST,JBLApplication.getJBLApplicationContext());
//            int savedRight = PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, JBLApplication.getJBLApplicationContext());
//
//            PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, leftProgress, JBLApplication.getJBLApplicationContext());
//            PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, rightProgress, JBLApplication.getJBLApplicationContext());
//            lastsavedAwarenessState = null;
//            if (leftProgress != savedLeft) {
//                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setLeftAwarenessPresetValue(AvneraManager.getAvenraManager(getActivity()).getLightX(), leftProgress);
//            }
//            if (rightProgress != savedRight) {
//                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRightAwarenessPresetValue(AvneraManager.getAvenraManager(getActivity()).getLightX(), rightProgress);
//            }
        }
    }

    private boolean mFromUser = false;
    private int mLeftProgress, mRightProgress;

    @Override
    public void onStartTrackingTouch(ANCController ANCController) {
        Log.d(TAG, "onStartTrackingTouch ");
    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {
        Log.d(TAG, "onStopTrackingTouch mLeftProgress = "+mLeftProgress + ", mRightProgress = "+mRightProgress+",mFromUser = "+mFromUser);
        if (mFromUser) {
            mFromUser = false;
            ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setLeftAwarenessPresetValue(AvneraManager.getAvenraManager(getActivity()).getLightX(), mLeftProgress);
            ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRightAwarenessPresetValue(AvneraManager.getAvenraManager(getActivity()).getLightX(), mRightProgress);
        }
    }

    @Override
    public void onBothThumbsTouched(ANCController seekArc, boolean touched) {

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

    private void updateANC(boolean ancResult){
        if (checkBoxNoiseCancel != null)
            checkBoxNoiseCancel.setChecked(ancResult);
    }


    private ANCAwarenessPreset lastsavedAwarenessState;
    private boolean isRequestingLeftANC, isRequestingRightANC;
    public void updateAAUI(int aaLevelingValue){
        boolean is150NC = AppUtils.is150NC(JBLApplication.getJBLApplicationContext());
        Log.d(TAG, "updateAmbientLevel: " + aaLevelingValue + "," + PreferenceUtils.getInt(PreferenceKeys.AWARENESS, JBLApplication.getJBLApplicationContext()) + ",is150NC=" + is150NC);
        switch (aaLevelingValue) {
            case 0:
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, 0, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, 0, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.None;
                break;
            case 1: //ANCAwarenessPreset.Low
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 28 : 25, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 28 : 25, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.Low;
                break;
            case 2: //ANCAwarenessPreset.Medium
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 58 : 55, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 58 : 55, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.Medium;
                break;
            case 3://ANCAwarenessPreset.High
                PreferenceUtils.setInt(PreferenceKeys.LEFT_PERSIST, is150NC ? 86 : 100, JBLApplication.getJBLApplicationContext());
                PreferenceUtils.setInt(PreferenceKeys.RIGHT_PERSIST, is150NC ? 86 : 100, JBLApplication.getJBLApplicationContext());
                lastsavedAwarenessState = ANCAwarenessPreset.High;
                break;
        }
        ancController.initProgress(PreferenceUtils.getInt(PreferenceKeys.LEFT_PERSIST, JBLApplication.getJBLApplicationContext()),
                PreferenceUtils.getInt(PreferenceKeys.RIGHT_PERSIST, JBLApplication.getJBLApplicationContext()), aaLevelingValue);
        PreferenceUtils.setInt(PreferenceKeys.AWARENESS, aaLevelingValue, JBLApplication.getJBLApplicationContext());

        isRequestingLeftANC = true;
        isRequestingRightANC = true;
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getLeftANCvalue(AvneraManager.getAvenraManager(getActivity()).getLightX());
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRightANCvalue(AvneraManager.getAvenraManager(getActivity()).getLightX());
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] var4) {
        super.lightXAppReadResult(var1, command, success, var4);
        if (success) {
            switch (command) {
                case AppANCEnable:
                    if (var4 != null) {
                        boolean ancResult = Utility.getBoolean(var4, 0);
                        updateANC(ancResult);
                    }
                    break;
                case AppANCAwarenessPreset:{
                    int intValue = com.avnera.smartdigitalheadset.Utility.getInt(var4, 0);
                    updateAAUI(intValue);
                    break;
                }
                case AppAwarenessRawSteps:{
                    int intValue = com.avnera.smartdigitalheadset.Utility.getInt(var4, 0) - 1;
                    Logger.d(TAG , "received raw steps call back rawSteps = " + intValue);
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRawSteps(intValue);
                    break;
                }
            }
        }
    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command command, byte[] var4) {
        super.lightXAppReceivedPush(var1, command, var4);
        Log.d(TAG, "lightXAppReceivedPush command is " + command);
        switch (command) {
            case AppPushANCEnable:
                ANCControlManager.getANCManager(mContext).getANCValue(AvneraManager.getAvenraManager(getActivity()).getLightX());
                break;
            case AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(mContext).getAmbientLeveling(AvneraManager.getAvenraManager(getActivity()).getLightX());
            }
            break;
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        super.receivedResponse(command, values, status);
        Log.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values.size() <= 0) {
            Log.d(TAG, "return, values size is " + values.size());
            return;
        }
        switch (command) {
            case AmCmds.CMD_ANC: {
                String value = values.iterator().next().getValue().toString();
                if (value.equalsIgnoreCase("true")
                        || value.equalsIgnoreCase("1")) {
                    updateANC(true);
                }else{
                    updateANC(false);
                }
                break;
            }
            case AmCmds.CMD_AmbientLeveling: {
                String value = values.iterator().next().getValue().toString();
                updateAAUI(AppUtils.levelTransfer(Integer.valueOf(value)));
                break;
            }
            case AmCmds.CMD_RawSteps: {
                int rawSteps = Integer.parseInt(values.iterator().next().getValue().toString()) - 1;
                Logger.d(TAG , "received raw steps call back rawSteps = " + rawSteps);
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRawSteps(rawSteps);
                break;
            }
        }
    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {
        super.receivedPushNotification(action, command, values, status);
        jbl.stc.com.logger.Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        switch (command){
            case AmCmds.CMD_ANCNotification: {
                jbl.stc.com.logger.Logger.d(TAG, "CMD_ANCNotification:" + ",values=" + values.iterator().next().getValue().toString() );
                PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, Integer.valueOf(values.iterator().next().getValue().toString() ), getActivity());
                if (Integer.valueOf(values.iterator().next().getValue().toString())==1){
                    checkBoxNoiseCancel.setChecked(true);
                }else if (Integer.valueOf(values.iterator().next().getValue().toString())==0){
                    checkBoxNoiseCancel.setChecked(false);
                }
                break;
            }
            case AmCmds.CMD_AmbientLevelingNotification:{
                String ambent = values.iterator().next().getValue().toString();
                jbl.stc.com.logger.Logger.d(TAG, "CMD_ANCNotification:" + ",values=" + ambent );
                updateAAUI(Integer.valueOf(ambent));
                break;
            }
        }
    }

}