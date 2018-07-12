package jbl.stc.com.fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jbl.stc.com.R;
import jbl.stc.com.adapter.ConnectedBeforeGridAdapter;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.ConnectedBeforeDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;

public class ConnectedBeforeFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = ConnectedBeforeFragment.class.getSimpleName();
    private View view;
    private ConnectedBeforeGridAdapter connectedBeforeGridAdapter;
    private GridView gridView;

    public void stopTimerConnected(){
        connectedBeforeGridAdapter.stopTimerConnected();
    }

    public void setSpecifiedDevice(BluetoothDevice bluetoothDevice){
        Message msg = new Message();
        msg.what = MSG_DEVICE_CONNECTED;
        msg.obj = bluetoothDevice;
        cbHandler.sendMessage(msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void startA2DPCheck(){
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            mBtAdapter.getProfileProxy(mContext, mListener, BluetoothProfile.A2DP);
        }
    }
    List<BluetoothDevice> deviceList;
    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if(profile == BluetoothProfile.A2DP) {
                deviceList = proxy.getConnectedDevices();
                Logger.d(TAG, " A2DP connected deviceList = "+deviceList +",size = "+deviceList.size());
                cbHandler.sendEmptyMessage(MSG_UPDATE_UI);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connected_before,
                container, false);
        startA2DPCheck();
        view.findViewById(R.id.relative_layout_connected_before_title).bringToFront();
        view.findViewById(R.id.image_view_connected_before_white_plus).setOnClickListener(this);
        gridView = view.findViewById(R.id.grid_view_connected_before);

        return view;
    }

    public int connectedDeviceThroughA2dp(){
        int connectedDeviceInc = 0;
        for (ConnectedBeforeDevice connectedBeforeDevice: lists){
            if (connectedBeforeDevice.a2dpConnected){
                connectedDeviceInc ++;
            }
        }
        return connectedDeviceInc;
    }

    List<ConnectedBeforeDevice> lists;
    private void initList(){
        lists = new ArrayList<>();
        Set<String> devicesSet = PreferenceUtils.getStringSet(getContext(), PreferenceKeys.CONNECTED_BEFORE_DEVICES);
        Logger.i(TAG, "deviceSet = " + devicesSet);
        for (String value : devicesSet) {
            ConnectedBeforeDevice connectedBeforeDevice = new ConnectedBeforeDevice();
            connectedBeforeDevice.a2dpConnected = false;
            if (value.toUpperCase().contains(JBLConstant.DEVICE_LIVE_650BTNC)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_LIVE_650BTNC;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_LIVE_400BT)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_LIVE_400BT;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_LIVE_500BT)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_LIVE_500BT;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_750NC)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_750NC;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_750nc_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_REFLECT_AWARE)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_REFLECT_AWARE;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.reflect_aware_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_150NC)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_150NC;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_150nc_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_700)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_700;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_700_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_100)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_100;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_100_icon);
            } else if (value.toUpperCase().contains(JBLConstant.DEVICE_EVEREST_ELITE_300)) {
                connectedBeforeDevice.deviceName = JBLConstant.DEVICE_EVEREST_ELITE_300;
                connectedBeforeDevice.deviceIcon = ContextCompat.getDrawable(getActivity(), R.mipmap.everest_elite_300_icon);
            }
            lists.add(connectedBeforeDevice);
        }
    }

    private void initView(){
        initList();
        connectedBeforeGridAdapter = new ConnectedBeforeGridAdapter();
        connectedBeforeGridAdapter.setConnectedBeforeList(lists);
        gridView.setAdapter(connectedBeforeGridAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_connected_before_white_menu: {
                switchFragment(new InfoFragment(), JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.image_view_connected_before_white_plus:{
                getActivity().onBackPressed();
                break;
            }
        }

    }

    private CbHandler cbHandler = new CbHandler(Looper.getMainLooper());
    private final static int MSG_UPDATE_UI = 0;
    private final static int MSG_DEVICE_CONNECTED = 1;
    private class CbHandler extends Handler{
        public CbHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_UI:{
                    initView();
                    break;
                }
                case MSG_DEVICE_CONNECTED:{
                    BluetoothDevice bluetoothDevice = ((BluetoothDevice)msg.obj);
                    for(ConnectedBeforeDevice connectedBeforeDevice: lists){
                        if (bluetoothDevice.getName().toUpperCase().contains(connectedBeforeDevice.deviceName)) {
                            connectedBeforeDevice.a2dpConnected = true;
                            connectedBeforeGridAdapter.setConnectedBeforeList(lists);
                            break;
                        }
                    }
                    stopTimerConnected();
                    break;
                }
            }
        }
    }
}
