package jbl.stc.com.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;


import java.io.FileNotFoundException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.ConnectedBeforeFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.fragment.InfoFragment;
import jbl.stc.com.fragment.LegalFragment;
import jbl.stc.com.fragment.NewTutorialFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.ProductsListFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.fragment.UnableConnectFragment;
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
//    private LinearLayout linearLayoutTips;
//    private TextView textViewTryAgain;
    private final static int MSG_SHOW_PRODUCT_LIST_FRAGMENT = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_SHOW_OTA_FRAGMENT = 3;
    private final static int MSG_SHOW_CONNECTED_BEFORE_FRAGMENT = 4;

    private DashboardHandler dashboardHandler = new DashboardHandler();

    private CheckUpdateAvailable checkUpdateAvailable;

    public static boolean isUpdatingFirmware = false;
    public static CopyOnWriteArrayList<FirmwareModel> mFwlist = new CopyOnWriteArrayList<>();

    private boolean mIsConnected = false;

    public TutorialAncDialog tutorialAncDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        setContentView(R.layout.activity_dashboard);
        registerReceiver(mBtReceiver, makeFilter());


        dashboardActivity = this;
        initView();
        selectFragmentToEnter();
        startCircle();
        //load the presetEQ
        InsertPredefinePreset insertPredefinePreset = new InsertPredefinePreset();
        insertPredefinePreset.executeOnExecutor(InsertPredefinePreset.THREAD_POOL_EXECUTOR, this);

    }

    private void selectFragmentToEnter(){
        Set<String> connectedBeforeDevices = PreferenceUtils.getStringSet(getDashboardActivity(), PreferenceKeys.CONNECTED_BEFORE_DEVICES);
        if (connectedBeforeDevices.size()>=1){
            dashboardHandler.removeMessages(MSG_SHOW_CONNECTED_BEFORE_FRAGMENT);
            dashboardHandler.sendEmptyMessage(MSG_SHOW_CONNECTED_BEFORE_FRAGMENT);
        }else {
            showProductLIst();
        }
    }

    private void showProductLIst(){
        if (relativeLayoutDiscovery.getVisibility() == View.VISIBLE) {
            dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_PRODUCT_LIST_FRAGMENT, 5000);
        }
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
                dashboardHandler.removeMessages(MSG_SHOW_CONNECTED_BEFORE_FRAGMENT);
                if (fr == null){
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }else if (!(fr instanceof  TurnOnBtTipsFragment)) {
                    Logger.i(TAG, "checkBluetooth open TurnOnBtTipsFragment");
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
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
                            selectFragmentToEnter();
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
        findViewById(R.id.image_view_discovery_menu_info).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
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
            dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
            if (!isUpdatingFirmware) {
                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 200);
            }else{
                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_OTA_FRAGMENT,200);
            }

        }else{
            dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 200);
            dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_PRODUCT_LIST_FRAGMENT, 5000);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_discovery_menu_info:{
                dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }else if (!(fr instanceof  InfoFragment)) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }
                break;
            }
//            case R.id.text_view_discovery_try_again:{
//                relativeLayoutDiscovery.setVisibility(View.VISIBLE);
//                relativeLayoutAnimation.setVisibility(View.VISIBLE);
//                linearLayoutTips.setVisibility(View.GONE);
//                startCircle();
//                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_PRODUCT_LIST_FRAGMENT,5000);
//                break;
//            }
        }

    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (isConnected && backStackEntryCount > 1) {
            if (fr instanceof HomeFragment) {
                AppUtils.hideFromForeground(this);
            }else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            if (isConnected) {
                AppUtils.hideFromForeground(this);
            } else {
                if (fr == null) {
                    Logger.d(TAG, "fr is null " + fr.getClass().getSimpleName());
                    return;
                }
                if (fr instanceof LegalFragment
                        || fr instanceof UnableConnectFragment) {
                    super.onBackPressed();
                }else if (fr instanceof InfoFragment
                        || fr instanceof ProductsListFragment) {
                    super.onBackPressed();
                    if (backStackEntryCount <= 1) {
                        showProductLIst();
                    }
                } else if (fr instanceof ConnectedBeforeFragment){
                    super.onBackPressed();
                } else {
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
                case MSG_SHOW_PRODUCT_LIST_FRAGMENT: {
                    dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
                    Log.i(TAG,"MSG_SHOW_PRODUCT_LIST_FRAGMENT");
//                    relativeLayoutDiscovery.setVisibility(View.VISIBLE);
//                    relativeLayoutAnimation.setVisibility(View.GONE);
//                    linearLayoutTips.setVisibility(View.VISIBLE);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new ProductsListFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else if (!(fr instanceof HomeFragment)) {
                        switchFragment(new ProductsListFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
//                    stopCircle();
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT: {
                    Log.i(TAG, "show homeFragment");
                    relativeLayoutDiscovery.setVisibility(View.GONE);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr !=null  && fr instanceof ConnectedBeforeFragment){
                        if (((ConnectedBeforeFragment)fr).connectedDeviceThroughA2dp() == 1){
                            goConnectedFragment();
                        }else{
                            ((ConnectedBeforeFragment)fr).setSpecifiedDevice(getSpecifiedDevice());
                        }
                    }
                    stopCircle();
                    break;
                }
                case MSG_SHOW_DISCOVERY:{
                    Log.i(TAG,"show discovery page");
                    relativeLayoutDiscovery.setVisibility(View.VISIBLE);
                    relativeLayoutAnimation.setVisibility(View.VISIBLE);
//                    linearLayoutTips.setVisibility(View.GONE);
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
                case MSG_SHOW_CONNECTED_BEFORE_FRAGMENT:{
                    dashboardHandler.removeMessages(MSG_SHOW_PRODUCT_LIST_FRAGMENT);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new ConnectedBeforeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }else if (!(fr instanceof  ConnectedBeforeFragment)) {
                        switchFragment(new ConnectedBeforeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                }
            }
        }
    }

    public void goConnectedFragment(){
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        String deviceNameStr = PreferenceUtils.getString(PreferenceKeys.MODEL, mContext, "");
        boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
        if (!isShowTutorialManyTimes) {
            PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
            if (AppUtils.isOldDevice(deviceNameStr)) {
                if (tutorialAncDialog == null) {
                    PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                    tutorialAncDialog = new TutorialAncDialog(DashboardActivity.this);
                    tutorialAncDialog.show();
                }

            }else{
                if (AppUtils.isNewDevice(deviceNameStr)) {
                    if (fr == null) {
                        switchFragment(new NewTutorialFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else if (!(fr instanceof HomeFragment)) {
                        switchFragment(new NewTutorialFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                }
            }
        }

        if (AppUtils.isOldDevice(deviceNameStr)){
            if (fr == null) {
                switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            } else if (!(fr instanceof HomeFragment)) {
                switchFragment(new HomeFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
            }
        }
    }

    public void setIsUpdateAvailable(boolean isUpdateAvailable) {
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (fr != null && fr instanceof SettingsFragment) {
            ((SettingsFragment)fr).showOta(isUpdateAvailable);
        }
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
