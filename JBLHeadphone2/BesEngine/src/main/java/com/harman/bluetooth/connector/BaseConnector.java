package com.harman.bluetooth.connector;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.harman.bluetooth.listeners.BesListener;
import com.harman.bluetooth.listeners.ConnectorListener;

import java.util.List;
import java.util.UUID;

public interface BaseConnector {
//
//    void addConnectListener(ConnectorListener connectorListener);
//
//    void removeConnectListener(ConnectorListener connectorListener);

    void setListener(List<BesListener> listBesListener);

    boolean connect(Context context, String address);

    boolean connect(Context context, BluetoothDevice device);

    boolean discoverServices();

    boolean requestMtu(int mtu);

    boolean enableCharacteristicNotify(UUID service, UUID rxCharacteristic, UUID descriptor);

    void close();

    boolean isConnected();

    boolean refresh();

    boolean setWriteCharacteristic(UUID service, UUID characteristic);

    boolean write(byte[] data);

    boolean write_no_rsp(byte[] data);
}
