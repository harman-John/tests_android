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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
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

import static jbl.stc.com.activity.DashboardActivity.*;

public class OTAFragment extends BaseFragment implements View.OnClickListener,OnDownloadedListener {
    public static final String TAG = OTAFragment.class.getSimpleName();

    private long batteryLevel;
    private String mOnLineFirmware = "UNKNOWN";
    private String onLineFwVersion = "0.0.0";
    private String deviceFwVersion = "0.0.0";
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
    private CheckUpdateAvailable checkUpdateAvailable;
    public static boolean USB_PERMISSION_CHECK;

    private DownloadProgrammingFile downloadProgrammingFile;

    private View view;
    private Bundle args;
    private TextView textViewUpdateStatus;
    private TextView textViewUpdateStatusTitle;
    private TextView textViewOTACircle;
    private TextView textViewProgress;
    private TextView textViewButtonDone;
//    private ImageView iv_ok;
    private ImageView imageViewBack;

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
        myHandler.sendEmptyMessage(MSG_CHECK_UPDATE);
        view.setOnClickListener(this);
        imageViewBack = view.findViewById(R.id.image_view_ota_back);
        imageViewBack.setOnClickListener(this);
        textViewUpdateStatus = view.findViewById(R.id.updateStatus);
        textViewUpdateStatus.setVisibility(View.GONE);
        textViewUpdateStatusTitle = view.findViewById(R.id.updateStatusTitle);
        textViewUpdateStatusTitle.setVisibility(View.GONE);
        textViewOTACircle = view.findViewById(R.id.text_view_ota_circle);
        textViewOTACircle.setOnClickListener(this);
        textViewProgress = view.findViewById(R.id.text_progress);
        textViewProgress.setOnClickListener(this);
        textViewButtonDone = view.findViewById(R.id.button_done);
//        iv_ok = view.findViewById(R.id.iv_ok);
        textViewButtonDone.setOnClickListener(this);
        otaInit();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (args != null && args.containsKey("lightXIsInBootloader")) {
            batteryLevel = 51;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_UPDATE_DEVICE);

