package jbl.stc.com.fragment;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connected_before,
                container, false);

        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        Set<String> connectedBeforeDevices = PreferenceUtils.getStringSet(getContext(), PreferenceKeys.CONNECTED_BEFORE_DEVICES);
        List<String> addLists = new ArrayList<>();

        for (BluetoothDevice pairedDevice : pairedDevices) {
            for (String deviceInfo : connectedBeforeDevices) {
                if (deviceInfo.contains(pairedDevice.getAddress())) {
                    addLists.add(deviceInfo);
                }
            }
        }

        connectedBeforeDevices.clear();
        for (String removeDevice : addLists) {
            connectedBeforeDevices.add(removeDevice);
        }

        gridView = view.findViewById(R.id.grid_view_connected_before);
        Set<String> devicesSet = PreferenceUtils.getStringSet(getContext(), PreferenceKeys.CONNECTED_BEFORE_DEVICES);
        Logger.i(TAG, "deviceSet = " + devicesSet + ",pairedDevices = " + pairedDevices);
        List<ConnectedBeforeDevice> lists = new ArrayList<>();
        for (String value : devicesSet) {
            ConnectedBeforeDevice connectedBeforeDevice = new ConnectedBeforeDevice();
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
        connectedBeforeGridAdapter = new ConnectedBeforeGridAdapter();
        connectedBeforeGridAdapter.setConnectedBeforeList(lists);
        gridView.setAdapter(connectedBeforeGridAdapter);
        return view;
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
        }

    }
}
