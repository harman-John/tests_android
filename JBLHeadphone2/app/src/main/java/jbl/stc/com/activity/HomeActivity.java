package jbl.stc.com.activity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.audioManager;
import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.harman.bluetooth.constants.EnumAAStatus;
import com.harman.bluetooth.constants.EnumAncStatus;
import com.harman.bluetooth.constants.EnumDeviceStatusType;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.constants.EnumEqPresetIdx;
import com.harman.bluetooth.req.CmdAASet;
import com.harman.bluetooth.req.CmdAncSet;
import com.harman.bluetooth.req.CmdCurrEq;
import com.harman.bluetooth.req.CmdDevStatus;
import com.harman.bluetooth.req.CmdEqPresetSet;
import com.harman.bluetooth.ret.RetCurrentEQ;

import jbl.stc.com.manager.LiveCmdManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.fragment.EqCustomFragment;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.listener.OnOtaListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.manager.LeManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.ArrayUtil;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.SharePreferenceUtil;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AaPopupWindow;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.BlurringView;
import jbl.stc.com.view.CustomFontTextView;
import jbl.stc.com.view.NotConnectedPopupWindow;
import jbl.stc.com.view.SaPopupWindow;


public class HomeActivity extends BaseActivity implements View.OnClickListener, OnOtaListener {
    public static final String TAG = HomeActivity.class.getSimpleName() + "aa";
    private BlurringView mBlurView;

    private final static int MSG_ANC = 0;
    private final static int MSG_BATTERY = 1;
    private final static int MSG_FIRMWARE_VERSION = 2;
    private final static int MSG_CURRENT_PRESET = 3;
    private final static int MSG_RAW_STEP = 4;
    private final static int MSG_AMBIENT_LEVEL = 5;
    private final static int MSG_READ_BATTERY_INTERVAL = 6;

    private final static int MSG_AA_LEFT = 32;
    private final static int MSG_AA_RIGHT = 33;

    private final static int MSG_UPDATE_CUSTOM_EQ = 8;
    private final static int MSG_CHECK_UPDATE = 10;
    private final static int MSG_FIRMWARE_INFO = 11;

    private final long timeInterval = 30 * 1000L;
    private ProgressBar progressBarBattery;
    private TextView textViewBattery;
    private TextView textViewCurrentEQ;
    private TextView textViewDeviceName;
    private ImageView imageViewDevice;
    private CheckBox checkBoxNoiseCancel;
    private ImageView imageViewAmbientAaware;
    private LinearLayout linearLayoutBattery;
    private AaPopupWindow aaPopupWindow;
    private SaPopupWindow saPopupwindow;

    private RelativeLayout relative_layout_home_eq_info;
    private String deviceName;
    private SaPopupWindow.OnSmartAmbientStatusReceivedListener mSaListener;
    private AppImageView image_view_ota_download;
    private NotConnectedPopupWindow notConnectedPopupWindow;
    private TutorialAncDialog tutorialAncDialog;

    private FrameLayout frameLayout;
    private HomeHandler homeHandler = new HomeHandler(Looper.getMainLooper());
    private float yDown;
    private float yMove;
    public static boolean isEnter = false;
    private RelativeLayout relative_layout_home_activity;
    private int screenHeight;
    private int screenWidth;

    public TutorialAncDialog getTutorialAncDialog() {
        return tutorialAncDialog;
    }

