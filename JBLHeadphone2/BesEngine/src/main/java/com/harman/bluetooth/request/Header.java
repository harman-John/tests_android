package com.harman.bluetooth.request;

import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class Header {

    private final static String TAG = Header.class.getSimpleName();

    private static byte[] mCommand;

    public static byte[] SET_APP_ACK = new byte[]{
            (byte) 0xaa,
            (byte) 0x01,
    };

    public static byte[] SET_APP_BYE = new byte[]{
            (byte) 0xaa,
            (byte) 0x03,
    };

    public static byte[] SET_APP_FIN_ACK = new byte[]{
            (byte) 0xaa,
            (byte) 0x05,
    };

    public static byte[] REQ_DEV_INFO = new byte[]{
            (byte) 0xaa,
            (byte) 0x11,
    };

    public static byte[] REQ_DEV_STATUS = new byte[]{
            (byte) 0xaa,
            (byte) 0x21,
    };

    public static byte[] SET_ANC = new byte[]{
            (byte) 0xaa,
            (byte) 0x31,
    };

    public static byte[] SET_AA_MODE = new byte[]{
            (byte) 0xaa,
            (byte) 0x32,
    };

    public static byte[] SET_AUTO_OFF = new byte[]{
            (byte) 0xaa,
            (byte) 0x33,
    };

    public static byte[] SET_EQ_PRESET = new byte[]{
            (byte) 0xaa,
            (byte) 0x40,
    };

    public static byte[] SET_EQ_SETTINGS = new byte[]{
            (byte) 0xaa,
            (byte) 0x41,
    };

    public static byte[] REQ_CURRENT_EQ = new byte[]{
            (byte) 0xaa,
            (byte) 0x42,
    };

    /**
     * DEVICE_STATUS_TYPE: (include all status)
     * REQ_ANC 0x31(1 byte): 0x00/0x01 means TALK_THRU/AMBIENT_AWARE
     * AA mode 0x32(1 byte): 0x00/0x01 means Talk Thru/AmbientAware
     * Auto off 0x33(1 byte): 1bit(MSB) - 0/1 means disable/enable, 7bit(LSB): auto off time value xx/min
     * EQ preset 0x34(1 byte): return EQ current preset, preset index
     */
    public static byte[][] DEVICE_STATUS_TYPE = new byte[][]{
            {(byte) 0x30},
            {(byte) 0x31},
            {(byte) 0x32},
            {(byte) 0x33},
            {(byte) 0x34},
    };

    /**
     * Payload length(1 byte): 0 - off, 1 - jazz, 2 - vocal, 3 - bass, 4 - custom
     */
    public static byte[][] EQ_PRESET = new byte[][]{
            {(byte) 0x00},
            {(byte) 0x01},
            {(byte) 0x02},
            {(byte) 0x03},
            {(byte) 0x04},
    };

    /**
     * Function to set command and payload
     *
     * @param header  static Command defined in this class, like IDENT_DEV
     * @param payload payload byte array to be sent.
     */
    public static synchronized void combine(byte[] header, byte[] payload) {
        if (payload == null)
            return;
        mCommand = new byte[3 + payload.length];
        System.arraycopy(header, 0, mCommand, 0, header.length);
        mCommand[header.length] = (byte) payload.length;
        System.arraycopy(payload, 0, mCommand, 3, payload.length);
        Logger.d(TAG, " combine to command: " + ArrayUtil.bytesToHex(mCommand));
    }

    /**
     * Function to compute a command with NO payload. Payload NOT Applicable.
     *
     * @param header byte[]
     */
    public static void combine(byte[] header) {
        mCommand = new byte[3];
        System.arraycopy(header, 0, mCommand, 0, header.length);
        mCommand[header.length] = (byte) 0x00;
        Logger.d(TAG, " combine to command without payload: " + ArrayUtil.bytesToHex(mCommand));
    }

    /**
     * Function to get Command
     */
    public static byte[] getCommand() {
        return mCommand;
    }



}
