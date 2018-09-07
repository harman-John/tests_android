package com.harman.bluetooth.req;

import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class BaseCmd {
    private final static String TAG = BaseCmd.class.getSimpleName();

    private byte[] mCommand;

    /**
     * Function to set command and payload
     *
     * @param header  static Command defined in this class, like IDENT_DEV
     * @param payload payload byte array to be sent.
     */
    public synchronized void combine(byte[] header, byte[] payload) {
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
    public void combine(byte[] header) {
        mCommand = new byte[3];
        System.arraycopy(header, 0, mCommand, 0, header.length);
        mCommand[header.length] = (byte) 0x00;
        Logger.d(TAG, " combine to command without payload: " + ArrayUtil.bytesToHex(mCommand));
    }

    /**
     * Function to get Command
     */
    public byte[] getCommand() {
        return mCommand;
    }

}