    private int mConnectStatus = -1;
    private RelativeLayout rootLayout;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private LinearLayout ll_deviceImage;
    private ImageView deviceImageView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        DeviceManager.getInstance(this).setOnRetListener(this);
        mConnectStatus = getIntent().getIntExtra(JBLConstant.KEY_CONNECT_STATUS, -1);
        PreferenceUtils.setInt(JBLConstant.KEY_CONNECT_STATUS,mConnectStatus,HomeActivity.this);
        Intent intent = getIntent();
        String action = intent.getAction();
        Logger.d(TAG, "onResume action =" + action + ",mConnectStatus =" + mConnectStatus);
        if (!TextUtils.isEmpty(action)
                && "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action) && mConnectStatus == -1) {
            Logger.i(TAG, "onCreate finished");
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }
        addActivity(this);
        Logger.d(TAG, "onCreate");
        rootLayout = findViewById(R.id.relative_layout_home_activity);
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        showTutorial();
        generateAAPopupWindow();
        generateSaPopupWindow();
        relative_layout_home_activity = findViewById(R.id.relative_Layout_home);
        findViewById(R.id.image_view_home_back).setOnClickListener(this);
        textViewDeviceName = findViewById(R.id.text_view_home_device_name);
        frameLayout = findViewById(R.id.frame_layout_home_device_image);
        frameLayout.setOnClickListener(this);
        imageViewDevice = findViewById(R.id.image_view_home_device_image);
        relative_layout_home_eq_info = findViewById(R.id.relative_layout_home_eq_info);
        relative_layout_home_eq_info.setVisibility(View.VISIBLE);
        TextView titleEqText = findViewById(R.id.titleEqText);
        if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            setEqMenuColor(false);
            relative_layout_home_eq_info.setAlpha((float) 0.5);
        } else {
            setEqMenuColor(true);
            titleEqText.setOnClickListener(this);
            findViewById(R.id.image_view_home_settings).setOnClickListener(this);
            findViewById(R.id.arrowUpImage).setOnClickListener(this);
        }
        textViewCurrentEQ = findViewById(R.id.text_view_home_eq_name);
        linearLayoutBattery = findViewById(R.id.linear_layout_home_battery);
        progressBarBattery = findViewById(R.id.progress_bar_battery);
        textViewBattery = findViewById(R.id.text_view_battery_level);
        image_view_ota_download = findViewById(R.id.image_view_ota_download);
        image_view_ota_download.setOnClickListener(this);
        checkBoxNoiseCancel = findViewById(R.id.image_view_home_noise_cancel);
        imageViewAmbientAaware = findViewById(R.id.image_view_home_ambient_aware);

        mBlurView = findViewById(R.id.view_home_blur);
        CreateEqTipsDialog createEqTipsDialog = new CreateEqTipsDialog(this);
        createEqTipsDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
            }

            @Override
            public void onCancel() {

            }
        });
        RelativeLayout linearLayoutNoiseCanceling = findViewById(R.id.relative_layout_home_noise_cancel);
        deviceName = ProductListManager.getInstance().getSelectDevice(mConnectStatus).deviceName;
        if (!DeviceFeatureMap.isFeatureSupported(deviceName, Feature.ENABLE_NOISE_CANCEL)) {
            linearLayoutNoiseCanceling.setVisibility(View.GONE);
        } else {
            linearLayoutNoiseCanceling.setVisibility(View.VISIBLE);
            if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
                checkBoxNoiseCancel.setOnClickListener(this);
            } else {
                linearLayoutNoiseCanceling.setAlpha((float) 0.5);
            }
        }

        RelativeLayout linearLayoutAmbientAware = findViewById(R.id.relative_layout_home_ambient_aware);
        if (!DeviceFeatureMap.isFeatureSupported(deviceName, Feature.ENABLE_AMBIENT_AWARE)) {
            linearLayoutAmbientAware.setVisibility(View.GONE);
        } else {
            findViewById(R.id.image_view_home_ambient_aware).setOnClickListener(this);
            if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                linearLayoutAmbientAware.setAlpha((float) 0.5);
            }
            if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                    || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                    || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA)) {
                CustomFontTextView textViewNoiseCancle = findViewById(R.id.text_view_home_noise_cancle);
                textViewNoiseCancle.setText(R.string.talkthru);
                checkBoxNoiseCancel.setOnClickListener(this);
                findViewById(R.id.relative_layout_home_noise_cancel).setVisibility(View.VISIBLE);
                linearLayoutAmbientAware.setVisibility(View.VISIBLE);
                ImageView imageViewAmbientAware = findViewById(R.id.image_view_home_ambient_aware);
                imageViewAmbientAware.setBackgroundResource(R.mipmap.aa_icon_non_active);
                imageViewAmbientAware.setTag("0");
                imageViewAmbientAware.setOnClickListener(this);

            }
        }


        updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
        initEvent();
        setDeviceImageHeight();
        setupEnterAnimations();
        setupExitAnimations();
    }

    private void setupEnterAnimations() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition
                .changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                enterReveal();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    void enterReveal() {
        int[] location = new int[2];
        frameLayout.getLocationOnScreen(location);
        int cx = location[0];
        int cy = location[1];
        int startRadius = frameLayout.getMeasuredHeight() / 2;
        int finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        Animator anim =
                ViewAnimationUtils.createCircularReveal(rootLayout,
                        cx + startRadius,
                        cy + startRadius,
                        startRadius,
                        finalRadius);
        anim.setDuration(500);
        rootLayout.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void setupExitAnimations() {
        Fade fade = new Fade();
        getWindow().setReturnTransition(fade);
        fade.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                transition.removeListener(this);
                exitReveal();
            }


            @Override
            public void onTransitionEnd(Transition transition) {
            }


            @Override
            public void onTransitionCancel(Transition transition) {
            }


            @Override
            public void onTransitionPause(Transition transition) {
            }


            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    void exitReveal() {
        int[] location = new int[2];
        frameLayout.getLocationOnScreen(location);
        int cx = location[0];
        int cy = location[1];
        int startRadius = frameLayout.getHeight() / 2;
        int initialRadius = (rootLayout.getWidth() + rootLayout.getHeight()) / 2;
        Animator anim =
                ViewAnimationUtils.createCircularReveal(rootLayout,
                        cx + startRadius,
                        cy + startRadius,
                        initialRadius,
                        startRadius);
        anim.setDuration(300);
        anim.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        Logger.d(TAG, "onResume action =" + action);
        if (TextUtils.isEmpty(action) || !"android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
            AnalyticsManager.getInstance(this).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
            Logger.d(TAG, "onResume " + DeviceConnectionManager.getInstance().getCurrentDevice());
            doResume();
        }
    }

    @Override
    public void onConnectStatus(Object... objects) {
        super.onConnectStatus(objects);
        boolean isConnected = (boolean) objects[0];
        if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            Logger.i(TAG, "on connect status, device is a2dp half connect");
            finish();
        } else if (!isConnected && !isOTADoing && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
            Logger.i(TAG, "on connect status, not connected, not ota");
            removeAllFragment();
            finish();
        } else if (isConnected && isOTADoing && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
            Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if (fr != null && fr instanceof OTAFragment) {
                Logger.i(TAG, "on connect status, connected, myDevice = " + mConnectStatus);
//                DeviceManager.getInstance(this).startA2DPCheck();
                ((OTAFragment) fr).otaSuccess(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);

        if (isOTADoing) {
            if (fr != null && fr instanceof OTAFragment && DeviceManager.getInstance(this).isInBootloader() && DeviceManager.getInstance(this).isConnected()) {
                final Toast toast = Toast.makeText(this, "Can't perform this action.", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 300);
                return;
            }
        } else {
            if (fr != null && fr instanceof OTAFragment) {
                onButtonDone();
            }
        }

        if ((fr != null) && fr instanceof EqSettingFragment) {
            DeviceManager.getInstance(this).setOnRetListener(this);
            LeManager.getInstance().setOnConnectStatusListener(this);
            LeManager.getInstance().setOnRetListener(this);
            ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
        }
        if (fr == null) {
            DeviceManager.getInstance(this).setOnRetListener(this);
            LeManager.getInstance().setOnConnectStatusListener(this);
            LeManager.getInstance().setOnRetListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        homeHandler.removeMessages(MSG_FIRMWARE_VERSION);
        homeHandler.removeMessages(MSG_READ_BATTERY_INTERVAL);
        if (notConnectedPopupWindow != null) {
            notConnectedPopupWindow.dismiss();
        }
        unregisterNetworkReceiverSafely();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (aaPopupWindow == null) {
            return;
        }
        aaPopupWindow.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_home_ambient_aware: {
                if (UiUtils.isConnected(mConnectStatus, HomeActivity.this)) {
                    if (AppUtils.isOldDevice(deviceName)) {
                        if (!checkBoxNoiseCancel.isChecked()) {
                            checkBoxNoiseCancel.setChecked(true);
                        }
                        showAncPopupWindow(findViewById(R.id.relative_layout_home_activity));

                        if (!checkBoxNoiseCancel.isChecked()) {
                            ANCControlManager.getANCManager(this).setANCValue(true);
                        }
                        ANCControlManager.getANCManager(getApplicationContext()).getAmbientLeveling();
                    } else if (AppUtils.isNewDevice(deviceName)) {
                        //showSaPopupWindow(findViewById(R.id.relative_layout_home_activity), null);
                        Logger.d(TAG, "tag: old device" + imageViewAmbientAaware.getTag());
                        if (imageViewAmbientAaware.getTag().equals("1")) {
                            imageViewAmbientAaware.setBackground(getResources().getDrawable(R.mipmap.aa_icon_non_active));
                            imageViewAmbientAaware.setTag("0");
                        } else if (imageViewAmbientAaware.getTag().equals("0")) {
                            imageViewAmbientAaware.setBackground(getResources().getDrawable(R.mipmap.aa_icon_active));
                            imageViewAmbientAaware.setTag("1");
                            checkBoxNoiseCancel.setChecked(false);

                        }
                        if (LeManager.getInstance().isConnected()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    CmdAncSet cmdAncSet = new CmdAncSet(checkBoxNoiseCancel.isChecked() ? EnumAncStatus.ON : EnumAncStatus.OFF);
                                    LiveCmdManager.getInstance().reqSetANC(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdAncSet);
                                    CmdAASet cmdAASet = new CmdAASet(checkBoxNoiseCancel.isChecked() ? EnumAAStatus.TALK_THRU : EnumAAStatus.AMBIENT_AWARE);
                                    LiveCmdManager.getInstance().reqSetAAMode(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdAASet);
                                }
                            }).start();
                        }
                    }
                }
                break;
            }
            case R.id.arrowUpImage: {
                EqSettingFragment fragment = new EqSettingFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(JBLConstant.KEY_CONNECT_STATUS,mConnectStatus);
                fragment.setArguments(bundle);
                switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_home_back: {
                DeviceManager.getInstance(this).setIsFromHome(true);
                onBackPressed();
                break;
            }
            case R.id.frame_layout_home_device_image: {

                createDeviceImageView(HomeActivity.this);
                break;
            }
            case R.id.image_view_home_settings: {
                switchFragment(new SettingsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_home_noise_cancel: {
                Logger.d(TAG, "on click, noise cancel, device name: " + deviceName);
                if (AppUtils.isNewDevice(deviceName)) {
                    if (checkBoxNoiseCancel.isChecked()) {
                        Logger.d(TAG, "noise cancel  checked");
                        checkBoxNoiseCancel.setChecked(true);
                        if (imageViewAmbientAaware.getTag().equals("1")) {
                            imageViewAmbientAaware.setBackgroundResource(R.mipmap.aa_icon_non_active);
                            imageViewAmbientAaware.setTag("0");
                        }
                    } else {
                        Logger.d(TAG, "noise cancel unchecked");
                        checkBoxNoiseCancel.setChecked(false);
                    }
                    if (LeManager.getInstance().isConnected()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CmdAncSet cmdAncSet = new CmdAncSet(checkBoxNoiseCancel.isChecked() ? EnumAncStatus.ON : EnumAncStatus.OFF);
                                LiveCmdManager.getInstance().reqSetANC(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdAncSet);
                            }
                        }).start();
                    }
                } else {
                    setANC();

                }
                break;
            }
            case R.id.titleEqText: {
                turnOnOffEq();
                break;
            }
            case R.id.image_view_ota_download: {
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }
        }
    }

    private void createDeviceImageView(final Context context) {
        final int height = UiUtils.getDashboardDeviceImageHeight(context);
        float x = (screenWidth - height) / 2;
        float y = UiUtils.dip2px(context, 62) + UiUtils.getDeviceImageMarginTop(context);
        frameLayout.setVisibility(View.INVISIBLE);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = (int) x;
        mWindowLayoutParams.y = (int) y;
        mWindowLayoutParams.alpha = 1.0f;
        mWindowLayoutParams.width = screenWidth;
        mWindowLayoutParams.height = screenHeight;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ll_deviceImage = new LinearLayout(context);
        WindowManager.LayoutParams ll_params = new WindowManager.LayoutParams();
        ll_params.gravity = Gravity.TOP | Gravity.LEFT;
        ll_params.width = height;
        ll_params.height = height;
        ll_deviceImage.setLayoutParams(ll_params);
        deviceImageView = new ImageView(context);
        deviceImageView.setBackgroundResource(R.drawable.shape_dashboard_device_circle);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = height;
        params.height = height;
        UiUtils.setDeviceImage(deviceName, deviceImageView);
        ll_deviceImage.addView(deviceImageView, params);
        mWindowManager.addView(ll_deviceImage, mWindowLayoutParams);

        int settingTitleBar = UiUtils.dip2px(context, 62);
        int settingDeviceNameMarginTop = UiUtils.dip2px(context, 5);
        int settingDeviceNameHeight = UiUtils.dip2px(context, 28);
        int settingDeviceImageMarginTop = UiUtils.dip2px(context, 10);
        final int settingDeviceImageMargin_ParentTop = settingTitleBar + settingDeviceNameMarginTop +
                settingDeviceNameHeight + settingDeviceImageMarginTop;
        final int settingDeviceImageHeight = UiUtils.dip2px(context, 120);
        final int settingDeviceImageMarginLeft = (screenWidth - settingDeviceImageHeight) / 2;


        ll_deviceImage.clearAnimation();
        deviceImageView.clearAnimation();
        final float startX = x;
        float startY = y;
        float endX = startX;
        float endY = startY + settingDeviceImageMargin_ParentTop - (UiUtils.getDeviceImageMarginTop(context) + UiUtils.dip2px(context, 62) + (height - settingDeviceImageHeight) / 2);
        ObjectAnimator animX = ObjectAnimator.ofFloat(ll_deviceImage, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(ll_deviceImage, "translationY",
                startY, endY);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(deviceImageView, "scaleY",
                1, (float) (settingDeviceImageHeight) / (float) (height));
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(deviceImageView, "scaleX",
                1, (float) (settingDeviceImageHeight) / (float) (height));
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY, animScaleX, animScaleY);
        animSetXY.setDuration(400);
        animSetXY.setInterpolator(new AccelerateDecelerateInterpolator());
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                switchFragment(new SettingsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                frameLayout.setVisibility(View.VISIBLE);
                mWindowLayoutParams.alpha = 0f;
                mWindowManager.updateViewLayout(ll_deviceImage, mWindowLayoutParams);
                if (ll_deviceImage != null) {
                    mWindowManager.removeView(ll_deviceImage);
                    ll_deviceImage = null;
                    deviceImageView = null;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();
    }

    private void showTutorial() {
        if (UiUtils.isConnected(mConnectStatus, HomeActivity.this)) {
            boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
            if (!isShowTutorialManyTimes) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                Logger.d(TAG, "showTutorial");
                if (tutorialAncDialog == null) {
                    tutorialAncDialog = new TutorialAncDialog(this);
                }
                if (!tutorialAncDialog.isShowing()) {
                    tutorialAncDialog.show();
                }

                tutorialAncDialog.setOnDialogListener(new OnDialogListener() {
                    @Override
                    public void onConfirm() {
                        doResume();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }
    }

    private void doResume() {
        DeviceManager.getInstance(this).setOnRetListener(this);
        LeManager.getInstance().setOnConnectStatusListener(this);
        LeManager.getInstance().setOnRetListener(this);
        if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
            getDeviceInfo();
        } else if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            if (!PreferenceUtils.getBoolean(PreferenceKeys.SHOW_NC_POP, this)) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_NC_POP, true, this);
                findViewById(R.id.relative_layout_home_activity).post(new Runnable() {
                    @Override
                    public void run() {
                        showNCPopupWindow();
                    }
                });
            }
        }
    }

    private void setDeviceImageHeight() {
        int height = UiUtils.getDashboardDeviceImageHeight(this);
        Logger.d(TAG, "height:" + height);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageViewDevice.getLayoutParams();
        params.height = height;
        params.width = height;
        imageViewDevice.setLayoutParams(params);
        int marginTop = (int) (height / 2 - height / 2 * Math.sin(45 * 3.14 / 180) - UiUtils.dip2px(this, 35) / 2);
        int marginRight = (int) (height / 2 - height / 2 * Math.cos(45 * 3.14 / 180) - UiUtils.dip2px(this, 35) / 2);
        image_view_ota_download.setTop(marginTop);
        image_view_ota_download.setRight(marginRight);
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) image_view_ota_download.getLayoutParams();
        params1.topMargin = marginTop;
        params1.rightMargin = marginRight;
        image_view_ota_download.setLayoutParams(params1);
    }

    public void showOta(boolean hasUpdate) {
        if (hasUpdate) {
            image_view_ota_download.setVisibility(View.VISIBLE);
        } else {
            image_view_ota_download.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {

        final int distance = UiUtils.dip2px(this, 80);
        final int bottomHeight = UiUtils.dip2px(HomeActivity.this, 70);
        if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
            relative_layout_home_activity.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            yDown = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            yMove = event.getRawY();
                            Logger.d(TAG, "yMove" + yMove);
                            if (!isEnter) {
                                if ((yDown - yMove) > distance && Math.abs(yMove - yDown) > distance) {
                                    isEnter = true;
                                    Logger.d(TAG, "Enter EqFragment");
                                    Logger.d(TAG, String.valueOf(yMove));
                                    EqSettingFragment fragment = new EqSettingFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(JBLConstant.KEY_CONNECT_STATUS,mConnectStatus);
                                    bundle.putFloat("rawY", screenHeight - bottomHeight);
                                    fragment.setArguments(bundle);
                                    switchFragment(fragment, 4);
                                    return false;
                                }
                            }
                            if (isEnter && EqSettingFragment.rootView != null) {
                                if ((yDown - yMove) > distance && Math.abs(yMove - yDown) > distance) {
                                    EqSettingFragment.rootView.setTranslationY(yMove);
                                    int height = (int) (screenHeight / 2 - UiUtils.dip2px(HomeActivity.this, 70) - (yDown - yMove) / 2);
                                    EqSettingFragment.changeShadeViewHeight(height, HomeActivity.this);
                                    int dragEqHeight = (int) ((yMove) / (screenHeight + UiUtils.getStatusHeight(HomeActivity.this) - UiUtils.dip2px(HomeActivity.this, 70)) * UiUtils.dip2px(HomeActivity.this, 70));
                                    EqSettingFragment.changeDragEqTitleBarHeight(dragEqHeight, HomeActivity.this);

                                    int fullWidth = (screenWidth - UiUtils.dip2px(HomeActivity.this, 70)) / 2;
                                    int x = (int) (fullWidth - yMove / (screenHeight + UiUtils.getStatusHeight(HomeActivity.this) - UiUtils.dip2px(HomeActivity.this, 70)) * fullWidth);
                                    int y = (int) (yMove - dragEqHeight / 2 + UiUtils.getStatusHeight(HomeActivity.this));
                                    EqSettingFragment.updateEqTitleLocation(x, y);
                                    EqSettingFragment.changeBottomEqAlpha(yMove / (screenHeight - distance));

                                } else {
                                    EqSettingFragment.startDragEqGoneAnimation();
                                    EqSettingFragment.startBottomEqGonenAnimation();
                                    isEnter = false;
                                    onBackPressed();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (EqSettingFragment.rootView != null) {
                                if ((screenHeight - bottomHeight < yDown && yDown < screenHeight)
                                        && (screenHeight - bottomHeight < yMove && yMove < screenHeight)
                                        && (Math.abs(yMove - yDown) < 10)) {
                                    //single click
                                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                                    if (((fr != null) && !(fr instanceof EqSettingFragment)) || (fr == null)) {
                                        EqSettingFragment fragment = new EqSettingFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putInt(JBLConstant.KEY_CONNECT_STATUS,mConnectStatus);
                                        fragment.setArguments(bundle);
                                        switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                                    }
                                } else {
                                    if (yMove > screenHeight / 2) {
                                        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                                        if ((fr != null) && fr instanceof EqSettingFragment) {
                                            Logger.d("EqSettingFragment", "onBack");
                                            EqSettingFragment.startDragEqGoneAnimation();
                                            EqSettingFragment.startBottomEqGonenAnimation();
                                            isEnter = false;
                                            onBackPressed();
                                        }
                                    } else {
                                        EqSettingFragment.rootView.setTranslationY(0);
                                        EqSettingFragment.startRecycleViewShowAnimation();
                                        int height = 0;
                                        EqSettingFragment.changeShadeViewHeight(height, HomeActivity.this);
                                        EqSettingFragment.setDragEqTitleBarGone();
                                        EqSettingFragment.startDragEqGoneAnimation();
                                        EqSettingFragment.startBottomEqGonenAnimation();
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }

    }


    private void generateSaPopupWindow() {
        saPopupwindow = new SaPopupWindow(this);
        saPopupwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //dismiss blur view
                if (mBlurView != null) {
                    mBlurView.setVisibility(View.GONE);
                }
            }
        });
        saPopupwindow.setOnSmartAmbientStatusReceivedListener(nativeSaListener);
    }


//    private void getRawSteps() {
//        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRawStepsByCmd();//get raw steps count of connected device
//    }

    private void generateAAPopupWindow() {
        aaPopupWindow = new AaPopupWindow(this);
        aaPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //dismiss blur view
                aaPopupWindow.setAAOff();
                if (mBlurView != null) {
                    mBlurView.setVisibility(View.GONE);
                }
                if (tutorialAncDialog != null && tutorialAncDialog.isShowing()) {
                    tutorialAncDialog.setTextViewTips(R.string.tutorial_tips_one);
                    tutorialAncDialog.showEqInfo();
                }
            }
        });
    }

    Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {
            if (LeManager.getInstance().isConnected()) {
                CmdDevStatus reqDevStatus = new CmdDevStatus(EnumDeviceStatusType.ALL_STATUS);
                LiveCmdManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, reqDevStatus);
            }else{
                ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
            }
        }
    };

    private void turnOnOffEq() {
        String curEqName = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, getString(R.string.off));
        String curEqNameExclusiveOff = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, this, "");
        if (curEqName.equals(getString(R.string.off))) {
            // turn on the eq
            Logger.d(TAG, "turn on the eq");
            if (TextUtils.isEmpty(curEqNameExclusiveOff)) {
                List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(this);
                Logger.d(TAG, "eqSize:" + eqModels.size());
                if (eqModels.size() < 5) {
                    if (LeManager.getInstance().isConnected()) {
                        LiveCmdManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(EnumEqPresetIdx.JAZZ));
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                    }
                } else {
                    if (LeManager.getInstance().isConnected()) {
                        // add the ble user eq code
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModels.get(4)));
                    }
                }
            } else {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, curEqNameExclusiveOff, this);
                if (curEqNameExclusiveOff.equals(getString(R.string.jazz))) {
                    if (LeManager.getInstance().isConnected()) {
                        LiveCmdManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(EnumEqPresetIdx.JAZZ));
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                    }
                } else if (curEqNameExclusiveOff.equals(getString(R.string.vocal))) {
                    if (LeManager.getInstance().isConnected()) {
                        LiveCmdManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(EnumEqPresetIdx.VOCAL));
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Vocal);
                    }
                } else if (curEqNameExclusiveOff.equals(getString(R.string.bass))) {
                    if (LeManager.getInstance().isConnected()) {
                        LiveCmdManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(EnumEqPresetIdx.BASS));
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Bass);
                    }
                } else {
                    EQModel eqModel = EQSettingManager.get().getEQModelByName(curEqNameExclusiveOff, this);
                    if (LeManager.getInstance().isConnected()) {
                        // add the ble user eq code
                    }else{
                        ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModel));
                    }
                }
            }
        } else {
            //turn off the eq
            Logger.d(TAG, "turn off the eq");
            if (LeManager.getInstance().isConnected()) {
                LiveCmdManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(EnumEqPresetIdx.OFF));
            }else{
                ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Off);
            }
        }
        homeHandler.removeCallbacks(applyRunnable);
        homeHandler.postDelayed(applyRunnable, 800);
    }

    public void setANC() {
        if (checkBoxNoiseCancel.isChecked()) {
            ANCControlManager.getANCManager(this).setANCValue(true);
        } else {
            ANCControlManager.getANCManager(this).setANCValue(false);
        }
    }

    public void tutorialSetANC() {
        if (checkBoxNoiseCancel.isChecked()) {
            checkBoxNoiseCancel.setChecked(false);
            ANCControlManager.getANCManager(this).setANCValue(false);
        } else {
            checkBoxNoiseCancel.setChecked(true);
            ANCControlManager.getANCManager(this).setANCValue(true);
        }
    }

    private void setOnSmartAmbientStatusReceivedListener(SaPopupWindow.OnSmartAmbientStatusReceivedListener listener) {
        this.mSaListener = listener;
    }

    public void showSaPopupWindow(View view, SaPopupWindow.OnSmartAmbientStatusReceivedListener listener) {
        mBlurView.setBlurredView(relative_layout_home_activity);
//        if (mBlurView.getBackground() == null) {
//            Bitmap image = BlurBuilder.blur(view);
//            mBlurView.setBackground(new BitmapDrawable(this.getResources(), image));
//        }
        mBlurView.invalidate();
        mBlurView.setVisibility(View.VISIBLE);
        mBlurView.setAlpha(0f);
        mBlurView.animate().alpha(1f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBlurView.setVisibility(View.VISIBLE);
                //OR
                mBlurView.setAlpha(1f);
            }
        });
        if (listener != null) {
            setOnSmartAmbientStatusReceivedListener(listener);
        }
        saPopupwindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
    }

    private SaPopupWindow.OnSmartAmbientStatusReceivedListener nativeSaListener = new SaPopupWindow.OnSmartAmbientStatusReceivedListener() {
        @Override
        public void onSaStatusReceived(boolean isDaEnable, boolean isTtEnable) {
            if (mSaListener != null) {
                mSaListener.onSaStatusReceived(isDaEnable, isTtEnable);
            }
        }
    };

    public void showAncPopupWindow(View view) {
//        if (mBlurView.getBackground() == null) {
//            Bitmap image = BlurBuilder.blur(view);
//            mBlurView.setBackground(new BitmapDrawable(this.getResources(), image));
//        }
        mBlurView.setBlurredView(relative_layout_home_activity);
        mBlurView.invalidate();
        mBlurView.setVisibility(View.VISIBLE);
        mBlurView.setAlpha(0f);
        mBlurView.animate().alpha(1f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBlurView.setVisibility(View.VISIBLE);
                //OR
                mBlurView.setAlpha(1f);
            }
        });
        aaPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
        aaPopupWindow.setImageViewAmbientAware((ImageView) findViewById(R.id.image_view_home_ambient_aware));
    }

    public void showNCPopupWindow() {
        notConnectedPopupWindow = new NotConnectedPopupWindow(this);
        notConnectedPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                aaPopupWindow.setAAOff();
                if (mBlurView != null) {
                    mBlurView.setVisibility(View.GONE);
                }
            }
        });
        mBlurView.setBlurredView(relative_layout_home_activity);
        mBlurView.invalidate();
        mBlurView.setVisibility(View.VISIBLE);
        mBlurView.setAlpha(0f);
        mBlurView.animate().alpha(1f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBlurView.setVisibility(View.VISIBLE);
                mBlurView.setAlpha(1f);
            }
        });
        notConnectedPopupWindow.showAtLocation(findViewById(R.id.relative_layout_home_activity), Gravity.NO_GRAVITY, 0, 0);
    }

    private void getDeviceInfo() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (LeManager.getInstance().isConnected()) {
                    linearLayoutBattery.setVisibility(View.VISIBLE);
                    getBleDeviceInfo();
                } else {
                    switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                        case NONE:
                            break;
                        case Connected_USBDevice:
                            linearLayoutBattery.setVisibility(View.VISIBLE);
                            progressBarBattery.setProgress(100);
                            textViewBattery.setText(getString(R.string.percent_100));
                            break;
                        case Connected_BluetoothDevice:
                            linearLayoutBattery.setVisibility(View.VISIBLE);
                            ANCControlManager.getANCManager(getApplicationContext()).getBatterLevel();
                            homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL, timeInterval);
                            break;
                    }
                    timeInterval();
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRawStepsByCmd();
                    Logger.e(TAG, "read boot image type");
                    timeInterval();
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).readBootImageType();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).readConfigModelNumber();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).readConfigProductName();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getAmbientLeveling();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).readBootVersionFileResource();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getFirmwareVersion();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getFirmwareInfo();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getANCValue();
                    if (AvneraManager.getAvenraManager().getLightX() == null) {
                        homeHandler.sendEmptyMessage(MSG_FIRMWARE_VERSION);
                    }
                }
            }
        }).start();

    }

    private void getBleDeviceInfo() {
        Logger.i(TAG,"get ble device info");
        LiveCmdManager.getInstance().reqDevInfo(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac);
        CmdDevStatus reqDevStatus = new CmdDevStatus(EnumDeviceStatusType.ALL_STATUS);
        LiveCmdManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, reqDevStatus);
        LiveCmdManager.getInstance().reqDevInfo(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac);
        CmdCurrEq cmdCurrEq = new CmdCurrEq(EnumEqCategory.GRAPHIC_EQ);
        LiveCmdManager.getInstance().reqCurrentEQ(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdCurrEq);
    }

    private void timeInterval() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onButtonDone() {
        DeviceManager.getInstance(this).setOnRetListener(this);
        LeManager.getInstance().setOnConnectStatusListener(this);
        getDeviceInfo();
    }

    private class HomeHandler extends Handler {

        HomeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_READ_BATTERY_INTERVAL: {
                    if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
                        homeHandler.removeMessages(MSG_READ_BATTERY_INTERVAL);
                        ANCControlManager.getANCManager(getApplicationContext()).getBatterLevel();
                    }
                    homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL, timeInterval);
                    break;
                }
                case MSG_BATTERY: {
                    updateBattery(msg.arg1);
                    break;
                }
                case MSG_ANC: {
                    updateANC(msg.arg1 == 1);
                    break;
                }
                case MSG_FIRMWARE_VERSION: {
                    updateFirmwareVersion();
                    break;
                }
                case MSG_AMBIENT_LEVEL: {
                    //for old devices
                    aaPopupWindow.updateAAUI(msg.arg1, (ImageView) findViewById(R.id.image_view_home_ambient_aware));//AppUtils.levelTransfer(msg.arg1)<---method for new device
                    break;
                }
                case MSG_AA_LEFT:
                    aaPopupWindow.updateAALeft(msg.arg1, (ImageView) findViewById(R.id.image_view_home_ambient_aware));
                    break;
                case MSG_AA_RIGHT:
                    aaPopupWindow.updateAARight(msg.arg1, (ImageView) findViewById(R.id.image_view_home_ambient_aware));
                    break;
                case MSG_CURRENT_PRESET: {
                    updateCurrentEQ(msg.arg1);
                    break;
                }
                case MSG_RAW_STEP: {
                    int rawSteps = msg.arg1 - 1;
                    Logger.d(TAG, "received raw steps call back rawSteps = " + rawSteps);
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRawSteps(rawSteps);
                    break;
                }
                case MSG_UPDATE_CUSTOM_EQ: {
                    relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                    ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                    String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getApplicationContext(), null);
                    PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, name, getApplicationContext());
                    Logger.d(TAG, "turnOnEq name:" + name);
                    if (name != null) {
                        textViewCurrentEQ.setText(name);
                        if (textViewCurrentEQ.getText().length() >= JBLConstant.MAX_MARQUEE_LEN) {
                            textViewCurrentEQ.setSelected(true);
                            textViewCurrentEQ.setMarqueeRepeatLimit(-1);
                        }
                    } else {
                        textViewCurrentEQ.setText(getString(R.string.custom_eq));
                    }
                    break;
                }
