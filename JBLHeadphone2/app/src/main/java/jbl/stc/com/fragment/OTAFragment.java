package jbl.stc.com.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.ImageType;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.Debug;
import com.avnera.smartdigitalheadset.LightX;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.data.FwTYPE;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.Cmd150Manager;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.ota.DownloadProgrammingFile;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.OTAUtil;

import static jbl.stc.com.data.ConnectedDeviceType.Connected_BluetoothDevice;

public class OTAFragment extends BaseFragment implements View.OnClickListener,OnDownloadedListener {
    public static final String TAG = OTAFragment.class.getSimpleName();

    private long batteryLevel;
    private String mLiveFirmware = "UNKNOWN";
    private String currentVersion = "0.0.0";
    private String rsrcSavedVersion = "0.0.0";
    private String rsrcVersion = "0.0.0";
    private ProgressInfo progressInfo = null;
    private class ProgressInfo{
        public double paramLen;
        public double dataLen;
        public double firmwareLen;
    }
    protected LightX lightX;

    private boolean mReceiverTag = false;
    private boolean isUpdateAvailable;
    private NetworkChangeReceiver networkChangeReceiver;
    private float progressFactor = 0.0f, divideFactor = 2;
    public static boolean isUpdatingFrameWork;
    private CheckUpdateAvailable checkUpdateAvailable;
    public static boolean USB_PERMISSION_CHECK;

    private DownloadProgrammingFile downloadProgrammingFile;
    private CopyOnWriteArrayList<FirmwareModel> fwlist = new CopyOnWriteArrayList<>();

    private View view;
    private Bundle args;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.args = args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentFW = FirmwareUtil.currentFirmware;
        batteryLevel = PreferenceUtils.getInt(PreferenceKeys.BATTERY_VALUE, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ota,
                container, false);
        view.setOnClickListener(this);
        view.findViewById(R.id.image_view_ota_back).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (args != null && args.containsKey("lightXIsInBootloader")) {
            batteryLevel = 51;
            try {
//                getActivity().leftHeaderBtn.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            startCheckingIfUpdateIsAvailable();
            registerConnectivity();
        } else {
            readBasicInformation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_UPDATE_DEVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_ota_back:{
                getActivity().onBackPressed();
                break;
            }
            case R.id.aa_popup_close_arrow:{
                if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {
//                    AlertsDialog.showSimpleDialogWithOKButton(null, getString(R.string.please_wait), getActivity());
//                    AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.please_wait));
                } else if (batteryLevel < 50) {
//                    AlertsDialog.showSimpleDialogWithOKButtonWithBack(null, getString(R.string.battery_alert), getActivity());
//                    AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.battery_alert));
                } else {
//                    FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.pls_Connect_while_upgrade);
//                    txtConnectMessage.setText(FirmwareUtil.disconnectHeadphoneText);
                    startDownloadFirmwareImage();
                    v.setOnClickListener(null);
                }
                break;
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.RUNNING)
                downloadProgrammingFile.cancel(true);
            if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING)
                checkUpdateAvailable.cancel(true);
//            getActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
            unregisterNetworkReceiverSafely();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readBasicInformation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG,"getFirmwareVersion 33");
                ANCControlManager.getANCManager(getActivity()).getFirmwareInfo(lightX);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ANCControlManager.getANCManager(getActivity()).getFirmwareVersion(lightX);
                if (lightX!= null) {
                    lightX.readBootVersionFileResource();
                }
                ANCControlManager.getANCManager(getActivity()).getBatterLeverl(lightX);
            }
        }).start();
    }

    private void registerConnectivity() {
        if (getActivity() == null)
            return;
        networkChangeReceiver = new NetworkChangeReceiver();
        /**
         * Connectivity change receiver for starting downloading of firmware binary automatically.
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(networkChangeReceiver, intentFilter);
        mReceiverTag = true;
    }

    private void unregisterNetworkReceiverSafely() {
        try {
            if (mReceiverTag) {
                mReceiverTag = false;
                getActivity().unregisterReceiver(networkChangeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDownloadFirmwareImage() {
        if (FirmwareUtil.isConnectionAvailable(getActivity())) {
            Logger.d(TAG,"startDownloadFirmwareImage");
//            mProgressBar.setVisibility(View.VISIBLE);
//            txtdownloading.setText("");
//            updateStatus.setText("Downloading...");
            if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {
                return;
            }
            downloadProgrammingFile = new DownloadProgrammingFile(getActivity(), this, fwlist);
            try {
                downloadProgrammingFile.executeOnExecutor(DownloadProgrammingFile.THREAD_POOL_EXECUTOR, OTAUtil.getURL(getActivity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            txtdownloading.setText(getString(R.string.no_internet_connection));
        }
    }

    public void setIsUpdateAvailable(boolean isUpdateAvailable, CopyOnWriteArrayList<FirmwareModel> fwList) {
        if (getActivity()== null){
            Log.e(TAG,"Activity is null");
            return;
        }

        if (!isAdded()){
            Log.e(TAG,"This fragment is not added");
            return;
        }

        this.isUpdateAvailable = isUpdateAvailable;
        Logger.d(TAG,"setIsUpdateAvailable isUpdateAvailable="+isUpdateAvailable);
        if (isUpdateAvailable) {
            String liveAppVersion = null;
            this.fwlist = fwList;
            divideFactor = 2 * fwlist.size();
//            linearLayout.setTag(DOWNLOAD);
            for (FirmwareModel model : fwlist) {
                /**
                 * reorder here so in case of any changed at server
                 */
                switch (model.getFwtype()) {
                    case APP:
                        liveAppVersion = model.getVersion();
                        break;
                    case RSRC:
                        rsrcVersion = model.getVersion();
                        break;
                    case BOOT:
                        break;
                    case PARAM:
                        liveAppVersion = model.getVersion();
                        break;
                }
            }