        readBasicInformation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_ota_back:{
                getActivity().onBackPressed();
                break;
            }
            case R.id.text_view_ota_circle:
            case R.id.text_progress:{
                if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {

                } else if (batteryLevel < 50) {
                    AlertsDialog.showSimpleDialogWithOKButtonWithBack(null, getString(R.string.battery_alert), getActivity());
                } else {
                    if (FirmwareUtil.isConnectionAvailable(getActivity())) {
                        startDownloadFirmwareImage();
                        v.setOnClickListener(null);
                        textViewOTACircle.setOnClickListener(null);
                        otaUpdating();
                    } else {
                        otaError(R.string.updating_failed_case_2);
                    }
                }
                break;
            }
            case R.id.button_done:{
                getDashboardActivity().onBackPressed();
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
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Logger.d(TAG,"getFirmwareInfo");
                ANCControlManager.getANCManager(getActivity()).getFirmwareInfo(lightX);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Logger.d(TAG,"getFirmwareVersion");
                ANCControlManager.getANCManager(getActivity()).getFirmwareVersion(lightX);
                if (lightX!= null) {
                    lightX.readBootVersionFileResource();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Logger.d(TAG,"getBatterLeverl");
                ANCControlManager.getANCManager(getActivity()).getBatterLeverl(lightX);

                myHandler.sendEmptyMessage(MSG_IN_BOOTLOADER_AUTO_START);
            }
        }).start();

    }

    private void registerConnectivity() {
        if (getActivity() == null)
            return;
        networkChangeReceiver = new NetworkChangeReceiver();
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
        Logger.d(TAG,"startDownloadFirmwareImage");
        if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {
            return;
        }
        downloadProgrammingFile = new DownloadProgrammingFile(getActivity(), this, DashboardActivity.mFwlist);
        try {
            downloadProgrammingFile.executeOnExecutor(DownloadProgrammingFile.THREAD_POOL_EXECUTOR, OTAUtil.getURL(getActivity()));
        } catch (Exception e) {
            e.printStackTrace();
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
            DashboardActivity.mFwlist = fwList;
            divideFactor = 2 * DashboardActivity.mFwlist.size();
            otaAvailable();
            if (args != null && args.containsKey("lightXIsInBootloader")) {
                startDownloadFirmwareImage();
            }
        } else {
            unregisterNetworkReceiverSafely();
        }
    }

    @Override
    public void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwList) throws FileNotFoundException {
        USB_PERMISSION_CHECK = true;
//        mProgressBar.setVisibility(View.GONE);
//        updateStatus.setVisibility(View.VISIBLE);
        DashboardActivity.mFwlist = fwList;
        if (fwList.size() != 0) {
            boolean isSuccessFulDownload = true;
            for (FirmwareModel model : fwList) {
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
                isDoingOTANow = true;
                if (lightX == null) {
                    startUpdate();
                    if (fwList.size() <= 0) {
                        isDoingOTANow = false;
                        return;
                    }
                    index =0;
                    FirmwareUtil.isUpdatingFirmWare.set(true);
                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateStarted(mOnLineFirmware);
                    if (progressInfo == null) {
                        progressInfo = new ProgressInfo();
                    }
                    for (FirmwareModel model : fwList) {
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
        AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.unplug_plug_while_update_error));
        otaError(R.string.updating_failed_case_3);
    }

    @Override
    public void onFailedToCheckUpdate() {

    }

    @Override
    public void onUpgradeUpdate(String liveVersion, String title) {
        mOnLineFirmware = liveVersion;
        Log.i(TAG,"mOnLineFirmware is "+ mOnLineFirmware);
    }

    private void startUpdate(){
        FirmwareUtil.isUpdatingFirmWare.set(true);
        otaUpdating();
        unregisterNetworkReceiverSafely();
        Logger.d(TAG, "Writing start");
    }

    public void startWritingFirmware() {
        try {
            if (DashboardActivity.mFwlist.size() <= 0) {
                Log.i(TAG,"startWritingFirmware fwlist size is 0");
                lightX.enterApplication();
                isDoingOTANow = false;
                return;
            }
            FirmwareUtil.isUpdatingFirmWare.set(true);
            int size = DashboardActivity.mFwlist.size() - 1;
            FirmwareModel firmwareModel = DashboardActivity.mFwlist.remove(size);
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
        Logger.d(TAG,"onLineFwVersion param   ====="+version);
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
        if (currentFW < 0){
            Log.i(TAG,"currentFw is "+currentFW+", unable to upgrade");
            imageUpdateError();
            return;
        }
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),
                data, currentVersion,
                ImageType.Parameters,currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void updateData(){
        FirmwareModel firmwareModel = findImage(FwTYPE.DATA);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG,"onLineFwVersion  data  ====="+version);
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
        Logger.d(TAG,"onLineFwVersion  firmware  ====="+version);
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
            if (checkUpdateAvailable != null && checkUpdateAvailable.isRunnuning())
                return;
            Logger.d(TAG,"startCheckingIfUpdateIsAvailable");
            try {
                String srcSavedVersion = PreferenceUtils.getString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, getActivity(), "");
                String currentVersion = PreferenceUtils.getString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.APP_VERSION, getActivity(), "");
                deviceFwVersion = srcSavedVersion;
                onLineFwVersion = currentVersion;
                if(getActivity() != null)
                    checkUpdateAvailable = CheckUpdateAvailable.start(this, getActivity(), this, OTAUtil.getURL(getActivity()), srcSavedVersion, currentVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            otaError(R.string.updating_failed_case_2);
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
                    deviceFwVersion = major + "." + minor + "." + revision;
                    Logger.d(TAG, "deviceFwVersion = " + deviceFwVersion);
//                    otaSuccess(deviceFwVersion);
                    PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, deviceFwVersion, getActivity());
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
                    onLineFwVersion = major + "." + minor + "." + revision;
//                    txtProgressVersion.setText("V" + onLineFwVersion + " Installed");
                    Logger.d(TAG, "onLineFwVersion is " + onLineFwVersion);
                    PreferenceUtils.setString(PreferenceKeys.FirmVersion, onLineFwVersion, getActivity());
                }
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
            Logger.d(TAG, "Exception====" + exception.toString());
//            txtProgressVersion.setText("Failed !");
//            txtUpdating.setText("Firmware update failed.");
            AlertsDialog.showToast(getActivity(), "Communication broke during update. Please try again");
            isDoingOTANow = false;
            FirmwareUtil.isUpdatingFirmWare.set(false);
            otaError(R.string.updating_failed_case_1);
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
                    if (progress * 100 == 100.0) {
                        progressFactor += 100.0;
                    }
                    updateOTAProgress(value);
                    break;

                case Checksum:
                    Logger.d(TAG, "Checksum..");
                    break;
                case Complete:
                    Logger.d(TAG, "~~~~~~~~~~Completed..");
                    if (DashboardActivity.mFwlist.size() <= 0) {
                        FirmwareUtil.isUpdatingFirmWare.set(false);
                        mIsEnterBootloader = false;
                        USB_PERMISSION_CHECK = false;
                        PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
                        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                            case NONE:
                                break;
                            case Connected_USBDevice:
                                deviceRePlug();
                                break;
                            case Connected_BluetoothDevice:
                                lightX.enterApplication();
                                Logger.d(TAG, "OTA enterApplication");
                                deviceRestarting();
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
            otaError(R.string.updating_failed_case_2);
            return;
        }
        Logger.d(TAG, " called lightXIsInBootloader aa isInBootloader = " + isInBootloader);
        if (isInBootloader) {
            Logger.d(TAG, "is in bootloader mode, start update");
            startUpdate();
            startWritingFirmware();
        } else {
            lightX.enterBootloader();
            mIsEnterBootloader = true;
            Logger.d(TAG, "OTA enterBootloader");
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
    public void isLightXInitialize() {
        super.isLightXInitialize();
        Logger.e(TAG, "isLightXInitialize mIsEnterBootloader=" + mIsEnterBootloader);
        if (mIsEnterBootloader) {
            lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
            if (lightX != null) {
                Logger.e(TAG, "readBootImageType");
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
                com.avnera.smartdigitalheadset.Log.e("onLineFwVersion : "+version);
                onLineFwVersion = version;
                PreferenceUtils.setString(PreferenceKeys.FirmVersion, version, getActivity());
                AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(onLineFwVersion);
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
                myHandler.sendEmptyMessageDelayed(MSG_TIMER_OUT, OTA_TIME_OUT);
                break;
            case UpdateProgress: {
                Logger.d(TAG,"message do remove");
                myHandler.removeMessages(MSG_TIMER_OUT);
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
                    updateOTAProgress(realProgress* 100.0);
                }else if (index ==2){
                    updateOTAProgress(100);
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
                Logger.d(TAG, "fwlist size =" + DashboardActivity.mFwlist.size());
                if (value!= null && !value.toString().equalsIgnoreCase("Success")){
                    imageUpdateError();
                    break;
                }
                Logger.d(TAG, "otaSteps =" + otaSteps + ",currentFW =" + currentFW);
                FirmwareUtil.isUpdatingFirmWare.set(true);

                switch (otaSteps) {
                    case 0: {
                        if (DashboardActivity.mFwlist.size() <= 0) {
                            isDoingOTANow = false;
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
                        if (DashboardActivity.mFwlist.size() <= 0) {
                            isDoingOTANow = false;
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
                        if (DashboardActivity.mFwlist.size() <= 0) {

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
                                            Logger.d(TAG, "OTA startFirmware deviceFwVersion = "+ mOnLineFirmware);
                                            String curVer = transferCurrentVersion(mOnLineFirmware);
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
                                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateComplete(mOnLineFirmware);
                                    deviceRestarting();
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    private void otaInit(){
        textViewProgress.setVisibility(View.GONE);
        textViewUpdateStatus.setVisibility(View.GONE);
        textViewUpdateStatusTitle.setVisibility(View.GONE);
        textViewOTACircle.setVisibility(View.GONE);
        textViewButtonDone.setVisibility(View.GONE);
    }

    private void otaAvailable(){
        for (FirmwareModel model : DashboardActivity.mFwlist) {
            switch (model.getFwtype()) {
                case RSRC:
                    Logger.i(TAG,"onLineFwVersion is "+model.getVersion() );
                    textViewUpdateStatus.setText(getString(R.string.firmware_is_available,model.getVersion()));
                    break;
                case BOOT:
                    break;
                case APP:
                case PARAM:
                    Logger.i(TAG,"onLineFwVersion is "+model.getVersion());
                    textViewUpdateStatus.setText(getString(R.string.firmware_is_available,model.getVersion()));
                    break;
            }
        }
        textViewOTACircle.setVisibility(View.VISIBLE);
        textViewProgress.setVisibility(View.GONE);
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.getPaint().setFakeBoldText(false);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.black));
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        textViewUpdateStatusTitle.setVisibility(View.GONE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.ota_install_button));
        textViewButtonDone.setVisibility(View.GONE);
//        iv_ok.setVisibility(View.GONE);
    }

    private void otaUpdating(){
        imageViewBack.setVisibility(View.GONE);
        textViewUpdateStatusTitle.setVisibility(View.VISIBLE);
        textViewUpdateStatusTitle.setText(R.string.firmware_is_updating_title);
        textViewUpdateStatusTitle.getPaint().setFakeBoldText(true);
        textViewUpdateStatusTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,19);
        textViewUpdateStatusTitle.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.black));
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(R.string.firmware_is_updating);
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(),R.mipmap.update_circle));
        textViewProgress.setVisibility(View.VISIBLE);
        textViewProgress.setText("0%");
        textViewButtonDone.setVisibility(View.GONE);
//        iv_ok.setVisibility(View.GONE);
    }

    public void otaSuccess(){
        imageViewBack.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(getString(R.string.firmware_is_installed,onLineFwVersion));
        textViewUpdateStatus.getPaint().setFakeBoldText(true);
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(),android.R.color.black));
        textViewUpdateStatusTitle.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.GONE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(),R.mipmap.update_succeeded));
        textViewButtonDone.setVisibility(View.VISIBLE);