//                case MSG_CHECK_MY_DEVICE:{
//                    Logger.i(TAG, "handleMessage MSG_CHECK_DEVICES start");
//                    Set<String> deviceList = (Set<String>) msg.obj;
//                    if (hasNewDevice(deviceList)) {
//                        initDeviceSet();
//                    }
//                    updateMyDeviceStatus(deviceList);
//                    Logger.i(TAG, "handleMessage MSG_CHECK_DEVICES end");
//                    break;
//                }
                case MSG_CHECK_UPDATE: {
                    startCheckingIfUpdateIsAvailable(HomeActivity.this);
                    registerConnectivity();
                    break;
                }
                case MSG_FIRMWARE_INFO: {
                    ANCControlManager.getANCManager(getApplicationContext()).getFirmwareInfo();
                    break;
                }
                case MSG_GET_DESIGN_EQ:{
                    CmdCurrEq cmdCurrEq = new CmdCurrEq(EnumEqCategory.DESIGN_EQ);
                    LiveCmdManager.getInstance().reqCurrentEQ(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdCurrEq);
                    break;
                }
            }
        }
    }

    private void updateANC(boolean onOff) {
        if (checkBoxNoiseCancel != null)
            checkBoxNoiseCancel.setChecked(onOff);
        if (tutorialAncDialog != null) {
            tutorialAncDialog.setChecked(onOff);
        }
    }

    public void setEqMenuColor(boolean onOff) {
        if (relative_layout_home_eq_info != null) {
            if (onOff) {
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
            } else {
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            }
        }
    }

    private void updateCurrentEQ(int index) {
        Logger.d(TAG, "eqIndex:" + index);
        switch (index) {
            case 0: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.off), this);
                textViewCurrentEQ.setText(getString(R.string.off));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq_off);
                break;
            }
            case 1: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.jazz), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.jazz), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.jazz));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.vocal), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.vocal), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.vocal));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.bass), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.bass), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.bass));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                ANCControlManager.getANCManager(this).getAppGraphicEQPresetBandSettings(GraphicEQPreset.User, 10);
                break;
            }
            default:
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, null);
                textViewCurrentEQ.setText(name != null ? name : getString(R.string.off));
                break;
        }
        if (tutorialAncDialog != null) {
            tutorialAncDialog.updateCurrentEQ(index);
        }
    }

    private void updateBattery(int value) {
        Logger.d(TAG, "battery value = " + value);
        PreferenceUtils.setInt(PreferenceKeys.BATTERY_VALUE, value, this);
        if (value == 255) {
            progressBarBattery.setProgress(100);
            textViewBattery.setText(getString(R.string.percent_100));
            progressBarBattery.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_not_charge));
        } else {
            progressBarBattery.setProgress(value);
            textViewBattery.setText(String.format("%s%%", String.valueOf(value)));
            if (value > 0 && value <= 15) {
                progressBarBattery.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_red_charge));
            } else if (value > 15 && value <= 30) {
                progressBarBattery.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_orange_charge));
            } else if (value > 30) {
                progressBarBattery.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_not_charge));
            }
        }
    }

