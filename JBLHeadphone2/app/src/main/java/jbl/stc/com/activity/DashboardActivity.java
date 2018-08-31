package jbl.stc.com.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.adapter.MyGridAdapter;
import jbl.stc.com.config.DeviceFeatureMap;
import jbl.stc.com.config.Feature;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.DiscoveryFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.fragment.UnableConnectFragment;
import jbl.stc.com.listener.OnCheckDevicesListener;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.manager.LeManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.InsertPredefinePreset;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.EqArcView;
import jbl.stc.com.view.MyDragGridView;

public class DashboardActivity extends BaseActivity implements View.OnClickListener, OnDownloadedListener, OnCheckDevicesListener{
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private static DashboardActivity dashboardActivity;
    private final static int MSG_SHOW_MY_PRODUCTS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int MSG_START_SCAN = 4;
    private final static int MSG_SHOW_PLUS_ANIMATION_UP = 5;
    private final static int MSG_SHOW_PLUS_ANIMATION_DOWN = 6;
    private DashboardHandler dashboardHandler = new DashboardHandler(Looper.getMainLooper());
    public static CopyOnWriteArrayList<FirmwareModel> mFwList = new CopyOnWriteArrayList<>();
    private MyDragGridView gridView;
    private MyGridAdapter myGridAdapter;
    private ImageView imageViewWhitePlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "on create");
        addActivity(this);
        ProductListManager.getInstance().initDeviceSet(this);
        DeviceManager.getInstance(this).setOnCreate();
        setContentView(R.layout.activity_dashboard);
        registerReceiver(mBtReceiver, makeFilter());
        dashboardActivity = this;
        initView();
        InsertPredefinePreset insertPredefinePreset = new InsertPredefinePreset();
        insertPredefinePreset.executeOnExecutor(InsertPredefinePreset.THREAD_POOL_EXECUTOR, this);
        ProductListManager.getInstance().setOnCheckDevicesListener(this);
        LeManager.getInstance().setOnConnectStatusListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LeManager.getInstance().onRequestPermissionsResult(requestCode, grantResults);
    }

    private void initView() {

        imageViewWhitePlus = findViewById(R.id.image_view_dashboard_white_plus);
        imageViewWhitePlus.setOnClickListener(this);
        EqArcView viewDelete = findViewById(R.id.delete_view);

        gridView = findViewById(R.id.grid_view_dashboard);
        myGridAdapter = new MyGridAdapter();

        myGridAdapter.setOnDeviceSelectedListener(new MyGridAdapter.OnDeviceItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                final MyDevice myDevice = myGridAdapter.mList.get(position);
                if (myDevice.deviceKey.equals(mContext.getString(R.string.plus))) {
                    dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                    dashboardHandler.sendEmptyMessage(MSG_SHOW_DISCOVERY);
                    return;
                }
                if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED
                        || myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                    Logger.d(TAG, "on device selected listener,in grid view, device selected: "+myDevice.deviceKey);
                    gridView.smoothScrollToPositionFromTop(position, 0);
                    dashboardHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showHomeActivity(myDevice);
                        }
                    }, 200);
                } else {
                    Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr instanceof UnableConnectFragment) {
                        Logger.d(TAG, "on device selected listener,in grid view, fragment is already UnableConnectFragment");
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(JBLConstant.DEVICE_MODEL_NAME, myDevice.deviceName);
                    UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
                    unableConnectFragment.setArguments(bundle);
                    switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }
            }
        });

        myGridAdapter.setMyAdapterList();
        myGridAdapter.setMenuBar((RelativeLayout) findViewById(R.id.relative_layout_dashboard_title));
        myGridAdapter.setImageViewPlus(imageViewWhitePlus);
        gridView.setDeleteView(viewDelete);
        gridView.setMenuBar((RelativeLayout) findViewById(R.id.relative_layout_dashboard_title));
        gridView.setAdapter(myGridAdapter);
        gridView.setVisibility(View.VISIBLE);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            int lastLocation = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (gridView.getLastVisiblePosition() == totalItemCount - 1) {
                    View v = view.getChildAt(view.getChildCount() - 1);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    int viewTop = UiUtils.getScreenSize(mContext)[1] - UiUtils.dip2px(mContext, 20);
                    int top = viewTop - v.getHeight() / 2;
                    if (!animateFirstUp && lastLocation < location[1] && location[1] > top) {
                        Logger.d(TAG, "on scroll, in grid view, plus icon fade in ");
                        animateFirstUp = true;
                        animateFirstDown = false;
                        startFadeAnim(imageViewWhitePlus, R.anim.fadin);
                    } else if (!animateFirstDown && lastLocation > location[1] && location[1] > top) {
                        Logger.d(TAG, "on scroll, in grid view, plus icon fade out ");
                        animateFirstDown = true;
                        animateFirstUp = false;
                        startFadeAnim(imageViewWhitePlus, R.anim.fadeout);
                    } else if (location[1] <= top) {
                        imageViewWhitePlus.setVisibility(View.INVISIBLE);
                    }
                    lastLocation = location[1];
                } else {
                    animateFirstDown = false;
                    animateFirstUp = false;
                }
            }
        });
        if (ProductListManager.getInstance().getMyDeviceList().size() == 0) {
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 2000);
        }
        findViewById(R.id.image_view_dashboard_white_menu).setOnClickListener(this);
    }

    private boolean animateFirstUp = false;
    private boolean animateFirstDown = false;

    private void startFadeAnim(View view, int ani) {
        Animation animation = AnimationUtils.loadAnimation(this, ani);
        animation.setInterpolator(new LinearInterpolator());
        animation.setFillAfter(true);
        if (view != null)
            view.startAnimation(animation);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onRestart() {
        Logger.d(TAG, "on restart");
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
        Logger.d(TAG, "on resume, is in background: " + isInBackground + ",is connected: " + DeviceManager.getInstance(this).isConnected());
        checkBluetooth();
        if (DeviceManager.getInstance(this).isConnected() ||
                LeManager.getInstance().isConnected()) {
            Logger.d(TAG, "on resume, is first user: " + DeviceManager.getInstance(this).isFromHome());
            if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice
                    && !DeviceManager.getInstance(this).isFromHome()) {
                DeviceManager.getInstance(this).setIsFromHome(false);
                Logger.d(TAG, "on resume, usb device");
                dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
            } else {
                Logger.d(TAG, "on resume, bt device");
                if (isInBackground) {
                    isInBackground = false;
                    dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
                }
            }
        }else{
            dashboardHandler.removeMessages(MSG_START_SCAN);
            dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 100);
            LeManager.getInstance().checkPermission(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceManager.getInstance(this).setOnPause();
        Logger.d(TAG, "on pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "on stop");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DeviceManager.getInstance(this).setOnActivityResult(requestCode, resultCode);
        Logger.d(TAG, "on activity result, request code is: " + requestCode);
        switch (requestCode) {
            case JBLConstant.REQUEST_CODE_INFO_ACTIVITY: {
                dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "on destroy");
        unregisterReceiver(mBtReceiver);
        super.onDestroy();
        finishActivity(this);
        DeviceManager.getInstance(this).setUsbDeviceStop();
        DeviceManager.getInstance(this).setOnDestroy();
    }

    private boolean isInBackground = false;

    @Override
    public void onConnectStatus(Object... objects) {
        super.onConnectStatus(objects);
        boolean isConnected = (boolean) objects[0];
        if (isConnected) {
            Logger.d(TAG, "on connect status, connected");

            removeAllFragment();
            if (!(currentActivity() instanceof DashboardActivity) && !DeviceManager.getInstance(this).isNeedOtaAgain()) {
                currentActivity().finish();
            }

            Logger.d(TAG, "on connect status, is foreground = " + isForeground());
            if (isForeground()) {
                dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
            } else {
                isInBackground = true;
            }

        } else {
            Logger.d(TAG, "on connect status, not connected");
            if (!(currentActivity() instanceof DashboardActivity)) {
                Logger.d(TAG, "on connect status, disconnect to show product list");
                removeAllFragment();
                currentActivity().finish();
            }
            LeManager.getInstance().checkPermission(this);
            myGridAdapter.setMyAdapterList();
        }
    }

//    public void checkDevices(Set<MyDevice> deviceList) {
//        myGridAdapter.setMyAdapterList(ProductListManager.getInstance().getMyDeviceList());
//        gridView.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_dashboard_white_menu: {
                startActivityForResult(new Intent(this, InfoActivity.class), JBLConstant.REQUEST_CODE_INFO_ACTIVITY);
                break;
            }
            case R.id.image_view_dashboard_white_plus: {
                dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                dashboardHandler.sendEmptyMessage(MSG_SHOW_DISCOVERY);
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
        myGridAdapter.setMyAdapterList();
        gridView.setVisibility(View.VISIBLE);
    }

    public static DashboardActivity getDashboardActivity() {
        return dashboardActivity;
    }

    @Override
    public void onCheckDevices() {
        myGridAdapter.setMyAdapterList();
        myGridAdapter.notifyDataSetChanged();
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
                    Logger.d(TAG, "handle message, show my product");
                    showMyProducts();
                    DeviceManager.getInstance(getDashboardActivity()).startA2DPCheck();
                    dashboardHandler.removeMessages(MSG_START_SCAN);
                    dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 2000);
                    Logger.d(TAG, "handle message, show my product end");
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT: {
                    Logger.d(TAG, "handle message, show homeFragment");
                    removeAllFragment();
                    if (currentActivity() instanceof InfoActivity) {
                        currentActivity().onBackPressed();
                    }
                    gridView.smoothScrollToPositionFromTop(0, 0);
                    dashboardHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showHomeActivity(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED));
                        }
                    }, 200);
                    break;
                }
                case MSG_SHOW_DISCOVERY: {
                    Logger.d(TAG, "handle message, show discovery page");
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr == null) {
                        switchFragment(new DiscoveryFragment(), JBLConstant.FADE_IN_OUT);
                    } else if (!(fr instanceof DiscoveryFragment)) {
                        if (!(fr instanceof TurnOnBtTipsFragment)) {
                            switchFragment(new DiscoveryFragment(), JBLConstant.FADE_IN_OUT);
                        }
                    }
                    break;
                }
                case MSG_START_SCAN: {
                    if (DeviceManager.getInstance(getDashboardActivity()).isConnected()) {
                        DeviceManager.getInstance(getDashboardActivity()).startA2DPCheck();
                    }
                    dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                    break;
                }
                case MSG_SHOW_PLUS_ANIMATION_UP: {
                    animateFirstUp = true;
                    startFadeAnim(imageViewWhitePlus, R.anim.fadin);
                    break;
                }
                case MSG_SHOW_PLUS_ANIMATION_DOWN: {
                    animateFirstDown = true;
                    startFadeAnim(imageViewWhitePlus, R.anim.fadeout);
                    break;
                }
            }
        }
    }

    public void showHomeActivity(MyDevice myDevice) {
        if (myDevice == null) {
            Logger.d(TAG, "show home activity, myDevice is null, return");
            return;
        }
        Logger.d(TAG, "show home activity");
        dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
        dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
        dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
        dashboardHandler.removeMessages(MSG_START_SCAN);
        boolean isShowTutorialManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_TUTORIAL_FIRST_TIME, getApplicationContext());
        if (!isShowTutorialManyTimes
                && DeviceManager.getInstance(this).isConnected()
                && myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED
                && DeviceFeatureMap.isFeatureSupported(myDevice.deviceName, Feature.ENABLE_TRUE_NOTE)) {
            Logger.d(TAG, "show home activity, show calibration activity");
            startActivity(new Intent(this, CalibrationActivity.class));
        } else {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(JBLConstant.KEY_CONNECT_STATUS, myDevice.connectStatus);
            myGridAdapter.getShareView().setVisibility(View.INVISIBLE);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, myGridAdapter.getShareView(), getString(R.string.share_element));
            startActivity(intent, options.toBundle());