//        otaSuccess(onLineFwVersion);
        isDoingOTANow = false;
//        iv_ok.setVisibility(View.VISIBLE);
    }

    private void deviceRePlug(){
        textViewProgress.setText("Plug out-in again");
    }

    private void deviceRestarting(){
        textViewProgress.setText("Restarting...");
    }

    private void otaError(int resId){
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(resId);
        textViewUpdateStatus.getPaint().setFakeBoldText(true);
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        textViewUpdateStatusTitle.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.VISIBLE);
        textViewOTACircle.setVisibility(View.VISIBLE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(),R.mipmap.update_failed));
        textViewProgress.setText("Failed!");
    }

    @Override
    public void receivedAdminEvent(AdminEvent event, Object value) {
        super.receivedAdminEvent(event,value);
        switch (event) {
            case AccessoryDisconnected:{
                Logger.d(TAG, "receivedAdminEvent AccessoryDisconnected");
                FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
                myHandler.removeMessages(MSG_TIMER_OUT);
                break;
            }
        }
    }

    private void updateOTAProgress(double realProgress){
        if (realProgress >= 0) {
            try {
                DecimalFormat oneDigit = new DecimalFormat("#,##0.0");//format to 1 decimal place
                String s = oneDigit.format(realProgress);
                textViewProgress.setText(s + " %");
            } catch (Exception e) {
                Logger.e(TAG ,"realProgress:"+realProgress);
            }
        }
    }

    private String transferCurrentVersion(String version){
        String[] tempString = version.split("\\.");
        return  String.format("%02x%02x%02x",Integer.valueOf(tempString[0]),Integer.valueOf(tempString[1]),Integer.valueOf(tempString[2]));
    }

    private MyHandler myHandler = new MyHandler();
    private final static int MSG_TIMER_OUT = 0;
    private final static int MSG_CHECK_UPDATE = 1;
    private final static int MSG_IN_BOOTLOADER_AUTO_START = 2;
    private final static int OTA_TIME_OUT = 90000;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_TIMER_OUT:
                    Logger.d(TAG,"message do error");
                    imageUpdateError();
                    break;
                case MSG_CHECK_UPDATE:
                    startCheckingIfUpdateIsAvailable();
                    registerConnectivity();
                    break;
                case MSG_IN_BOOTLOADER_AUTO_START:
                    Log.i(TAG,"mIsInBootloader "+mIsInBootloader);
                    if (mIsInBootloader){
                        if (FirmwareUtil.isConnectionAvailable(getActivity())) {
                            startDownloadFirmwareImage();
                            otaUpdating();
                        } else {
                            otaError(R.string.updating_failed_case_2);
                        }
                    }
                    break;
            }
        }
    }

    private void imageUpdateError(){
        FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
        Logger.d(TAG, "imageUpdateError");
        Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager(getActivity()).getAudioManager(),JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
        otaError(R.string.updating_failed_case_1);
        isDoingOTANow = false;
        FirmwareUtil.isUpdatingFirmWare.set(false);
        imageViewBack.setVisibility(View.VISIBLE);
        AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateFailed(mOnLineFirmware);
    }

    private FirmwareModel findImage(FwTYPE type){
        int pos = 0;
        for (int i=0; i< DashboardActivity.mFwlist.size();i++){
            if (DashboardActivity.mFwlist.get(i).getFwtype() == type){
                pos = i;
                break;
            }
        }
        return DashboardActivity.mFwlist.remove(pos);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (isAdded()) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnected()) {
                        if (FirmwareUtil.isUpdatingFirmWare.get()) {
                            Logger.d(TAG, "Already updating");
                            return;
                        }
                        if (isUpdateAvailable && checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.FINISHED) {
                            if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED) {
                                startDownloadFirmwareImage();
                            }else{
                                startCheckingIfUpdateIsAvailable();
                            }
                        } else if (checkUpdateAvailable == null) { // In case of failure retry for server update
                            startCheckingIfUpdateIsAvailable();
                        }
                    } else {
                        if (FirmwareUtil.isUpdatingFirmWare.get()) {
                            Logger.d(TAG, "Already updating");
                            return;
                        }
                        if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED) {
                            boolean isSuccessFulDownload = true;
                            for (FirmwareModel model : DashboardActivity.mFwlist) {
                                if (!model.isSuccess()) {
                                    isSuccessFulDownload = false;
                                    break;
                                }
                            }
                            if (isSuccessFulDownload) {
                                Logger.d(TAG, "No Internet No impact as data downloaded.");
                            }
                        } else if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.RUNNING) {
                            downloadProgrammingFile.cancel(true);
                            Logger.d(TAG, "No Internet downloadProgrammingFile.cancel");
                        } else if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING) {
                            checkUpdateAvailable.cancel(true);
                            Logger.d(TAG, "No Internet checkUpdateAvailable.cancel");
                        } else {
                            otaError(R.string.updating_failed_case_2);
                        }
                    }
                }
            }
        }
    }
}
