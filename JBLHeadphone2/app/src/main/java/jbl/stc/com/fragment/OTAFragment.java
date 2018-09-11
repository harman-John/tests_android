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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.AccessoryInfo;
import com.avnera.audiomanager.ImageType;
import com.avnera.smartdigitalheadset.Debug;
import com.avnera.smartdigitalheadset.LightX;
import com.harman.bluetooth.engine.BesEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.data.DeviceConnectionManager;
import jbl.stc.com.data.FwTYPE;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.listener.OnOtaListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.Cmd150Manager;
import jbl.stc.com.manager.DeviceManager;
import jbl.stc.com.manager.LiveManager;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.ota.CheckUpdateAvailable;
import jbl.stc.com.ota.DownloadProgrammingFile;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.EnumCommands;
import jbl.stc.com.utils.FirmwareUtil;
import jbl.stc.com.utils.OTAUtil;
import jbl.stc.com.view.AppButton;
import jbl.stc.com.view.ShadowLayout;

import static jbl.stc.com.activity.DashboardActivity.isOTADoing;
import static jbl.stc.com.activity.DashboardActivity.mFwList;

public class OTAFragment extends BaseFragment implements View.OnClickListener, OnDownloadedListener {
    public static final String TAG = OTAFragment.class.getSimpleName();

    private long batteryLevel;
    private String mOnLineFirmware = "UNKNOWN";
    private String onLineFwVersion = "0.0.0";
    private String deviceFwVersion = "0.0.0";
    private ProgressInfo progressInfo = null;

    private class ProgressInfo {
        public double paramLen;
        public double dataLen;
        public double firmwareLen;
    }

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
    private TextView textViewOTACircle;
    private TextView textViewProgress;
    private AppButton textViewButtonDone;
    private ShadowLayout shadowLayout;
    //    private ImageView iv_ok;
    private ImageView imageViewBack;


