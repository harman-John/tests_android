package jbl.stc.com.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.utils.BlurBuilder;

public class HomeFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private View view;
    private CreateEqTipsDialog createEqTipsDialog;

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
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        popupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);

        Bitmap image = BlurBuilder.blur(view);
        popupWindow_view.findViewById(R.id.linear_layout_blur).setBackground(new BitmapDrawable(getActivity().getResources(), image));
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
    }

}
