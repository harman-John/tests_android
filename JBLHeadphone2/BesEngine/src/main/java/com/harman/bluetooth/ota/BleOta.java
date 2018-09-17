package com.harman.bluetooth.ota;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.harman.bluetooth.constants.Constants;
import com.harman.bluetooth.constants.EnumOtaState;
import com.harman.bluetooth.core.LeDevice;
import com.harman.bluetooth.listeners.BleListener;
import com.harman.bluetooth.ret.RetResponse;
import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;
import com.harman.bluetooth.utils.ProfileUtils;
import com.harman.bluetooth.utils.SPHelper;

import java.io.IOException;
import java.io.InputStream;


public class BleOta implements BleListener {
    private final static String TAG = BleOta.class.getSimpleName();

    //    protected static final String KEY_OTA_FILE = "ota_file";
    private static final String OTA_FILE = "live_400.bin";

    private static final byte[] OTA_PASS_RESPONSE = new byte[]{0x11, 0x22};
    private static final byte[] OTA_RESEND_RESPONSE = new byte[]{0x33, 0x44};

//    protected static final int DEFAULT_MTU = 512;
//
//    private static final int REQUEST_OTA_FILE = 0X00;
//    private static final int REQUEST_DEVICE = 0x01;

    private static final int MSG_UPDATE_INFO = 0x00;
    private static final int MSG_UPDATE_PROGRESS = 0x01;
    private static final int MSG_OTA_TIME_OUT = 0x02;
    private static final int MSG_SEND_INFO_TIME_OUT = 0x03;
    private static final int MSG_UPDATE_RESULT_INFO = 0x04;


    //    protected static final int CMD_CONNECT = 0x80;
    private static final int CMD_DISCONNECT = 0x81;
    private static final int CMD_LOAD_FILE = 0x82;
    private static final int CMD_START_OTA = 0x83;
    private static final int CMD_OTA_NEXT = 0x84;
    //    protected static final int CMD_SEND_FILE_INFO = 0x85;
    private static final int CMD_LOAD_FILE_FOR_NEW_PROFILE = 0x86;
    private static final int CMD_RESEND_MSG = 0X88;
    private static final int CMD_LOAD_FILE_FOR_NEW_PROFILE_SPP = 0X89;
    private static final int CMD_LOAD_OTA_CONFIG = 0x90;
    private static final int CMD_START_OTA_CONFIG = 0x91;
    private static final int CMD_OTA_CONFIG_NEXT = 0x92;

    private static final int STATE_IDLE = 0x00;
    //    private static final int STATE_CONNECTING = 0x01;
    private static final int STATE_CONNECTED = 0x02;
    //    protected static final int STATE_DISCONNECTING = 0x03;
//    private static final int STATE_DISCONNECTED = 0x04;
    private static final int STATE_OTA_ING = 0x05;
    private static final int STATE_OTA_FAILED = 0x06;
    private static final int STATE_OTA_CONFIG = 0x07;
//    protected static final int STATE_BUSY = 0x0F;

    private volatile int mState = STATE_IDLE;
    //    protected BluetoothDevice mDevice;
//
//    private boolean mExit = false;
//
//    private HandlerThread mCmdThread;
    private CmdHandler mCmdHandler = new CmdHandler(Looper.getMainLooper());

    private byte[][][] mOtaData;
    private int mOtaPacketCount = 0;
    private int mOtaPacketItemCount = 0;
    private boolean mSupportNewOtaProfile = false;

    private byte[][] mOtaConfigData;
    private int mOtaConfigPacketCount = 0;

    private int totalPacketCount = 0;

    private final Object mOtaLock = new Object();

    private int mMtu;

    private volatile boolean mWritten = true;

//    private final String OTA_CONFIG_TAG = "ota_config";
//    private OtaConfigFragment mOtaConfigDialog;

//    private long castTime;//temp data for log
//    protected long sendMsgFailCount = 0;//temp data for log
//    private long otaImgSize = 0;
//
//    protected final int RECONNECT_MAX_TIMES = 5; // 5 times
//    protected final int RECONNECT_SPAN = 3000; // 3 seconds
//    private int reconnectTimes = 0;

    private int totalCount = 0;
    private int failedCount = 0;

    private Context mContext;

    private BleListener mListeners;

    public void addListener(BleListener listeners) {
        mListeners = listeners;
    }

    private void notifyOtaUpdate(EnumOtaState state, int progress) {
        synchronized (mOtaLock) {
            mListeners.onLeOta(null, state, progress);
        }
    }

//    TextView mAddress;
//    TextView mName;
//    TextView mOtaFile;
//    TextView mOtaInfo;
//    TextView mUpdateStatic ;
//    ProgressBar mOtaProgress;
//    Button pickDevice , pickOtaFile , startOta ;

//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.pick_device:
//                if (isIdle()) {
//                    pickDevice(REQUEST_DEVICE);
//                }
//                break;
//            case R.id.pick_ota_file:
//                if (isIdle()) {
//                    pickFile(REQUEST_OTA_FILE);
//                }
//                break;
//            case R.id.start_ota:
//                readyOta();
//                break;
//        }
//    }

//    private void initView() {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        mAddress = (TextView) findViewById(R.id.address);
//        mName = (TextView) findViewById(R.id.name);
//        mOtaFile = (TextView) findViewById(R.id.ota_file);
//        mOtaInfo = (TextView) findViewById(R.id.ota_info);
//        mUpdateStatic = (TextView) findViewById(R.id.update_static);
//        mOtaProgress = (ProgressBar) findViewById(R.id.ota_progress);
//        pickDevice = (Button) findViewById(R.id.pick_device);
//        pickOtaFile = (Button) findViewById(R.id.pick_ota_file);
//        startOta = (Button) findViewById(R.id.start_ota);
//        pickDevice.setOnClickListener(this);
//        pickOtaFile.setOnClickListener(this);
//        startOta.setOnClickListener(this);
//
//        mOtaFile.setText(SPHelper.getPreference(this, KEY_OTA_FILE, "").toString());
//        mName.setText(loadLastDeviceName());
//        mAddress.setText(loadLastDeviceAddress());
//        mDevice = BtHelper.getRemoteDevice(this, mAddress.getText().toString());
//
//        mOtaConfigDialog = new OtaConfigFragment();
//        mOtaConfigDialog.setOtaConfigCallback(mOtaConfigCallback);
//    }