    private int otaSteps = 0;
    private int currentFW = 0;
    private int index = 0;

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
        textViewOTACircle = view.findViewById(R.id.text_view_ota_circle);
        textViewOTACircle.setOnClickListener(this);
        textViewProgress = view.findViewById(R.id.text_progress);
        textViewProgress.setOnClickListener(this);
        textViewButtonDone = view.findViewById(R.id.button_done);
        shadowLayout = view.findViewById(R.id.shadowLayout);
//        iv_ok = view.findViewById(R.id.iv_ok);
        textViewButtonDone.setOnClickListener(this);
        otaInit();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (args != null && args.containsKey("lightXIsInBootloader")) {
            Logger.i(TAG, "on resume, lightX is in bootloader ");
            batteryLevel = 51;
        } else {
            myHandler.sendEmptyMessage(MSG_READ_BASIC_INFO);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.i(TAG, "on resume");
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_UPDATE_DEVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_ota_back: {
                getActivity().onBackPressed();
                if (mOnOtaListener != null) {
                    mOnOtaListener.onButtonDone();
                }
                break;
            }
            case R.id.text_view_ota_circle:
            case R.id.text_progress: {
                if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {

                } else if (batteryLevel < 50) {
                    AlertsDialog.showSimpleDialogWithOKButtonWithBack(null, getString(R.string.battery_alert), getActivity());
                } else {
                    if (FirmwareUtil.isConnectionAvailable(getActivity())) {
                        Logger.e(TAG, "on click, start download firmwareImage");
                        if (!(args != null && args.containsKey("lightXIsInBootloader"))) {
                            startDownloadFirmwareImage();
                            v.setOnClickListener(null);
                            textViewOTACircle.setOnClickListener(null);
                            otaUpdating();
                        }
                    } else {
                        otaError(false, R.string.update_failed_connection, R.string.update_failed_connection_detail);
                    }
                }
                break;
            }
            case R.id.button_done: {
                if (((TextView) v).getText().equals(getString(R.string.got_it))) {
                    getActivity().onBackPressed();
                } else if (((TextView) v).getText().equals(getString(R.string.retry))) {
                    if (FirmwareUtil.isUpdatingFirmWare.get()) {
                        if (AvneraManager.getAvenraManager().getLightX() != null) {
                            progressFactor = 0.0f;
                            DeviceManager.getInstance(getActivity()).setIsNeedOtaAgain(true);
                            startCheckingIfUpdateIsAvailable();
                            otaUpdating();
                        }
                    }
                } else {
                    getActivity().onBackPressed();
                    if (mOnOtaListener != null) {
                        mOnOtaListener.onButtonDone();
                    }
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

    public void getDeviceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BesEngine.getInstance().isConnected()) {
                    LiveManager.getInstance().updateImage(ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED).mac);
                }else{
                    interval();
                    Logger.d(TAG, "read basic information, getFirmwareInfo");
                    ANCControlManager.getANCManager(getActivity()).getFirmwareInfo();
                    interval();
                    Logger.d(TAG, "read basic information, getFirmwareVersion");
                    ANCControlManager.getANCManager(getActivity()).getFirmwareVersion();
                    ANCControlManager.getANCManager(getActivity()).readBootVersionFileResource();
                    interval();
                    Logger.d(TAG, "read basic information, getBatterLevel");
                    ANCControlManager.getANCManager(getActivity()).getBatterLevel();
                }
            }
        }).start();

    }

    private void interval(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == DownloadProgrammingFile.Status.RUNNING) {
            return;
        }
        Logger.d(TAG, "start download firmwareImage .....................................mFwList size = " + mFwList.size());
        downloadProgrammingFile = new DownloadProgrammingFile(getActivity(), this, mFwList);
        try {
            downloadProgrammingFile.executeOnExecutor(DownloadProgrammingFile.THREAD_POOL_EXECUTOR, OTAUtil.getURL(getActivity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsUpdateAvailable(boolean isUpdateAvailable, CopyOnWriteArrayList<FirmwareModel> fwList) {
        if (getActivity() == null) {
            Logger.e(TAG, "set is update available, Activity is null");
            return;
        }

        if (!isAdded()) {
            Logger.e(TAG, "set is update available, This fragment is not added");
            return;
        }

        this.isUpdateAvailable = isUpdateAvailable;
        Logger.d(TAG, "set is update available,  isUpdateAvailable=" + isUpdateAvailable + ",isNeedOtaAgain =" + DeviceManager.getInstance(getActivity()).isNeedOtaAgain() + ",fwList size = " + fwList.size());
        if (isUpdateAvailable) {
            mFwList = fwList;
            divideFactor = 2 * DashboardActivity.mFwList.size();
            if (args != null && args.containsKey("lightXIsInBootloader") || DeviceManager.getInstance(getActivity()).isNeedOtaAgain()) {
                DeviceManager.getInstance(getActivity()).setIsNeedOtaAgain(false);
                Logger.e(TAG, "set is update available,  startDownloadFirmwareImage");
                startDownloadFirmwareImage();
            } else {
                otaAvailable();
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
        Logger.d(TAG, "on downloaded firmware, ...................fwList size = " + fwList.size());
        DashboardActivity.mFwList = fwList;
        if (fwList.size() != 0) {
            boolean isSuccessFulDownload = true;
            for (FirmwareModel model : fwList) {
                Logger.d(TAG, "on downloaded firmware, version = " + model.getVersion());
                if (!model.isSuccess()) {
                    isSuccessFulDownload = false;
                    break;
                }
            }
            Logger.i(TAG, "on downloaded firmware, isSuccessFulDownload = " + isSuccessFulDownload);
            if (isSuccessFulDownload) {
                isOTADoing = true;
                if (AvneraManager.getAvenraManager().getLightX() == null) {
                    Logger.i(TAG, "on downloaded firmware, lightX is null");
                    startUpdate();
                    if (fwList.size() <= 0) {
                        isOTADoing = false;
                        return;
                    }
                    index = 0;
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
                } else {
                    Logger.e(TAG, "on downloaded firmware, onDownloadedFirmware readBootImageType");
                    ANCControlManager.getANCManager(getActivity()).readBootImageType();
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
//        otaError(R.string.updating_failed_case_3);
        Logger.i(TAG, "on failed download");
    }

    @Override
    public void onFailedToCheckUpdate() {

    }

    @Override
    public void onUpgradeUpdate(String liveVersion, String title) {
        mOnLineFirmware = liveVersion;
        Logger.d(TAG, "on upgrade update, mOnLineFirmware is " + mOnLineFirmware);
    }

    private void startUpdate() {
        FirmwareUtil.isUpdatingFirmWare.set(true);
        otaUpdating();
        unregisterNetworkReceiverSafely();
        Logger.d(TAG, "start update");
    }

    public void startWritingFirmware() {
        try {
            if (mFwList.size() <= 0) {
                Logger.d(TAG, "start writing firmware, fwList size is 0");
                ANCControlManager.getANCManager(getActivity()).enterApplication();
                isOTADoing = false;
                return;
            }
            FirmwareUtil.isUpdatingFirmWare.set(true);
            int size = mFwList.size() - 1;
            Logger.i(TAG, "start writing firmware,  otaFwListSaved size = " + mFwList.size());
            FirmwareModel firmwareModel = mFwList.remove(size);
            byte[] data;
            switch (firmwareModel.getFwtype()) {
                case APP:
                    data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
                    Logger.i(TAG, "start writing firmware,  APP getName =  " + firmwareModel.getName());
                    Logger.d(TAG, "start writing firmware, first 1024 bytes of firmware to write:\n" + Debug.hexify(data, 0, 1024));
                    AvneraManager.getAvenraManager().getLightX().writeFirmware(LightX.FirmwareRegion.Application, data);
                    break;
                case RSRC:
                    data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
                    Logger.i(TAG, "start writing firmware,  RSRC getName =  " + firmwareModel.getName());
                    Logger.d(TAG, "start writing firmware, first 1024 bytes of firmware to write:\n" + Debug.hexify(data, 0, 1024));
                    AvneraManager.getAvenraManager().getLightX().writeFirmware(LightX.FirmwareRegion.Resource, data);
                    break;
                case BOOT:
                    startWritingFirmware();
                    break;
            }
//        } catch (ConcurrentMdificationException concurrentException) {
//            //already an update running, UI stucks at 0% otherwise.
//            concurrentException.printStackTrace();
//            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted 0", getString(R.string.unplug_plug_while_update_error), getActivity());
//            AnalyticsManager.getInstance(getActivity()).reportUsbUpdateAlert(getString(R.string.unplug_plug_while_update_error));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateParam() {
        Logger.d(TAG, "update param, disable accessory interrupts");
        Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager().getAudioManager(), JBLConstant.DISABLE_ACCESSORY_INTERRUPTS);
        FirmwareModel firmwareModel = findImage(FwTYPE.PARAM);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG, "update param, onLineFwVersion param   =====" + version);
        byte[] data;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG, "update param, File not file error!");
            return;
        }
        if (data == null){
            Logger.e(TAG,"update param, data is null, return");
            return;
        }
        otaSteps = 0;
        Logger.d(TAG, "update param, currentFW =" + currentFW);
        Logger.d(TAG, "update param, PARAM length =" + data.length);
        if (currentFW < 0) {
            Logger.d(TAG, "update param, currentFw is " + currentFW + ", unable to upgrade");
            imageUpdateError();
            return;
        }
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager().getAudioManager(),
                data, currentVersion,
                ImageType.Parameters, currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void updateData() {
        FirmwareModel firmwareModel = findImage(FwTYPE.DATA);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG, "update data, onLineFwVersion  data  =====" + version);
        byte[] data;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG, "update data, File not file error!");
            return;
        }
        otaSteps = 1;
        Logger.d(TAG, "update data, Data length =" + data.length);
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager().getAudioManager(),
                data, currentVersion,
                ImageType.Data, currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void updateFirmware() {
        FirmwareModel firmwareModel = findImage(FwTYPE.DATA);
        String version = firmwareModel.getVersion();
        String currentVersion = transferCurrentVersion(version);
        Logger.d(TAG, "update firmware, onLineFwVersion  firmware  =====" + version);
        byte[] data;
        try {
            data = FirmwareUtil.readInputStream(new FileInputStream(firmwareModel.getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(TAG, "update firmware, File not file error!");
            return;
        }
        otaSteps = 2;
        Logger.d(TAG, "update firmware, Firmware length =" + data.length);
        Cmd150Manager.getInstance().updateImage(AvneraManager.getAvenraManager().getAudioManager(),
                data, currentVersion,
                ImageType.Firmware, currentFW == 0 ? (byte) 1 : (byte) 0);
    }

    private void startCheckingIfUpdateIsAvailable() {
        if (getActivity() == null)
            return;
        if (FirmwareUtil.isConnectionAvailable(getActivity())) {
            if (checkUpdateAvailable != null && checkUpdateAvailable.isRunnuning())
                return;
            Logger.d(TAG, "start checking if update is available, modelNumber" + AppUtils.getModelNumber(getActivity()));
            try {
                String srcSavedVersion = PreferenceUtils.getString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, getActivity(), "0.0.0");
                String currentVersion = PreferenceUtils.getString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.APP_VERSION, getActivity(), "");
                deviceFwVersion = srcSavedVersion;
                onLineFwVersion = currentVersion;

                Logger.d(TAG, "start checking if update is available,  srcSavedVersion = " + srcSavedVersion + ",currentVersion = " + currentVersion);
                if (getActivity() != null)
                    checkUpdateAvailable = CheckUpdateAvailable.start(this, getActivity(), this, OTAUtil.getURL(getActivity()), srcSavedVersion, currentVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            otaError(false, R.string.update_failed_connection, R.string.update_failed_connection_detail);
            if (checkUpdateAvailable != null) {
                checkUpdateAvailable.cancel(true);
            }
        }

    }

//    @Override
//    public void lightXReadBootResult(final LightX lightX, final Command command, final boolean success, final int i, final byte[] buffer) {
//        Logger.d(TAG, "lightXReadBootResult command is " + command + " result is " + success);
//        if (success) {
//            switch (command) {
//                case BootReadVersionFile: { //RSRC version is coming in ASCII character which is different format from app version.
//                    int result[] = AppUtils.parseVersionFromASCIIbuffer(buffer);
//                    int major = result[0];
//                    int minor = result[1];
//                    int revision = result[2];
//                    deviceFwVersion = major + "." + minor + "." + revision;
//                    Logger.d(TAG, "deviceFwVersion = " + deviceFwVersion);
////                    otaSuccess(deviceFwVersion);
//                    PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, deviceFwVersion, getActivity());
//                }
//                break;
//            }
//        }
//    }

//    @Override
//    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
//        super.lightXAppReadResult(var1, command, success, buffer);
//        Logger.d(TAG, "command is " + command + " result is " + success);
//        if (success) {
//            switch (command) {
//                case AppFirmwareVersion: {
//                    int major, minor, revision;
//                    major = buffer[0];
//                    minor = buffer[1];
//                    revision = buffer[2];
//                    onLineFwVersion = major + "." + minor + "." + revision;
////                    txtProgressVersion.setText("V" + onLineFwVersion + " Installed");
//                    Logger.d(TAG, "onLineFwVersion is " + onLineFwVersion);
//                    PreferenceUtils.setString(PreferenceKeys.FirmVersion, onLineFwVersion, getActivity());
//                }
//                break;
//                case AppBatteryLevel:
//                    batteryLevel = com.avnera.smartdigitalheadset.Utility.getUnsignedInt(buffer, 0);
//                    break;
//
//
//            }
//        }
//    }

//    @Override
//    public boolean lightXFirmwareWriteStatus(final LightX lightX, LightX.FirmwareRegion firmwareRegion, final LightX.FirmwareWriteOperation firmwareWriteOperation, final double progress, Exception exception) {
//        //Plug Unplug JBL_Aware headphones, the text is not correct. It should come as “Please connect the headphones to resume firmware update.”
//        //In case of Bluetooth the exception was not null, but for JBL_Aware it was null. This text needs to be set in both cases, indicating that update is running.
////        FirmwareUtil.disconnectHeadphoneText = getAppActivity().getResources().getString(R.string.pls_Connect_while_upgrade_disconnected);
//
//        if (exception != null) {
//            Logger.d(TAG, "lightXFirmwareWriteStatus Exception====" + exception.toString());
//            AlertsDialog.showToast(getActivity(), "Communication broke during update. Please try again");
////            isOTADoing = false;
////            FirmwareUtil.isUpdatingFirmWare.set(false);
////            otaError(R.string.updating_failed_case_1);
//            Logger.d(TAG, String.format("lightXFirmwareWriteStatus %s firmware %s exception: %s", firmwareWriteOperation, firmwareRegion, exception.getLocalizedMessage()));
//        } else {
//            double value = progress * 100;
//            value = (progressFactor + value) / divideFactor;
//            switch (firmwareWriteOperation) {
//                case Erase:
//                    Logger.d(TAG, "Erasing.." + value);
//                case Verify:
//                    Logger.d(TAG, "Verify..");
//                case Write:
//                    myHandler.removeMessages(MSG_TIMER_OUT);
//                    myHandler.sendEmptyMessageDelayed(MSG_TIMER_OUT, OTA_TIME_OUT);
//                    if (progress * 100 == 100.0) {
//                        progressFactor += 100.0;
//                    }
//                    Logger.d(TAG, "Write.." + value);
//                    updateOTAProgress(value);
//                    break;
//                case Checksum:
//                    Logger.d(TAG, "Checksum..");
//                    break;
//                case Complete: {
//                    Logger.d(TAG, "~~~~~~~~~~Completed..");
//                    myHandler.removeMessages(MSG_TIMER_OUT);
//                    if (DashboardActivity.mFwList.size() <= 0) {
//                        FirmwareUtil.isUpdatingFirmWare.set(false);
//                        USB_PERMISSION_CHECK = false;
//                        PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
//                        switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//                            case NONE:
//                                break;
//                            case Connected_USBDevice:
//                                deviceRePlug();
//                                break;
//                            case Connected_BluetoothDevice:
//                                AvneraManager.getAvenraManager().getLightX().enterApplication();
//                                Logger.d(TAG, "OTA enterApplication");
//                                deviceRestarting();
//                                break;
//                        }
//                    } else {
//                        startWritingFirmware();
//                    }
//                    break;
//                }
//                default:{
//                    Logger.d(TAG, "--------------default..");
//                }
//            }
//        }
//        return false;
//    }


//    @Override
//    public void lightXIsInBootloader(final LightX lightX, boolean isInBootloader) {
//        /**Return if Coneectivity is  not present **/
//        if (!FirmwareUtil.isConnectionAvailable(getActivity())) {
////            txtdownloading.setText(getString(R.string.no_internet_connection));
//            if (checkUpdateAvailable != null) {
//                checkUpdateAvailable.cancel(true);
//            }
//            otaError(false,R.string.update_failed_connection,R.string.update_failed_connection_detail);
//            return;
//        }
//
//        Logger.d(TAG, " called lightXIsInBootloader aa isInBootloader = " + isInBootloader);
//        if (isInBootloader) {
//            Logger.d(TAG, "is in bootloader mode, start update");
//            startUpdate();
//            startWritingFirmware();
//        } else {
//            FirmwareUtil.isUpdatingFirmWare.set(true);
//            AvneraManager.getAvenraManager().getLightX().enterBootloader();
//            Logger.d(TAG, "OTA enterBootloader");
//        }
//    }

//    @Override
//    public void lightXReadConfigResult(LightX lightX, Command command, boolean success, String value) {
//        Logger.d(TAG, "command is " + command + " result is " + success);
//        if (success) {
//            Logger.d(TAG, "config string for " + command + ": " + value);
//            switch (command) {
//                case ConfigProductName:
//                case ConfigManufacturerName:
//                case ConfigModelNumber:
//            }
//        } else {
//            Logger.e(TAG, "failed to read config for " + command);
//        }
//    }


//    @Override
//    public void isLightXInitialize() {
//        super.isLightXInitialize();
//        Logger.e(TAG, "isLightXInitialize FirmwareUtil.isUpdatingFirmWare.get() =" + FirmwareUtil.isUpdatingFirmWare.get());
//        if (FirmwareUtil.isUpdatingFirmWare.get()) {
//            myHandler.sendEmptyMessageDelayed(MSG_GO_TO_BOOTLOADER,1000);
//        }else if (isOTADoing && !FirmwareUtil.isUpdatingFirmWare.get()){
//            otaSuccess(null);
//        }
//    }

//    @Override
//    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
//        if (FirmwareUtil.isUpdatingFirmWare.get()){
//            Logger.d(TAG,"firmware is updating, ignore message");
//            return;
//        }
//        Logger.d(TAG,"receivedResponse command ="+command+",values="+values+",status="+status);
//        switch (command){
//            case AmCmds.CMD_BatteryLevel: {
//                batteryLevel = Integer.valueOf(values.iterator().next().getValue().toString());
//                break;
//            }
//            case AmCmds.CMD_FirmwareVersion: {
//                AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager().getAudioManager().getAccessoryStatus();
//                String version = accessoryInfo.getFirmwareRev();
//                Logger.e(TAG,"onLineFwVersion : "+version);
//                onLineFwVersion = version;
//                PreferenceUtils.setString(PreferenceKeys.FirmVersion, version, getActivity());
//                AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(onLineFwVersion);
//                break;
//            }
//            case AmCmds.CMD_FWInfo: {
//                currentFW = Integer.valueOf(values.get(3).getValue().toString());
//                Logger.e(TAG, "FirmwareUtil.currentFirmware = : " + currentFW);
//                break;
//            }
//        }
//    }

//    @Override
//    public void receivedStatus(StatusEvent name, Object value) {
//        Logger.d(TAG,"receivedStatus StatusEvent = "+name+",value = "+value);
//        switch(name){
//            case PrepImageError:{
//                imageUpdateError();
//                break;
//            }
//            /**
//             * Get this event when device is doing OTA.
//             */
//            case ImageUpdatePreparing:
//                Logger.d(TAG,"message do start");
//                myHandler.sendEmptyMessageDelayed(MSG_TIMER_OUT, OTA_TIME_OUT);
//                break;
//            case UpdateProgress: {
//                Logger.d(TAG,"message do remove");
//                myHandler.removeMessages(MSG_TIMER_OUT);
////                txtUpdating.setText(getString(R.string.updating));
//                String pro = value.toString();
//                if (!pro.equals("Success") ) {
//                    Logger.d(TAG,"pro is = "+pro);
//                    double progress = Double.valueOf(pro);
//                    double realProgress =0;
//                    double totalLen = progressInfo.paramLen +progressInfo.dataLen+progressInfo.firmwareLen;
//                    if (index == 0){
//                        realProgress = progress /127.0  * (progressInfo.paramLen/totalLen);
//                    }else if (index == 1){
//                        realProgress = progress/100.0 * (progressInfo.dataLen/totalLen) + (progressInfo.paramLen/totalLen);
//                    }else if (index == 2){
//                        realProgress = progress/100.0 *(progressInfo.firmwareLen/totalLen) + ((progressInfo.paramLen+progressInfo.dataLen)/totalLen);
//                    }
//                    Logger.d(TAG,"realProgress is = "+realProgress);
//                    updateOTAProgress(realProgress* 100.0);
//                }else if (index ==2){
//                    updateOTAProgress(100);
//                }
//                break;
//            }
//            /**
//             * Get this event when finished one OTA step.
//             * OTA steps {@see CmdManager.updateImage}
//             */
//            case ImageUpdateComplete: {
//                break;
//            }
//            /**
//             * Get this event when finished one OTA step.
//             * OTA steps {@see CmdManager.updateImage}
//             */
//            case ImageUpdateFinalize: {
//                Logger.d(TAG, "fwlist size =" + DashboardActivity.mFwList.size());
//                if (value!= null && !value.toString().equalsIgnoreCase("Success")){
//                    imageUpdateError();
//                    break;
//                }
//                Logger.d(TAG, "otaSteps =" + otaSteps + ",currentFW =" + currentFW);
//                FirmwareUtil.isUpdatingFirmWare.set(true);
//
//                switch (otaSteps) {
//                    case 0: {
//                        if (DashboardActivity.mFwList.size() <= 0) {
//                            isOTADoing = false;
//                            return;
//                        }
//                        index++;
//                        try {
//                            updateData();
//                        } catch (ConcurrentModificationException concurrentException) {
//                            concurrentException.printStackTrace();
//                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted 1", getString(R.string.unplug_plug_while_update_error), getActivity());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                    case 1: {
//                        if (DashboardActivity.mFwList.size() <= 0) {
//                            isOTADoing = false;
//                            return;
//                        }
//                        index++;
//                        try {
//                            updateFirmware();
//                        } catch (ConcurrentModificationException concurrentException) {
//                            concurrentException.printStackTrace();
//                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted 2", getString(R.string.unplug_plug_while_update_error), getActivity());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                    case 2: {
//                        Logger.d(TAG, "last ota step");
//                        FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
//                        otaSteps = -1;
//                        if (DashboardActivity.mFwList.size() <= 0) {
//
//                            FirmwareUtil.isUpdatingFirmWare.set(false);
//                            USB_PERMISSION_CHECK = false;
//                            PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
//                            switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
//                                case NONE:
//                                    break;
//                                case Connected_USBDevice:
//                                    break;
//                                case Connected_BluetoothDevice:
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            try {
//                                                Thread.sleep(1000);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                            PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity());
//                                            Logger.d(TAG, "OTA startFirmware deviceFwVersion = "+ mOnLineFirmware);
//                                            String curVer = transferCurrentVersion(mOnLineFirmware);
//                                            int valueOf = Integer.valueOf(curVer, 16);
//                                            Cmd150Manager.getInstance().setFirmwareVersion(AvneraManager.getAvenraManager().getAudioManager()
//                                                    ,valueOf);
//                                            Logger.d(TAG, "enable accessory interrupts handle ImageUpdateFinalize step 2 then start firmware");
//                                            Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager().getAudioManager(), JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
//                                            Logger.d(TAG, "OTA " + "startFirmware");
//                                            Cmd150Manager.getInstance().startFirmware(AvneraManager.getAvenraManager().getAudioManager(),
//                                                    currentFW == 0 ? (byte) 1 : (byte) 0);
//                                            Logger.d(TAG, "OTA startFirmware over");
//                                        }
//                                    }).start();
//                                    Logger.d(TAG, "OTA enterApplication");
//                                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateComplete(mOnLineFirmware);
//                                    deviceRestarting();
//                                    break;
//                            }
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//
//    }

    private void otaInit() {
        textViewProgress.setVisibility(View.GONE);
        textViewUpdateStatus.setVisibility(View.GONE);
        textViewOTACircle.setVisibility(View.GONE);
        textViewButtonDone.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
    }

    private void otaAvailable() {
        if (getActivity() == null) {
            Logger.d(TAG, "ota available, getActivity is null");
            return;
        }
        for (FirmwareModel model : DashboardActivity.mFwList) {
            switch (model.getFwtype()) {
                case RSRC:
                    break;
                case BOOT:
                    break;
                case APP:
                case PARAM:
                    Logger.i(TAG, "ota available, onLineFwVersion is " + model.getVersion());
                    onLineFwVersion = model.getVersion();
                    textViewUpdateStatus.setText(getString(R.string.firmware_is_available, model.getVersion()));
                    break;
            }
        }
        Logger.d(TAG, "ota available, - - - - - - - - - - - -");
        textViewOTACircle.setVisibility(View.VISIBLE);
        textViewProgress.setVisibility(View.GONE);
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.getPaint().setFakeBoldText(false);
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_4C596B));

        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ota_install_button));
        textViewButtonDone.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
//        iv_ok.setVisibility(View.GONE);
    }

    private void otaUpdating() {
        if (getActivity() == null) {
            Logger.d(TAG, "ota updating, getActivity is null");
            return;
        }
        Logger.d(TAG, "ota updating, - - - - - - - - - - - -");
        imageViewBack.setVisibility(View.GONE);

        String title = getString(R.string.firmware_is_updating_title);
        String content = getString(R.string.firmware_is_updating);
        SpannableStringBuilder builder = new SpannableStringBuilder(title);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n");
        builder.append(content);
        builder.setSpan(new AbsoluteSizeSpan(16, true), title.length(), content.length() + title.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(builder);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_4C596B));
        textViewUpdateStatus.setGravity(Gravity.CENTER_VERTICAL);

        textViewOTACircle.setVisibility(View.VISIBLE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.update_circle));
        textViewProgress.setVisibility(View.VISIBLE);
        textViewProgress.setText("0%");
        textViewButtonDone.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
//        iv_ok.setVisibility(View.GONE);
    }

    private OnOtaListener mOnOtaListener;

    public void otaSuccess(OnOtaListener onOtaListener) {
        mOnOtaListener = onOtaListener;
        if (getActivity() == null) {
            Logger.d(TAG, "ota success, getActivity is null");
            return;
        }
        Logger.d(TAG, "ota success, - - - - - - - - - - - -");
        imageViewBack.setVisibility(View.VISIBLE);

        String title = getString(R.string.firmware_is_installed, onLineFwVersion);
        SpannableStringBuilder builder = new SpannableStringBuilder(title);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(builder);
        textViewUpdateStatus.getPaint().setFakeBoldText(true);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_4C596B));

        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
        textViewProgress.setVisibility(View.GONE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.update_succeeded));
        shadowLayout.setVisibility(View.VISIBLE);
        shadowLayout.setShape("rectangle");
        textViewButtonDone.setVisibility(View.VISIBLE);
        textViewButtonDone.setText(R.string.done);
        textViewButtonDone.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        textViewButtonDone.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_with_round_corner_left_red_right_orange));

        isOTADoing = false;
        shadowLayout.setVisibility(View.VISIBLE);
    }

    private void deviceRePlug() {
        imageViewBack.setVisibility(View.GONE);
        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(R.string.replug);
        textViewUpdateStatus.getPaint().setFakeBoldText(true);
        textViewUpdateStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.update_succeeded));
        textViewButtonDone.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.GONE);
    }

    private void deviceRestarting() {
        textViewProgress.setText("Restarting...");
    }

    private void otaError(boolean isRetry, int errTitle, int errMsg) {
        if (getActivity() == null) {
            Logger.d(TAG, "ota error, getActivity is null");
            return;
        }
        Logger.d(TAG, "ota error, - - - - - - - - - - - -");

        String title = getString(errTitle);
        String content = getString(errMsg);
        SpannableStringBuilder builder = new SpannableStringBuilder(title);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n");
        builder.append(content);
        builder.setSpan(new AbsoluteSizeSpan(16, true), title.length(), content.length() + title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewUpdateStatus.setVisibility(View.VISIBLE);
        textViewUpdateStatus.setText(builder);
        textViewUpdateStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_4C596B));

        textViewProgress.setVisibility(View.VISIBLE);
        textViewOTACircle.setVisibility(View.VISIBLE);
        textViewOTACircle.setBackground(ContextCompat.getDrawable(getActivity(), R.mipmap.update_failed));
        textViewProgress.setVisibility(View.INVISIBLE);
        shadowLayout.setVisibility(View.VISIBLE);
        shadowLayout.setShape("other");
        textViewButtonDone.setVisibility(View.VISIBLE);
        textViewButtonDone.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_with_round_corner_hollow));
        textViewButtonDone.setTextColor(ContextCompat.getColor(getActivity(), R.color.orange_FF5F00));

        if (isRetry) {
            textViewButtonDone.setText(R.string.retry);
        } else {
            textViewButtonDone.setText(R.string.got_it);
        }
    }