//            txtProgressVersion.setVisibility(View.GONE);
//            iconCloud.setVisibility(View.VISIBLE);
//            txtUpdating.setVisibility(View.VISIBLE);
//            mProgressBar.setVisibility(View.GONE);
//            if (!TextUtils.isEmpty(liveAppVersion)) {
//                txtUpdating.setText(getAppActivity().getString(R.string.install_ver) + " " + liveAppVersion);
//                updateStatus.setText("Update available V" + liveAppVersion);
//            } else {
//                txtUpdating.setText(getAppActivity().getString(R.string.install_ver) + " " + rsrcVersion);
//                updateStatus.setText("Update available V" + rsrcVersion);
//            }
//            mainCircle.setBackgroundResource(R.drawable.orange);
//            txtProgressVersion.setTextColor(getAppActivity().getResources().getColor(R.color.white));
            if (args != null && args.containsKey("lightXIsInBootloader")) {
//                linearLayout.setTag(DOWNLOAD);
                startDownloadFirmwareImage();
            }
        } else {
//            linearLayout.setTag(NOUPDATE);
//            updateStatus.setText("No update available");
//            mProgressBar.setVisibility(View.INVISIBLE);
            unregisterNetworkReceiverSafely();
        }
    }

    @Override
    public void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwlist) throws FileNotFoundException {
        USB_PERMISSION_CHECK = true;
        this.fwlist = fwlist;
//        mProgressBar.setVisibility(View.GONE);
//        updateStatus.setVisibility(View.VISIBLE);
        if (fwlist.size() != 0) {
            boolean isSuccessFulDownload = true;
            for (FirmwareModel model : fwlist) {
                Logger.d(TAG,"onDownloadedFirmware version = "+model.getVersion());
                if (!model.isSuccess()) {
                    isSuccessFulDownload = false;
                    break;
                }
                switch (model.getFwtype()) {
                    case APP:
                        break;
                    case RSRC:
                        break;
                    case DATA:
                        break;
                    case FIRMWARE:
                        break;
                    case BOOT:
                        break;
                }

            }
            if (isSuccessFulDownload) {
//                txtdownloading.setText("");
//                lightX.readBootImageType();
                if (lightX == null) {
                    startUpdate();
                    if (fwlist.size() <= 0) {
                        isUpdatingFrameWork = false;
                        return;
                    }
                    index =0;
                    FirmwareUtil.isUpdatingFirmWare.set(true);
                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateStarted(mLiveFirmware);
                    if (progressInfo == null) {
                        progressInfo = new ProgressInfo();
                    }
                    for (FirmwareModel model : fwlist) {
                        switch (model.getFwtype()) {
                            case PARAM:
                                progressInfo.paramLen = FirmwareUtil.readInputStream(new FileInputStream(model.getFile())).length;
                                break;
                            case DATA:
                                progressInfo.dataLen = FirmwareUtil.readInputStream(new FileInputStream(model.getFile())).length;
                                break;
                            case FIRMWARE:
                                progressInfo.firmwareLen = FirmwareUtil.readInputStream(new FileInputStream(model.getFile())).length;
                                break;
                        }
                    }
                    updateParam();
                }else{
                    ANCControlManager.getANCManager(getActivity()).readBootImageType(lightX);
                }
            } else {
                onFailedDownload();
            }
        } else
            onFailedDownload();

    }

    @Override
    public void onFailedDownload() {
        if (isUpdateAvailable) {
//            linearLayout.setTag(DOWNLOAD);
        }
//        mProgressBar.setVisibility(View.GONE);
//        AlertsDialog.showToast(getAppActivity(), getAppActivity().getString(R.string.download_failed));
        AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted", getString(R.string.unplug_plug_while_update_error), getActivity());
        AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.unplug_plug_while_update_error));
    }

    @Override
    public void onFailedToCheckUpdate() {

//        linearLayout.setTag(CHECKINGFOR_UPDATE);
//        mProgressBar.setVisibility(View.GONE);
//        AlertsDialog.showToast(getAppActivity(), getAppActivity().getString(R.string.update_failed));
    }

    @Override
    public void onUpgradeUpdate(String liveVersion, String title) {
        mLiveFirmware = liveVersion;
    }

    private void startUpdate(){
        isUpdatingFrameWork = false;
        FirmwareUtil.isUpdatingFirmWare.set(true);
//        txtUpdating.setVisibility(View.VISIBLE);
//        mainCircle.setBackgroundResource(R.drawable.orange);
//        txtProgressVersion.setTextColor(getResources().getColor(R.color.white));
//        txtProgressVersion.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtProgressVersion.getTextSize()*2);
//        iconCloud.setVisibility(View.GONE);
//        txtProgressVersion.setVisibility(View.VISIBLE);
//        txtProgressVersion.setText("0 %");
//        linearLayout.setOnClickListener(null);

//        fadeIn = new AlphaAnimation(0, 1);
//        fadeIn.setDuration(1000);
//        fadeIn.setRepeatMode(Animation.REVERSE);
//        fadeIn.setRepeatCount(Animation.INFINITE);
//        fadeIn.setInterpolator(new LinearInterpolator());
//        txtUpdating.setAnimation(fadeIn);
//        if (AppUtils.is150NC(getAppActivity())) {
//            updateStatus.setText(getString(R.string.firmwareUpdating1));
//        } else if (AppUtils.is750Device(getAppActivity())) {
//            updateStatus.setText(getString(R.string.firmwareUpdating750));
//        } else {
//            updateStatus.setText(getString(R.string.firmwareUpdating));
//        }
//        txtdownloading.setVisibility(View.VISIBLE);
        //set downloading prompt text as per JBL aware or JBL everest
        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
            case NONE:
                break;
            case Connected_USBDevice:
//                txtdownloading.setText(getString(R.string.don_close_app_usb_headset));
                break;
            case Connected_BluetoothDevice:
//                if (AppUtils.is150NC(getAppActivity())){
//                    txtdownloading.setText(getString(R.string.don_close_app_bluetooth_headset1));
//                }else if (AppUtils.is750Device(getAppActivity())) {
//                    txtdownloading.setText(getString(R.string.don_close_app_bluetooth_headset_750));
//                } else {
//                    txtdownloading.setText(getString(R.string.don_close_app_bluetooth_headset));
//                }
                break;
        }