    private OtaHandler mOtaHandler = new OtaHandler(Looper.getMainLooper());


    private class OtaHandler extends Handler {

        OtaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_INFO:
//                    mOtaInfo.setText(msg.obj.toString());
                    break;
                case MSG_UPDATE_RESULT_INFO:
//                    if(mUpdateStatic != null){
//                        mUpdateStatic.setText(msg.obj.toString());
//                    }
                    break;
                case MSG_UPDATE_PROGRESS:
                    notifyOtaUpdate(EnumOtaState.Progress, (Integer) msg.obj);
//                    if(mOtaProgress != null){
//                        mOtaProgress.setProgress((Integer) msg.obj);
//                    }else{
//                        Logger.i(TAG, "mOtaProgress is null");
//                    }
                    break;
                case MSG_OTA_TIME_OUT:
                case MSG_SEND_INFO_TIME_OUT:
//                    Logger.i(TAG, "MSG_SEND_INFO_TIME_OUT|MSG_SEND_INFO_TIME_OUT time out");
//                    if(mOtaInfo != null){
//                        mOtaInfo.setText(msg.arg1);
//                    }else{
//                        Logger.i(TAG, "mOtaInfo is null");
//                    }
                    sendCmdDelayed(msg.arg2, 0);
                    break;
                default:// donot left the default , even nothing to do
            }
        }
    }


//    private boolean isIdle() {
//        return mState == STATE_IDLE || mState == STATE_OTA_FAILED || mState == STATE_DISCONNECTED;
//    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Logger.i(TAG , "onCreate");
//        setContentView(R.layout.act_ota);
//        initView();
//        initConfig();
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onRestoreInstanceState(savedInstanceState, persistentState);
//    }