//    private void updateUSBBattery() {
//        progressBarBattery.setVisibility(View.INVISIBLE);
//        textViewBattery.setVisibility(View.INVISIBLE);
//        textViewBattery.setVisibility(View.INVISIBLE);
//    }

    private void updateFirmwareVersion() {
        audioManager am = AvneraManager.getAvenraManager().getAudioManager();
        if (am == null) {
            Logger.d(TAG, "am is null, not 150NC device");
            return;
        }
        AccessoryInfo accessoryInfo = am.getAccessoryStatus();
        PreferenceUtils.setString(PreferenceKeys.PRODUCT, accessoryInfo.getName(), this);
        deviceName = accessoryInfo.getModelNumber();
        AppUtils.setModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext(), deviceName);
        Logger.d(TAG, "modelName : " + accessoryInfo.getModelNumber());
        updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
        String version = accessoryInfo.getFirmwareRev();
        if (version.length() >= 5) {
            Logger.d(TAG, "currentVersion : " + version);
            PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.APP_VERSION, version, this);
        }
        String hardVersion = accessoryInfo.getHardwareRev();
        if (hardVersion.length() >= 5) {
            Logger.d(TAG, "hardVersion : " + hardVersion);
//                JBLPreferenceUtil.setString(AppUtils.RSRC_VERSION, fwVersion, this);
            PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, hardVersion, this);
        }
        AnalyticsManager.getInstance(this).reportFirmwareVersion(hardVersion);
        homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishActivity(this);
        unregisterNetworkReceiverSafely();
    }

    private NetworkChangeReceiver networkChangeReceiver;

    private void registerConnectivity() {
        if (!mReceiverTag) {
            networkChangeReceiver = new NetworkChangeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            Logger.i(TAG, "registerConnectivity");
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.registerReceiver(networkChangeReceiver, intentFilter);
            mReceiverTag = true;
        }
    }

    private boolean mReceiverTag = false;

    private void unregisterNetworkReceiverSafely() {
        try {
            if (mReceiverTag) {
                mReceiverTag = false;
                Logger.i(TAG, "unregisterNetworkReceiverSafely");
                this.unregisterReceiver(networkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(TAG, "onReceive");
            if (isFinishing()) {
                Logger.d(TAG, "NetworkChangeReceiver, activity is finishing, return");
                return;
            }
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    startCheckingIfUpdateIsAvailable(HomeActivity.this);
                } else {
                    showOta(false);
                }
            }
        }

    }

    private void sendMessageTo(int command, int arg1) {
        Message msg = new Message();
        msg.what = command;
        msg.arg1 = arg1;
        homeHandler.sendMessage(msg);
    }

    private void parseCustomEQ(byte[] v) {
        if (v != null && v.length == 48) {
            Logger.d(TAG, "EqBand value1 = " + Arrays.toString(v));
            int[] eqArray = new int[10];
            eqArray[0] = v[8];
            eqArray[1] = v[12];
            eqArray[2] = v[16];
            eqArray[3] = v[20];
            eqArray[4] = v[24];
            eqArray[5] = v[28];
            eqArray[6] = v[32];
            eqArray[7] = v[36];
            eqArray[8] = v[40];
            eqArray[9] = v[44];
            boolean isHave = false;
            String curEQName = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, "");
            if (!TextUtils.isEmpty(curEQName)) {
                EQModel eqModel = EQSettingManager.get().getEQModelByName(curEQName, this);
                if (eqModel != null) {
                    if (EQSettingManager.get().isTheSameEQ(eqModel, eqArray)) {
                        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                        sendMessageTo(MSG_UPDATE_CUSTOM_EQ, -1);
                        Logger.d(TAG, "Have the same EQ:" + eqModel.eqName);
                        return;
                    }
                }

            }
            String eqName = PreferenceUtils.getString(PreferenceKeys.MODEL, this, null) + " EQ";
            List<EQModel> models = EQSettingManager.get().getCompleteEQList(this);
            if (models != null && models.size() > 4) {
                for (int i = 4; i < models.size(); i++) {
                    EQModel eqModel = models.get(i);
                    if (EQSettingManager.get().isTheSameEQ(eqModel, eqArray)) {
                        isHave = true;
                        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                        sendMessageTo(MSG_UPDATE_CUSTOM_EQ, -1);
                        Logger.d(TAG, "Have the same EQ:" + eqModel.eqName);
                        break;
                    }
                }
                if (!isHave) {
                    Logger.d(TAG, "create a new EQ");
                    EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                    EQSettingManager.get().addCustomEQ(eqModel, this);
                    PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                    sendMessageTo(MSG_UPDATE_CUSTOM_EQ, -1);
                }

            } else {
                Logger.d(TAG, "create a new EQ");
                EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                EQSettingManager.get().addCustomEQ(eqModel, this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                sendMessageTo(MSG_UPDATE_CUSTOM_EQ, -1);
            }

        }
    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);

