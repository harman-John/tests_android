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
import android.os.Build;
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
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
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
import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.audioManager;
import com.avnera.audiomanager.responseResult;

import com.avnera.smartdigitalheadset.Command;

import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jbl.stc.com.R;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.EqCustomFragment;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.listener.ConnectListener;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.listener.OnOtaListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AaPopupWindow;

import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.BlurringView;
import jbl.stc.com.view.CustomFontTextView;
import jbl.stc.com.view.NotConnectedPopupWindow;
import jbl.stc.com.view.SaPopupWindow;

import static java.lang.Integer.highestOneBit;
import static java.lang.Integer.valueOf;


public class HomeActivity extends BaseActivity implements View.OnClickListener, ConnectListener, OnOtaListener {
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
    private final static int MSG_CHECK_MY_DEVICE = 9;
    private final static int MSG_CHECK_UPDATE = 10;
    private final static int MSG_FIRMWARE_INFO = 11;

    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
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
    private TextView titleEqText;
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
        titleEqText = (TextView) findViewById(R.id.titleEqText);
        if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            setEqMenuColor(false);
            relative_layout_home_eq_info.setAlpha((float) 0.5);
        } else {
            setEqMenuColor(true);
            titleEqText.setOnClickListener(this);
            findViewById(R.id.image_view_home_settings).setOnClickListener(this);
            //relative_layout_home_eq_info.setOnClickListener(this);
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
        if (!TextUtils.isEmpty(action)
                && "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {

        } else {
            AnalyticsManager.getInstance(this).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
            Logger.d(TAG, "onResume " + DeviceConnectionManager.getInstance().getCurrentDevice());
            doResume();
        }
    }

    @Override
    public void connectDeviceStatus(boolean isConnected) {
        super.connectDeviceStatus(isConnected);

        if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            Logger.i(TAG, "connectDeviceStatus A2DP_HALF_CONNECTED");
            finish();
        } else if (!isConnected && !isOTADoing && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
            Logger.i(TAG, "connectDeviceStatus not connected, not ota");
            removeAllFragment();
            finish();
        } else if (isConnected && isOTADoing && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
            Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if (fr != null && fr instanceof OTAFragment) {
                Logger.i(TAG, "connectDeviceStatus myDevice = " + mConnectStatus);
//                DeviceManager.getInstance(this).startA2DPCheck();
                ((OTAFragment) fr).otaSuccess(this);
            }
        }
    }

    @Override
    public void checkDevices(Set<MyDevice> deviceList) {
        super.checkDevices(deviceList);
//        Logger.i(TAG, "MSG_CHECK_DEVICES deviceList = " + deviceList);
//        Message msg = new Message();
//        msg.what = MSG_CHECK_MY_DEVICE;
//        msg.obj = deviceList;
//        homeHandler.sendMessage(msg);
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
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
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
                            if (isEnter && EqSettingFragment.rootView!=null) {
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


    private void getRawSteps() {
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).getRawStepsByCmd();//get raw steps count of connected device
    }

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

                switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                    case NONE:
                        break;
                    case Connected_USBDevice:
                        linearLayoutBattery.setVisibility(View.VISIBLE);
                        progressBarBattery.setProgress(100);
                        textViewBattery.setText("100%");
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
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
        getDeviceInfo();
    }

    private class HomeHandler extends Handler {

        public HomeHandler(Looper looper) {
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
            textViewBattery.setText("100%");
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

    private void updateUSBBattery() {
        progressBarBattery.setVisibility(View.INVISIBLE);
        textViewBattery.setVisibility(View.INVISIBLE);
        textViewBattery.setVisibility(View.INVISIBLE);
    }

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

    private void sendMessageTo(int command, String arg1) {
        Message msg = new Message();
        msg.what = command;
        if (arg1 != null)
            msg.arg1 = valueOf(arg1);
        homeHandler.sendMessage(msg);
    }


    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        super.receivedResponse(command, values, status);
        Logger.d(TAG, "-----> receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values.size() <= 0) {
            Logger.d(TAG, "return, values size is " + values.size());
            return;
        }
        switch (command) {
            case AmCmds.CMD_ANC: {
                Logger.d(TAG, "do get anc");
                String value = values.iterator().next().getValue().toString();
                String tmp = "0";
                if (value.equalsIgnoreCase("true")
                        || value.equalsIgnoreCase("1")) {
                    tmp = "1";
                }
                sendMessageTo(MSG_ANC, tmp);
                break;
            }
            case AmCmds.CMD_AmbientLeveling: {
                Logger.d(TAG, "do get ambient leveling");
                sendMessageTo(MSG_AMBIENT_LEVEL, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_RawSteps: {
                Logger.d(TAG, "do get raw steps");
                sendMessageTo(MSG_RAW_STEP, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_BatteryLevel: {
                Logger.d(TAG, "do get battery level");
                String bl = values.iterator().next().getValue().toString();
                //batteryValue = valueOf(bl);
                sendMessageTo(MSG_BATTERY, bl);
                break;
            }
            case AmCmds.CMD_Geq_Current_Preset: {
                Logger.d(TAG, "do get current preset");
                sendMessageTo(MSG_CURRENT_PRESET, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_GrEqBandGains: {
                Logger.d(TAG, "do eqBand gains=" + command + ",values=" + values + ",status=" + status);
                if (values != null && values.size() > 0) {
                   /* Logger.d(TAG,"name = "+ values.get(0).getName());
                    byte[] v = (byte[]) (values.get(0).getValue());
                    Logger.d(TAG,"value = "+ Arrays.toString(v));*/

                    Logger.d(TAG, "name = " + values.iterator().next().getName().toString());
                    byte[] v = (byte[]) (values.iterator().next().getValue());
                    Logger.d(TAG, "value = " + Arrays.toString(v));
                    int presetIndext = v[0];
                    int numBands = v[4];
                    int value = v[8];
                    Logger.d(TAG, "presetIndext:" + presetIndext + "numBands:" + numBands + "value:" + value);
                }
                break;
            }
            case AmCmds.CMD_FirmwareVersion: {
                Logger.d(TAG, "do get firmware version ");
                sendMessageTo(MSG_FIRMWARE_VERSION, null);
                break;
            }
            case AmCmds.CMD_FWInfo: {
                Logger.d(TAG, "get 150nc current firmware.");
                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
                Logger.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            }
            case AmCmds.CMD_RawLeft:
                Logger.d(TAG, "do get raw left =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_LEFT, values.iterator().next().getValue().toString());
                break;
            case AmCmds.CMD_RawRight:
                Logger.d(TAG, "do get raw right =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_RIGHT, values.iterator().next().getValue().toString());
                break;
            case AmCmds.CMD_GraphicEqPresetBandSettings: {
                Logger.d(TAG, "do get graphic eqPreset band settings =" + command + ",values=" + values + ",status=" + status);
                parseCustomEQ((byte[]) (values.iterator().next().getValue()));
                homeHandler.sendEmptyMessage(MSG_FIRMWARE_INFO);
                break;
            }

        }

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
                        isHave = true;
                        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                        sendMessageTo(MSG_UPDATE_CUSTOM_EQ, null);
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
                        sendMessageTo(MSG_UPDATE_CUSTOM_EQ, null);
                        Logger.d(TAG, "Have the same EQ:" + eqModel.eqName);
                        break;
                    }
                }
                if (!isHave) {
                    Logger.d(TAG, "create a new EQ");
                    EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                    EQSettingManager.get().addCustomEQ(eqModel, this);
                    PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                    sendMessageTo(MSG_UPDATE_CUSTOM_EQ, null);
                }

            } else {
                Logger.d(TAG, "create a new EQ");
                EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                EQSettingManager.get().addCustomEQ(eqModel, this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                sendMessageTo(MSG_UPDATE_CUSTOM_EQ, null);
            }

        }
    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {
        super.receivedPushNotification(action, command, values, status);
        Logger.d(TAG, "-------->receive push notification command =" + command + ",values=" + values + ",status=" + status);
        switch (command) {
            case AmCmds.CMD_ANCNotification: {
                Logger.d(TAG, "CMD_ANCNotification:" + ",values=" + values.iterator().next().getValue().toString());
                PreferenceUtils.setInt(PreferenceKeys.ANC_VALUE, Integer.valueOf(values.iterator().next().getValue().toString()), this);
                if (Integer.valueOf(values.iterator().next().getValue().toString()) == 1) {
                    checkBoxNoiseCancel.setChecked(true);
                } else if (Integer.valueOf(values.iterator().next().getValue().toString()) == 0) {
                    checkBoxNoiseCancel.setChecked(false);
                }
                break;
            }
            case AmCmds.CMD_AmbientLevelingNotification: {

                if (aaPopupWindow == null) {
                    Logger.i(TAG, "aaPopupWindow is null");
                    return;
                }
                aaPopupWindow.updateAAUI(AppUtils.levelTransfer(Integer.valueOf(values.iterator().next().getValue().toString())));//new devices
            }
            break;
            case AmCmds.CMD_FWInfo: {
                Logger.d(TAG, "get 150nc current firmware.");
                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
                Logger.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            }
        }
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] var4) {
        super.lightXAppReadResult(var1, command, success, var4);
        if (success) {
            switch (command) {
                case App_0xB3:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().setIsCalibrationComplete(Utility.getBoolean(var4, 0));
                    break;
                case AppANCAwarenessPreset:
                    Logger.d(TAG, "AppANCAwarenessPreset");
                    int intValue = Utility.getInt(var4, 0);
//                    update(intValue);
                    sendMessageTo(MSG_AMBIENT_LEVEL, String.valueOf(intValue));
                    break;
                case AppANCEnable:
                    if (var4 != null) {
                        boolean ancResult = Utility.getBoolean(var4, 0);
                        updateANC(ancResult);
                    }
                    break;
                case AppAwarenessRawLeft:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppAwarenessRawRight:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppAwarenessRawSteps:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    int rawSteps = Utility.getInt(var4, 0) - 1;
                    Logger.d(TAG, "received raw steps call back rawSteps = " + rawSteps);
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRawSteps(rawSteps);

                    break;
                case AppGraphicEQCurrentPreset:
                    long currentPreset = Utility.getUnsignedInt(var4, 0);
                    Logger.d(TAG, "lightXAppReadResult" + command + " is " + currentPreset);
                    updateCurrentEQ((int) currentPreset);
                    break;
                case AppGraphicEQBandFreq:
//                    readAppReturn = Utility.getUnsignedInt(var4, 0);
                    break;
                case AppBatteryLevel:
                    long batteryValue = Utility.getUnsignedInt(var4, 0);
                    Logger.d(TAG, command + " is " + batteryValue);
                    updateBattery((int) batteryValue);
                    break;
                case AppFirmwareVersion:
                    int major = var4[0];
                    int minor = var4[1];
                    int revision = var4[2];
                    Logger.d(TAG, "AppCurrVersion = " + major + "." + minor + "." + revision + ",modelNumber" + AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()));
                    PreferenceUtils.setString(AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()), PreferenceKeys.APP_VERSION, major + "." + minor + "." + revision, this);
                    homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                    break;

                case AppGraphicEQPresetBandSettings: {
                    Logger.d(TAG, "Eq band:" + Arrays.toString(var4));
                    int preset = Utility.getInt(var4, 0);
                    int numBands = Utility.getInt(var4, 4);
                    parseCustomEQ(var4);
                }
                break;
            }
        } else {
            switch (command) {
                case AppGraphicEQCurrentPreset:
                    updateCurrentEQ(8);
                    break;
                case AppANCEnable:
                    boolean anc = Utility.getBoolean(var4, 0);
                    updateANC(anc);
                    break;
            }
        }
    }

    @Override
    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean success, final int i, final byte[] buffer) {
        Logger.d(TAG, "lightXReadBootResult command is " + command + " result is " + success);
        if (isFinishing()) {
            Logger.d(TAG, "lightXReadBootResult, activity is finishing, return");
            return;
        }
        if (success) {
            switch (command) {
                case BootReadVersionFile: {
                    int result[] = AppUtils.parseVersionFromASCIIbuffer(buffer);
                    int major = result[0];
                    int minor = result[1];
                    int revision = result[2];
                    String rsrcSavedVersion = major + "." + minor + "." + revision;
                    PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, rsrcSavedVersion, this);
                    Logger.d(TAG, "rsrcSavedVersion=" + rsrcSavedVersion);
                    homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
                }
                break;
            }
        } else {
            homeHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
        }
    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command command, byte[] var4) {
        super.lightXAppReceivedPush(var1, command, var4);
        Logger.d(TAG, "lightXAppReceivedPush command is " + command);
        switch (command) {
            case AppPushANCEnable:
                ANCControlManager.getANCManager(this).getANCValue();
                break;
            case AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(this).getAmbientLeveling();
            }
            break;
        }
    }

    @Override
    public void lightXReadConfigResult(LightX var1, Command command, boolean success, String var4) {
        super.lightXReadConfigResult(var1, command, success, var4);
        Logger.d(TAG, "lightXReadConfigResult command = " + command);
        if (success) {
            switch (command) {
                case ConfigProductName:
                    PreferenceUtils.setString(PreferenceKeys.PRODUCT, var4, this);
                    break;
                case ConfigModelNumber:
                    deviceName = var4;
                    Logger.d(TAG, "lightXReadConfigResult deviceName = " + deviceName);
                    AppUtils.setModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext(), deviceName);
                    updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
                    break;
            }
        }
    }

    @Override
    public void lightXAppWriteResult(LightX var1, Command var2, boolean var3) {
        super.lightXAppWriteResult(var1, var2, var3);
        Logger.d(TAG, "lightXAppWriteResult");
        if (var3) {
            switch (var2) {
                case App_0xB3:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().setIsCalibrationComplete(true);
                    break;
                case AppANCAwarenessPreset:
//                    avneraGetters();
                    break;
            }
        } else {
            switch (var2) {
                case App_0xB2:
//                    if (Calibration.getCalibration() != null)
//                        Calibration.getCalibration().calibrationFailed();
                    break;
            }

        }
    }

    @Override
    public void lightXIsInBootloader(LightX var1, boolean isInBootloaderMode) {
        super.lightXIsInBootloader(var1, isInBootloaderMode);
        Logger.d(TAG, "lightXIsInBootloader =" + isInBootloaderMode);
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
