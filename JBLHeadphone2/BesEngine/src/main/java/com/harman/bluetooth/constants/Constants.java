package com.harman.bluetooth.constants;

import java.util.UUID;

public class Constants {

    public static final UUID GATT_SERVICE_UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");

    public static final UUID BLE_RX_TX_SERVICE_UUID = UUID.fromString("65786365-6c70-6f69-6e74-2e636f6d0000");
    public static final UUID RX_CHAR_UUID = UUID.fromString("65786365-6c70-6f69-6e74-2e636f6d0001");
    public static final UUID TX_CHAR_UUID = UUID.fromString("65786365-6c70-6f69-6e74-2e636f6d0002");
//    public static final UUID BES_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID OTA_SERVICE_NORMAL_UUID = UUID.fromString("01000100-0000-1000-8000-009078563412");
    public static final UUID OTA_CHARACTERISTIC_NORMAL_UUID = UUID.fromString("03000300-0000-1000-8000-009278563412");
    public static final UUID OTA_SERVICE_OTA_UUID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID OTA_CHARACTERISTIC_OTA_UUID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID OTA_DESCRIPTOR_OTA_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final boolean DEFAULT_CLEAR_USER_DATA = true;
    public static final boolean DEFAULT_UPDATE_BT_ADDRESS = false;
    public static final boolean DEFAULT_UPDATE_BT_NAME = false;
    public static final boolean DEFAULT_UPDATE_BLE_ADDRESS = false;
    public static final boolean DEFAULT_UPDATE_BLE_NAME = false;

    public static final String KEY_OTA_CONFIG_CLEAR_USER_DATA = "ota_config_clear_user_data";
    public static final String KEY_OTA_CONFIG_UPDATE_BT_ADDRESS = "ota_config_update_bt_address";
    public static final String KEY_OTA_CONFIG_UPDATE_BT_ADDRESS_VALUE = "ota_config_update_bt_address_value";
    public static final String KEY_OTA_CONFIG_UPDATE_BT_NAME = "ota_config_update_bt_name";
    public static final String KEY_OTA_CONFIG_UPDATE_BT_NAME_VALUE = "ota_config_update_bt_name_value";
    public static final String KEY_OTA_CONFIG_UPDATE_BLE_ADDRESS = "ota_config_update_ble_address";
    public static final String KEY_OTA_CONFIG_UPDATE_BLE_ADDRESS_VALUE = "ota_config_update_ble_address_value";
    public static final String KEY_OTA_CONFIG_UPDATE_BLE_NAME = "ota_config_update_ble_name";
    public static final String KEY_OTA_CONFIG_UPDATE_BLE_NAME_VALUE = "ota_config_update_ble_name_value";


    public static final int DEFAULT_MTU = 512;

}
