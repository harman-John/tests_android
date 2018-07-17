package jbl.stc.com.fragment;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.adapter.ConnectedBeforeGridAdapter;
import jbl.stc.com.adapter.MyProductsGridAdapter;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.ConnectedBeforeDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;

public class MyProductsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = MyProductsFragment.class.getSimpleName();
    private View view;
    private MyProductsGridAdapter myProductsGridAdapter;
    private GridView gridView;
    private BluetoothDevice mBluetoothDevice;
    private List<ConnectedBeforeDevice> lists;
    private Context mContext;

    public void removeConnectBeforeMessage(){
        myProductsGridAdapter.removeAllMessage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_products,
                container, false);
        view.findViewById(R.id.relative_layout_my_products_title).bringToFront();
//        view.findViewById(R.id.image_view_my_products_white_plus).setOnClickListener(this);
        view.findViewById(R.id.image_view_my_products_white_menu).setOnClickListener(this);
        gridView = view.findViewById(R.id.grid_view_connected_before);
        initList();
        initView();
        return view;
    }

    public int getA2dpConnectedDevices(){
        int connectedDeviceInc = 0;
        for (ConnectedBeforeDevice connectedBeforeDevice: lists){
            if (connectedBeforeDevice.a2dpConnected){
                connectedDeviceInc ++;
            }
        }
        return connectedDeviceInc;
    }


    public void setDeviceConnected(){
        Message msg = new Message();
        msg.what = MSG_DEVICE_CONNECTED;
        mBluetoothDevice = DashboardActivity.getDashboardActivity().getSpecifiedDevice();
        cbHandler.sendMessage(msg);
    }

    public void setDeviceDisconnected(){
        for(ConnectedBeforeDevice connectedBeforeDevice: lists){
            connectedBeforeDevice.a2dpConnected = false;
        }
        myProductsGridAdapter.setConnectedBeforeList(lists);
    }

    private void initView(){
        myProductsGridAdapter = new MyProductsGridAdapter();
        myProductsGridAdapter.setConnectedBeforeList(lists);
        gridView.setAdapter(myProductsGridAdapter);
    }

    private void initList(){
        lists = new ArrayList<>();
        Set<String> devicesSet = PreferenceUtils.getStringSet(getActivity(), PreferenceKeys.CONNECTED_BEFORE_DEVICES);
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

    @Override
    public void onResume() {
        super.onResume();
        if (DashboardActivity.getDashboardActivity().isConnected()){
            setDeviceConnected();
        }else {
            setDeviceDisconnected();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_my_products_white_menu: {
                getActivity().onBackPressed();
                break;
            }
//            case R.id.image_view_my_products_white_plus:{
//                if (DashboardActivity.getDashboardActivity().isConnected()
//                        &&DashboardActivity.getDashboardActivity().getConnectedBeforeCount() == 1){
//                    DashboardActivity.getDashboardActivity().goHomeFragment();
//                }else{
//                    getActivity().onBackPressed();
//                }
//                break;
//            }
        }

    }

    private CbHandler cbHandler = new CbHandler(Looper.getMainLooper());
    private final static int MSG_DEVICE_CONNECTED = 0;
    private class CbHandler extends Handler{
        public CbHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DEVICE_CONNECTED:{
                    for(ConnectedBeforeDevice connectedBeforeDevice: lists){
                        if (mBluetoothDevice!= null
                                && mBluetoothDevice.getName()!=null
                                && mBluetoothDevice.getName().toUpperCase().contains(connectedBeforeDevice.deviceName)) {
                            connectedBeforeDevice.a2dpConnected = true;
                            myProductsGridAdapter.setConnectedBeforeList(lists);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }
}