//        Logger.d(TAG, "onReceive command:" + enumCommands + ",value:" + objects[0]);
        switch (enumCommands) {
            case CMD_ANC: {
                int anc = (Integer) objects[0];
                Logger.d(TAG, "on receive, cmd anc: " + anc);
                sendMessageTo(MSG_ANC, anc);
                break;
            }
            case CMD_AMBIENT_LEVELING: {
                int aaLevel = (int) objects[0];
                Logger.d(TAG, "on receive, cmd ambient: " + aaLevel);
                sendMessageTo(MSG_AMBIENT_LEVEL, aaLevel);
                break;
            }
            case CMD_RAW_STEPS: {
                int rawSteps = (int) objects[0];
                Logger.d(TAG, "on receive, cmd raw steps: " + rawSteps);
                sendMessageTo(MSG_RAW_STEP, rawSteps);
                break;
            }
            case CMD_BATTERY_LEVEL: {
                int battery = (int) objects[0];
                Logger.d(TAG, "on receive, cmd battery level: " + battery);
                sendMessageTo(MSG_BATTERY, battery);
                break;
            }
            case CMD_GEQ_CURRENT_PRESET: {
                int currentPreset = (int) objects[0];
                Logger.d(TAG, "on receive, cmd eq current preset: " + currentPreset);
                sendMessageTo(MSG_CURRENT_PRESET, currentPreset);
                break;
            }
            case CMD_GRAPHIC_EQ_BAND_GAINS: {
                break;
            }
            case CMD_FIRMWARE_VERSION: {
                Logger.d(TAG, "on receive, cmd firmware version");
                if (objects[0] != null) {
                    PreferenceUtils.setString(AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()), PreferenceKeys.APP_VERSION, (String) objects[0], this);
                    homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                } else {
                    sendMessageTo(MSG_FIRMWARE_VERSION, -1);
                }
                break;
            }
            case CMD_RAW_LEFT:
                int rawLeft = (int) objects[0];
                Logger.d(TAG, "on receive, cmd raw left: " + rawLeft);
                sendMessageTo(MSG_AA_LEFT, rawLeft);
                break;
            case CMD_RAW_RIGHT:
                int rawRight = (int) objects[0];
                Logger.d(TAG, "on receive, cmd raw right: " + rawRight);
                sendMessageTo(MSG_AA_RIGHT, rawRight);
                break;
            case CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS: {
                if (objects[0] != null) {
                    byte[] eqBytes = (byte[]) (objects[0]);
                    Logger.d(TAG, "on receive, cmd eq band settings: " + ArrayUtil.toHex(eqBytes));
                    parseCustomEQ(eqBytes);
//                    homeHandler.sendEmptyMessage(MSG_FIRMWARE_INFO);
                } else {
                    //TODO: bes live update eq settings.
                    RetCurrentEQ retCurrentEQ = (RetCurrentEQ) objects[1];
                    if (retCurrentEQ != null) {
                        Logger.d(TAG, "retCurrentEQ:" + retCurrentEQ.enumEqCategory);
                        if (retCurrentEQ.enumEqCategory == EnumEqCategory.DESIGN_EQ) {
                            //save the designEq
                            List<RetCurrentEQ> retCurrentEQList = SharePreferenceUtil.readCurrentEqSet(HomeActivity.this, SharePreferenceUtil.BLE_DESIGN_EQ);
                            if (retCurrentEQList!=null&&retCurrentEQList.size()>0)
                            Logger.d(TAG, "retCurrentEQ ble design eq band count:" + retCurrentEQList.get(0).bandCount);
                            List<RetCurrentEQ> retCurrentEQS = new ArrayList<>();
                            retCurrentEQS.add(retCurrentEQ);
                            SharePreferenceUtil.saveCurrentEqSet(HomeActivity.this, retCurrentEQS, SharePreferenceUtil.BLE_DESIGN_EQ);
                        } else if (retCurrentEQ.enumEqCategory == EnumEqCategory.GRAPHIC_EQ) {
                            //parse the graficEq
                            parseBleCustomEq(retCurrentEQ);


                        }
                    }
                }
                break;
            }
            case CMD_IsInBootloader: {
                boolean isInBootloader = (boolean) objects[0];
                Logger.d(TAG, "on receive, cmd is in boot loader: " + isInBootloader);
                doInBootLoaderMode(isInBootloader);
                break;
            }
            case CMD_ConfigProductName: {
                String productName = (String) objects[0];
                Logger.d(TAG, "on receive, cmd config product name: " + productName);
                PreferenceUtils.setString(PreferenceKeys.PRODUCT, productName, this);
                break;
            }
            case CMD_ConfigModelNumber: {
                Logger.d(TAG, "on receive, cmd config model number");
                AppUtils.setModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext(), deviceName);
                updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
                break;
            }
            case CMD_AppPushANCEnable: {
                Logger.d(TAG, "on receive, cmd app push anc enable");
                ANCControlManager.getANCManager(this).getANCValue();
                break;
            }
            case CMD_AppPushANCAwarenessPreset: {
                Logger.d(TAG, "on receive, cmd app push ambient level");
                ANCControlManager.getANCManager(this).getAmbientLeveling();
                break;
            }
            case CMD_ANC_NOTIFICATION: {
                int ancValue = (Integer) objects[0];
                Logger.d(TAG, "on receive, cmd anc notification: " + ancValue);
                PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, (Integer) objects[0], this);
                if (ancValue == 1) {
                    checkBoxNoiseCancel.setChecked(true);
                } else if (ancValue == 0) {
                    checkBoxNoiseCancel.setChecked(false);
                }
                break;
            }
            case CMD_AA_Notification: {
                if (aaPopupWindow == null) {
                    Logger.i(TAG, "aaPopupWindow is null");
                    return;
                }
                int amVal = (Integer) objects[0];
                Logger.d(TAG, "on receive, cmd ambient notification: " + amVal);
                aaPopupWindow.updateAAUI(amVal, (ImageView) findViewById(R.id.image_view_home_ambient_aware));
                break;
            }
            case CMD_BootReadVersionFile: {
                Logger.d(TAG, "on receive, cmd boot read version file");
                if ((boolean) objects[1]) {
                    PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, (String) objects[0], this);
                }
                homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                break;
            }
            case CMD_FW_INFO: {
                FirmwareUtil.currentFirmware = (Integer) objects[0];
                Logger.d(TAG, "on receive, cmd fw info:" + FirmwareUtil.currentFirmware);
                break;
            }
        }
    }

    private void parseBleCustomEq(RetCurrentEQ retCurrentEQ) {
        for (int i=0;i<retCurrentEQ.bandCount;i++){
            Logger.d(TAG,"reCurrentEQ GraphicEq fc:"+i+retCurrentEQ.bands[i].fc);
        }
        List<RetCurrentEQ> retCurrentEQList = SharePreferenceUtil.readCurrentEqSet(HomeActivity.this,SharePreferenceUtil.BLE_EQS);
        boolean isExist = false;
        if (retCurrentEQList!=null && retCurrentEQList.size()>0){
            Logger.d(TAG,"retCurrentEQList size is "+retCurrentEQList.size());
            for (int i =0;i<retCurrentEQList.size();i++){
                RetCurrentEQ retCurrentEQModel = retCurrentEQList.get(i);
                if (retCurrentEQModel.enumEqPresetIdx==retCurrentEQ.enumEqPresetIdx&&retCurrentEQModel.sampleRate==retCurrentEQ.sampleRate
                        &&retCurrentEQModel.gain0==retCurrentEQ.gain0 &&retCurrentEQModel.gain1==retCurrentEQ.gain1
                        &&retCurrentEQModel.bandCount == retCurrentEQ.bandCount){
                    for (int j =0;j<retCurrentEQModel.bandCount;j++){
                        if (retCurrentEQModel.bands[j].type ==retCurrentEQ.bands[j].type &&retCurrentEQModel.bands[j].gain ==retCurrentEQ.bands[j].gain
                                &&retCurrentEQModel.bands[j].fc ==retCurrentEQ.bands[j].fc&&retCurrentEQModel.bands[j].q ==retCurrentEQ.bands[j].q){
                            isExist = true;
                        }
                    }
                    if (isExist){
                        break;
                    }
                }
            }
            if (!isExist){
                Logger.d(TAG,"retCurrentEQ is not exist,add it");
                retCurrentEQList.add(retCurrentEQ);
            }else{
                Logger.d(TAG,"retCurrentEQ is exist");
            }
        }else{
            Logger.d(TAG,"retCurrentEQList size is 0 ,add it");
            retCurrentEQList.add(retCurrentEQ);
        }
        SharePreferenceUtil.saveCurrentEqSet(HomeActivity.this,retCurrentEQList,SharePreferenceUtil.BLE_EQS);
        List<RetCurrentEQ> retGraphicEQs = new ArrayList<>();
        retGraphicEQs.add(retCurrentEQ);
        SharePreferenceUtil.saveCurrentEqSet(HomeActivity.this,retGraphicEQs,SharePreferenceUtil.BLE_EQS);
        homeHandler.sendEmptyMessageDelayed(MSG_GET_DESIGN_EQ, 500);
    }

    private final static int MSG_GET_DESIGN_EQ = 7;
    private void doInBootLoaderMode(boolean isInBootloaderMode) {
        if (isInBootloaderMode) {
            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                case NONE:
                    break;
                case Connected_USBDevice:
                case Connected_BluetoothDevice:
                    try {
                        Logger.d(TAG, "Enter OTAFragment page");
                        OTAFragment otaFragment = new OTAFragment();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("lightXIsInBootloader", true);
                        otaFragment.setArguments(bundle);
                        switchFragment(otaFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    break;
            }
        }
    }
}
