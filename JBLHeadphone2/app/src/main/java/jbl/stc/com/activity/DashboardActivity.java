package jbl.stc.com.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.view.JblCircleView;

public class DashboardActivity extends DeviceManagerActivity implements View.OnClickListener,OnDownloadedListener {
    private static final String TAG = DashboardActivity.class.getSimpleName() + "aa";
    private JblCircleView jblCircleView;
    private static DashboardActivity dashboardActivity;
    private ImageView image_view_logo;
    private LinearLayout ll_cannot_see;
    private RelativeLayout relativeLayoutDiscovery;
    private TextView txtTips;
    private final static int SHOW_UN_FOUND_TIPS = 0;
    private final static int MSG_SHOW_HOME_FRAGMENT = 1;
    private final static int MSG_SHOW_DISCOVERY = 2;
    private final static int REQUEST_CODE = 0;

    private DashboardHandler dashboardHandler = new DashboardHandler();

    private CheckUpdateAvailable checkUpdateAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        setContentView(R.layout.activity_dashboard);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        dashboardActivity = this;
        initView();
        dashboardHandler.sendEmptyMessageDelayed(SHOW_UN_FOUND_TIPS,10000);
    }

    private void initView() {
        startCircle();
        relativeLayoutDiscovery = findViewById(R.id.containerLayoutDiscovery);
        image_view_logo = (ImageView) findViewById(R.id.image_view_logo);
        ll_cannot_see = (LinearLayout) findViewById(R.id.ll_cannot_see);
        txtTips = (TextView) findViewById(R.id.txtTips);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        stopCircle();
        super.onDestroy();
    }

    @Override
    public void connectDeviceStatus(boolean isConnected){
        super.connectDeviceStatus(isConnected);
        Log.d(TAG, " connectDeviceStatus isConnected = "+isConnected);
        if(isConnected){
            dashboardHandler.removeMessages(SHOW_UN_FOUND_TIPS);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME_FRAGMENT,200);
        }else{
            dashboardHandler.removeMessages(MSG_SHOW_DISCOVERY);
            dashboardHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCOVERY,200);
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
                finish();
            }
        }
    }

    private void startCircle(){
        if (jblCircleView == null) {
            jblCircleView = (JblCircleView) findViewById(R.id.jblCircleView);
            jblCircleView.setVisibility(View.VISIBLE);
            jblCircleView.addWave();
            jblCircleView.start();
        }
    }

    private void stopCircle(){
        if (jblCircleView != null) {
            jblCircleView.setVisibility(View.GONE);
            jblCircleView.stop();
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
                    stopCircle();
                    image_view_logo.setVisibility(View.GONE);
                    ll_cannot_see.setVisibility(View.VISIBLE);
                    txtTips.setVisibility(View.VISIBLE);
                    break;
                }
                case MSG_SHOW_HOME_FRAGMENT:{
                    Log.i(TAG,"show homeFragment");
                    stopCircle();
                    relativeLayoutDiscovery.setVisibility(View.GONE);
                    switchFragment(new HomeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    break;
                }
                case MSG_SHOW_DISCOVERY:{
                    Log.i(TAG,"show discovery page");
                    relativeLayoutDiscovery.setVisibility(View.VISIBLE);
                    removeAllFragment();
                    startCircle();
                    image_view_logo.setVisibility(View.VISIBLE);
                    ll_cannot_see.setVisibility(View.INVISIBLE);
                    txtTips.setVisibility(View.VISIBLE);
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
}
