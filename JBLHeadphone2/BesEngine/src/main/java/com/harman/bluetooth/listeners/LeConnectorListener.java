package com.harman.bluetooth.listeners;


public interface LeConnectorListener extends ConnectorListener {

    void onServicesDiscovered(int status);

    void onCharacteristicNotifyEnabled(int status);

    void onWritten(int status);

    void onMtuChanged(int status, int mtu);

}
