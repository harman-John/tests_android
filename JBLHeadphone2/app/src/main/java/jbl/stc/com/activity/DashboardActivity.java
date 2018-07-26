package jbl.stc.com.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mtp.MtpConstants;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.adapter.MyGridAdapter;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.dialog.TutorialAncDialog;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.entity.FirmwareModel;
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
import jbl.stc.com.view.DeleteView;
import jbl.stc.com.view.MyDragGridView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener, OnDownloadedListener {
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private static DashboardActivity dashboardActivity;
    private final static int MSG_SHOW_MY_PRODUCTS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_SHOW_OTA_FRAGMENT = 3;
    private final static int MSG_START_SCAN = 4;

    private DashboardHandler dashboardHandler = new DashboardHandler(Looper.getMainLooper());

    private CheckUpdateAvailable checkUpdateAvailable;

    public static boolean isUpdatingFirmware = false;
    public static CopyOnWriteArrayList<FirmwareModel> mFwlist = new CopyOnWriteArrayList<>();

    public TutorialAncDialog tutorialAncDialog;

    private MyDragGridView gridView;
    private List<MyDevice> lists;
    private MyGridAdapter myGridAdapter;
    private TextView textViewTips;
    private DeleteView viewDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
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
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED){
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
                if (myDeviceA2dp.equals(myDevice)){
                    Logger.i(TAG,"name = "+myDevice.deviceName);
                    if (myDevice.deviceName.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                        Logger.i(TAG,"isConnected = "+isConnected);
                        if (isConnected){
                            myDevice.connectStatus = ConnectStatus.A2DP_CONNECTED;
                        }
                    }else {
                        if (getSpecifiedDevice() != null) {
                            String mainDeviceKey = getSpecifiedDevice().getName() + "-" + getSpecifiedDevice().getAddress();
                            if (isConnected && mainDeviceKey.equalsIgnoreCase(myDeviceA2dp.deviceKey)) {
                                myDevice.connectStatus = ConnectStatus.A2DP_CONNECTED;
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

    public MyDevice getMyDeviceA2dpConnected() {
        for (MyDevice myDevice : lists) {
            if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED) {
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
        Logger.i(TAG,"has new Device");
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        checkBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        unregisterReceiver(mBtReceiver);
        super.onDestroy();
    }

    @Override
    public void connectDeviceStatus(boolean isConnected) {
        super.connectDeviceStatus(isConnected);
        Log.d(TAG, " connectDeviceStatus isConnected = " + isConnected);

        if (isConnected) {
            if (isUpdatingFirmware) {
                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_OTA_FRAGMENT, 200);
            } else {
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (!(fr != null && fr instanceof HomeFragment)){
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_MY_PRODUCTS, 200);
                }
            }

        } else {
            Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
            if (fr != null && fr instanceof HomeFragment){
                MyDevice myDevice = ((HomeFragment)fr).getMyDeviceInHome();
                if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED){
                    removeAllFragment();
                }
            }
            updateDisconnectedAdapter();
            myGridAdapter.setMyAdapterList(lists);
        }
    }

    public void removeDeviceList(String key){
        MyDevice temp = null;
        for(MyDevice myDevice: lists) {
            if (myDevice.deviceKey.equalsIgnoreCase(key)) {
                temp = myDevice;
                break;
            }
        }
        if (temp!= null)
            lists.remove(temp);
        AppUtils.removeMyDevice(mContext,key);
        super.removeDeviceList(key);
    }

    public void checkDevices(Set<String> deviceList) {
        Logger.i(TAG,"checkDevices deviceList = "+deviceList);
        super.checkDevices(deviceList);
        if (hasNewDevice(deviceList)) {
            initMyGridAdapterList();
            updateConnectedStatusAdapter(deviceList);
            showMyProducts();
        } else {
            updateConnectedStatusAdapter(deviceList);
            myGridAdapter.setMyAdapterList(lists);
        }
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
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            AppUtils.hideFromForeground(this);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                Logger.i(TAG,"onBackPressed MSG_START_SCAN");
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
                    Log.i(TAG, "show homeFragment");
                    removeAllFragment();
                    goHomeFragment(getMyDeviceA2dpConnected());
                    break;
                }
                case MSG_SHOW_DISCOVERY: {
                    Log.i(TAG, "show discovery page");
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
                case MSG_SHOW_OTA_FRAGMENT: {
                    Log.i(TAG, "show OTAFragment");
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    } else if (!(fr instanceof OTAFragment)) {
                        switchFragment(new OTAFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
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
            }
        }
    }

    public void goHomeFragment(MyDevice myDevice) {
        Logger.d(TAG,"goHomeFragment");
        dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
        dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
        dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
        dashboardHandler.removeMessages(MSG_START_SCAN);
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
        if (isConnected()) {
            boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
            if (!isShowTutorialManyTimes) {
                PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                if (tutorialAncDialog == null) {
                    PreferenceUtils.setBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, true, getApplicationContext());
                    tutorialAncDialog = new TutorialAncDialog(DashboardActivity.this);
                }
                if (!tutorialAncDialog.isShowing()) {
                    tutorialAncDialog.show();
                }
            }
        }
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
                            if (lists.size() == 0){
                                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY,2000);
                            }
                            break;
                        }
                        default: {
                            Logger.i(TAG, "open TurnOnBtTipsFragment");
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