//    @Override
//    public void receivedAdminEvent(AdminEvent event, Object value) {
//        super.receivedAdminEvent(event,value);
//        switch (event) {
//            case AccessoryDisconnected:{
//                Logger.d(TAG, "receivedAdminEvent AccessoryDisconnected");
//                FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
//                myHandler.removeMessages(MSG_TIMER_OUT);
//                break;
//            }
//        }
//    }

    private void updateOTAProgress(double realProgress) {
        if (realProgress >= 0) {
            try {
                DecimalFormat oneDigit = new DecimalFormat("#,##0.0");//format to 1 decimal place
                String s = oneDigit.format(realProgress);
                textViewProgress.setText(s + " %");
            } catch (Exception e) {
                Logger.e(TAG, "update ota progres, realProgress:" + realProgress);
            }
        }
    }

    private String transferCurrentVersion(String version) {
        String[] tempString = version.split("\\.");
        return String.format("%02x%02x%02x", Integer.valueOf(tempString[0]), Integer.valueOf(tempString[1]), Integer.valueOf(tempString[2]));
    }

    private MyHandler myHandler = new MyHandler();
    private final static int MSG_TIMER_OUT = 0;
    private final static int MSG_CHECK_UPDATE = 1;
    private final static int MSG_GO_TO_BOOTLOADER = 2;
    private final static int MSG_READ_BASIC_INFO = 3;
    private final static int OTA_TIME_OUT = 90000;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIMER_OUT:
                    Logger.d(TAG, "handle message - timer out, do error");
                    imageUpdateError();
                    break;
                case MSG_CHECK_UPDATE:
                    startCheckingIfUpdateIsAvailable();
                    registerConnectivity();
                    break;
                case MSG_GO_TO_BOOTLOADER:
                    if (DeviceManager.getInstance(getActivity()).isNeedOtaAgain()) {
                        otaError(true, R.string.update_failed_firmware, R.string.update_failed_firmware_detail_1);
                    } else {
                        if (AvneraManager.getAvenraManager().getLightX() != null) {
                            Logger.e(TAG, "handle message - msg go to bootloader, read boot image type");
                            AvneraManager.getAvenraManager().getLightX().readBootImageType();
                        }
                    }
                    break;
                case MSG_READ_BASIC_INFO:
                    getDeviceInfo();
                    break;
            }
        }
    }

    private void imageUpdateError() {
        if (getActivity() == null) {
            return;
        }
        imageViewBack.setVisibility(View.GONE);
        FirmwareUtil.disconnectHeadphoneText = getString(R.string.plsConnect);
        Logger.d(TAG, "image update error");
        Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager().getAudioManager(), JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
        otaError(true, R.string.update_failed_firmware, R.string.update_failed_firmware_detail_1);
//        isOTADoing = false;
//        FirmwareUtil.isUpdatingFirmWare.set(false);
        AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateFailed(mOnLineFirmware);
    }

    private FirmwareModel findImage(FwTYPE type) {
        int pos = 0;
        for (int i = 0; i < DashboardActivity.mFwList.size(); i++) {
            if (DashboardActivity.mFwList.get(i).getFwtype() == type) {
                pos = i;
                break;
            }
        }
        return DashboardActivity.mFwList.remove(pos);
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
                            Logger.d(TAG, "on receive, network receiver, already updating");
                            return;
                        }
                        if (isUpdateAvailable && checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.FINISHED) {
                            if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED) {
                                Logger.e(TAG, "on receive, network receiver, NetworkChangeReceiver startDownloadFirmwareImage");
                                startDownloadFirmwareImage();
                            } else {
                                startCheckingIfUpdateIsAvailable();
                            }
                        } else if (checkUpdateAvailable == null) { // In case of failure retry for server update
                            startCheckingIfUpdateIsAvailable();
                        }
                    } else {
                        if (FirmwareUtil.isUpdatingFirmWare.get()) {
                            Logger.d(TAG, "on receive, network receiver, Already updating");
                            return;
                        }
                        if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.FINISHED) {
                            boolean isSuccessFulDownload = true;
                            for (FirmwareModel model : DashboardActivity.mFwList) {
                                if (!model.isSuccess()) {
                                    isSuccessFulDownload = false;
                                    break;
                                }
                            }
                            if (isSuccessFulDownload) {
                                Logger.d(TAG, "on receive, network receiver, No Internet No impact as data downloaded.");
                            }
                        } else if (downloadProgrammingFile != null && downloadProgrammingFile.getStatus() == AsyncTask.Status.RUNNING) {
                            downloadProgrammingFile.cancel(true);
                            Logger.d(TAG, "on receive, network receiver, No Internet downloadProgrammingFile.cancel");
                        } else if (checkUpdateAvailable != null && checkUpdateAvailable.getStatus() == AsyncTask.Status.RUNNING) {
                            checkUpdateAvailable.cancel(true);
                            Logger.d(TAG, "on receive, network receiver, No Internet checkUpdateAvailable.cancel");
                        } else {
                            otaError(false, R.string.update_failed_download, R.string.update_failed_download_detail);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);
        switch (enumCommands) {
            case CMD_AccessoryDisconnected: {
                Logger.d(TAG, "on receive, AccessoryDisconnected");
                FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
                myHandler.removeMessages(MSG_TIMER_OUT);
                break;
            }
            case CMD_OTA_PrepImageError: {
                imageUpdateError();
                break;
            }
            case CMD_BATTERY_LEVEL: {
                batteryLevel =  (int) objects[0];
                break;
            }
            case CMD_FIRMWARE_VERSION: {
                String version = (String) objects[0];
                if (version == null) {
                    AccessoryInfo accessoryInfo = AvneraManager.getAvenraManager().getAudioManager().getAccessoryStatus();
                    version = accessoryInfo.getFirmwareRev();
                    Logger.e(TAG, "on receive, onLineFwVersion : " + version);
                }
                onLineFwVersion = version;
                PreferenceUtils.setString(PreferenceKeys.FirmVersion, version, getActivity());
                AnalyticsManager.getInstance(getActivity()).reportFirmwareVersion(onLineFwVersion);
                break;
            }
            case CMD_BootReadVersionFile: { //RSRC version is coming in ASCII character which is different format from app version.
                deviceFwVersion = (String) objects[0];
                Logger.d(TAG, "on receive, deviceFwVersion = " + deviceFwVersion);
                PreferenceUtils.setString(AppUtils.getModelNumber(getActivity()), PreferenceKeys.RSRC_VERSION, deviceFwVersion, getActivity());
            }
            break;
            case CMD_FW_INFO: {
                currentFW = (int) objects[0];
                Logger.e(TAG, "on receive, currentFirmware = : " + currentFW);
                break;
            }
            case CMD_IsLightXInitialize: {
                if (FirmwareUtil.isUpdatingFirmWare.get()) {
                    myHandler.sendEmptyMessageDelayed(MSG_GO_TO_BOOTLOADER, 1000);
                } else if (isOTADoing && !FirmwareUtil.isUpdatingFirmWare.get()) {
                    otaSuccess(null);
                }
            }
            case CMD_IsInBootloader: {
                boolean isInBootloader = (boolean) objects[0];
                if (!FirmwareUtil.isConnectionAvailable(getActivity())) {
                    if (checkUpdateAvailable != null) {
                        checkUpdateAvailable.cancel(true);
                    }
                    otaError(false, R.string.update_failed_connection, R.string.update_failed_connection_detail);
                    return;
                }

                Logger.d(TAG, "on receive, called lightXIsInBootloader aa isInBootloader = " + isInBootloader);
                if (isInBootloader) {
                    Logger.d(TAG, "on receive, is in bootloader mode, start update");
                    startUpdate();
                    startWritingFirmware();
                } else {
                    FirmwareUtil.isUpdatingFirmWare.set(true);
                    AvneraManager.getAvenraManager().getLightX().enterBootloader();
                    Logger.d(TAG, "on receive, OTA enterBootloader");
                }
            }
            /*
              Get this event when device is doing OTA.
             */
            case CMD_OTA_ImageUpdatePreparing:
                Logger.d(TAG, "on receive, message do start");
                myHandler.sendEmptyMessageDelayed(MSG_TIMER_OUT, OTA_TIME_OUT);
                break;
            case CMD_OTA_UpdateProgress: {
                Logger.d(TAG, "on receive, message do remove");
                myHandler.removeMessages(MSG_TIMER_OUT);
//                txtUpdating.setText(getString(R.string.updating));
                String pro = (String) objects[0];
                if (!pro.equals("Success")) {
                    Logger.d(TAG, "on receive, pro is = " + pro);
                    double progress = Double.valueOf(pro);
                    double realProgress = 0;
                    double totalLen = progressInfo.paramLen + progressInfo.dataLen + progressInfo.firmwareLen;
                    if (index == 0) {
                        realProgress = progress / 127.0 * (progressInfo.paramLen / totalLen);
                    } else if (index == 1) {
                        realProgress = progress / 100.0 * (progressInfo.dataLen / totalLen) + (progressInfo.paramLen / totalLen);
                    } else if (index == 2) {
                        realProgress = progress / 100.0 * (progressInfo.firmwareLen / totalLen) + ((progressInfo.paramLen + progressInfo.dataLen) / totalLen);
                    }
                    Logger.d(TAG, "on receive, realProgress is = " + realProgress);
                    updateOTAProgress(realProgress * 100.0);
                } else if (index == 2) {
                    updateOTAProgress(100);
                }
                break;
            }
            /*
              Get this event when finished one OTA step.
              OTA steps {@see CmdManager.updateImage}
             */
            case CMD_OTA_ImageUpdateComplete: {
                break;
            }
            /*
              Get this event when finished one OTA step.
              OTA steps {@see CmdManager.updateImage}
             */
            case CMD_OTA_ImageUpdateFinalize: {
                Logger.d(TAG, "on receive, fwlist size =" + DashboardActivity.mFwList.size());
                String val = (String) objects[0];
                if (val != null && !val.equalsIgnoreCase("Success")) {
                    imageUpdateError();
                    break;
                }
                Logger.d(TAG, "on receive, otaSteps =" + otaSteps + ",currentFW =" + currentFW);
                FirmwareUtil.isUpdatingFirmWare.set(true);

                switch (otaSteps) {
                    case 0: {
                        if (DashboardActivity.mFwList.size() <= 0) {
                            isOTADoing = false;
                            return;
                        }
                        index++;
                        try {
                            updateData();
                        } catch (ConcurrentModificationException concurrentException) {
                            concurrentException.printStackTrace();
                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted 1", getString(R.string.unplug_plug_while_update_error), getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 1: {
                        if (DashboardActivity.mFwList.size() <= 0) {
                            isOTADoing = false;
                            return;
                        }
                        index++;
                        try {
                            updateFirmware();
                        } catch (ConcurrentModificationException concurrentException) {
                            concurrentException.printStackTrace();
                            AlertsDialog.showSimpleDialogWithOKButtonWithRelaunch("Update Interrupted 2", getString(R.string.unplug_plug_while_update_error), getActivity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 2: {
                        Logger.d(TAG, "on receive, last ota step");
                        FirmwareUtil.disconnectHeadphoneText = getActivity().getResources().getString(R.string.plsConnect);
                        otaSteps = -1;
                        if (DashboardActivity.mFwList.size() <= 0) {

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
                                            Logger.d(TAG, "on receive, OTA startFirmware deviceFwVersion = " + mOnLineFirmware);
                                            String curVer = transferCurrentVersion(mOnLineFirmware);
                                            int valueOf = Integer.valueOf(curVer, 16);
                                            Cmd150Manager.getInstance().setFirmwareVersion(AvneraManager.getAvenraManager().getAudioManager()
                                                    , valueOf);
                                            Logger.d(TAG, "on receive, enable accessory interrupts handle ImageUpdateFinalize step 2 then start firmware");
                                            Cmd150Manager.getInstance().setFirmwareUpdateState(AvneraManager.getAvenraManager().getAudioManager(), JBLConstant.ENABLE_ACCESSORY_INTERRUPTS);
                                            Logger.d(TAG, "on receive, OTA startFirmware");
                                            Cmd150Manager.getInstance().startFirmware(AvneraManager.getAvenraManager().getAudioManager(),
                                                    currentFW == 0 ? (byte) 1 : (byte) 0);
                                            Logger.d(TAG, "on receive, OTA startFirmware over");
                                        }
                                    }).start();
                                    Logger.d(TAG, "on receive, OTA enterApplication");
                                    AnalyticsManager.getInstance(getActivity()).reportFirmwareUpdateComplete(mOnLineFirmware);
                                    deviceRestarting();
                                    break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case CMD_FirmwareWriteStatus: {
                LightX.FirmwareWriteOperation firmwareWriteOperation = (LightX.FirmwareWriteOperation) objects[0];
                double progress = (double) objects[1];
                Exception exception = (Exception) objects[2];
                if (exception != null) {
                    Logger.d(TAG, "lightXFirmwareWriteStatus Exception====" + exception.toString());
                    AlertsDialog.showToast(getActivity(), "Communication broke during update. Please try again");
                } else {
                    double value = progress * 100;
                    value = (progressFactor + value) / divideFactor;
                    switch (firmwareWriteOperation) {
                        case Erase:
                            Logger.d(TAG, "Erasing.." + value);
                        case Verify:
                            Logger.d(TAG, "Verify..");
                        case Write:
                            myHandler.removeMessages(MSG_TIMER_OUT);
                            myHandler.sendEmptyMessageDelayed(MSG_TIMER_OUT, OTA_TIME_OUT);
                            if (progress * 100 == 100.0) {
                                progressFactor += 100.0;
                            }
                            Logger.d(TAG, "Write.." + value);
                            updateOTAProgress(value);
                            break;
                        case Checksum:
                            Logger.d(TAG, "Checksum..");
                            break;
                        case Complete: {
                            Logger.d(TAG, "~~~~~~~~~~Completed..");
                            myHandler.removeMessages(MSG_TIMER_OUT);
                            if (DashboardActivity.mFwList.size() <= 0) {
                                FirmwareUtil.isUpdatingFirmWare.set(false);
                                USB_PERMISSION_CHECK = false;
                                PreferenceUtils.setBoolean(AppUtils.IsNeedToRefreshCommandRead, false, getActivity()); // set RSRC,APP version for Checking version at home.
                                switch (DeviceConnectionManager.getInstance().getCurrentDevice()) {
                                    case NONE:
                                        break;
                                    case Connected_USBDevice:
                                        deviceRePlug();
                                        break;
                                    case Connected_BluetoothDevice:
                                        AvneraManager.getAvenraManager().getLightX().enterApplication();
                                        Logger.d(TAG, "OTA enterApplication");
                                        deviceRestarting();
                                        break;
                                }
                            } else {
                                startWritingFirmware();
                            }
                            break;
                        }
                        default: {
                            Logger.d(TAG, "--------------default..");
                        }
                    }
                }
            }
            break;
        }
    }
}