//            overridePendingTransition(R.anim.fadin, R.anim.fadeout);
        }
    }

    @Override
    public void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwList) {

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
                    Logger.d(TAG, "check bluetooth, fr is null");
                    return;
                }
                if (fr instanceof TurnOnBtTipsFragment) {
                    removeAllFragment();
                }
            } else {
                if (fr == null) {
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                } else if (!(fr instanceof TurnOnBtTipsFragment)) {
                    Logger.i(TAG, "check bluetooth, open fragment turn on bt tips");
                    switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }
            }
        }
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        return filter;
    }

    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Logger.i(TAG, "on receive, bluetooth state receiver, intent/action is null");
                return;
            }
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    Fragment fr = getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_ON: {
                            if (fr == null) {
                                Logger.d(TAG, "on receive, bluetooth state receiver, state is on, but fr is null");
                                return;
                            }
                            if (fr instanceof TurnOnBtTipsFragment) {
                                removeAllFragment();
                            }
                            if (ProductListManager.getInstance().getMyDeviceList().size() == 0) {
                                dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 2000);
                            }
                            LeManager.getInstance().checkPermission(DashboardActivity.this);
                            break;
                        }
                        default: {
                            Logger.i(TAG, "on receive, bluetooth state receiver, state is default, open fragment turn on bt tipss");
                            dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
                            dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
                            dashboardHandler.removeMessages(MSG_SHOW_MY_PRODUCTS);
                            dashboardHandler.removeMessages(MSG_START_SCAN);
                            if (fr == null) {
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            } else if (!(fr instanceof TurnOnBtTipsFragment)) {
                                switchFragment(new TurnOnBtTipsFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                            }
                            break;
                        }
                    }
                    break;
                }
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:{
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_CONNECTED: {
                            Logger.i(TAG, "on receive, bluetooth state receiver, state connected: "+ device.getName()+"-"+device.getAddress());
                            break;
                        }
                        case BluetoothAdapter.STATE_DISCONNECTED: {
                            Logger.i(TAG, "on receive, bluetooth state receiver, state disconnected: "+ device.getName()+"-"+device.getAddress());
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };
}
