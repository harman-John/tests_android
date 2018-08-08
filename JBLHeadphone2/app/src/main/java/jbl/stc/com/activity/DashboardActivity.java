package jbl.stc.com.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.adapter.MyGridAdapter;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.CalibrationFragment;
import jbl.stc.com.fragment.DiscoveryFragment;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.fragment.InfoFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.fragment.SettingsFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.InsertPredefinePreset;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.view.EqArcView;
import jbl.stc.com.view.MyDragGridView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener, OnDownloadedListener {
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private static DashboardActivity dashboardActivity;
    private final static int MSG_SHOW_MY_PRODUCTS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_OTA_SUCCESS = 3;
    private final static int MSG_START_SCAN = 4;
    private final static int MSG_CHECK_DEVICES = 5;

    private DashboardHandler dashboardHandler = new DashboardHandler(Looper.getMainLooper());

    private CheckUpdateAvailable checkUpdateAvailable;

    public static boolean isOTADoing = false;
    public static CopyOnWriteArrayList<FirmwareModel> mFwList = new CopyOnWriteArrayList<>();

    public TutorialAncDialog tutorialAncDialog;

    private MyDragGridView gridView;
    private List<MyDevice> lists;
    private MyGridAdapter myGridAdapter;
    private TextView textViewTips;
    private EqArcView viewDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate");
        setContentView(R.layout.activity_dashboard);
        registerReceiver(mBtReceiver, makeFilter());
        dashboardActivity = this;
        initView();
        InsertPredefinePreset insertPredefinePreset = new InsertPredefinePreset();
        insertPredefinePreset.executeOnExecutor(InsertPredefinePreset.THREAD_POOL_EXECUTOR, this);
    }

    private void initView() {
        viewDelete = findViewById(R.id.delete_view);
        textViewTips = findViewById(R.id.text_view_dashboard_tips);

        Drawable drawable = getResources().getDrawable(R.mipmap.white_plus);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        SpannableString spannableString = new SpannableString("+");
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        spannableString.setSpan(imageSpan, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        textViewTips.append(getString(R.string.my_products_tips_front));
        textViewTips.append(spannableString);
        textViewTips.append(getString(R.string.my_products_tips_end));

        gridView = findViewById(R.id.grid_view_dashboard);
        myGridAdapter = new MyGridAdapter();
        lists = new ArrayList<>();
        initMyGridAdapterList();
        myGridAdapter.setMyAdapterList(lists);
        gridView.setDeleteView(viewDelete);
        gridView.setMenuBar((RelativeLayout) findViewById(R.id.relative_layout_dashboard_title));
        gridView.setAdapter(myGridAdapter);
        if (lists.size() == 0) {
            gridView.setVisibility(View.GONE);
            textViewTips.setVisibility(View.VISIBLE);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 2000);
        } else {
            textViewTips.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.image_view_dashboard_white_menu).setOnClickListener(this);
        findViewById(R.id.image_view_dashboard_white_plus).setOnClickListener(this);
    }

    private void initMyGridAdapterList() {
        lists.clear();
        Set<String> devicesSet = PreferenceUtils.getStringSet(getApplicationContext(), PreferenceKeys.MY_DEVICES);
        Logger.i(TAG, "deviceSet = " + devicesSet);
        for (String value : devicesSet) {
            lists.add(AppUtils.getMyDevice(value));
        }
    }

    private void updateDisconnectedAdapter() {
        for (MyDevice myDevice : lists) {
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
                break;
            }
        }
    }

    private void updateConnectedStatusAdapter(Set<String> deviceList) {
        for (MyDevice myDevice : lists) {
            myDevice.connectStatus = ConnectStatus.A2DP_UNCONNECTED;
        }
        List<MyDevice> myDeviceListA2dp = new ArrayList<>();
        for (String key : deviceList) {
            myDeviceListA2dp.add(AppUtils.getMyDevice(key));
        }
        for (MyDevice myDeviceA2dp : myDeviceListA2dp) {
            for (MyDevice myDevice : lists) {
                if (myDeviceA2dp.equals(myDevice)) {
                    Logger.i(TAG, "myDeviceA2dp deviceKey= " + myDeviceA2dp.deviceKey);
                    if (myDevice.deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                        Logger.i(TAG, "isConnected = " + isConnected);
                        if (isConnected) {
                            myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
                        }
                    } else {
                        if (specifiedDevice != null) {
                            String mainDeviceKey = specifiedDevice.getName() + "-" + specifiedDevice.getAddress();
                            Logger.i(TAG, "mainDeviceKey = " + mainDeviceKey);
                            if (isConnected && mainDeviceKey.toUpperCase().equalsIgnoreCase(myDeviceA2dp.deviceKey.toUpperCase())) {
                                Logger.i(TAG, "DEVICE_CONNECTED = " + mainDeviceKey);
                                myDevice.connectStatus = ConnectStatus.DEVICE_CONNECTED;
                            } else {
                                myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                            }
                        } else {
                            myDevice.connectStatus = ConnectStatus.A2DP_HALF_CONNECTED;
                        }
                    }
                }
            }
        }
    }

    public MyDevice getMyDeviceConnected() {
        for (MyDevice myDevice : lists) {
            Logger.i(TAG, "deviceKey= " + myDevice.deviceKey + ",connectStatus = " + myDevice.connectStatus);
            if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                return myDevice;
            }
        }
        return null;
    }

    private boolean hasNewDevice(Set<String> deviceList) {
        Set<String> set1 = new HashSet<>(deviceList);
        Set<String> set2 = new HashSet<>();
        for (MyDevice myDevice : lists) {
            String device = myDevice.deviceKey;
            set2.add(device);
        }
        set2.retainAll(set1);
        if (set2.size() == set1.size()) {
            return false;
        }
        Logger.i(TAG, "has new Device");
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
        checkBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy");
        unregisterReceiver(mBtReceiver);
        super.onDestroy();
    }

    @Override
    public void connectDeviceStatus(boolean isConnected) {
        super.connectDeviceStatus(isConnected);
        Logger.d(TAG, " connectDeviceStatus isConnected = " + isConnected);

        if (isConnected) {
            if (isOTADoing) {
                dashboardHandler.sendEmptyMessageDelayed(MSG_OTA_SUCCESS, 200);
            } else {
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (!(fr != null && fr instanceof HomeFragment) && !isNeedOtaAgain) {
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_MY_PRODUCTS, 200);
                }
            }

        } else {
            Logger.d(TAG, "isOTADoing = " + isOTADoing);
            if (!isOTADoing) {
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr != null && fr instanceof HomeFragment) {
                    Logger.d(TAG, "disconnect home fragment ");
                    MyDevice myDevice = ((HomeFragment) fr).getMyDeviceInHome();
                    if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                        removeAllFragment();
                    }
                } else {
                    Logger.d(TAG, "disconnect not home fragment ");
                    if (getMyDeviceConnected() != null && getMyDeviceConnected().connectStatus == ConnectStatus.DEVICE_CONNECTED) {
                        Logger.d(TAG, "disconnect not home fragment removeAllFragment");
                        removeAllFragment();
                    }
                }
                updateDisconnectedAdapter();
                myGridAdapter.setMyAdapterList(lists);
            } else {

            }
        }
    }

    public void removeDeviceList(String key) {
        MyDevice temp = null;
        for (MyDevice myDevice : lists) {
            if (myDevice.deviceKey.equalsIgnoreCase(key)) {
                temp = myDevice;
                break;
            }
        }
        if (temp != null)
            lists.remove(temp);
        AppUtils.removeMyDevice(mContext, key);
        if (AppUtils.getMyDeviceSize(mContext) == 0) {
            textViewTips.setVisibility(View.VISIBLE);
        }
        super.removeDeviceList(key);
    }

    public void checkDevices(Set<String> deviceList) {
        super.checkDevices(deviceList);
        Message msg = new Message();
        msg.what = MSG_CHECK_DEVICES;
        msg.obj = deviceList;
        dashboardHandler.sendMessage(msg);
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_dashboard_white_menu: {
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                } else if (!(fr instanceof InfoFragment)) {
                    switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }
                break;
            }
            case R.id.image_view_dashboard_white_plus: {
                dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                } else if (!(fr instanceof InfoFragment)) {
                    switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                }
                break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (isOTADoing) {
            if (fr != null && fr instanceof OTAFragment && mIsInBootloader && isConnected) {
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
                if (((OTAFragment) fr).isDisableGoBack()) {
                    return;
                }
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            AppUtils.hideFromForeground(this);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                Logger.i(TAG, "onBackPressed MSG_START_SCAN");
                dashboardHandler.removeMessages(MSG_START_SCAN);
                dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
            }
            super.onBackPressed();
        }
    }

    private void showMyProducts() {
        removeAllFragment();
        dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
        dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
        myGridAdapter.setMyAdapterList(lists);
        gridView.setVisibility(View.VISIBLE);
        textViewTips.setVisibility(View.GONE);
    }

    @Override
    public void startA2DPCheck() {
        super.startA2DPCheck();
    }

    public static DashboardActivity getDashboardActivity() {
        return dashboardActivity;
    }

    private class DashboardHandler extends Handler {

        DashboardHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_MY_PRODUCTS: {
                    showMyProducts();
//                    if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_BluetoothDevice) {
                    startA2DPCheck();
//                    }
                    dashboardHandler.removeMessages(MSG_START_SCAN);
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 2000);
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT: {
                    Logger.d(TAG, "show homeFragment");
                    removeAllFragment();
                    goHomeFragment(getMyDeviceConnected());
                    break;
                }
                case MSG_SHOW_DISCOVERY: {
                    Logger.d(TAG, "show discovery page");
                    if (lists.size() == 0) {
                        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if (fr == null) {
                            switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        } else if (!((fr instanceof DiscoveryFragment) || (fr instanceof TurnOnBtTipsFragment))) {
                            switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                        }
                    }
                    break;
                }
                case MSG_OTA_SUCCESS: {
                    Logger.d(TAG, "Ota success");
                    checkDevices(devicesSet);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr != null && fr instanceof OTAFragment) {
                        ((OTAFragment) fr).otaSuccess();
                    }
                    break;
                }
                case MSG_START_SCAN: {
                    if (isConnected()) {
                        startA2DPCheck();
                    }
                    dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                    break;
                }
                case MSG_CHECK_DEVICES: {
                    Set<String> deviceList = (Set<String>) msg.obj;
                    Logger.i(TAG, "MSG_CHECK_DEVICES deviceList = " + deviceList);
                    if (hasNewDevice(deviceList)) {
                        initMyGridAdapterList();
                        updateConnectedStatusAdapter(deviceList);
                        showMyProducts();
                    } else {
                        updateConnectedStatusAdapter(deviceList);
                        myGridAdapter.setMyAdapterList(lists);
                    }
                    break;
                }
            }
        }
    }

    public void goHomeFragment(MyDevice myDevice) {
        Logger.d(TAG, "goHomeFragment");
        dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
        dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
        dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
        dashboardHandler.removeMessages(MSG_START_SCAN);
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (isConnected() && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
            if (!isShowTutorialManyTimes) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                if (tutorialAncDialog == null) {
                    tutorialAncDialog = new TutorialAncDialog(DashboardActivity.this);
                }
                if (DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_TRUE_NOTE)) {
                    Logger.d(TAG, "truenote");
                    CalibrationFragment calibrationFragment = new CalibrationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
                    calibrationFragment.setArguments(bundle);
                    if (fr == null) {
                        switchFragment(calibrationFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else if (!(fr instanceof CalibrationFragment)) {
                        switchFragment(calibrationFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                } else {
                    Logger.d(TAG, "not truenote");
                    if (!tutorialAncDialog.isShowing()) {
                        tutorialAncDialog.show();
                    }
                    showHomeFragment(myDevice);
                }
            } else {
                showHomeFragment(myDevice);
            }
        } else {
            showHomeFragment(myDevice);
        }

    }

    private void showHomeFragment(MyDevice myDevice) {
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
        homeFragment.setArguments(bundle);
        if (fr == null) {
            switchFragment(homeFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
        } else if (!(fr instanceof HomeFragment)) {
            switchFragment(homeFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
        }
    }

    public void setIsUpdateAvailable(boolean isUpdateAvailable) {
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (fr != null && fr instanceof SettingsFragment) {
            ((SettingsFragment) fr).showOta(isUpdateAvailable);
        } else if (fr != null && fr instanceof HomeFragment) {
            ((HomeFragment) fr).showOta(isUpdateAvailable);
        }
    }

    public void startCheckingIfUpdateIsAvailable() {
        Logger.d(TAG, "AppUtils.getModelNumber(this)=" + AppUtils.getModelNumber(this));
        Logger.d(TAG, "startCheckingIfUpdateIsAvailable isConnectionAvailable=" + FirmwareUtil.isConnectionAvailable(this));
        String srcSavedVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.RSRC_VERSION, this, "0.0.0");
        String currentVersion = PreferenceUtils.getString(AppUtils.getModelNumber(this), PreferenceKeys.APP_VERSION, this, "");
        Logger.d(TAG, "srcSavedVersion = " + srcSavedVersion + ",currentVersion = " + currentVersion);
        if (FirmwareUtil.isConnectionAvailable(this) && !TextUtils.isEmpty(srcSavedVersion) && !TextUtils.isEmpty(currentVersion)) {
            Logger.d(TAG, "checkUpdateAvailable = " + checkUpdateAvailable);
            if (checkUpdateAvailable != null && checkUpdateAvailable.isRunnuning()) {
                Logger.d(TAG, "CheckUpdateAvailable is running so return");
                return;
            }
            Logger.d(TAG, "CheckUpdateAvailable.start()");
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

    private void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
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
                if (fr == null) {
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                } else if (!(fr instanceof TurnOnBtTipsFragment)) {
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
            if (intent == null || intent.getAction() == null) {
                Logger.i(TAG, "intent or its action is null");
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
                            if (lists.size() == 0) {
                                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 2000);
                            }
                            break;
                        }
                        default: {
                            Logger.i(TAG, "open TurnOnBtTipsFragment");
                            dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                            dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
                            dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
                            dashboardHandler.removeMessages(MSG_OTA_SUCCESS);
                            dashboardHandler.removeMessages(MSG_START_SCAN);
                            if (fr == null) {
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            } else if (!(fr instanceof TurnOnBtTipsFragment)) {
                                Logger.i(TAG, "checkBluetooth open TurnOnBtTipsFragment");
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            }
                            break;
                        }
                    }
            }
        }
    };
}
