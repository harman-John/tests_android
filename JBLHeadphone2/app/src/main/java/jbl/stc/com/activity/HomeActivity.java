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
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
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
import com.harman.bluetooth.constants.Band;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.EQModel;
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
import jbl.stc.com.manager.LiveManager;
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
    private ProgressBar batteryPb;
    private TextView batteryTv;
    private TextView eqNameTv;
    private TextView deviceNameTv;
    private ImageView deviceImage;
    private CheckBox noiseCancelCb;
    private ImageView ambientAwareIv;
    private LinearLayout batteryLl;
    private AaPopupWindow aaPopupWindow;

    private RelativeLayout eqInfoBarRl;
    private String deviceName;
    private AppImageView otaDownloadIv;
    private NotConnectedPopupWindow notConnectedPopupWindow;
    private TutorialAncDialog tutorialAncDialog;

    private FrameLayout deviceImageFl;
    private HomeHandler homeHandler = new HomeHandler(Looper.getMainLooper());
    private float yDown;
    private float yMove;
    public static boolean isEnter = false;
    private RelativeLayout homeRl;
    private int screenHeight;
    private int screenWidth;

    public TutorialAncDialog getTutorialAncDialog() {
        return tutorialAncDialog;
    }

    private int mConnectStatus = -1;
    private RelativeLayout rootHomeRl;
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
        PreferenceUtils.setInt(JBLConstant.KEY_CONNECT_STATUS, mConnectStatus, HomeActivity.this);
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
        Logger.d(TAG, "onCreate");
        rootHomeRl = (RelativeLayout) findViewById(R.id.relative_layout_home_root);
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        showTutorial();
        generateAAPopupWindow();
        homeRl = (RelativeLayout) findViewById(R.id.relative_layout_home);
        findViewById(R.id.image_view_home_back).setOnClickListener(this);
        deviceImageFl = (FrameLayout) findViewById(R.id.frame_layout_home_device_image);
        deviceImageFl.setOnClickListener(this);
        deviceImage = (ImageView) findViewById(R.id.image_view_home_device_image);
        deviceNameTv = (TextView) findViewById(R.id.text_view_home_device_name);
        eqInfoBarRl = (RelativeLayout) findViewById(R.id.relative_layout_home_eq_info);
        eqInfoBarRl.setVisibility(View.VISIBLE);
        TextView titleEqText = (TextView) findViewById(R.id.titleEqText);
        if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            setEqMenuColor(false);
            eqInfoBarRl.setAlpha((float) 0.5);
        } else {
            setEqMenuColor(true);
            titleEqText.setOnClickListener(this);
            findViewById(R.id.image_view_home_settings).setOnClickListener(this);
            findViewById(R.id.arrowUpImage).setOnClickListener(this);
        }
        eqNameTv = (TextView) findViewById(R.id.text_view_home_eq_name);
        batteryLl = (LinearLayout) findViewById(R.id.linear_layout_home_battery);
        batteryPb = (ProgressBar) findViewById(R.id.progress_bar_battery);
        batteryTv = (TextView) findViewById(R.id.text_view_battery_level);
        otaDownloadIv = (AppImageView) findViewById(R.id.image_view_ota_download);
        otaDownloadIv.setOnClickListener(this);
        noiseCancelCb = (CheckBox) findViewById(R.id.image_view_home_noise_cancel);
        ambientAwareIv = (ImageView) findViewById(R.id.image_view_home_ambient_aware);

        mBlurView = (BlurringView) findViewById(R.id.view_home_blur);

        deviceName = ProductListManager.getInstance().getSelectDevice(mConnectStatus).deviceName;
        Logger.i(TAG, "on create, device name = " + deviceName);
        setViewNoiseCancel(findViewById(R.id.relative_layout_home_noise_cancel), Feature.ENABLE_NOISE_CANCEL);
        setViewAmbientAware(findViewById(R.id.relative_layout_home_ambient_aware), Feature.ENABLE_AMBIENT_AWARE);

        updateDeviceNameAndImage(deviceName, deviceImage, deviceNameTv);
        initEvent();
        setDeviceImageHeight();
        setupEnterAnimations();
        setupExitAnimations();
    }

    private void setViewNoiseCancel(View view, Feature feature) {
        if (deviceName != null && DeviceFeatureMap.isFeatureSupported(deviceName, feature)) {
            view.setVisibility(View.VISIBLE);
            if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
                noiseCancelCb.setOnClickListener(this);
            } else {
                view.setAlpha((float) 0.5);
            }
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setViewAmbientAware(View view, Feature feature) {
        if (DeviceFeatureMap.isFeatureSupported(deviceName, feature)) {
            ambientAwareIv.setOnClickListener(this);
            if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                view.setAlpha((float) 0.5);
            }
            if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                    || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                    || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA)) {
                CustomFontTextView textViewNoiseCancel = (CustomFontTextView) findViewById(R.id.text_view_home_noise_cancle);
                textViewNoiseCancel.setText(R.string.talkthru);
                noiseCancelCb.setOnClickListener(this);
                noiseCancelCb.setBackgroundResource(R.drawable.checkbox_talk_through_selector);
                findViewById(R.id.relative_layout_home_noise_cancel).setVisibility(View.VISIBLE);
                view.setVisibility(View.VISIBLE);
                ambientAwareIv.setBackgroundResource(R.mipmap.aa_icon_non_active);
                ambientAwareIv.setTag("0");
                ambientAwareIv.setOnClickListener(this);
            }
        } else {
            view.setVisibility(View.GONE);
        }
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
        deviceImageFl.getLocationOnScreen(location);
        int cx = location[0];
        int cy = location[1];
        int startRadius = deviceImageFl.getMeasuredHeight() / 2;
        int finalRadius = Math.max(rootHomeRl.getWidth(), rootHomeRl.getHeight());

        Animator anim =
                ViewAnimationUtils.createCircularReveal(rootHomeRl,
                        cx + startRadius,
                        cy + startRadius,
                        startRadius,
                        finalRadius);
        anim.setDuration(500);
        rootHomeRl.setVisibility(View.VISIBLE);
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
        deviceImageFl.getLocationOnScreen(location);
        int cx = location[0];
        int cy = location[1];
        int startRadius = deviceImageFl.getHeight() / 2;
        int initialRadius = (rootHomeRl.getWidth() + rootHomeRl.getHeight()) / 2;
        Animator anim =
                ViewAnimationUtils.createCircularReveal(rootHomeRl,
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
            AnalyticsManager.getInstance().setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
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

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (fr == null || count <= 1) {
            DeviceManager.getInstance(this).setOnRetListener(this);
            LiveManager.getInstance().setOnConnectStatusListener(this);
            LiveManager.getInstance().setOnRetListener(this);
        }

        if ((fr != null) && fr instanceof EqSettingFragment) {
            if (LiveManager.getInstance().isConnected()) {
                CmdDevStatus reqDevStatus = new CmdDevStatus(EnumDeviceStatusType.ALL_STATUS);
                LiveManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, reqDevStatus);
            } else {
                ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
            }
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
                        if (!noiseCancelCb.isChecked()) {
                            noiseCancelCb.setChecked(true);
                            ANCControlManager.getANCManager(this).setANCValue(true);
                        }
                        ANCControlManager.getANCManager(getApplicationContext()).getAmbientLeveling();
                        showAncPopupWindow(findViewById(R.id.relative_layout_home_root));
                    } else if (AppUtils.isNewDevice(deviceName)) {
                        Logger.d(TAG, "tag: new device" + ambientAwareIv.getTag());
                        if (ambientAwareIv.getTag().equals("1")) {
                            ambientAwareIv.setBackground(getResources().getDrawable(R.mipmap.aa_icon_non_active));
                            ambientAwareIv.setTag("0");
                        } else if (ambientAwareIv.getTag().equals("0")) {
                            ambientAwareIv.setBackground(getResources().getDrawable(R.mipmap.aa_icon_active));
                            ambientAwareIv.setTag("1");
                            noiseCancelCb.setChecked(false);
                        }
                        setBleAAComand(noiseCancelCb, ambientAwareIv);
                    }
                }
                break;
            }
            case R.id.arrowUpImage: {
                EqSettingFragment fragment = new EqSettingFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(JBLConstant.KEY_CONNECT_STATUS, mConnectStatus);
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
                createDeviceImageView(HomeActivity.this);
                break;
            }
            case R.id.image_view_home_noise_cancel: {
                Logger.d(TAG, "on click, noise cancel, device name: " + deviceName);
                if (AppUtils.isNewDevice(deviceName)) {
                    if (noiseCancelCb.isChecked()) {
                        Logger.d(TAG, "noise cancel  checked");
                        noiseCancelCb.setChecked(true);
                        if (ambientAwareIv.getTag().equals("1")) {
                            ambientAwareIv.setBackgroundResource(R.mipmap.aa_icon_non_active);
                            ambientAwareIv.setTag("0");
                        }
                    } else {
                        Logger.d(TAG, "noise cancel unchecked");
                        noiseCancelCb.setChecked(false);
                    }
                    setBleAAComand(noiseCancelCb, ambientAwareIv);
                } else {
                    setANC();
                    timeInterval();
                    ANCControlManager.getANCManager(getApplicationContext()).getAmbientLeveling();
                }
                break;
            }
            case R.id.titleEqText: {
                switchEq();
                break;
            }
            case R.id.image_view_ota_download: {
                switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
        }
    }

    public void setBleAAComand(final CheckBox checkBoxNoiseCancel, final ImageView imageViewAmbientAaware) {
        if (LiveManager.getInstance().isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Logger.d(TAG, "tag: new device setBleAAComand" + imageViewAmbientAaware.getTag());
                    CmdAASet cmdAASet = null;
                    if (checkBoxNoiseCancel.isChecked() && imageViewAmbientAaware.getTag().equals("0")) {
                        cmdAASet = new CmdAASet(EnumAAStatus.TALK_THRU);
                    } else if (!checkBoxNoiseCancel.isChecked() && imageViewAmbientAaware.getTag().equals("1")) {
                        cmdAASet = new CmdAASet(EnumAAStatus.AMBIENT_AWARE);
                    } else if (!checkBoxNoiseCancel.isChecked() && imageViewAmbientAaware.getTag().equals("0")) {
                        cmdAASet = new CmdAASet(EnumAAStatus.OFF);
                    }
                    if (cmdAASet != null) {
                        LiveManager.getInstance().reqSetAAMode(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdAASet);
                    }
                }
            }).start();
        }
    }

    private void createDeviceImageView(final Context context) {
        final int h = UiUtils.getDashboardDeviceImageHeight(context);
        float x = (screenWidth - h) / 2;
        float y = UiUtils.dip2px(context, 62) + UiUtils.getDeviceImageMarginTop(context);
        deviceImageFl.setVisibility(View.INVISIBLE);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = 51;
        mWindowLayoutParams.x = (int) x;
        mWindowLayoutParams.y = (int) y;
        mWindowLayoutParams.alpha = 1.0f;
        mWindowLayoutParams.width = screenWidth;
        mWindowLayoutParams.height = screenHeight;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ll_deviceImage = new LinearLayout(context);
        WindowManager.LayoutParams ll_params = new WindowManager.LayoutParams();
        ll_params.gravity = 51;
        ll_params.width = h;
        ll_params.height = h;
        ll_deviceImage.setLayoutParams(ll_params);
        deviceImageView = new ImageView(context);
        deviceImageView.setBackgroundResource(R.drawable.shape_dashboard_device_circle);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = 51;
        params.width = h;
        params.height = h;
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

        ll_deviceImage.clearAnimation();
        deviceImageView.clearAnimation();
        float endY = y + settingDeviceImageMargin_ParentTop - (UiUtils.getDeviceImageMarginTop(context) + UiUtils.dip2px(context, 62) + (h - settingDeviceImageHeight) / 2);
        ObjectAnimator animX = ObjectAnimator.ofFloat(ll_deviceImage, "translationX",
                x, x);
        ObjectAnimator animY = ObjectAnimator.ofFloat(ll_deviceImage, "translationY",
                y, endY);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(deviceImageView, "scaleY",
                1, (float) (settingDeviceImageHeight) / (float) (h));
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(deviceImageView, "scaleX",
                1, (float) (settingDeviceImageHeight) / (float) (h));
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
                deviceImageFl.setVisibility(View.VISIBLE);
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
        LiveManager.getInstance().setOnConnectStatusListener(this);
        LiveManager.getInstance().setOnRetListener(this);
        if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
            getDeviceInfo();
        } else if (mConnectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            if (!PreferenceUtils.getBoolean(PreferenceKeys.SHOW_NC_POP, this)) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_NC_POP, true, this);
                findViewById(R.id.relative_layout_home_root).post(new Runnable() {
                    @Override
                    public void run() {
                        showNotConnectedPopupWindow();
                    }
                });
            }
        }
    }

    private void setDeviceImageHeight() {
        int h = UiUtils.getDashboardDeviceImageHeight(this);
        Logger.d(TAG, "height:" + h);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) deviceImage.getLayoutParams();
        params.height = h;
        params.width = h;
        deviceImage.setLayoutParams(params);
        int marginTop = (int) (h / 2 - h / 2 * Math.sin(45 * 3.14 / 180) - UiUtils.dip2px(this, 35) / 2);
        int marginRight = (int) (h / 2 - h / 2 * Math.cos(45 * 3.14 / 180) - UiUtils.dip2px(this, 35) / 2);
        otaDownloadIv.setTop(marginTop);
        otaDownloadIv.setRight(marginRight);
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) otaDownloadIv.getLayoutParams();
        params1.topMargin = marginTop;
        params1.rightMargin = marginRight;
        otaDownloadIv.setLayoutParams(params1);
    }

    public void showOta(boolean hasUpdate) {
        if (hasUpdate) {
            otaDownloadIv.setVisibility(View.VISIBLE);
        } else {
            otaDownloadIv.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {

        final int distance = UiUtils.dip2px(this, 80);
        final int bottomHeight = UiUtils.dip2px(HomeActivity.this, 70);
        if (mConnectStatus == ConnectStatus.DEVICE_CONNECTED) {
            homeRl.setOnTouchListener(new View.OnTouchListener() {
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
                                    bundle.putInt(JBLConstant.KEY_CONNECT_STATUS, mConnectStatus);
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
                                        bundle.putInt(JBLConstant.KEY_CONNECT_STATUS, mConnectStatus);
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
            if (LiveManager.getInstance().isConnected()) {
                CmdDevStatus reqDevStatus = new CmdDevStatus(EnumDeviceStatusType.ALL_STATUS);
                LiveManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, reqDevStatus);
            } else {
                ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset();
            }
        }
    };

    private void switchEq() {
        String curEqName = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, getString(R.string.off));
        String curEqNameExclusiveOff = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, this, "");
        if (curEqName.equals(getString(R.string.off))) {
            Logger.d(TAG, "switch eq on");
            if (TextUtils.isEmpty(curEqNameExclusiveOff)) {
                List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(this);
                Logger.d(TAG, "switch eq on, eq size:" + eqModels.size());
                if (eqModels.size() < 5) {
                    requestPresetIndex(EnumEqPresetIdx.JAZZ, GraphicEQPreset.Jazz);
                } else {
                    if (LiveManager.getInstance().isConnected()) {
                        LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, EQSettingManager.get().getBleEqSettingFromEqModel(eqModels.get(4)));
                    } else {
                        ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModels.get(4)));
                    }
                }
            } else {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, curEqNameExclusiveOff, this);
                if (curEqNameExclusiveOff.equals(getString(R.string.jazz))) {
                    requestPresetIndex(EnumEqPresetIdx.JAZZ, GraphicEQPreset.Jazz);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.vocal))) {
                    requestPresetIndex(EnumEqPresetIdx.VOCAL, GraphicEQPreset.Vocal);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.bass))) {
                    requestPresetIndex(EnumEqPresetIdx.BASS, GraphicEQPreset.Bass);
                } else {
                    EQModel eqModel = EQSettingManager.get().getEQModelByName(curEqNameExclusiveOff, this);
                    if (LiveManager.getInstance().isConnected()) {
                        LiveManager.getInstance().reqSetEQSettings(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, EQSettingManager.get().getBleEqSettingFromEqModel(eqModel));
                    } else {
                        ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModel));
                    }
                }
            }
        } else {
            Logger.d(TAG, "switch eq off");
            requestPresetIndex(EnumEqPresetIdx.OFF, GraphicEQPreset.Off);
        }
        homeHandler.removeCallbacks(applyRunnable);
        homeHandler.postDelayed(applyRunnable, 800);
    }

    private void requestPresetIndex(EnumEqPresetIdx enumEqPresetIdx, GraphicEQPreset graphicEQPreset) {
        if (LiveManager.getInstance().isConnected()) {
            LiveManager.getInstance().reqSetEQPreset(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdEqPresetSet(enumEqPresetIdx));
        } else {
            ANCControlManager.getANCManager(this).applyPresetWithoutBand(graphicEQPreset);
        }
    }

    public void setANC() {
        if (noiseCancelCb.isChecked()) {
            ANCControlManager.getANCManager(this).setANCValue(true);
        } else {
            ANCControlManager.getANCManager(this).setANCValue(false);
        }
    }

    public void tutorialSetANC(CheckBox checkBoxNoiseCancel) {
        if (checkBoxNoiseCancel.isChecked()) {
            if (LiveManager.getInstance().isConnected()) {
                if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC)) {
                    LiveManager.getInstance().reqSetANC(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdAncSet(EnumAncStatus.OFF));
                }
            } else {
                ANCControlManager.getANCManager(this).setANCValue(false);
            }
        } else {
            if (LiveManager.getInstance().isConnected()) {
                if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_650BTNC)) {
                    LiveManager.getInstance().reqSetANC(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, new CmdAncSet(EnumAncStatus.ON));
                }
            } else {
                ANCControlManager.getANCManager(this).setANCValue(true);
            }
        }
    }

    public void showAncPopupWindow(View view) {
        mBlurView.setBlurredView(homeRl);
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
        aaPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
        aaPopupWindow.setImageViewAmbientAware((ImageView) findViewById(R.id.image_view_home_ambient_aware));
    }

    public void showNotConnectedPopupWindow() {
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
        mBlurView.setBlurredView(homeRl);
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
        notConnectedPopupWindow.showAtLocation(findViewById(R.id.relative_layout_home_root), Gravity.NO_GRAVITY, 0, 0);
    }

    private void getDeviceInfo() {

        if (isFinishing()){
            Logger.d(TAG,"get device info, is finishing");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (LiveManager.getInstance().isConnected()) {
                    getBleDeviceInfo();
                } else {
                    switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                        case NONE:
                            break;
                        case Connected_USBDevice:
                            homeHandler.sendEmptyMessage(MSG_REFLECT_AWARE_BATTERY_STATUS);
                            break;
                        case Connected_BluetoothDevice:
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

    private void updateReflectAwareBatteryStatus(){
        batteryPb.setProgress(100);
        batteryTv.setText(getString(R.string.percent_100));
    }

    private void getBleDeviceInfo() {
        Logger.i(TAG, "get ble device info");
        LiveManager.getInstance().reqDevInfo(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac);
        CmdDevStatus reqDevStatus = new CmdDevStatus(EnumDeviceStatusType.ALL_STATUS);
        LiveManager.getInstance().reqDevStatus(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, reqDevStatus);
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
        LiveManager.getInstance().setOnConnectStatusListener(this);
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
                    Logger.d(TAG, "AA Mode:" + msg.arg1);
                    if (!LiveManager.getInstance().isConnected()) {
                        aaPopupWindow.updateAAUI(msg.arg1, (ImageView) findViewById(R.id.image_view_home_ambient_aware));//AppUtils.levelTransfer(msg.arg1)<---method for new device
                    } else if (deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT) || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)) {
                        updateBleAAUI(msg.arg1);
                    }
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
                    eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq);
                    ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                    String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, getApplicationContext(), null);
                    PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, name, getApplicationContext());
                    Logger.d(TAG, "turnOnEq name:" + name);
                    if (name != null) {
                        eqNameTv.setText(name);
                        if (eqNameTv.getText().length() >= JBLConstant.MAX_MARQUEE_LEN) {
                            eqNameTv.setSelected(true);
                            eqNameTv.setMarqueeRepeatLimit(-1);
                        }
                    } else {
                        eqNameTv.setText(getString(R.string.custom_eq));
                    }
                    break;
                }
                case MSG_CHECK_UPDATE: {
                    startCheckingIfUpdateIsAvailable(HomeActivity.this);
                    registerConnectivity();
                    break;
                }
                case MSG_FIRMWARE_INFO: {
                    ANCControlManager.getANCManager(getApplicationContext()).getFirmwareInfo();
                    break;
                }
                case MSG_GET_DESIGN_EQ: {
                    CmdCurrEq cmdCurrEq = new CmdCurrEq(EnumEqCategory.DESIGN_EQ);
                    LiveManager.getInstance().reqCurrentEQ(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdCurrEq);
                    break;
                }
                case MSG_REFLECT_AWARE_BATTERY_STATUS:{
                    updateReflectAwareBatteryStatus();
                    break;
                }
            }
        }
    }

    private void updateBleAAUI(int aaValue) {
        if (aaValue == 0) {
            noiseCancelCb.setChecked(false);
            ambientAwareIv.setTag("0");
            ambientAwareIv.setBackgroundResource(R.mipmap.aa_icon_non_active);
        } else if (aaValue == 1) {
            noiseCancelCb.setChecked(true);
            ambientAwareIv.setTag("0");
            ambientAwareIv.setBackgroundResource(R.mipmap.aa_icon_non_active);
        } else if (aaValue == 2) {
            noiseCancelCb.setChecked(false);
            ambientAwareIv.setTag("1");
            ambientAwareIv.setBackgroundResource(R.mipmap.aa_icon_active);
        }
        if (tutorialAncDialog != null) {
            tutorialAncDialog.updateBleAAUI(aaValue);
        }
    }

    private void updateANC(boolean onOff) {
        if (noiseCancelCb != null) {
            Logger.d(TAG, "update ANC" + onOff);
            noiseCancelCb.setChecked(onOff);
        }
        if (tutorialAncDialog != null) {
            if (!(deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                    || deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT))) {
                tutorialAncDialog.setChecked(onOff);
            }
        }

    }

    public void setEqMenuColor(boolean onOff) {
        if (eqInfoBarRl != null) {
            if (onOff) {
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq);
            } else {
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            }
        }
    }

    private void updateCurrentEQ(int index) {
        Logger.d(TAG, "eqIndex:" + index);
        switch (index) {
            case 0: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.off), this);
                eqNameTv.setText(getString(R.string.off));
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq_off);
                break;
            }
            case 1: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.jazz), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.jazz), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                eqNameTv.setText(getString(R.string.jazz));
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.vocal), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.vocal), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                eqNameTv.setText(getString(R.string.vocal));
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.bass), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.bass), this);
                ((JBLApplication) getApplication()).globalEqInfo.eqOn = true;
                eqNameTv.setText(getString(R.string.bass));
                eqInfoBarRl.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                if (LiveManager.getInstance().isConnected()) {
                    CmdCurrEq cmdCurrEq = new CmdCurrEq(EnumEqCategory.GRAPHIC_EQ);
                    LiveManager.getInstance().reqCurrentEQ(ProductListManager.getInstance().getSelectDevice(mConnectStatus).mac, cmdCurrEq);
                } else {
                    ANCControlManager.getANCManager(this).getAppGraphicEQPresetBandSettings(GraphicEQPreset.User, 10);
                }
                break;
            }
            default:
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, null);
                eqNameTv.setText(name != null ? name : getString(R.string.off));
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
            batteryPb.setProgress(100);
            batteryTv.setText(getString(R.string.percent_100));
            batteryPb.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_not_charge));
        } else {
            batteryPb.setProgress(value);
            batteryTv.setText(String.format("%s%%", String.valueOf(value)));
            if (value > 0 && value <= 15) {
                batteryPb.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_red_charge));
            } else if (value > 15 && value <= 30) {
                batteryPb.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_orange_charge));
            } else if (value > 30) {
                batteryPb.setProgressDrawable(getResources().getDrawable(R.drawable.horizontal_progress_not_charge));
            }
        }
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
        AppUtils.setModelNumber(JBLApplication.getJBLApplicationContext(), deviceName);
        Logger.d(TAG, "modelName : " + accessoryInfo.getModelNumber());
        updateDeviceNameAndImage(deviceName, deviceImage, deviceNameTv);
        String version = accessoryInfo.getFirmwareRev();
        if (version.length() >= 5) {
            Logger.d(TAG, "currentVersion : " + version);
            PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.APP_VERSION, version, this);
        }
        String hardVersion = accessoryInfo.getHardwareRev();
        if (hardVersion.length() >= 5) {
            Logger.d(TAG, "hardVersion : " + hardVersion);
            PreferenceUtils.setString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, hardVersion, this);
        }
        AnalyticsManager.getInstance().reportFirmwareVersion(hardVersion);
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

    private void parseCustomEQ(int[] eqArray) {
        if (eqArray.length == 10) {
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
        if (LiveManager.getInstance().isConnected()) {
            homeHandler.sendEmptyMessageDelayed(MSG_GET_DESIGN_EQ, 1000);
        }
    }

    private void parseBleCustomEq(RetCurrentEQ retCurrentEQ) {
        Band[] bands = retCurrentEQ.bands;

        if (bands.length == 10) {
            int[] eqArray = new int[10];
            for (int i = 0; i < bands.length; i++) {
                Logger.d(TAG, "reCurrentEQ GraphicEq fc:" + i + bands[i].fc + "gain:" + bands[i].gain);
                eqArray[i] = (int) bands[i].gain;
            }
            parseCustomEQ(eqArray);
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
                    PreferenceUtils.setString(AppUtils.getModelNumber(JBLApplication.getJBLApplicationContext()), PreferenceKeys.APP_VERSION, (String) objects[0], this);
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
                    if (eqBytes.length == 48) {
                        Logger.d(TAG, "EqBand value1 = " + Arrays.toString(eqBytes));
                        int[] eqArray = new int[10];
                        eqArray[0] = eqBytes[8];
                        eqArray[1] = eqBytes[12];
                        eqArray[2] = eqBytes[16];
                        eqArray[3] = eqBytes[20];
                        eqArray[4] = eqBytes[24];
                        eqArray[5] = eqBytes[28];
                        eqArray[6] = eqBytes[32];
                        eqArray[7] = eqBytes[36];
                        eqArray[8] = eqBytes[40];
                        eqArray[9] = eqBytes[44];
                        parseCustomEQ(eqArray);
                    }

//                    homeHandler.sendEmptyMessage(MSG_FIRMWARE_INFO);
                } else {
                    RetCurrentEQ retCurrentEQ = (RetCurrentEQ) objects[1];
                    if (retCurrentEQ != null) {
                        Logger.d(TAG, "on receive, retCurrentEQ:" + retCurrentEQ.enumEqCategory);
                        Band[] bands = retCurrentEQ.bands;
                        for (int i = 0; i < bands.length; i++) {
                            Logger.d(TAG, "on receive, retCurrentEQ:" + i + ":gain:" + bands[i].gain + ";q:" + i + bands[i].q);
                        }
                        if (retCurrentEQ.enumEqCategory == EnumEqCategory.DESIGN_EQ) {
                            //save the designEq
                            List<RetCurrentEQ> retCurrentEQS = new ArrayList<>();
                            retCurrentEQS.add(retCurrentEQ);
                            SharePreferenceUtil.saveCurrentEqSet(HomeActivity.this, retCurrentEQS, SharePreferenceUtil.BLE_DESIGN_EQ);
                        } else if (retCurrentEQ.enumEqCategory == EnumEqCategory.GRAPHIC_EQ) {
                            //parse the graficEq
                            parseBleCustomEq(retCurrentEQ);
                        }
                    } else {
                        Logger.d(TAG, "retCurrentEQ is null");
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
                updateDeviceNameAndImage(deviceName, deviceImage, deviceNameTv);
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
                    noiseCancelCb.setChecked(true);
                } else if (ancValue == 0) {
                    noiseCancelCb.setChecked(false);
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


    private final static int MSG_GET_DESIGN_EQ = 7;
    private final static int MSG_REFLECT_AWARE_BATTERY_STATUS = 9;

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
