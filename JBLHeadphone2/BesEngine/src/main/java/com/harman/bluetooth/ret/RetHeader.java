package com.harman.bluetooth.ret;

public class RetHeader {


    /**
     * Payload length: 0x02
     * Request command id(1 byte): the command id which is sent from app
     * Status code(1 byte): response status, 0 indicates success, other failure
     */
    public static final String RET_DEV_ACK = "aa00";

    /**
     * Payload length: 0x01
     * Message code(1 byte): meeage code that describe the quit reason
     * Eg: Unknown(0x00), device power off(0x01)...
     */
    public static final String RET_DEV_BYE= "aa02";

    public static final String RET_DEV_FIN_ACK = "aa04";

    /**
     * Payload length: 0xnn
     * Device name(16 bytes): UTF-8, max to 16 bytes
     * Product id(2 bytes)
     * Model id(1 bytes)
     * Battery status(1 bytes): 1bit(MSB), charging status, 1 means battery charging. 7bits(LSB), 0-100 percent 0%-100%.
     * Mac address(6 bytes): Bluetooth mac address
     * Firmware version(3 bytes):
     *          Byte 1 - major
     *          Byte 2 - minor
     *          Byte 3 - revision
     *          eg: v1.2.3
     *          Byte 1 - 0x01
     *          Byte 2 - 0x02
     *          Byte 3 - 0x03
     */
    public static final String RET_DEV_INFO = "aa12";

    /**
     * Payload length: 0xnn
     * Status type(1byte): status type id DEVICE_STATUS_TYPE
     *      DEVICE_STATUS_TYPE: (include all status)
     *      REQ_ANC 0x31(1 byte): 0x00/0x01 means TALK_THRU/AMBIENT_AWARE
     *      AA mode 0x32(1 byte): 0x00/0x01 means Talk Thru/AmbientAware
     *      Auto off 0x33(1 byte): 1bit(MSB) - 0/1 means disable/enable, 7bit(LSB): auto off time value xx/min
     *      EQ preset 0x34(1 byte): return EQ current preset, preset index
     */
    public static final String RET_DEV_STATUS = "aa22";

    /**
     * Payload length(0xnn):
     *      Preset index(1 byte): 0/1/2/3/4 means off/jazz/vocal/bass/custom
     *      EQ category(1 byte):  0x00/0x01/0x02 Design EQ/Graphic EQ/Total EQ
     *      Sample Rate(1 byte): Value * k (ex. Value = 48, actual rate = 48 * k)
     *      Gain0(1 byte): Left gain value
     *      Gain1(1 byte): Right gain value
     *      Band Count(1 byte): Number of the band count
     *      IIR param
     * Footer (Identifier: 0xAA, Command ID: 0x04, PayloadLen: 0x00)
     */
    public static final String RET_CURRENT_EQ = "aa43";


    /**
     * Device status typeALL_STATUS
     */
    public static final String ALL_STATUS_TYPE = "30";
    public static final String ANC_TYPE ="31";
    public static final String AA_MODE_TYPE = "32";
    public static final String AUTO_OFF_TYPE = "33";
    public static final String EQ_PRESET_TYPE = "34";
}
