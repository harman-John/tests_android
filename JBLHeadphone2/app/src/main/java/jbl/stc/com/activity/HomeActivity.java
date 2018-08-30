package jbl.stc.com.activity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.harman.bluetooth.request.RequestCommands;

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
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AaPopupWindow;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.BlurringView;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        DeviceManager.getInstance(this).setOnRetListener(this);
        mConnectStatus = getIntent().getIntExtra(JBLConstant.KEY_CONNECT_STATUS, -1);
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
            checkBoxNoiseCancel = findViewById(R.id.image_view_home_noise_cancel);
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
                TextView textViewAmbientAware = findViewById(R.id.text_view_home_ambient_aware);
                textViewAmbientAware.setText(R.string.smart_ambient);
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
            doResume();
        }

        if (fr == null) {
            doResume();
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
                if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
                    if (AppUtils.isOldDevice(deviceName)) {
                        if (!checkBoxNoiseCancel.isChecked()) {
                            checkBoxNoiseCancel.setChecked(true);
                        }
                        setANC();
                        showAncPopupWindow(findViewById(R.id.relative_layout_home_activity));
                    } else if (AppUtils.isNewDevice(deviceName)) {
                        showSaPopupWindow(findViewById(R.id.relative_layout_home_activity), null);
                    }
                }
                break;
            }
            case R.id.arrowUpImage: {
                switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_home_back: {
                DeviceManager.getInstance(this).setIsFromHome(true);
                onBackPressed();
                break;
            }
            case R.id.frame_layout_home_device_image:
            case R.id.image_view_home_settings: {
                switchFragment(new SettingsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_home_noise_cancel: {
                setANC();
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

    private void showTutorial() {
        if (DeviceManager.getInstance(this).isConnected() && mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
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
                                        switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
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
            ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
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
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                } else {
                    ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModels.get(4)));
                }
            } else {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, curEqNameExclusiveOff, this);
                if (curEqNameExclusiveOff.equals(getString(R.string.jazz))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.vocal))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Vocal);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.bass))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Bass);
                } else {
                    EQModel eqModel = EQSettingManager.get().getEQModelByName(curEqNameExclusiveOff, this);
                    ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModel));
                }
            }
        } else {
            //turn off the eq
            Logger.d(TAG, "turn off the eq");
            ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Off);
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

        getAAValue();
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

        homeHandler.post(new Runnable() {
            @Override
            public void run() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestCommands requestCommands = new RequestCommands();
                        requestCommands.reqDevInfo();
                    }
                }).start();

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
                        ANCControlManager.getANCManager(getApplicationContext()).getBatterLeverl();
                        homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL, timeInterval);
                        break;
                }
                homeHandler.sendEmptyMessage(MSG_FIRMWARE_INFO);
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRawStepsByCmd();
                Logger.e(TAG, "read boot image type");
                ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).readBootImageType();
                ANCControlManager.getANCManager(getApplicationContext()).getANCValue();
                if (AvneraManager.getAvenraManager().getLightX() == null) {
                    homeHandler.sendEmptyMessage(MSG_FIRMWARE_VERSION);
                }
                ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();

                ANCControlManager.getANCManager(getApplicationContext()).readConfigModelNumber();
                ANCControlManager.getANCManager(getApplicationContext()).readConfigProductName();
                homeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ANCControlManager.getANCManager(getApplicationContext()).readBootVersionFileResource();
                        ANCControlManager.getANCManager(getApplicationContext()).getFirmwareVersion();
                    }
                }, 200);
            }
        });

    }

    private void getAAValue() {
        ANCControlManager.getANCManager(this).getAmbientLeveling();
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
                        ANCControlManager.getANCManager(getApplicationContext()).getBatterLeverl();
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
                    aaPopupWindow.updateAAUI(msg.arg1);//AppUtils.levelTransfer(msg.arg1)<---method for new device
                    break;
                }
                case MSG_AA_LEFT:
                    aaPopupWindow.updateAALeft(msg.arg1);
                    break;
                case MSG_AA_RIGHT:
                    aaPopupWindow.updateAARight(msg.arg1);
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
                    ((JBLApplication) getApplication()).deviceInfo.eqOn = true;
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
        //ANCControlManager.getANCManager(this).getAppGraphicEQPresetBandSettings(lightX, GraphicEQPreset.Jazz,9);
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
                ((JBLApplication) getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.jazz));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.vocal), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.vocal), this);
                ((JBLApplication) getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.vocal));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.bass), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.bass), this);
                ((JBLApplication) getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.bass));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                //ANCControlManager.getANCManager(this).getAppGraphicEQBand(GraphicEQPreset.User, lightX);
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
        if (isFinishing()) {
            Logger.d(TAG, "do on receive error, activity is finishing");
            return;
        }
        switch (enumCommands) {
            case CMD_ANC: {
                sendMessageTo(MSG_ANC, (Integer) objects[0]);
                break;
            }
            case CMD_AMBIENT_LEVELING: {
                int aaLevel = (int) objects[0];
                sendMessageTo(MSG_AMBIENT_LEVEL, aaLevel);
                break;
            }
            case CMD_RAW_STEPS: {
                int rawSteps = (int) objects[0];
                Logger.d(TAG, "do get raw steps");
                sendMessageTo(MSG_RAW_STEP, rawSteps);
                break;
            }
            case CMD_BATTERY_LEVEL: {
                int battery = (int) objects[0];
                Logger.d(TAG, "do get battery level, battery = "+battery);
                sendMessageTo(MSG_BATTERY, battery);
                break;
            }
            case CMD_GEQ_CURRENT_PRESET: {
                int currentPreset = (int) objects[0];
                Logger.d(TAG, "do get current preset");
                sendMessageTo(MSG_CURRENT_PRESET, currentPreset);
                break;
            }
            case CMD_GRAPHIC_EQ_BAND_GAINS: {
                break;
            }
            case CMD_FIRMWARE_VERSION: {
                Logger.d(TAG, "do get firmware version ");
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
                sendMessageTo(MSG_AA_LEFT, rawLeft);
                break;
            case CMD_RAW_RIGHT:
                int rawRight = (int) objects[0];
                sendMessageTo(MSG_AA_RIGHT, rawRight);
                break;
            case CMD_GRAPHIC_EQ_PRESET_BAND_SETTINGS: {
                parseCustomEQ((byte[]) (objects[0]));
                homeHandler.sendEmptyMessage(MSG_FIRMWARE_INFO);
                break;
            }
            case CMD_IsInBootloader: {
                doInBootLoaderMode((boolean) objects[0]);
                break;
            }
            case CMD_ConfigProductName: {
                PreferenceUtils.setString(PreferenceKeys.PRODUCT, (String) objects[0], this);
                break;
            }
            case CMD_ConfigModelNumber: {
                AppUtils.setModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext(), deviceName);
                updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
                break;
            }
            case CMD_AppPushANCEnable: {
                ANCControlManager.getANCManager(this).getANCValue();
                break;
            }
            case CMD_AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(this).getAmbientLeveling();
                break;
            }
            case CMD_ANC_NOTIFICATION: {
                int ancValue = (Integer) objects[0];
                Logger.d(TAG, "get anc notification value =" + ancValue);
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
                aaPopupWindow.updateAAUI((Integer) objects[0]);
                break;
            }
            case CMD_BootReadVersionFile: {
                if ((boolean) objects[1]) {
                    PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, (String) objects[0], this);
                    homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                } else {
                    homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                    break;
                }
                break;
            }
            case CMD_FW_INFO: {
                FirmwareUtil.currentFirmware = (Integer) objects[0];
                Logger.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            }
        }
    }

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
