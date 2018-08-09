package jbl.stc.com.activity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import jbl.stc.com.R;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.CreateEqTipsDialog;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.EqCustomFragment;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.listener.ConnectListener;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.AaPopupWindow;

import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.view.AppImageView;
import jbl.stc.com.view.BlurringView;
import jbl.stc.com.view.NotConnectedPopupWindow;
import jbl.stc.com.view.SaPopupWindow;

import static java.lang.Integer.valueOf;


public class HomeActivity extends BaseActivity implements View.OnClickListener , ConnectListener{
    public static final String TAG = HomeActivity.class.getSimpleName();
    private BlurringView mBlurView;

    private HomeHandler homeHandler = new HomeHandler(Looper.getMainLooper());
    private final static int MSG_ANC = 0;
    private final static int MSG_BATTERY = 1;
    private final static int MSG_FIRMWARE_VERSION = 2;
    private final static int MSG_CURRENT_PRESET = 3;
    private final static int MSG_RAW_STEP = 4;
    private final static int MSG_AMBIENT_LEVEL = 5;
    private final static int MSG_READ_BATTERY_INTERVAL = 6;

    private final static int MSG_AA_LEFT = 32;
    private final static int MSG_AA_RIGHT = 33;

    private final static int MSG_SEND_CMD_GET_FIRMWARE = 7;
    private final static int MSG_UPDATE_CUSTOME_EQ = 8;

    private final long timeInterval = 30 * 1000L, pollingTime = 1000L;
    private ProgressBar progressBarBattery;
    private TextView textViewBattery;
    private TextView textViewCurrentEQ;
    private TextView textViewDeviceName;
    private ImageView imageViewDevice;
    private CheckBox checkBoxNoiseCancel;
    private LinearLayout linearLayoutBattery;
    private LightX lightX;
    private AaPopupWindow aaPopupWindow;
    private SaPopupWindow saPopupwindow;

    private FrameLayout relative_layout_home_eq_info;
    private String deviceName;
    private SaPopupWindow.OnSmartAmbientStatusReceivedListener mSaListener;
    private View bluredView;
    private MyDevice myDevice;
    private TextView titleEqText;
    private AppImageView image_view_ota_download;
    private NotConnectedPopupWindow notConnectedPopupWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        addActivity(this);
        Logger.d(TAG, "onCreateView");
        Bundle b = getIntent().getBundleExtra("bundle");
        if (b != null) {
            myDevice = b.getParcelable(JBLConstant.KEY_MY_DEVICE);
        }
        lightX = AvneraManager.getAvenraManager(this).getLightX();
        generateAAPopupWindow();
        generateSaPopupWindow();
        findViewById(R.id.image_view_home_settings).setOnClickListener(this);
        findViewById(R.id.image_view_home_back).setOnClickListener(this);
        textViewDeviceName = findViewById(R.id.text_view_home_device_name);

        imageViewDevice = findViewById(R.id.image_view_home_device_image);
        bluredView = findViewById(R.id.relative_Layout_home);
        relative_layout_home_eq_info = (FrameLayout) findViewById(R.id.relative_layout_home_eq_info);
        relative_layout_home_eq_info.setVisibility(View.VISIBLE);
        titleEqText = (TextView) findViewById(R.id.titleEqText);
        titleEqText.setOnClickListener(this);
        if (myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            setEqMenuColor(false);
            relative_layout_home_eq_info.setAlpha((float) 0.5);
        } else {
            setEqMenuColor(true);
            relative_layout_home_eq_info.setOnClickListener(this);
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
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_NOISE_CANCEL)) {
            linearLayoutNoiseCanceling.setVisibility(View.GONE);
        } else {
            linearLayoutNoiseCanceling.setVisibility(View.VISIBLE);
            checkBoxNoiseCancel = findViewById(R.id.image_view_home_noise_cancel);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                checkBoxNoiseCancel.setOnClickListener(this);
            } else {
                linearLayoutNoiseCanceling.setAlpha((float) 0.5);
            }
        }

        RelativeLayout linearLayoutAmbientAware = findViewById(R.id.relative_layout_home_ambient_aware);
        if (!DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_AMBIENT_AWARE)) {
            linearLayoutAmbientAware.setVisibility(View.GONE);
        } else {
            findViewById(R.id.image_view_home_ambient_aware).setOnClickListener(this);
            if (myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                linearLayoutAmbientAware.setAlpha((float) 0.5);
            }
            if (myDevice.deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_400BT)
                    || myDevice.deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_500BT)
                    || myDevice.deviceName.equalsIgnoreCase(JBLConstant.DEVICE_LIVE_FREE_GA)) {
                TextView textViewAmbientAware = findViewById(R.id.text_view_home_ambient_aware);
                textViewAmbientAware.setText(R.string.smart_ambient);
            }
        }
        if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            getRawSteps();
            if (lightX != null) {
                Logger.e(TAG, "readBootImageType");
                lightX.readBootImageType();
            }
        }
        deviceName = myDevice.deviceName;