//        txtUpdating.setText(getAppActivity().getString(R.string.updating));
//        try {
//            getAppActivity().leftHeaderBtn.setVisibility(View.INVISIBLE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        unregisterNetworkReceiverSafely();
        Logger.d(TAG, "Writing start");
    }

    public void startWritingFirmware() {
        try {
            if (fwlist.size() <= 0) {
                isUpdatingFrameWork = false;
//                FirmwareUtil.isUpdatingFirmWare.set(false);
//                lightX.enterApplication();
//                Logger.d("OTA " + "enterApplication");
//                txtUpdating.setText(getString(R.string.update_successful));
//                try {
//                    getAppActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
//                } catch (Exception e) {
//                }
                return;
            }
            FirmwareUtil.isUpdatingFirmWare.set(true);
            int size = fwlist.size() - 1;
            FirmwareModel firmwareModel = fwlist.remove(size);
            byte[] data;
            switch (firmwareModel.getFwtype()) {
                case APP:
                    data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
                    Logger.d(TAG, "first 1024 bytes of firmware to write:\n" + Debug.hexify(data, 0, 1024));
                    lightX.writeFirmware(LightX.FirmwareRegion.Application, data);
                    break;
                case RSRC:
                    data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
                    Logger.d(TAG, "first 1024 bytes of firmware to write:\n" + Debug.hexify(data, 0, 1024));
                    lightX.writeFirmware(LightX.FirmwareRegion.Resource, data);
                    break;
                case BOOT:
                    startWritingFirmware();
                    break;
            }
        } catch (ConcurrentModificationException concurrentException) {
            //already an update running, UI stucks at 0% otherwise.
            concurrentException.printStackTrace();
            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted", getString(R.string.unplug_plug_while_update_error), getActivity());
            AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.unplug_plug_while_update_error));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateParam(){
        Logger.d(TAG, "disable accessory interrupts");
        Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),JBLConstant.DISABLE_ACCESSORY_INTERRUPTS);
        FirmwareModel firmwareModel = findImage(FwTYPE.PARAM);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG,"currentVersion param   ====="+currentVersion);
        byte[] data ;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG,"File not file error!");
            return;
        }
        otaSteps = 0;
        Logger.d(TAG,"currentFW ="+currentFW);
        Logger.d(TAG,"PARAM length ="+data.length);
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),
                data, currentVersion,
                ImageType.Parameters,currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void updateData(){
        FirmwareModel firmwareModel = findImage(FwTYPE.DATA);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG,"currentVersion  data  ====="+currentVersion);
        byte[] data;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG,"File not file error!");
            return;
        }
        otaSteps = 1;
        Logger.d(TAG,"Data length ="+data.length);
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),
                data, currentVersion,
                ImageType.Data,currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void updateFirmware(){
        FirmwareModel firmwareModel = findImage(FwTYPE.DATA);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG,"currentVersion  firmware  ====="+currentVersion);
        byte[] data ;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG,"File not file error!");
            return;
        }
        otaSteps = 2;
        Logger.d(TAG,"Firmware length ="+data.length);
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),
                data, currentVersion,
                ImageType.Firmware,currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void startCheckingIfUpdateIsAvailable() {
        if (getActivity() == null)
            return;
        if (FirmwareUtil.isConnectionAvailable(getActivity())) {
            if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING)
                return;
            Logger.d(TAG,"startCheckingIfUpdateIsAvailable");
//            mProgressBar.setVisibility(View.VISIBLE);
//            txtdownloading.setText("");
//            linearLayout.setTag("");
            try {
                if(getActivity() != null)
                    checkUpdateAvailable = CheckUpdateAvailable.start(this, getActivity(), this, OTAUtil.getURL(getActivity()), rsrcSavedVersion, currentVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            txtdownloading.setText(getString(R.string.no_internet_connection));
            if (checkUpdateAvailable != null) {
                checkUpdateAvailable.cancel(true);
            }
        }

    }

    @Override
    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean success, final int i, final byte[] buffer) {
        Logger.d(TAG, "lightXReadBootResult command is " + command + " result is " + success);
        if (success) {
            switch (command) {
                case BootReadVersionFile: { //RSRC version is coming in ASCII character which is different format from app version.
                    int result[] = AppUtils.parseVersionFromASCIIbuffer(buffer);
                    int major = result[0];
                    int minor = result[1];
                    int revision = result[2];
                    rsrcSavedVersion = major + "." + minor + "." + revision;
                    Logger.d(TAG, "rsrcSavedVersion = " + rsrcSavedVersion);
                    PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, rsrcSavedVersion, getActivity());
                }
                break;
            }
        }
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
        super.lightXAppReadResult(var1, command, success, buffer);
        Logger.d(TAG, "command is " + command + " result is " + success);
        if (success) {
            switch (command) {
                case AppFirmwareVersion: {
                    int major, minor, revision;
                    major = buffer[0];
                    minor = buffer[1];
                    revision = buffer[2];
                    currentVersion = major + "." + minor + "." + revision;
//                    txtProgressVersion.setText("V" + currentVersion + " Installed");
                    PreferenceUtils.setString(PreferenceKeys.FirmVersion, currentVersion, getActivity());
                }
                /*
                 Wait until resource command return resource version.
                 */
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCheckingIfUpdateIsAvailable();
                        registerConnectivity();
                    }
                }, 1000);
                break;
                case AppBatteryLevel:
                    batteryLevel = com.avnera.smartdigitalheadset.Utility.getUnsignedInt(buffer, 0);
                    break;


            }
        }
    }

    @Override
    public boolean lightXFirmwareWriteStatus(final LightX lightX, LightX.FirmwareRegion firmwareRegion, final LightX.FirmwareWriteOperation firmwareWriteOperation, final double progress, Exception exception) {
        //Plug Unplug JBL_Aware headphones, the text is not correct. It should come as “Please connect the headphones to resume firmware update.”
        //In case of Bluetooth the exception was not null, but for JBL_Aware it was null. This text needs to be set in both cases, indicating that update is running.
//        FirmwareUtil.disconnectHeadphoneText = getAppActivity().getResources().getString(R.string.pls_Connect_while_upgrade_disconnected);

        if (exception != null) {
            Logger.d("demo", "Exception====" + exception.toString());
//            txtProgressVersion.setText("Failed !");
//            txtUpdating.setText("Firmware update failed.");
            AlertsDialog.showToast(getActivity(), "Communication broke during update. Please try again");
            isUpdatingFrameWork = false;
            FirmwareUtil.isUpdatingFirmWare.set(false);
            try {
//                getActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.d(TAG, String.format("%s firmware %s exception: %s", firmwareWriteOperation, firmwareRegion, exception.getLocalizedMessage()));
        } else {
//            Logger.d(String.format("%s firmware %s: %.02f%%", firmwareWriteOperation, firmwareRegion, progress * 100.0));
//            Logger.d("demo", firmwareWriteOperation + "------" + progress * 100.0 + "----" + progress + "---New proVal---" + progressFactor);
            double value = progress * 100;
            value = (progressFactor + value) / divideFactor;
            switch (firmwareWriteOperation) {
                case Erase:
                    Logger.d(TAG, "Erasing.." + value);
                case Verify:
                    Logger.d(TAG, "Verify..");
                case Write:
//                    if (txtUpdating.getText().toString().equalsIgnoreCase(getString(R.string.erasing)))
//                        txtUpdating.setText(getString(R.string.updating));
//
//                    upgradeProgress(progress,value);
                    break;

                case Checksum:
                    Logger.d(TAG, "Checksum..");
                    break;
                case Complete:
                    Logger.d(TAG, "~~~~~~~~~~Completed..");
                    if (fwlist.size() <= 0) {
                        isUpdatingFrameWork = false;
                        FirmwareUtil.isUpdatingFirmWare.set(false);
                        USB_PERMISSION_CHECK = false;
                        PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
                        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                            case NONE:
                                break;
                            case Connected_USBDevice:
//                                FirmwareUtil.disconnectHeadphoneText = getAppActivity().getResources().getString(R.string.plsConnect);
                                Logger.d(TAG, "OTA " + "No need of enterApplication");
//                                fadeIn.cancel();
//                                updateStatus.setText(getString(R.string.resetHeadphones));
//                                txtdownloading.setText(getString(R.string.replug));
//                                txtUpdating.setText("");
//                                txtUpdatingAware.setVisibility(View.VISIBLE);
//                                txtUpdatingAware.setText(getString(R.string.update_successful));
//                                linearLayout.setVisibility(View.GONE);
//                                relCableAnimation.setVisibility(View.VISIBLE);
//                                Animation animation = AnimationUtils.loadAnimation(getAppActivity(), R.anim.move);
//                                imgCableAnimation.setAnimation(animation);
//                                animation.start();
                                break;
                            case Connected_BluetoothDevice:
//                                FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.pls_Connect_restart);
                                lightX.enterApplication();
                                Logger.d(TAG, "OTA enterApplication");
//                                txtUpdating.setText(getString(R.string.update_successful));
                                try {
//                                    getActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    } else {
                        startWritingFirmware();
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void lightXIsInBootloader(final LightX lightX, boolean isInBootloader) {
        /**Return if Coneectivity is  not present **/
        if (!FirmwareUtil.isConnectionAvailable(getActivity())) {
//            txtdownloading.setText(getString(R.string.no_internet_connection));
            if (checkUpdateAvailable != null) {
                checkUpdateAvailable.cancel(true);
            }
            return;
        }

        isUpdatingFrameWork = true;

        Logger.d(TAG, " called lightXIsInBootloader isInBootloader = " + isInBootloader);
        if (isInBootloader) {
            Logger.d(TAG, "is in bootloader mode, start update");
            startUpdate();
            startWritingFirmware();
        } else {
            lightX.enterBootloader();
            Logger.d(TAG, "OTA " + "enterBootloader");
        }
    }

    @Override
    public void lightXReadConfigResult(LightX lightX, Command command, boolean success, String value) {
        Logger.d(TAG, "command is " + command + " result is " + success);
        if (success) {
            Logger.d(TAG, "config string for " + command + ": " + value);
            switch (command) {
                case ConfigProductName:
                case ConfigManufacturerName:
                case ConfigModelNumber:
            }
        } else {
            Logger.e(TAG, "failed to read config for " + command);
        }
    }


    @Override
    public void isLightXintialize() {
        super.isLightXintialize();
        Logger.d(TAG, "OTA " + "reconnected");
        Logger.e(TAG, "initizlie isUpdatingFrameWork=" + isUpdatingFrameWork + "");
        if (isUpdatingFrameWork) {
            lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
            if (lightX != null) {
                lightX.readBootImageType();
            }
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        if (FirmwareUtil.isUpdatingFirmWare.get()){
            Logger.d(TAG,"firmware is updating, ignore message");
            return;
        }
        Logger.d(TAG,"receivedResponse command ="+command+",values="+values+",status="+status);
        switch (command){
            case AmCmds.CMD_BatteryLevel: {
                batteryLevel = Integer.valueOf(values.iterator().next().getValue().toString());
                break;
            }
            case AmCmds.CMD_FirmwareVersion: {
                AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager(getActivity()).getAudioManager().getAccessoryStatus();
                String version = accessoryInfo.getFirmwareRev();
                com.avnera.smartdigitalheadset.Log.e("currentVersion : "+version);
                currentVersion = version;
//                txtProgressVersion.setText("V" + currentVersion + " Installed");
                PreferenceUtils.setString(PreferenceKeys.FirmVersion, version, getActivity());
//                txtProgressVersion.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        startCheckingIfUpdateIsAvailable();
//                        registerConnectivity();
//                    }
//                }, 1000);
                AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(currentVersion);
                break;
            }
            case AmCmds.CMD_FWInfo: {
                currentFW = Integer.valueOf(values.get(3).getValue().toString());
                Log.e(TAG, "FirmwareUtil.currentFirmware = : " + currentFW);
                break;
            }
        }
    }

    int otaSteps =0;
    int currentFW = 0;
    int index =0;
    @Override
    public void receivedStatus(StatusEvent name, Object value) {
        Logger.d(TAG,"receivedStatus StatusEvent = "+name+",value = "+value);
        switch(name){
            case PrepImageError:{
                imageUpdateError();
                break;
            }
            /**
             * Get this event when device is doing OTA.
             */
            case ImageUpdatePreparing:
                Logger.d(TAG,"message do start");
                myHandler.sendEmptyMessageDelayed(TIMER_OUT, OTA_TIME_OUT);
                break;
            case UpdateProgress: {
                Logger.d(TAG,"message do remove");
                myHandler.removeMessages(TIMER_OUT);
//                txtUpdating.setText(getString(R.string.updating));
                String pro = value.toString();
                if (!pro.equals("Success") ) {
                    Logger.d(TAG,"pro is = "+pro);
                    double progress = Double.valueOf(pro);
                    double realProgress =0;
                    double totalLen = progressInfo.paramLen +progressInfo.dataLen+progressInfo.firmwareLen;
                    if (index == 0){
                        realProgress = progress /127.0  * (progressInfo.paramLen/totalLen);
                    }else if (index == 1){
                        realProgress = progress/100.0 * (progressInfo.dataLen/totalLen) + (progressInfo.paramLen/totalLen);
                    }else if (index == 2){
                        realProgress = progress/100.0 *(progressInfo.firmwareLen/totalLen) + ((progressInfo.paramLen+progressInfo.dataLen)/totalLen);
                    }
                    Logger.d(TAG,"realProgress is = "+realProgress);
//                    updateOTAProgress(realProgress* 100.0);
                }else if (index ==2){
//                    updateOTAProgress(100);
                }
                break;
            }
            /**
             * Get this event when finished one OTA step.
             * OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateComplete: {
                break;
            }
            /**
             * Get this event when finished one OTA step.
             * OTA steps {@see CmdManager.updateImage}
             */
            case ImageUpdateFinalize: {
                Logger.d(TAG, "fwlist size =" + fwlist.size());
                if (value!= null && !value.toString().equalsIgnoreCase("Success")){
                    imageUpdateError();
                    break;
                }
                Logger.d(TAG, "otaSteps =" + otaSteps + ",currentFW =" + currentFW);
                FirmwareUtil.isUpdatingFirmWare.set(true);

                switch (otaSteps) {
                    case 0: {
                        if (fwlist.size() <= 0) {
                            isUpdatingFrameWork = false;
                            return;
                        }
                        index++;
                        try {
                            updateData();
                        } catch (ConcurrentModificationException concurrentException) {
                            concurrentException.printStackTrace();
                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted", getString(R.string.unplug_plug_while_update_error), getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 1: {
                        if (fwlist.size() <= 0) {
                            isUpdatingFrameWork = false;
                            return;
                        }
                        index++;
                        try {
                            updateFirmware();
                        } catch (ConcurrentModificationException concurrentException) {
                            concurrentException.printStackTrace();
                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted", getString(R.string.unplug_plug_while_update_error), getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 2: {
                        Logger.d(TAG, "last ota step");
                        FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
                        otaSteps = -1;
                        if (fwlist.size() <= 0) {
                            isUpdatingFrameWork = false;
                            FirmwareUtil.isUpdatingFirmWare.set(false);
                            USB_PERMISSION_CHECK = false;
                            PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
                            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                                case NONE:
                                    break;
                                case Connected_USBDevice:
                                    break;
                                case Connected_BluetoothDevice:
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity());
                                            Logger.d(TAG, "OTA startFirmware rsrcSavedVersion = "+mLiveFirmware);
                                            String curVer = transferCurrentVersion(mLiveFirmware);
                                            int valueOf = Integer.valueOf(curVer, 16);
                                            Cmd150Manager.getInstance().setFirmwareVersion(AvneraManager.getAvenraManager(getActivity()).getAudioManager()
                                                    ,valueOf);
                                            Logger.d(TAG, "enable accessory interrupts handle ImageUpdateFinalize step 2 then start firmware");
                                            Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager(getActivity()).getAudioManager(), JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
                                            Logger.d(TAG, "OTA " + "startFirmware");
                                            Cmd150Manager.getInstance().startFirmware(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),
                                                    currentFW == 0 ? (byte) 1 : (byte) 0);
                                            Logger.d(TAG, "OTA startFirmware over");
                                        }
                                    }).start();

                                    Logger.d(TAG, "OTA enterApplication");
//                                    txtUpdating.setText(getString(R.string.update_successful));
                                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateComplete(mLiveFirmware);
                                    try {
//                                        getActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
                                    } catch (Exception e) {
                                    }
                                    myHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            headPhoneStatus(false);
                                        }
                                    },2000);
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    @Override
    public void receivedAdminEvent(AdminEvent event, Object value) {
        super.receivedAdminEvent(event,value);
        switch (event) {
            case AccessoryDisconnected:{
                Logger.d(TAG, "receivedAdminEvent AccessoryDisconnected");
                FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
                myHandler.removeMessages(TIMER_OUT);
                break;
            }
        }
    }

    private String transferCurrentVersion(String version){
        String[] tempString = version.split("\\.");
        return  String.format("%02x%02x%02x",Integer.valueOf(tempString[0]),Integer.valueOf(tempString[1]),Integer.valueOf(tempString[2]));
    }

    private MyHandler myHandler = new MyHandler();
    private final static int TIMER_OUT = 0;
    private final static int OTA_TIME_OUT = 90000;
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TIMER_OUT:
                    Logger.d(TAG,"message do error");
                    imageUpdateError();
                    break;
            }
        }
    }

    private void imageUpdateError(){
        FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
        Logger.d(TAG, "imageUpdateError");
        Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
//        txtProgressVersion.setText("Failed !");
//        txtUpdating.setText("Firmware update failed.");
        AlertsDialog.showToast(getActivity(), "Communication broke during update. Please try again");
        isUpdatingFrameWork = false;
        FirmwareUtil.isUpdatingFirmWare.set(false);
        try {
//            getActivity().leftHeaderBtn.setVisibility(View.VISIBLE);
        } catch (Exception e) {

        }
        AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateFailed(mLiveFirmware);
    }

    private FirmwareModel findImage(FwTYPE type){
        int pos = 0;
        for (int i=0; i< fwlist.size();i++){
            if (fwlist.get(i).getFwtype() == type){
                pos = i;
                break;
            }
        }
        return fwlist.remove(pos);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (isAdded()) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                //should check null because in air plan mode it will be null
                if (netInfo != null && netInfo.isConnected()) {
                    /**Hide the TXT is VISIBLE **/
//                    if (txtdownloading.getVisibility() == View.VISIBLE) {
//                        txtdownloading.setVisibility(View.INVISIBLE);
//                    }

                    if (FirmwareUtil.isUpdatingFirmWare.get()) {
                        Logger.d(TAG, "Already updating");
                        return;
                    }
                    if (isUpdateAvailable && checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.FINISHED) {
                        if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED)
                            startDownloadFirmwareImage();
                    } else if (checkUpdateAvailable == null) { // In case of failure retry for server update
                        startCheckingIfUpdateIsAvailable();
                    }
                } else {
                    /** MAKE the TXT VISIBLE if INVISIBLE**/
//                    if (txtdownloading.getVisibility() == View.INVISIBLE) {
//                        txtdownloading.setVisibility(View.VISIBLE);
//                    }
//                    txtdownloading.setText(getString(R.string.no_internet_connection));

                    if (FirmwareUtil.isUpdatingFirmWare.get()) {
                        Logger.d(TAG, "Already updating");
                        return;
                    }
                    if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED) {
                        boolean isSuccessFulDownload = true;
                        for (FirmwareModel model : fwlist) {
                            if (!model.isSuccess()) {
                                isSuccessFulDownload = false;
                                break;
                            }
                        }
                        if (isSuccessFulDownload) {
//                            txtdownloading.setText("");
                            Logger.d(TAG, "No Internet No impact as data downloaded.");
                        }
                    } else if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.RUNNING) {
                        downloadProgrammingFile.cancel(true);
                        Logger.d(TAG, "No Internet downloadProgrammingFile.cancel");
                    } else if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING) {
                        checkUpdateAvailable.cancel(true);
                        Logger.d(TAG, "No Internet checkUpdateAvailable.cancel");
                    }
//                    mProgressBar.setVisibility(View.GONE);
                }
            }
        }
    }
}
