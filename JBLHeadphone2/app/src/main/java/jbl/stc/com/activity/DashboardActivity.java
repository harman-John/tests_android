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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
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
import jbl.stc.com.data.ConnectedDeviceType;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.DiscoveryFragment;
import jbl.stc.com.fragment.TurnOnBtTipsFragment;
import jbl.stc.com.fragment.UnableConnectFragment;
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
    private final static int MSG_SHOW_FRAGMENT = 6;

    private DashboardHandler dashboardHandler = new DashboardHandler(Looper.getMainLooper());

    private CheckUpdateAvailable checkUpdateAvailable;

    public static CopyOnWriteArrayList<FirmwareModel> mFwList = new CopyOnWriteArrayList<>();

    private MyDragGridView gridView;
    private MyGridAdapter myGridAdapter;
//    private TextView textViewTips;
    private EqArcView viewDelete;
    private ImageView imageViewWhitePlus;

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

        imageViewWhitePlus = findViewById(R.id.image_view_dashboard_white_plus);
        imageViewWhitePlus.setOnClickListener(this);
        viewDelete = findViewById(R.id.delete_view);
//        textViewTips = findViewById(R.id.text_view_dashboard_tips);

        gridView = findViewById(R.id.grid_view_dashboard);
        myGridAdapter = new MyGridAdapter();

        myGridAdapter.setOnDeviceSelectedListener(new MyGridAdapter.OnDeviceItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                MyDevice myDevice = myGridAdapter.mLists.get(position);
                if (myDevice.deviceKey.equals(mContext.getString(R.string.plus))){
                    switchFragment(new DiscoveryFragment(), JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT);
                    return;
                }
                if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED
                        || myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                    Logger.d(TAG, "onSelected Show home fragment");
                    showHomeActivity(myDevice);
                } else {
                    Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                    if (fr instanceof UnableConnectFragment) {
                        Logger.d(TAG, "fr is already UnableConnectFragment");
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(JBLConstant.DEVICE_MODEL_NAME, myDevice.deviceName);
                    UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
                    unableConnectFragment.setArguments(bundle);
                    DashboardActivity.getDashboardActivity().switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }
            }
        });

        myGridAdapter.setMyAdapterList(DeviceManager.getInstance(this).getMyDeviceList());
        myGridAdapter.setMenuBar((RelativeLayout) findViewById(R.id.relative_layout_dashboard_title));
        myGridAdapter.setImageViewPlus(imageViewWhitePlus);
        gridView.setDeleteView(viewDelete);
        gridView.setMenuBar((RelativeLayout) findViewById(R.id.relative_layout_dashboard_title));
        gridView.setAdapter(myGridAdapter);
        gridView.setVisibility(View.VISIBLE);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Logger.d(TAG,"onScroll state changed view = "+view
                        +",scrollState ="+scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                Logger.d(TAG,"on scroll firstVisibleItem = "+ firstVisibleItem
                        +",visibleItemCount = "+visibleItemCount
                        +",totalItemCount = "+totalItemCount);
                if (gridView.getLastVisiblePosition() == totalItemCount -1){
                    View v = view.getChildAt(view.getChildCount()-1);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    Logger.d(TAG,"get view height = "+ v.getHeight()
                            +",getScreenSize = "+UiUtils.getScreenSize(mContext)[1]
                            +",screen 30dp = "+UiUtils.dip2px(mContext,20)
                            +",location1 =" + location[1]);
                    int viewTop = UiUtils.getScreenSize(mContext)[1] - UiUtils.dip2px(mContext,20);
                    if (location[1] <= viewTop  &&  location[1] >= viewTop- v.getHeight()/2){
                        float percent = ((float)viewTop - (float)location[1])/((float)(v.getHeight()/2));
                        float alpha = 1 - percent;
                        Logger.d(TAG,"alpha is "+ alpha+",percent ="+percent);
                        imageViewWhitePlus.setAlpha(alpha);
                        imageViewWhitePlus.setVisibility(View.VISIBLE);
                    }else if (location[1] <= viewTop - v.getHeight()/2 && location[1] >= viewTop- v.getHeight()){
                        imageViewWhitePlus.setAlpha((float) 0);
                        imageViewWhitePlus.setVisibility(View.GONE);
                    }
                }else{
                    imageViewWhitePlus.setAlpha((float) 1.0);
                }
            }
        });
        if (DeviceManager.getInstance(this).getMyDeviceList().size() == 0) {
//            textViewTips.setVisibility(View.VISIBLE);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY, 2000);
        } else {
//            textViewTips.setVisibility(View.GONE);
        }
        findViewById(R.id.image_view_dashboard_white_menu).setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
        Logger.d(TAG, "onResume isInBackground =" + isInBackground+",isConnected ="+DeviceManager.getInstance(this).isConnected());
        checkBluetooth();
        if (DeviceManager.getInstance(this).isConnected()) {
            dashboardHandler.removeMessages(MSG_START_SCAN);
            dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 100);
        }
        if (DeviceManager.getInstance(this).isConnected()) {
            Logger.d(TAG, "onResume isFirstUser ="+DeviceManager.getInstance(this).isFromHome());
            if (DeviceConnectionManager.getInstance().getCurrentDevice() == ConnectedDeviceType.Connected_USBDevice
                    && !DeviceManager.getInstance(this).isFromHome()) {
                DeviceManager.getInstance(this).setIsFromHome(false);
                Logger.d(TAG, "onResume Connected_USBDevice");
                dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
            } else {
                Logger.d(TAG, "onResume bt device");
                if (isInBackground) {
                    isInBackground = false;
                    dashboardHandler.sendEmptyMessage(MSG_SHOW_MY_PRODUCTS);
                }
            }
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
        DeviceManager.getInstance(this).setUsbDeviceStop();
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

            Logger.d(TAG, " connectDeviceStatus isForeground = "+isForeground());
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
//            updateDisconnectedAdapter();
            myGridAdapter.setMyAdapterList(DeviceManager.getInstance(this).getMyDeviceList());
        }
    }

    public void removeDeviceList(String key) {
        MyDevice temp = null;
        for (MyDevice myDevice : DeviceManager.getInstance(this).getMyDeviceList()) {
            if (myDevice.deviceKey.equalsIgnoreCase(key)) {
                temp = myDevice;
                break;
            }
        }
        if (temp != null)
            DeviceManager.getInstance(this).getMyDeviceList().remove(temp);
        AppUtils.removeMyDevice(mContext, key);
//        if (AppUtils.getMyDeviceSize(mContext) == 0) {
//            textViewTips.setVisibility(View.VISIBLE);
//        }
        DeviceManager.getInstance(this).removeDeviceList(key);
    }

    public void checkDevices(Set<String> deviceList) {
//        Message msg = new Message();
//        msg.what = MSG_CHECK_MY_DEVICE;
//        msg.obj = deviceList;
//        dashboardHandler.sendMessage(msg);
        showMyProducts();
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
        myGridAdapter.setMyAdapterList(DeviceManager.getInstance(this).getMyDeviceList());
        gridView.setVisibility(View.VISIBLE);
//        if(DeviceManager.getInstance(this).getMyDeviceList().size() ==0) {
//            textViewTips.setVisibility(View.VISIBLE);
//        }else{
//            textViewTips.setVisibility(View.GONE);
//        }
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
                    Logger.d(TAG, "msg show my product");
                    showMyProducts();
                    DeviceManager.getInstance(getDashboardActivity()).startA2DPCheck();
                    dashboardHandler.removeMessages(MSG_START_SCAN);
                    dashboardHandler.removeMessages(MSG_SHOW_HOME_FRAGMENT);
                    dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT, 2000);
                    Logger.d(TAG, "msg show my product end");
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT: {
                    Logger.d(TAG, "show homeFragment");
                    removeAllFragment();
                    showHomeActivity(DeviceManager.getInstance(getDashboardActivity()).getMyDeviceConnected());
                    break;
                }
                case MSG_SHOW_DISCOVERY: {
                    Logger.d(TAG, "show discovery page");
                    if (DeviceManager.getInstance(getDashboardActivity()).getMyDeviceList().size() == 0) {
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
                    if (DeviceManager.getInstance(getDashboardActivity()).isConnected()) {
                        DeviceManager.getInstance(getDashboardActivity()).startA2DPCheck();
                    }
                    dashboardHandler.sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
                    break;
                }
//                case MSG_CHECK_MY_DEVICE: {
//                    Set<String> deviceList = (Set<String>) msg.obj;
//                    Logger.i(TAG, "MSG_CHECK_MY_DEVICE deviceList = " + deviceList);
//                    if (hasNewDevice(DeviceManager.getInstance(getDashboardActivity()).getDevicesSet())) {
//                        initMyDeviceList();
//                        updateMyDeviceStatus(deviceList);
//                        showMyProducts();
//                    } else {
//                        updateMyDeviceStatus(DeviceManager.getInstance(getDashboardActivity()).getDevicesSet());
//                        myGridAdapter.setMyAdapterList(DeviceManager.getInstance(getDashboardActivity()).getMyDeviceList());
//                    }
//                    break;
//                }
            }
        }
    }

    public void showHomeActivity(MyDevice myDevice) {
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
            Logger.d(TAG, "truenote");
            startActivity(new Intent(this, CalibrationActivity.class));
        } else {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(JBLConstant.KEY_CONNECT_STATUS, myDevice.connectStatus);
            startActivity(intent);
            overridePendingTransition(R.anim.fadin, R.anim.fadeout);
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
                            if (DeviceManager.getInstance(getDashboardActivity()).getMyDeviceList().size() == 0) {
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
