package jbl.stc.com.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.LegalLandingDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.fragment.InfoFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.fragment.TutorialFragment;
import jbl.stc.com.listener.DismissListener;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.InsertPredefinePreset;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener,OnDownloadedListener {
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private JblCircleView jblCircleView;
    private static DashboardActivity dashboardActivity;
    private RelativeLayout relativeLayoutDiscovery;
    private RelativeLayout relativeLayoutAnimation;
    private LinearLayout linearLayoutTips;
    private TextView textViewTryAgain;
    private final static int SHOW_UN_FOUND_TIPS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_SHOW_OTA_FRAGMENT = 3;
    private final static int REQUEST_CODE = 0;

    private DashboardHandler dashboardHandler = new DashboardHandler();

    private CheckUpdateAvailable checkUpdateAvailable;

    public static boolean isUpdatingFirmware = false;
    public static CopyOnWriteArrayList<FirmwareModel> mFwlist = new CopyOnWriteArrayList<>();

    private boolean mIsConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        setContentView(R.layout.activity_dashboard);



        registerReceiver(mBtReceiver, makeFilter());
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        dashboardActivity = this;
        initView();
        startCircle();
        dashboardHandler.sendEmptyMessageDelayed(SHOW_UN_FOUND_TIPS,5000);
        //load the presetEQ
        InsertPredefinePreset insertPredefinePreset = new InsertPredefinePreset();
        insertPredefinePreset.executeOnExecutor(InsertPredefinePreset.THREAD_POOL_EXECUTOR, this);
    }

    private void checkBluetooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter!=null) {
            Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if (bluetoothAdapter.isEnabled()) {
                if (fr == null) {
                    Logger.d(TAG, "fr is null");
                    return;
                }
                if (fr instanceof TurnOnBtTipsFragment) {
                    removeAllFragment();
                }
            } else {
                if (fr == null){
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }else if (!(fr instanceof  TurnOnBtTipsFragment)) {
                    Logger.i(TAG, "checkBluetooth open TurnOnBtTipsFragment");
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }
            }
        }
    }


    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null){
                Logger.i(TAG,"intent or its action is null");
                return;
            }
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_ON: {
                            if (fr == null) {
                                Logger.d(TAG, "fr is null");
                                return;
                            }
                            if (fr instanceof TurnOnBtTipsFragment) {
                                removeAllFragment();
                            }
                            break;
                        }
                        default:{
                            Logger.i(TAG,"open TurnOnBtTipsFragment");
                            if (fr == null){
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            }else if (!(fr instanceof  TurnOnBtTipsFragment)) {
                                Logger.i(TAG, "checkBluetooth open TurnOnBtTipsFragment");
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            }
                            break;
                        }
                    }
            }
        }
    };


    private void initView() {
        relativeLayoutDiscovery = findViewById(R.id.relative_layout_discovery);
        relativeLayoutAnimation = findViewById(R.id.relative_layout_discovery_animation);
        linearLayoutTips = findViewById(R.id.linear_layout_discovery_tips);
        linearLayoutTips.setVisibility(View.GONE);

        findViewById(R.id.image_view_discovery_menu_info).setOnClickListener(this);
        textViewTryAgain = findViewById(R.id.text_view_discovery_try_again);
        textViewTryAgain.setOnClickListener(this);

        TextView textViewAdviceOne = findViewById(R.id.text_view_discovery_advice_one);
        SpannableString spannableString = new SpannableString(getString(R.string.advice_one));
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View arg0) {

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(android.R.color.white));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
                ds.clearShadowLayer();
            }

        }, 35, 57, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewAdviceOne.setText(spannableString);
        textViewAdviceOne.setMovementMethod(LinkMovementMethod.getInstance());
        TextView textViewAdviceThree = findViewById(R.id.text_view_discovery_advice_three);

        SpannableString spannableStringThree = new SpannableString(getString(R.string.advice_three));
        spannableStringThree.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View arg0) {

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(android.R.color.white));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
                ds.clearShadowLayer();
            }

        }, 66, 92, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewAdviceThree.setText(spannableStringThree);
        textViewAdviceThree.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        boolean legalPersist = PreferenceUtils.getBoolean(PreferenceKeys.LEGAL_PERSIST,this);
        if (!legalPersist){
            LegalLandingDialog legalLandingDialog = (LegalLandingDialog)this.getSupportFragmentManager().findFragmentByTag(LegalLandingDialog.Companion.getTAG());
            if (legalLandingDialog != null && legalLandingDialog.getDialog() != null)
                return;
            legalLandingDialog = new LegalLandingDialog();
            legalLandingDialog.show(this.getSupportFragmentManager(), LegalLandingDialog.Companion.getTAG());
            legalLandingDialog.setOnDismissListener(new DismissListener() {
                @Override
                public void onDismiss(int reason) {
                    if (mIsConnected) {
                        PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_PERSIST, true, getApplicationContext());
                        switchFragment(new TutorialFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                    }
                }
            });
        }
        checkBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        stopCircle();
        unregisterReceiver(mBtReceiver);
        super.onDestroy();
    }

    @Override
    public void connectDeviceStatus(boolean isConnected) {
        super.connectDeviceStatus(isConnected);
        Log.d(TAG, " connectDeviceStatus isConnected = " + isConnected);

        mIsConnected = isConnected;
        if (isConnected) {
            dashboardHandler.removeMessages(SHOW_UN_FOUND_TIPS);
            if (!isUpdatingFirmware) {
                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 200);
            }else{
                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_OTA_FRAGMENT,200);
            }
        }else{
            dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_discovery_menu_info:{
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }else if (!(fr instanceof  InfoFragment)) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }
                break;
            }
            case R.id.text_view_discovery_try_again:{
                relativeLayoutDiscovery.setVisibility(View.VISIBLE);
                relativeLayoutAnimation.setVisibility(View.VISIBLE);
                linearLayoutTips.setVisibility(View.GONE);
                startCircle();
                dashboardHandler.sendEmptyMessageDelayed(SHOW_UN_FOUND_TIPS,5000);
                break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (isConnected && backStackEntryCount > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (isConnected) {
                AppUtils.hideFromForeground(this);
            } else {
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr != null) {
                    Logger.d(TAG, "onBackStackChanged " + fr.getClass().getSimpleName());
                }
                if (fr instanceof InfoFragment){
                    super.onBackPressed();
                }else {
                    finish();
                }
            }
        }
    }

    private void startCircle(){
        if (jblCircleView == null) {
            jblCircleView = findViewById(R.id.jbl_circle_view_dashboard);
            jblCircleView.setVisibility(View.VISIBLE);
            jblCircleView.circle();
        }
    }

    private void stopCircle(){
        if (jblCircleView != null) {
            jblCircleView.stop();
            jblCircleView.setVisibility(View.GONE);
            jblCircleView = null;
        }
    }

    public static DashboardActivity getDashboardActivity() {
        return dashboardActivity;
    }

    private class DashboardHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_UN_FOUND_TIPS: {
                    Log.i(TAG,"show tips");
                    relativeLayoutDiscovery.setVisibility(View.VISIBLE);
                    relativeLayoutAnimation.setVisibility(View.GONE);
                    linearLayoutTips.setVisibility(View.VISIBLE);
                    stopCircle();
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT:{
                    Log.i(TAG,"show homeFragment");
                    relativeLayoutDiscovery.setVisibility(View.GONE);

                    boolean isShowTutorialManyTimes = false;//PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME,getApplicationContext());
                    if (!isShowTutorialManyTimes){
                        PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if (fr == null) {
                            switchFragment(new TutorialFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        } else if (!(fr instanceof HomeFragment)) {
                            switchFragment(new TutorialFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        }
                    }else {
                        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if (fr == null) {
                            switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        } else if (!(fr instanceof HomeFragment)) {
                            switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        }
                    }
                    stopCircle();
                    break;
                }
                case MSG_SHOW_DISCOVERY:{
                    Log.i(TAG,"show discovery page");
                    relativeLayoutDiscovery.setVisibility(View.VISIBLE);
                    relativeLayoutAnimation.setVisibility(View.VISIBLE);
                    linearLayoutTips.setVisibility(View.GONE);
                    removeAllFragment();
                    startCircle();
                    break;
                }
                case MSG_SHOW_OTA_FRAGMENT:{
                    Log.i(TAG,"show OTAFragment");
                    relativeLayoutDiscovery.setVisibility(View.GONE);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }else if (!(fr instanceof  OTAFragment)) {
                        switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                    stopCircle();
                    break;
                }
            }
        }
    }

    public void setIsUpdateAvailable(boolean isUpdateAvailable) {

    }

    public void startCheckingIfUpdateIsAvailable() {
        Log.d(TAG, "startCheckingIfUpdateIsAvailable isConnectionAvailable=" + FirmwareUtil.isConnectionAvailable(this));
        String srcSavedVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, this, "");
        String currentVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.APP_VERSION, this, "");
        if (FirmwareUtil.isConnectionAvailable(this) && !TextUtils.isEmpty(srcSavedVersion) && !TextUtils.isEmpty(currentVersion)) {
            Log.d(TAG, "checkUpdateAvailable = " + checkUpdateAvailable);
            if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING) {
                Log.d(TAG, "CheckUpdateAvailable is running so return");
                return;
            }
            Log.d(TAG, "CheckUpdateAvailable.start()");
            checkUpdateAvailable = CheckUpdateAvailable.start(this, this, this, OTAUtil.getURL(this), srcSavedVersion, currentVersion);
        }
    }


    @Override
    public void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwlist) throws FileNotFoundException {

    }

    @Override
    public void onFailedDownload() {

    }

    @Override
    public void onFailedToCheckUpdate() {

    }

    @Override
    public void onUpgradeUpdate(String liveVersion, String title) {

    }

    public class LinkStyleSpan extends StyleSpan {

        public LinkStyleSpan(int style) {
            super(style);
        }

        @Override
        public int describeContents() {
            return super.describeContents();
        }

        @Override
        public int getSpanTypeId() {
            return super.getSpanTypeId();
        }

        @Override
        public int getStyle() {
            return super.getStyle();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setFakeBoldText(true);
            ds.setUnderlineText(true);
            super.updateDrawState(ds);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            paint.setFakeBoldText(true);
            paint.setUnderlineText(true);
            super.updateMeasureState(paint);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
        }
    }
}