//    protected void initConfig() {
//        mCmdThread = new HandlerThread(TAG);
//        mCmdThread.start();
//    }
//
//    //    @Override
////    protected void onDestroy() {
////        super.onDestroy();
////        Logger.i(TAG , "onDestroy");
//    private void close() {
//        if (mOtaHandler != null) {
//            mOtaHandler.removeMessages(MSG_SEND_INFO_TIME_OUT);
//            mOtaHandler.removeMessages(MSG_OTA_TIME_OUT);
//        }
//        if (mCmdHandler != null) {
//            mCmdHandler.removeMessages(CMD_OTA_NEXT);
//            mCmdHandler.removeMessages(CMD_OTA_CONFIG_NEXT);
//        }
//        if (mCmdThread != null && mCmdThread.isAlive()) {
//            mCmdThread.quit();
//        }
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                exit();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    protected void exit() {
//        Logger.i(TAG , "exit");
//        if (mState == STATE_IDLE) {
//            finish();
//        } else {
//            showConfirmDialog(R.string.ota_exit_tips, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    exitOta();
//                    finish();
//                }
//            });
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Logger.i(TAG , "onActivityResult");
//        if (requestCode == REQUEST_OTA_FILE) {
//            onPickFile(resultCode, data);
//        } else if (requestCode == REQUEST_DEVICE) {
//            onPickDevice(resultCode, data);
//        }
//    }

//    @Override
//    public void onConnectionStateChanged(boolean connected) {
//        Logger.i(TAG, "onConnectionStateChanged " + connected + "; " + mState);
//        if (connected) {
//            onConnected();
//        } else {
//            removeTimeout();
//            if (mState == STATE_CONNECTING) {
//                Logger.i(TAG, "mState == STATE_CONNECTING");
//                reconnectTimes++;
//                if(reconnectTimes > RECONNECT_MAX_TIMES){
//                    updateInfo(R.string.connect_failed);
//                    mState = STATE_DISCONNECTED;
//                    onOtaFailed();
//                }else{
//                    updateInfo(String.format(getString(R.string.connect_reconnect_try), reconnectTimes));
//                    reconnect();
//                }
//            } else if (mState == STATE_OTA_ING) {
//                Logger.i(TAG, "mState == STATE_OTA_ING");
//                onOtaFailed();
//            } else if (mState != STATE_IDLE) {
//                Logger.i(TAG, "mState != STATE_IDLE");
//                updateInfo(R.string.disconnected);
//                mState = STATE_DISCONNECTED;
//                onOtaFailed();
//            }
//        }
//    }
//
//    protected void updateInfo(int info) {
//        updateInfo(getString(info));
//    }

    private void updateResultInfo(String info) {
        Message message = mOtaHandler.obtainMessage(MSG_UPDATE_RESULT_INFO);
        message.obj = info;
        mOtaHandler.sendMessage(message);
    }

//    private void updateInfo(String info) {
//        Message message = mOtaHandler.obtainMessage(MSG_UPDATE_INFO);
//        message.obj = info;
//        mOtaHandler.sendMessage(message);
//    }

    private void updateProgress(int progress) {
        Message message = mOtaHandler.obtainMessage(MSG_UPDATE_PROGRESS);
        message.obj = progress;
        mOtaHandler.sendMessage(message);
//        notifyOtaUpdate(EnumOtaState.Success, progress);
    }

    private void sendCmdDelayed(int cmd, long millis) {
        mCmdHandler.removeMessages(cmd);
        if (millis == 0) {
            mCmdHandler.sendEmptyMessage(cmd);
        } else {
            mCmdHandler.sendEmptyMessageDelayed(cmd, millis);
        }
    }

    private void sendTimeout(int cmd, long millis) {
        Logger.i(TAG, "sendTimeout cmd " + cmd + " ; millis " + millis);
        Message message = mOtaHandler.obtainMessage(MSG_OTA_TIME_OUT);
        message.arg2 = cmd;
        mOtaHandler.sendMessageDelayed(message, millis);
    }

    private void removeTimeout() {
        Logger.i(TAG, "removeTimeout");
        mOtaHandler.removeMessages(MSG_OTA_TIME_OUT);
    }

//    private void onPickDevice(int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            mDevice = data.getParcelableExtra(LeScanActivity.EXTRA_DEVICE);
//            if (mDevice != null) {
//                saveLastDeviceName(mDevice.getName());
//                saveLastDeviceAddress(mDevice.getAddress());
//                mAddress.setText(mDevice.getAddress());
//                mName.setText(mDevice.getName());
//            }
//        }
//    }

//    private void onPickFile(int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            mOtaData = null;
//            String file = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
//            SPHelper.putPreference(this, KEY_OTA_FILE, file);
//            mOtaFile.setText(file);
//        }
//    }

//    protected void onConnecting() {
//        Logger.i(TAG, "onConnecting");
////        LogUtils.writeForOTAStatic(TAG , "onConnecting ");
//        castTime = System.currentTimeMillis();//startTime
////        updateInfo(R.string.connecting_device);
//        mState = STATE_CONNECTING;
//    }

//    protected void onConnected() {
//        Logger.i(TAG, "onConnected");
////        sendCmdDelayed(CMD_SEND_FILE_INFO, 0);
////        LogUtils.writeForOTAStatic(TAG , "onConnected ");
////        updateInfo(R.string.connected);
//        mState = STATE_CONNECTED;
//        reconnectTimes = 0;
//    }

//    protected void onConnectFailed() {
//        Logger.i(TAG, "onConnectFailed");
////        LogUtils.writeForOTAStatic(TAG , "onConnectFailed "+((System.currentTimeMillis() - castTime)/1000));
////        updateInfo(R.string.connect_failed);
//        mState = STATE_DISCONNECTED;
//    }

    private void onLoadFileFailed() {
        Logger.i(TAG, "onLoadFileFailed");
//        updateInfo(R.string.load_file_failed);
        notifyOtaUpdate(EnumOtaState.Fail, -1);
    }

    private void onLoadFileSuccessfully() {
        Logger.i(TAG, "onLoadFileSuccessfully");
//        updateInfo(R.string.load_file_successfully);
        sendCmdDelayed(CMD_START_OTA, 0);
    }

    private void onLoadOtaConfigFailed() {
        Logger.i(TAG, "onLoadOtaConfigFailed");
//        updateInfo(R.string.load_ota_config_failed);
        notifyOtaUpdate(EnumOtaState.Fail, -1);
    }

    private void onLoadOtaConfigSuccessfully() {
        Logger.i(TAG, "onLoadOtaConfigSuccessfully");
//        updateInfo(R.string.load_ota_config_successfully);
        sendCmdDelayed(CMD_START_OTA_CONFIG, 0);
    }

    private void onOtaOver() {
        Logger.i(TAG, "onOtaOver");
        totalCount++;
        String result = "升级统计结果：总升级次数 = " + totalCount + "  失败次数 = " + failedCount;
        updateResultInfo(result);
//        int updateTime = (int)((System.currentTimeMillis() - castTime)/1000) ;
//        String msg = "升级成功 耗时 "+updateTime+" s"+" 重发包数 "+sendMsgFailCount+" 速度 :"+otaImgSize/(updateTime == 0?otaImgSize:updateTime)+" B/s";
//        LogUtils.writeForOTAStatic(TAG , msg);
//        msg = "升级成功 耗时 "+updateTime+" s"+" 速度 :"+otaImgSize/(updateTime == 0?otaImgSize:updateTime)+" B/s";
//        updateInfo(msg);
//        updateProgress(100);
        mOtaPacketCount = 0;
        mOtaPacketItemCount = 0;
        mOtaConfigPacketCount = 0;
        mState = STATE_IDLE;
    }

    private void onOtaFailed() {
        Logger.i(TAG, "onOtaFailed");
        totalCount++;
        failedCount++;
//        String result = "升级统计结果：总升级次数 = "+totalCount+"  失败次数 = "+failedCount ;
//        updateResultInfo(result);
//        int updateTime = (int)((System.currentTimeMillis() - castTime)/1000) ;
//        String msg = "升级失败 耗时 "+updateTime+" s"+" 重发包数 "+sendMsgFailCount+" 速度 :"+otaImgSize/(updateTime == 0?otaImgSize:updateTime)+" B/s";
//        LogUtils.writeForOTAStatic(TAG , msg);
//        msg = "升级失败 耗时 "+updateTime+" s"+" 速度 :"+otaImgSize/(updateTime == 0?otaImgSize:updateTime)+" B/s";
//        updateInfo(msg);
//        reconnectTimes = 0;
        mOtaPacketCount = 0;
        mOtaPacketItemCount = 0;
        mOtaConfigPacketCount = 0;
        mState = STATE_OTA_FAILED;
    }

    private void onOtaConfigFailed() {
//        updateInfo(R.string.ota_config_failed);
        mOtaConfigData = null;
        mOtaConfigPacketCount = 0;
        mOtaConfigPacketCount = 0;
        mState = STATE_IDLE;
    }

//    protected void onWritten() {
//        mWritten = true;
//        Logger.i(TAG, "onWritten mWritten = true");
//    }

    private LeDevice leDevice;

    public void setLeDevice(LeDevice leDevice) {
        this.leDevice = leDevice;
        leDevice.setBesListener(this);
    }

    public void setCharacter(Context context) {
        updateProgress(0);
        mContext = context;
        leDevice.getOtaWriteCharacter();
//        leDevice.getOtaReadCharacter();
    }

    private void sendFileInfo() {
        mState = STATE_CONNECTED;
//        reconnectTimes = 0;
        Logger.i(TAG, "send file info");
        //FileInputStream inputStream = null;
        InputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(getOtaFile());
            inputStream = mContext.getAssets().open(getOtaFile());
            int totalSize = inputStream.available();
//            otaImgSize = totalSize;

            int dataSize = totalSize - 4;
            Logger.i(TAG, "send file info, data size: " + dataSize);
            byte[] data = new byte[dataSize];
            int readBytes = inputStream.read(data, 0, dataSize);
            if (readBytes >= 0) {
                long crc32 = ArrayUtil.crc32(data, 0, dataSize);
                Logger.i(TAG, "send file info, crc32: " + crc32);
                //            Message message = mOtaHandler.obtainMessage(MSG_SEND_INFO_TIME_OUT);
                ////            message.arg1 = R.string.old_ota_profile;
                //            message.arg2 = CMD_LOAD_FILE;
                //            mOtaHandler.sendMessageDelayed(message, 5000);
                sendData(new byte[]{(byte) 0x80, 0x42, 0x45, 0x53, 0x54, (byte) dataSize, (byte) (dataSize >> 8), (byte) (dataSize >> 16), (byte) (dataSize >> 24), (byte) crc32, (byte) (crc32 >> 8), (byte) (crc32 >> 16), (byte) (crc32 >> 24)});
            }
            Logger.e(TAG, "send file info, read to array error, read bytes: " + readBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadOtaConfig() {
        Logger.e(TAG, "loadOtaConfig");
        //FileInputStream inputStream = null;
        InputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(getOtaFile());
            inputStream = mContext.getAssets().open(getOtaFile());
            int dataSize = inputStream.available();
            byte[] data = new byte[dataSize];
            int readBytes = inputStream.read(data, 0, dataSize);
            if (readBytes >= 0) {
                int configLength = 92;
                byte[] config = new byte[configLength];
                int lengthOfFollowingData = configLength - 4;
                config[0] = (byte) lengthOfFollowingData;
                config[1] = (byte) (lengthOfFollowingData >> 8);
                config[2] = (byte) (lengthOfFollowingData >> 16);
                config[3] = (byte) (lengthOfFollowingData >> 24);
                config[4] = data[dataSize - 4];
                config[5] = data[dataSize - 3];
                config[6] = data[dataSize - 2];

                boolean clearUserData = (boolean) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_CLEAR_USER_DATA, Constants.DEFAULT_CLEAR_USER_DATA);
                boolean updateBtAddress = (boolean) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BT_ADDRESS, Constants.DEFAULT_UPDATE_BT_ADDRESS);
                boolean updateBtName = (boolean) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BT_NAME, Constants.DEFAULT_UPDATE_BT_NAME);
                boolean updateBleAddress = (boolean) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BLE_ADDRESS, Constants.DEFAULT_UPDATE_BLE_ADDRESS);
                boolean updateBleName = (boolean) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BLE_NAME, Constants.DEFAULT_UPDATE_BLE_NAME);
                byte enable = 0x00;
                enable |= (clearUserData ? 0x01 : 0x00);
                enable |= (updateBtName ? (0x01 << 1) : 0x00);
                enable |= (updateBleName ? (0x01 << 2) : 0x00);
                enable |= (updateBtAddress ? (0x01 << 3) : 0x00);
                enable |= (updateBleAddress ? (0x01 << 4) : 0x00);
                config[8] = enable;
                if (updateBtName) {
                    String btName = (String) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BT_NAME_VALUE, "");
                    assert btName != null;
                    byte[] btNameBytes = btName.getBytes();
                    int btNameLength = btNameBytes.length;
                    if (btNameLength > 32) {
                        btNameLength = 32;
                    }
                    System.arraycopy(btNameBytes, 0, config, 12, btNameLength);
                }
                if (updateBleName) {
                    String bleName = (String) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BLE_NAME_VALUE, "");
                    assert bleName != null;
                    byte[] bleNameBytes = bleName.getBytes();
                    int bleNameLength = bleNameBytes.length;
                    if (bleNameLength > 32) {
                        bleNameLength = 32;
                    }
                    System.arraycopy(bleNameBytes, 0, config, 44, bleNameLength);
                }
                if (updateBtAddress) {
                    String btAddress = (String) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BT_ADDRESS_VALUE, "");
                    for (int i = 0; i < 6; i++) {
                        assert btAddress != null;
                        config[76 + 5 - i] = Integer.valueOf(btAddress.substring(i, i * 2 + 2), 16).byteValue();
                    }
                }
                if (updateBleAddress) {
                    String bleAddress = (String) SPHelper.getPreference(mContext, Constants.KEY_OTA_CONFIG_UPDATE_BLE_ADDRESS_VALUE, "");
                    for (int i = 0; i < 6; i++) {
                        assert bleAddress != null;
                        config[82 + 5 - i] = Integer.valueOf(bleAddress.substring(i, i * 2 + 2), 16).byteValue();
                    }
                }
                long crc32 = ArrayUtil.crc32(config, 0, configLength - 4);
                config[88] = (byte) crc32;
                config[89] = (byte) (crc32 >> 8);
                config[90] = (byte) (crc32 >> 16);
                config[91] = (byte) (crc32 >> 24);

                int mtu = getMtu();
                Logger.d(TAG, "load ota config, mtu: " + mMtu);
                int packetPayload = mtu - 1;
                int packetCount = (configLength + packetPayload - 1) / packetPayload;
                mOtaConfigData = new byte[packetCount][];
                int position = 0;
                for (int i = 0; i < packetCount; i++) {
                    if (position + packetPayload > configLength) {
                        packetPayload = configLength - position;
                    }
                    mOtaConfigData[i] = new byte[packetPayload + 1];
                    mOtaConfigData[i][0] = (byte) 0x86;
                    System.arraycopy(config, position, mOtaConfigData[i], 1, packetPayload);
                    position += packetPayload;
                }
            }
            Logger.e(TAG, "load ota config, read to array error, read bytes: " + readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            mOtaConfigData = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOtaConfigData != null) {
            onLoadOtaConfigSuccessfully();
        } else {
            onLoadOtaConfigFailed();
        }
    }

    private void loadFileForNewProfile() {
        Logger.i(TAG, "loadFileForNewProfile");
        mSupportNewOtaProfile = true;
        //FileInputStream inputStream = null;
        InputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(getOtaFile());
            inputStream = mContext.getAssets().open(getOtaFile());
            int totalSize = inputStream.available();
            int dataSize = totalSize - 4;
            int mtu = getMtu();
            int packetPayload = ProfileUtils.calculateBLESinglePacketLen(dataSize, mtu, isBle());
            int totalPacketCount = (dataSize + packetPayload - 1) / packetPayload;
            int onePercentBytes = ProfileUtils.calculateBLEOnePercentBytes(dataSize);
            int crcCount = (dataSize + onePercentBytes - 1) / onePercentBytes;
            this.totalPacketCount = totalPacketCount;
            mOtaData = new byte[crcCount + 1][][];
            Logger.e(TAG, "new profile imgeSize: " + dataSize + "; totalPacketCount " + totalPacketCount + "; onePercentBytes " + onePercentBytes + "; crcCount " + crcCount);
            byte[] data = new byte[dataSize];
            int bytesRead = inputStream.read(data, 0, dataSize);
            if (bytesRead >= 0) {
                int position = 0;
                for (int i = 0; i < crcCount; i++) {
                    int crcBytes = onePercentBytes; //要校验百分之一的数据量
                    int length = (crcBytes + packetPayload - 1) / packetPayload; //根据MTU ，算出百分之一需要多少个包满足要求
                    if (crcCount - 1 == i) { // 最后一包取余数
                        crcBytes = dataSize - position;
                        length = (crcBytes + packetPayload - 1) / packetPayload;
                    }
                    Logger.i(TAG, "CRC BYTES = " + crcBytes);
                    mOtaData[i] = new byte[length + 1][]; //加 1 为增加最后结束整包校验命令
                    int realSinglePackLen;
                    int crcPosition = position;
                    for (int j = 0; j < length; j++) {
                        realSinglePackLen = packetPayload;
                        if (j == length - 1) { //每百分之一的最后一包取余数
                            realSinglePackLen = (crcBytes % packetPayload == 0) ? packetPayload : crcBytes % packetPayload;
                        }
                        mOtaData[i][j] = new byte[realSinglePackLen + 1];
                        System.arraycopy(data, position, mOtaData[i][j], 1, realSinglePackLen);
                        mOtaData[i][j][0] = (byte) 0x85;
                        position += realSinglePackLen;
                    }
                    long crc32 = ArrayUtil.crc32(data, crcPosition, crcBytes);
                    mOtaData[i][length] = new byte[]{(byte) 0x82, 0x42, 0x45, 0x53, 0x54, (byte) crc32, (byte) (crc32 >> 8), (byte) (crc32 >> 16), (byte) (crc32 >> 24)};
                }
                mOtaData[crcCount] = new byte[1][];
                mOtaData[crcCount][0] = new byte[]{(byte) 0x88};
            }

            Logger.e(TAG, "load file for new profile, read to array error, read bytes: " + bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
            mOtaData = null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOtaData == null) {
            onLoadFileFailed();
        } else {
            onLoadFileSuccessfully();
        }
    }

    private void loadFileForNewProfileSPP() {
        Logger.i(TAG, "loadFileForNewProfile");
        mSupportNewOtaProfile = true;
        //FileInputStream inputStream = null;
        InputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(getOtaFile());
            inputStream = mContext.getAssets().open(getOtaFile());
            int totalSize = inputStream.available();
            int dataSize = totalSize - 4;
            int packetPayload = ProfileUtils.calculateSppSinglePacketLen(dataSize);
            int packetTotalCount = ProfileUtils.calculateSppTotalPacketCount(dataSize);
            int crcCount = ProfileUtils.calculateSppTotalCrcCount(dataSize);
            mOtaData = new byte[crcCount + 1][][];
            Logger.e(TAG, "new profile totalLength: " + totalSize + "; packetTotalCount " + packetTotalCount + "; packet payload " + packetPayload + "; crcCount " + crcCount);
            byte[] data = new byte[dataSize];
            int bytesArray = inputStream.read(data, 0, dataSize);
            if (bytesArray >=0) {
                int position = 0;
                for (int i = 0; i < crcCount; i++) {
                    int startIndex = (int) Math.ceil(i * 1.0 / crcCount * packetTotalCount);
                    int endIndex = (int) Math.ceil((i + 1) * 1.0 / crcCount * packetTotalCount);
                    int length = endIndex - startIndex;
                    int crcPosition = position;
                    mOtaData[i] = new byte[length + 1][];
                    for (int j = 0; j < length; j++) {
                        if (position + packetPayload > dataSize) {
                            packetPayload = dataSize - position;
                        }
                        mOtaData[i][j] = new byte[packetPayload + 1];
                        System.arraycopy(data, position, mOtaData[i][j], 1, packetPayload);
                        mOtaData[i][j][0] = (byte) 0x85;
                        position += packetPayload;
                    }
                    long crc32 = ArrayUtil.crc32(data, crcPosition, position - crcPosition);
                    mOtaData[i][length] = new byte[]{(byte) 0x82, 0x42, 0x45, 0x53, 0x54, (byte) crc32, (byte) (crc32 >> 8), (byte) (crc32 >> 16), (byte) (crc32 >> 24)};
                }
                mOtaData[crcCount] = new byte[1][];
                mOtaData[crcCount][0] = new byte[]{(byte) 0x88};
            }
            Logger.e(TAG, "load file for new profile spp, read to array error, read bytes: " + bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
            mOtaData = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOtaData == null) {
            onLoadFileFailed();
        } else {
            onLoadFileSuccessfully();
        }
    }

    private void loadFile() {
        Logger.i(TAG, "loadFile");
        mSupportNewOtaProfile = false;
        //FileInputStream inputStream = null;
        InputStream inputStream = null;
        try {
            //inputStream = new FileInputStream(getOtaFile());
            inputStream = mContext.getAssets().open(getOtaFile());
            int totalSize = inputStream.available();
            int dataSize = totalSize - 4;
            int packetPayload = 256; //mtu - 16;
            int packetCount = (dataSize + packetPayload - 1) / packetPayload;

            mOtaData = new byte[packetCount][][];
            int position = 0;
            Logger.e(TAG, "totalLength: " + totalSize + " packetCount " + packetCount + " packet payload " + packetPayload);
            for (int i = 0; i < packetCount; i++) {
                if (position + 256 > dataSize) {
                    packetPayload = dataSize - position;
                }
                mOtaData[i] = new byte[1][];
                mOtaData[i][0] = new byte[packetPayload + 16];
                int bytes = inputStream.read(mOtaData[i][0], 16, packetPayload);
                if (bytes >= 0) {
                    long crc32 = ArrayUtil.crc32(mOtaData[i][0], 16, packetPayload);
                    mOtaData[i][0][0] = (byte) 0xBE;
                    mOtaData[i][0][1] = 0x64;
                    mOtaData[i][0][2] = (byte) packetCount;
                    mOtaData[i][0][3] = (byte) (packetCount >> 8);
                    mOtaData[i][0][4] = (byte) packetPayload;
                    mOtaData[i][0][5] = (byte) (packetPayload >> 8);
                    mOtaData[i][0][6] = (byte) (packetPayload >> 16);
                    mOtaData[i][0][7] = (byte) (packetPayload >> 24);
                    mOtaData[i][0][8] = (byte) crc32;
                    mOtaData[i][0][9] = (byte) (crc32 >> 8);
                    mOtaData[i][0][10] = (byte) (crc32 >> 16);
                    mOtaData[i][0][11] = (byte) (crc32 >> 24);
                    mOtaData[i][0][12] = (byte) i;
                    mOtaData[i][0][13] = (byte) (i >> 8);
                    mOtaData[i][0][14] = 0x00;
                    mOtaData[i][0][15] = (byte) ~ArrayUtil.checkSum(mOtaData[i][0], 15);
                    position += packetPayload;
                }
                Logger.e(TAG, "load file, read to array error, read bytes: " + bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mOtaData = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOtaData == null) {
            onLoadFileFailed();
        } else {
            onLoadFileSuccessfully();
        }
    }

//    protected void readyOta() {
//        Logger.i(TAG, "readyOta " + mState);
//        if (TextUtils.isEmpty(mAddress.getText())) {
//            showToast(getString(R.string.pick_device_tips));
//            return;
//        }
//        if (TextUtils.isEmpty(mOtaFile.getText())) {
//            showToast(getString(R.string.pick_File_tips));
//            return;
//        }
//        if (isIdle()) {
//            mOtaConfigDialog.show(getSupportFragmentManager(), OTA_CONFIG_TAG);
//            //updateProgress(0);
//            //sendCmdDelayed(CMD_CONNECT, 0);
//        }
//    }

//    protected void reconnect(){
//        Logger.i(TAG, "reconnect " + mState+" SPAN TIME IS "+RECONNECT_SPAN);
////        LogUtils.writeForOTAStatic(TAG, "reconnect " + mState+" SPAN TIME IS "+RECONNECT_SPAN);
//        mState = STATE_IDLE;
//        if (isIdle()) {
//            updateProgress(0);
//            sendCmdDelayed(CMD_CONNECT, RECONNECT_SPAN);
//        }
//    }


    private void startOta() {
        Logger.i(TAG, "startOta " + mSupportNewOtaProfile);
//        updateInfo(R.string.ota_ing);
        mState = STATE_OTA_ING;
        sendCmdDelayed(CMD_OTA_NEXT, 0);
    }

    private void startOtaConfig() {
        Logger.e(TAG, "startOta " + mState);
        mState = STATE_OTA_CONFIG;
        sendCmdDelayed(CMD_OTA_CONFIG_NEXT, 0);
    }

//    protected void pickFile(int request) {
//        startActivityForResult(new Intent(this, FilePickerActivity.class), request);
//    }

    private void otaNext() {
        synchronized (mOtaLock) {
            if (mState != STATE_OTA_ING || mOtaData == null) {
                Logger.i(TAG, "otaNext  -> mState != STATE_OTA_ING || mOtaData == null ");
                return;
            }
            if (mOtaPacketCount == mOtaData.length) {
                Logger.i(TAG, "otaNext -> mState != STATE_OTA_ING || mOtaData == null ");
                return;
            }
            Logger.i(TAG, "otaNext totalPacketCount = " + totalPacketCount + " ; subCount " + mOtaPacketCount + "; " + mOtaPacketItemCount + "; " + mOtaData[mOtaPacketCount].length);

            if (mSupportNewOtaProfile || mWritten) {

                if ((mOtaPacketItemCount < mOtaData[mOtaPacketCount].length)) {
                    boolean sendRet = sendData(mOtaData[mOtaPacketCount][mOtaPacketItemCount]);
                    if (!sendRet) {
                        Logger.i(TAG, "otaNext write failed , try to resend");
                        sendCmdDelayed(CMD_OTA_NEXT, 40);
                    } else {
                        if (!mSupportNewOtaProfile && mOtaPacketCount == mOtaData.length - 1) {
                            onOtaOver();
                            return;
                        }
                        mOtaPacketItemCount++;
                        if (mOtaPacketItemCount == mOtaData[mOtaPacketCount].length) {
                            removeTimeout();
//                            sendTimeout(R.string.ota_time_out, CMD_DISCONNECT, 30000);   //RESEND
                            sendTimeout(CMD_DISCONNECT, 30000);
                        } else {
                            removeTimeout();
//                            sendTimeout(R.string.ota_time_out, CMD_RESEND_MSG, 10000);   //RESEND
                            sendTimeout(CMD_RESEND_MSG, 10000);
                        }
                    }
                }
            } else {
                Logger.i(TAG, "otaNext  -> (mSupportNewOtaProfile || mWritten) is false, written: " + mWritten);
            }
        }
    }

    private void otaConfigNext() {
        synchronized (mOtaLock) {
            if (mState != STATE_OTA_CONFIG || mOtaConfigData == null) {
                Logger.i(TAG, "otaConfigNext mState != STATE_OTA_CONFIG || mOtaConfigData == null");
                return;
            }
            if (mOtaConfigPacketCount == mOtaConfigData.length) {
                Logger.i(TAG, "otaConfigNext mOtaConfigPacketCount == mOtaConfigData.length");
                return;
            }
            Logger.i(TAG, "otaConfigNext " + mOtaConfigPacketCount + "; " + mOtaConfigData.length + " mWritten = " + mWritten);
            if (!sendData(mOtaConfigData[mOtaConfigPacketCount])) {
                Logger.e(TAG, "otaConfigNext write failed");
                sendCmdDelayed(CMD_OTA_CONFIG_NEXT, 10);
            } else {
                mOtaConfigPacketCount++;
                if (mOtaConfigPacketCount == mOtaConfigData.length) {
//                        sendTimeout(R.string.ota_config_time_out, CMD_DISCONNECT, 5000);
                    sendTimeout(CMD_DISCONNECT, 5000);
                }
            }
        }
    }

//    protected void exitOta() {
//        removeTimeout();
//        mOtaHandler.removeMessages(MSG_SEND_INFO_TIME_OUT);
//        mExit = true;
//    }

    private void otaNextDelayed() {
        synchronized (mOtaLock) {
            if (mState == STATE_OTA_ING) {
                sendCmdDelayed(CMD_OTA_NEXT, (long) 10);
            } else if (mState == STATE_OTA_CONFIG) {
                sendCmdDelayed(CMD_OTA_CONFIG_NEXT, (long) 10);
            }
        }
    }


    @Override
    public void onRetReceived(BluetoothDevice bluetoothDevice, RetResponse retResponse) {
        if (!(retResponse.object instanceof byte[])) {
            return;
        }
        byte[] data = (byte[]) retResponse.object;
        Logger.i(TAG, "onReceive data = " + ArrayUtil.bytesToHex(data));
        synchronized (mOtaLock) {
            Logger.e(TAG, "onReceive " + ArrayUtil.toHexAppendComma(data));
            if (ArrayUtil.isEqual(OTA_PASS_RESPONSE, data)) {
                removeTimeout();
                mOtaPacketItemCount = 0;
                mOtaPacketCount++;
                updateProgress(mOtaPacketCount * 100 / mOtaData.length);
                sendCmdDelayed(CMD_OTA_NEXT, 0);
            } else if (ArrayUtil.isEqual(OTA_RESEND_RESPONSE, data)) {
                removeTimeout();
                mOtaPacketItemCount = 0;
                sendCmdDelayed(CMD_OTA_NEXT, 0);
            } else if (ArrayUtil.startsWith(data, new byte[]{(byte) 0x81, 0x42, 0x45, 0x53, 0x54})) {
                mOtaHandler.removeMessages(MSG_SEND_INFO_TIME_OUT);
                int softwareVersion = ((data[5] & 0xFF) | ((data[6] & 0xFF) << 8));
                int hardwareVersion = ((data[7] & 0xFF) | ((data[8] & 0xFF) << 8));
                Logger.e(TAG, "softwareVersion " + Integer.toHexString(softwareVersion) + "; hardwareVersion " + Integer.toHexString(hardwareVersion));
                mMtu = (data[9] & 0xFF) | ((data[10] & 0xFF) << 8);
                sendCmdDelayed(CMD_LOAD_OTA_CONFIG, 0);
            } else if ((data[0] & 0xFF) == 0x83) {
                if (data.length == 4 && (data[2] & 0xff) == 0x84) {
                    removeTimeout();
                    if ((data[3] & 0xFF) == 0x01) {
                        onOtaOver();
//                        sendCmdDelayed(CMD_DISCONNECT, 0);
                        notifyOtaUpdate(EnumOtaState.Success, -1);
                    } else if ((data[3] & 0xFF) == 0X00) {
                        onOtaFailed();
//                        sendCmdDelayed(CMD_DISCONNECT, 0);
                        notifyOtaUpdate(EnumOtaState.Fail, -1);
                    }
                    mOtaPacketItemCount = 0;
                } else {
                    removeTimeout();
                    if ((data[1] & 0xFF) == 0x01) {
                        mOtaPacketCount++;
                        updateProgress(mOtaPacketCount * 100 / mOtaData.length);
                    } else if ((data[1] & 0xFF) == 0X00) {
                        updateProgress(mOtaPacketCount * 100 / mOtaData.length);
                    }
                    mOtaPacketItemCount = 0;
                    Logger.e("test", "befor time " + System.currentTimeMillis());
                    sendCmdDelayed(CMD_OTA_NEXT, 0);
                }
            } else if ((data[0] & 0xFF) == 0x84) {
                removeTimeout();
                if ((data[1] & 0xFF) == 0x01) {
                    onOtaOver();
//                    sendCmdDelayed(CMD_DISCONNECT, 0);
                    notifyOtaUpdate(EnumOtaState.Success, -1);
                } else if ((data[1] & 0xFF) == 0X00) {
                    onOtaFailed();
//                    sendCmdDelayed(CMD_DISCONNECT, 0);
                    notifyOtaUpdate(EnumOtaState.Fail, -1);
                }
                mOtaPacketItemCount = 0;

            } else if ((data[0] & 0xFF) == 0x87) {
                removeTimeout();
                if ((data[1] & 0xFF) == 0x01) {
                    if (isBle()) {
                        sendCmdDelayed(CMD_LOAD_FILE_FOR_NEW_PROFILE, 0);
                    } else {
                        sendCmdDelayed(CMD_LOAD_FILE_FOR_NEW_PROFILE, 0);
//                   		 sendCmdDelayed(CMD_LOAD_FILE_FOR_NEW_PROFILE_SPP, 0);
                    }
                } else {
                    onOtaConfigFailed();
//                    sendCmdDelayed(CMD_DISCONNECT, 0);
                    notifyOtaUpdate(EnumOtaState.Fail, -1);
                }
            }
        }
    }

    private int getMtu() {
        return mMtu;
    }

    private boolean sendData(byte[] data) {

        return leDevice.writeOta(data, false);
    }

    private boolean isBle() {
        return true;
    }

    private String getOtaFile() {
        return OTA_FILE;
    }

    public class CmdHandler extends Handler {
        CmdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case CMD_CONNECT:
//                    connect();
//                    break;
                case CMD_DISCONNECT:
                    notifyOtaUpdate(EnumOtaState.Fail, -1);
//                    disconnect();
                    break;
                case CMD_LOAD_FILE:
                    loadFile();
                    break;
                case CMD_OTA_NEXT:
                    Logger.e(TAG, "after time " + System.currentTimeMillis());
                    otaNext();
                    break;
                case CMD_START_OTA:
                    startOta();
                    break;


//                case CMD_SEND_FILE_INFO:
//                    sendFileInfo();
//                    break;
                case CMD_LOAD_OTA_CONFIG:
                    loadOtaConfig();
                    break;
                case CMD_START_OTA_CONFIG:
                    startOtaConfig();
                    break;
                case CMD_OTA_CONFIG_NEXT:
                    otaConfigNext();
                    break;
                case CMD_LOAD_FILE_FOR_NEW_PROFILE:
                    loadFileForNewProfile();
                    break;
                case CMD_LOAD_FILE_FOR_NEW_PROFILE_SPP:
                    loadFileForNewProfileSPP();
                    break;
                case CMD_RESEND_MSG:
                    Logger.i(TAG, "resend the msg");
                    sendCmdDelayed(CMD_OTA_NEXT, 0);
                    break;
            }
        }
    }

    @Override
    public void onLeConnectStatus(BluetoothDevice bluetoothDevice, boolean isConnected) {

    }

    @Override
    public void onMtuChanged(BluetoothDevice bluetoothDevice, int status, int mtu) {
        mMtu = mtu;
    }

    @Override
    public void onLeOta(BluetoothDevice bluetoothDevice, EnumOtaState state, int progress) {
        sendFileInfo();
    }

    @Override
    public void onWritten(int status) {
        if (status == 0) {
            Logger.d(TAG, "on written SUCCESS");
            otaNextDelayed();
        } else {
            Logger.e(TAG, "on written return false status is " + status);
        }
    }
}
