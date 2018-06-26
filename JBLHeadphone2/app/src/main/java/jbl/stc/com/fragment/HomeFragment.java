package jbl.stc.com.fragment;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.avnera.audiomanager.AdminEvent;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.listener.AwarenessChangeListener;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.BlurBuilder;
import jbl.stc.com.view.AAPopupwindow;
import jbl.stc.com.view.ANCController;
import jbl.stc.com.view.CircularInsideLayout;

public class HomeFragment extends BaseFragment implements View.OnClickListener,AwarenessChangeListener, ANCController.OnSeekArcChangeListener{
    private static final String TAG = HomeFragment.class.getSimpleName();
    private View view, mBlurView;
    private CreateEqTipsDialog createEqTipsDialog;
    private AAPopupwindow popupwindow;

    private PopupWindow popupWindow;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,
                container, false);
        view.findViewById(R.id.image_view_settings).setOnClickListener(this);
        view.findViewById(R.id.image_view_info).setOnClickListener(this);
        view.findViewById(R.id.image_view_ambient_aware).setOnClickListener(this);
        view.findViewById(R.id.deviceImageView);
        view.findViewById(R.id.eqSwitchLayout);
        view.findViewById(R.id.eqInfoLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.eqInfoLayout).setOnClickListener(this);
        view.findViewById(R.id.image_view_noise_cancel);
        view.findViewById(R.id.eqNameText);
        view.findViewById(R.id.titleEqText);
        view.findViewById(R.id.eqDividerView);
        view.findViewById(R.id.batteryProgressBar);
        view.findViewById(R.id.batteryLevelText);
        view.findViewById(R.id.text_view_ambient_aware);
        createEqTipsDialog = new CreateEqTipsDialog(getActivity());
        createEqTipsDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment(),JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
            }

            @Override
            public void onCancel() {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_ambient_aware: {
                showAncPopupWindow();
                break;
            }
            case R.id.eqInfoLayout: {
                switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_info:{
                switchFragment(new InfoFragment(),JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                break;
            }
            case R.id.image_view_settings:{
                switchFragment(new SettingsFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }

    protected void showAncPopupWindow() {
        View popupWindow_view = getLayoutInflater().inflate(R.layout.popup_window_anc, null,
                false);
        final ANCController ancController = popupWindow_view.findViewById(R.id.circularSeekBar);
        CircularInsideLayout circularInsideLayout = popupWindow_view.findViewById(R.id.imageContainer);
        circularInsideLayout.setonAwarenesChangeListener(this);
        ancController.setCircularInsideLayout(circularInsideLayout);
        ancController.setOnSeekArcChangeListener(this);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);

        if(mBlurView == null){
            //generate blur view
            mBlurView = view.findViewById(R.id.blur_view);
            Bitmap image = BlurBuilder.blur(view);
            mBlurView.setBackground(new BitmapDrawable(getActivity().getResources(), image));
        }
        mBlurView.setVisibility(View.VISIBLE);
        // set animation effect
        popupWindow.setAnimationStyle(R.style.style_down_to_top);
        popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });
        popupWindow.showAsDropDown(view);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //dismiss blur view
                if(mBlurView != null){
                    mBlurView.setVisibility(View.GONE);
                }
            }
        });
        popupWindow_view.findViewById(R.id.noiseText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ancController.setSwitchOff(false);
            }
        });

    }

    @Override
    public void onMedium() {
        //on AA medium checked
    }

    @Override
    public void onLow() {
       //on AA low checked
    }

    @Override
    public void onHigh() {
      //on AA high checked
    }

    @Override
    public void onProgressChanged(ANCController ANCController, int leftProgress, int rightProgress, boolean fromUser) {
       //controller progress
    }

    @Override
    public void onStartTrackingTouch(ANCController ANCController) {

    }

    @Override
    public void onStopTrackingTouch(ANCController ANCController) {

    }
}
