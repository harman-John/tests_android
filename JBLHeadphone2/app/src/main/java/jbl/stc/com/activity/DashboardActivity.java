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
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.adapter.MyGridAdapter;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.DiscoveryFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.listener.ConnectListener;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.InsertPredefinePreset;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.EqArcView;
import jbl.stc.com.view.MyDragGridView;

public class DashboardActivity extends BaseActivity implements View.OnClickListener, OnDownloadedListener, ConnectListener {
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private static DashboardActivity dashboardActivity;
    private final static int MSG_SHOW_MY_PRODUCTS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_START_SCAN = 4;
    private final static int MSG_CHECK_MY_DEVICE = 5;

    private DashboardHandler dashboardHandler = new DashboardHandler(Looper.getMainLooper());

    private CheckUpdateAvailable checkUpdateAvailable;

    public static CopyOnWriteArrayList<FirmwareModel> mFwList = new CopyOnWriteArrayList<>();

    private MyDragGridView gridView;
    private MyGridAdapter myGridAdapter;
    private TextView textViewTips;
    private EqArcView viewDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate");
        addActivity(this);
        DeviceManager.getInstance(this).setOnCreate();
//        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
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
        int marginTop = UiUtils.getDeviceNameMarginTop(this);
        gridView.setPadding(0, marginTop, 0, UiUtils.dip2px(this, 20));
        if (lists == null) {
            lists = new ArrayList<>();
        }
        initMyDeviceList();
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

    @Override
    protected void onRestart() {
        Logger.d(TAG, "onRestart");
        super.onRestart();
        DeviceManager.getInstance(this).setOnRestart();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        DeviceManager.getInstance(this).setOnPostResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceManager.getInstance(this).setOnResume();
        Logger.d(TAG, "onResume isConnectedCalled =" + isConnectedCalled + ",isInBackground =" + isInBackground);
        checkBluetooth();
        if (isConnected()) {
            dashboardHandler.removeMessages(MSG_START_SCAN);
            dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 100);
        }
        if (isConnected() && isInBackground) {
            isInBackground = false;
            dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
        } else if (isConnected() && isConnectedCalled) {
            isConnectedCalled = false;
            dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceManager.getInstance(this).setOnPause();
        Logger.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop");
        DeviceManager.getInstance(this).setOnStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DeviceManager.getInstance(this).setOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy");
        unregisterReceiver(mBtReceiver);
        super.onDestroy();
        finishActivity(this);
        DeviceManager.getInstance(this).setOnDestroy();
    }

    private boolean isInBackground = false;

    @Override
    public void connectDeviceStatus(boolean isConnected) {

        if (isConnected) {
            Logger.d(TAG, " connectDeviceStatus true");

            removeAllFragment();
            if (!(currentActivity() instanceof DashboardActivity) && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
                currentActivity().finish();
            }
            if (isForeground()) {
                dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
            } else {
                isInBackground = true;
            }

        } else {
            Logger.d(TAG, "connectDeviceStatus false");
            if (!(currentActivity() instanceof DashboardActivity)) {//&& fr instanceof HomeFragment) {
                Logger.d(TAG, "disconnect home fragment ");
                removeAllFragment();
                currentActivity().finish();
            }
            updateDisconnectedAdapter();
            myGridAdapter.setMyAdapterList(lists);
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
        DeviceManager.getInstance(this).removeDeviceList(key);
    }

    public void checkDevices(Set<String> deviceList) {
        Message msg = new Message();
        msg.what = MSG_CHECK_MY_DEVICE;
        msg.obj = deviceList;
        dashboardHandler.sendMessage(msg);
    }

    public boolean isConnected() {
        return DeviceManager.getInstance(this).isConnected();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_dashboard_white_menu: {
                startActivity(new Intent(this, InfoActivity.class));
                break;
            }
            case R.id.image_view_dashboard_white_plus: {
                dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                if (fr == null) {
                    switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                } else if (!(fr instanceof DiscoveryFragment)) {
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
//            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
//                Logger.i(TAG, "onBackPressed MSG_START_SCAN");
//                dashboardHandler.removeMessages(MSG_START_SCAN);
//                dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
//            }
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

    public void startA2DPCheck() {
        DeviceManager.getInstance(this).startA2DPCheck();
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
                    startA2DPCheck();
                    dashboardHandler.removeMessages(MSG_START_SCAN);
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 2000);
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT: {
                    Logger.d(TAG, "show homeFragment");
                    removeAllFragment();
                    showHomeActivity(getMyDeviceConnected());
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
                case MSG_START_SCAN: {
                    if (isConnected()) {
                        startA2DPCheck();
                    }
                    dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                    break;
                }
                case MSG_CHECK_MY_DEVICE: {
                    Set<String> deviceList = (Set<String>) msg.obj;
                    Logger.i(TAG, "MSG_CHECK_MY_DEVICE deviceList = " + deviceList);
                    if (hasNewDevice(deviceList)) {
                        initMyDeviceList();
                        updateMyDeviceStatus(deviceList);
                        showMyProducts();
                    } else {
                        updateMyDeviceStatus(deviceList);
                        myGridAdapter.setMyAdapterList(lists);
                    }
                    break;
                }
            }
        }
    }

    public void showHomeActivity(MyDevice myDevice) {
        dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
        dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
        dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
        dashboardHandler.removeMessages(MSG_START_SCAN);
        boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
        if (!isShowTutorialManyTimes
                && DeviceManager.getInstance(this).isConnected()
                && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED
                && DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_TRUE_NOTE)) {
            Logger.d(TAG, "truenote");

            Bundle b = new Bundle();
            b.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
            Intent intent = new Intent(this, CalibrationActivity.class);
            intent.putExtra("bundle", b);
            startActivity(intent);
        } else {
            Bundle b = new Bundle();
            b.putParcelable(JBLConstant.KEY_MY_DEVICE, myDevice);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("bundle", b);
            startActivity(intent);
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
