package com.harman.bluetooth.listeners;

public interface ConnectorListener {

    void onConnectionStateChanged(boolean connected);

    void onReceive(byte[] data);
}