//        deviceName = PreferenceUtils.getString(PreferenceKeys.MODEL, this, "");
        updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
        initEvent();
        setDeviceImageHeight();
    }

    private void setDeviceImageHeight() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenheigth = dm.heightPixels;
        int screenwidth = dm.widthPixels;
        int statusHeight = UiUtils.getStatusHeight(this);
        int height = (int) (screenheigth - UiUtils.dip2px(this, 200) - statusHeight) / 2;
        Logger.d(TAG, "HomeFrag statusHeigth:" + statusHeight + "screenheight:" + screenheigth + "200dp:" + UiUtils.dip2px(this, 200) + "height:" + height);
        if (height > UiUtils.dip2px(this, 240)) {
            height = UiUtils.dip2px(this, 240);
            Logger.d(TAG, "height:" + "240dp");
        }
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

    private void initEvent() {

        final GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getY() - e2.getY() > 25 && Math.abs(velocityY) > 25) {
                    switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                }
                return false;
            }
        };
        final GestureDetector gestureDetector = new GestureDetector(gestureListener);
        relative_layout_home_eq_info.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);

            }

        });


    }

    public MyDevice getMyDeviceInHome() {
        return myDevice;
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
        ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext())
                .getRawStepsByCmd(AvneraManager.getAvenraManager(JBLApplication.getJBLApplicationContext()).getLightX());//get raw steps count of connected device
    }

    private void generateAAPopupWindow() {
        aaPopupWindow = new AaPopupWindow(this, lightX);
        aaPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //dismiss blur view
                aaPopupWindow.setAAOff();
                if (mBlurView != null) {
                    mBlurView.setVisibility(View.GONE);
                }
                if (DashboardActivity.getDashboardActivity().tutorialAncDialog != null && DashboardActivity.getDashboardActivity().tutorialAncDialog.isShowing()) {
                    DashboardActivity.getDashboardActivity().tutorialAncDialog.setTextViewTips(R.string.tutorial_tips_one);
                    DashboardActivity.getDashboardActivity().tutorialAncDialog.showEqInfo();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance(this).setScreenName(AnalyticsManager.SCREEN_CONTROL_PANEL);
        Logger.d(TAG, "onResume " + DeviceConnectionManager.getInstance().getCurrentDevice());
        doResume();
    }

    private void doResume(){
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
        if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
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
                    ANCControlManager.getANCManager(this).getBatterLeverl(lightX);
                    homeHandler.sendEmptyMessageDelayed(MSG_READ_BATTERY_INTERVAL, timeInterval);
                    break;
            }
            getDeviceInfo();
        } else if (myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
            if (!PreferenceUtils.getBoolean(PreferenceKeys.SHOW_NC_POP, this)) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_NC_POP, true, this);
                showNCPopupWindow();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if ((fr != null)&& fr instanceof EqSettingFragment) {
            doResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        homeHandler.removeMessages(MSG_READ_BATTERY_INTERVAL);
        if (notConnectedPopupWindow != null) {
            notConnectedPopupWindow.dismiss();
        }
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
                if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                    if (AppUtils.isOldDevice(deviceName)) {
                        if (!checkBoxNoiseCancel.isChecked()){
                            checkBoxNoiseCancel.setChecked(true);
                        }
                        setANC();
                        showAncPopupWindow();
                    } else if (AppUtils.isNewDevice(deviceName)) {
                        showSaPopupWindow();
                    }
                }
                break;
            }
            case R.id.relative_layout_home_eq_info: {
                switchFragment(new EqSettingFragment(), JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            }
            case R.id.image_view_home_back: {
                this.onBackPressed();
                break;
            }
            case R.id.image_view_home_settings: {
                SettingsFragment settingsFragment = new SettingsFragment();
//                Bundle bundle = new Bundle();
//                bundle.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
//                settingsFragment.setArguments(bundle);
                switchFragment(settingsFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
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

    Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {
            ANCControlManager.getANCManager(getApplicationContext()).getCurrentPreset(lightX);
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
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz, lightX);
                } else {
                    ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModels.get(4)), lightX);
                }
            } else {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, curEqNameExclusiveOff, this);
                if (curEqNameExclusiveOff.equals(getString(R.string.jazz))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Jazz, lightX);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.vocal))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Vocal, lightX);
                } else if (curEqNameExclusiveOff.equals(getString(R.string.bass))) {
                    ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Bass, lightX);
                } else {
                    EQModel eqModel = EQSettingManager.get().getEQModelByName(curEqNameExclusiveOff, this);
                    ANCControlManager.getANCManager(this).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(eqModel), lightX);
                }
            }
        } else {
            //turn off the eq
            Logger.d(TAG, "turn off the eq");
            ANCControlManager.getANCManager(this).applyPresetWithoutBand(GraphicEQPreset.Off, lightX);
        }
        homeHandler.removeCallbacks(applyRunnable);
        homeHandler.postDelayed(applyRunnable, 800);
    }

    public void setANC() {
        if (checkBoxNoiseCancel.isChecked()) {
            ANCControlManager.getANCManager(this).setANCValue(lightX, true);
        } else {
            ANCControlManager.getANCManager(this).setANCValue(lightX, false);
        }
    }

    private void setOnSmartAmbientStatusReceivedListener(SaPopupWindow.OnSmartAmbientStatusReceivedListener listener) {
        this.mSaListener = listener;
    }

    private void showSaPopupWindow() {
        showSaPopupWindow( null);
    }

    public void showSaPopupWindow(SaPopupWindow.OnSmartAmbientStatusReceivedListener listener) {
        mBlurView.setBlurredView(bluredView);
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
        saPopupwindow.showAtLocation(findViewById(R.id.relative_layout_home_fragment), Gravity.NO_GRAVITY, 0, 0);
    }

    private SaPopupWindow.OnSmartAmbientStatusReceivedListener nativeSaListener = new SaPopupWindow.OnSmartAmbientStatusReceivedListener() {
        @Override
        public void onSaStatusReceived(boolean isDaEnable, boolean isTtEnable) {
            if (mSaListener != null) {
                mSaListener.onSaStatusReceived(isDaEnable, isTtEnable);
            }
        }
    };

    public void showAncPopupWindow() {
//        if (mBlurView.getBackground() == null) {
//            Bitmap image = BlurBuilder.blur(view);
//            mBlurView.setBackground(new BitmapDrawable(this.getResources(), image));
//        }
        mBlurView.setBlurredView(bluredView);
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
        aaPopupWindow.showAtLocation(findViewById(R.id.relative_layout_home_fragment), Gravity.NO_GRAVITY, 0, 0);

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
        mBlurView.setBlurredView(bluredView);
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
        notConnectedPopupWindow.showAtLocation(findViewById(R.id.relative_layout_home_fragment), Gravity.NO_GRAVITY, 0, 0);
    }

    private void getDeviceInfo() {

        ANCControlManager.getANCManager(this).getANCValue(lightX);
        updateFirmwareVersion();
        ANCControlManager.getANCManager(this).getCurrentPreset(lightX);
        ANCControlManager.getANCManager(this).getFirmwareInfo(lightX);
        if (lightX != null) {
            Logger.d(TAG, "getDeviceInfo");
            lightX.readConfigModelNumber();
            lightX.readConfigProductName();
            homeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lightX.readBootVersionFileResource();
                }
            },200);
        }
    }

    private void getAAValue() {
        ANCControlManager.getANCManager(this).getAmbientLeveling(lightX);
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
                    homeHandler.removeMessages(MSG_READ_BATTERY_INTERVAL);
                    ANCControlManager.getANCManager(getApplicationContext()).getBatterLeverl(lightX);
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
                case MSG_SEND_CMD_GET_FIRMWARE: {
                    ANCControlManager.getANCManager(getApplicationContext()).getFirmwareVersion(lightX);
                    break;
                }
                case MSG_UPDATE_CUSTOME_EQ: {
                    relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                    ((JBLApplication)getApplication()).deviceInfo.eqOn = true;
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
            }
        }
    }

    private void updateANC(boolean onOff) {
        if (checkBoxNoiseCancel != null)
            checkBoxNoiseCancel.setChecked(onOff);
        if (DashboardActivity.getDashboardActivity().tutorialAncDialog != null) {
            DashboardActivity.getDashboardActivity().tutorialAncDialog.setChecked(onOff);
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
        if (this == null) {
            return;
        }
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
                ((JBLApplication)getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.jazz));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 2: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.vocal), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.vocal), this);
                ((JBLApplication)getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.vocal));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 3: {
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, getString(R.string.bass), this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, getString(R.string.bass), this);
                ((JBLApplication)getApplication()).deviceInfo.eqOn = true;
                textViewCurrentEQ.setText(getString(R.string.bass));
                relative_layout_home_eq_info.setBackgroundResource(R.drawable.shape_gradient_eq);
                break;
            }
            case 4: {
                //ANCControlManager.getANCManager(this).getAppGraphicEQBand(GraphicEQPreset.User, lightX);
                ANCControlManager.getANCManager(this).getAppGraphicEQPresetBandSettings(lightX, GraphicEQPreset.User, 10);
                break;
            }
            default:
                String name = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, this, null);
                textViewCurrentEQ.setText(name != null ? name : getString(R.string.off));
                break;
        }
        if (DashboardActivity.getDashboardActivity().tutorialAncDialog != null) {
            DashboardActivity.getDashboardActivity().tutorialAncDialog.updateCurrentEQ(index);
        }
    }

    private void updateBattery(int value) {
        Logger.d(TAG, "battery value = " + value);
        if (this == null) {
            return;
        }
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
        audioManager am = AvneraManager.getAvenraManager(this).getAudioManager();
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
        startCheckingIfUpdateIsAvailable(HomeActivity.this);
        registerConnectivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishActivity(this);
        unregisterNetworkReceiverSafely();
    }

    private NetworkChangeReceiver networkChangeReceiver;
    private void registerConnectivity() {
        if (this == null)
            return;
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        Logger.i(TAG,"registerConnectivity");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(networkChangeReceiver, intentFilter);
        mReceiverTag = true;
    }

    private boolean mReceiverTag = false;
    private void unregisterNetworkReceiverSafely() {
        try {
            if (mReceiverTag) {
                mReceiverTag = false;
                Logger.i(TAG,"unregisterNetworkReceiverSafely");
                this.unregisterReceiver(networkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(TAG,"onReceive");
            if (isFinishing()){
                Logger.d(TAG,"NetworkChangeReceiver, activity is finishing, return");
                return;
            }
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    startCheckingIfUpdateIsAvailable(HomeActivity.this);
                }else{
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
        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        if (values.size() <= 0) {
            Logger.d(TAG, "return, values size is " + values.size());
            return;
        }
        switch (command) {
            case AmCmds.CMD_ANC: {
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
                sendMessageTo(MSG_AMBIENT_LEVEL, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_RawSteps: {
                sendMessageTo(MSG_RAW_STEP, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_BatteryLevel: {
                String bl = values.iterator().next().getValue().toString();
                //batteryValue = valueOf(bl);
                sendMessageTo(MSG_BATTERY, bl);
                break;
            }
            case AmCmds.CMD_Geq_Current_Preset: {
                sendMessageTo(MSG_CURRENT_PRESET, values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_GrEqBandGains: {
                Logger.d(TAG, "EqBand command aaaa=" + command + ",values=" + values + ",status=" + status);
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
                sendMessageTo(MSG_FIRMWARE_VERSION, null);
                break;
            }
            case AmCmds.CMD_FWInfo: {
                FirmwareUtil.currentFirmware = Integer.valueOf(values.get(3).getValue().toString());
                Logger.d(TAG, "FirmwareUtil.currentFirmware =" + FirmwareUtil.currentFirmware);
                break;
            }
            case AmCmds.CMD_RawLeft:
                Logger.d(TAG, "CMD_RawLeft =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_LEFT, values.iterator().next().getValue().toString());
                break;
            case AmCmds.CMD_RawRight:
                Logger.d(TAG, "CMD_RawRight =" + values.iterator().next().getValue().toString());
                sendMessageTo(MSG_AA_RIGHT, values.iterator().next().getValue().toString());
                break;
            case AmCmds.CMD_GraphicEqPresetBandSettings: {
                Logger.d(TAG, "EqBand command aaaa=" + command + ",values=" + values + ",status=" + status);
                /*if (values!=null&&values.size()>0){
                    for (int i=0;i<values.size();i++){
                        Logger.d(TAG,"EqBand name = "+ values.get(i).getName());
                        byte[] v = (byte[]) (values.get(i).getValue());
                        Logger.d(TAG,"EqBand value = "+ Arrays.toString(v));
                    }
                }*/
                if (values != null) {
                    byte[] v = (byte[]) (values.iterator().next().getValue());
                    parseCustomeEQ(v);
                }

                break;
            }

        }

    }

    private void parseCustomeEQ(byte[] v) {
        if (this == null) {
            return;
        }
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
                        sendMessageTo(MSG_UPDATE_CUSTOME_EQ, null);
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
                        sendMessageTo(MSG_UPDATE_CUSTOME_EQ, null);
                        Logger.d(TAG, "Have the same EQ:" + eqModel.eqName);
                        break;
                    }
                }
                if (!isHave) {
                    Logger.d(TAG, "create a new EQ");
                    EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                    EQSettingManager.get().addCustomEQ(eqModel, this);
                    PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                    sendMessageTo(MSG_UPDATE_CUSTOME_EQ, null);
                }

            } else {
                Logger.d(TAG, "create a new EQ");
                EQModel eqModel = EQSettingManager.get().getCustomeEQModelFromValues(eqArray, eqName);
                EQSettingManager.get().addCustomEQ(eqModel, this);
                PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, eqModel.eqName, this);
                sendMessageTo(MSG_UPDATE_CUSTOME_EQ, null);
            }

        }
    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {
        super.receivedPushNotification(action, command, values, status);
        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
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
                    Logger.i(TAG,"aaPopupWindow is null");
                    return;
                }
                aaPopupWindow.updateAAUI(AppUtils.levelTransfer(Integer.valueOf(values.iterator().next().getValue().toString())));//new devices
            }
            break;

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
                    int intValue = com.avnera.smartdigitalheadset.Utility.getInt(var4, 0);
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
                    int rawSteps = com.avnera.smartdigitalheadset.Utility.getInt(var4, 0) - 1;
                    Logger.d(TAG, "received raw steps call back rawSteps = " + rawSteps);
                    ANCControlManager.getANCManager(JBLApplication.getJBLApplicationContext()).setRawSteps(rawSteps);

                    break;
                case AppGraphicEQCurrentPreset:
                    long currentPreset = Utility.getUnsignedInt(var4, 0);
                    Logger.d(TAG, "lightXAppReadResult"+ command + " is " + currentPreset);
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
                    Logger.d(TAG, "AppCurrVersion = " + major + "." + minor + "." + revision+",modelNumber"+AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()));
                    PreferenceUtils.setString(AppUtils.getModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext()), PreferenceKeys.APP_VERSION, major + "." + minor + "." + revision, this);
                    break;

                case AppGraphicEQPresetBandSettings: {
                    Logger.d(TAG, "Eq band:" + Arrays.toString(var4));
                    int preset = Utility.getInt(var4, 0);
                    int numBands = Utility.getInt(var4, 4);
                    parseCustomeEQ(var4);
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
        if (isFinishing()){
            Logger.d(TAG,"lightXReadBootResult, activity is finishing, return");
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
                    startCheckingIfUpdateIsAvailable(HomeActivity.this); /** Now start checking for update to show red bubble on setting icon*/
                    registerConnectivity();
                }
                break;
            }
        } else {
            startCheckingIfUpdateIsAvailable(HomeActivity.this);
            registerConnectivity();
        }
    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command command, byte[] var4) {
        super.lightXAppReceivedPush(var1, command, var4);
        Logger.d(TAG, "lightXAppReceivedPush command is " + command);
        switch (command) {
            case AppPushANCEnable:
                ANCControlManager.getANCManager(this).getANCValue(lightX);
                break;
            case AppPushANCAwarenessPreset: {
                ANCControlManager.getANCManager(this).getAmbientLeveling(lightX);
            }
            break;
        }
    }

    @Override
    public void lightXReadConfigResult(LightX var1, Command command, boolean success, String var4) {
        super.lightXReadConfigResult(var1, command, success, var4);
        if (this == null){
            return;
        }
        Logger.d(TAG, "lightXReadConfigResult command = "+command);
        if (success) {
            switch (command) {
                case ConfigProductName:
                    PreferenceUtils.setString(PreferenceKeys.PRODUCT, var4, this);
                    break;
                case ConfigModelNumber:
                    deviceName = var4;
                    Logger.d(TAG, "lightXReadConfigResult deviceName = "+deviceName);
                    AppUtils.setModelNumber(DashboardActivity.getDashboardActivity().getApplicationContext(), deviceName);
                    updateDeviceNameAndImage(deviceName, imageViewDevice, textViewDeviceName);
                    homeHandler.sendEmptyMessageDelayed(MSG_SEND_CMD_GET_FIRMWARE, 200);
                    switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                        case Connected_USBDevice:
                            break;
                        case Connected_BluetoothDevice:
                            break;
                    }
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
