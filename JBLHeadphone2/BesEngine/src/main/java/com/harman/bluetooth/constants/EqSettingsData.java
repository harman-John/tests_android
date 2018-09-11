package com.harman.bluetooth.constants;

import com.harman.bluetooth.utils.ArrayUtil;
import com.harman.bluetooth.utils.Logger;

public class EqSettingsData {
    private static final String TAG = EqSettingsData.class.getSimpleName();
    private byte[] commands;
    public int packageCount;
    public int sendIdx;

    public EqSettingsData(byte[] bytes) {
        commands = new byte[bytes.length];
        System.arraycopy(bytes, 0, commands, 0, bytes.length);
        float count = ((float) commands.length - 4f) / 79f;
        int countInt = (int) count;

        if ((commands.length - 4) < 79f) {
            packageCount = 1;
        } else if (count > countInt) {
            packageCount = countInt + 1;
        } else {
            packageCount = countInt;
        }
        sendIdx = 0;
    }

    public byte[] getHeader() {
        byte[] header = new byte[4];
        System.arraycopy(commands, 0, header, 0, 4);
        return header;
    }

    public byte[] getPayload(int pIdx) {
        int length = commands.length - 4 + packageCount;
        int leftBytes = (commands.length - 5) % 79;
        if (length >= 80 * (pIdx + 1)) {
            byte[] pLoad = new byte[80];
            pLoad[0] = (byte) pIdx;
            System.arraycopy(commands, 4 + pIdx*80, pLoad, 1, 79);
            return pLoad;
        } else {
            byte[] pLoad = new byte[leftBytes];
            Logger.d(TAG,"p load len: "+pLoad.length);
            Logger.d(TAG,"cmd load len: "+commands.length);
            Logger.d(TAG,"left bytes len: "+leftBytes);

            Logger.d(TAG,"commands : "+ ArrayUtil.bytesToHex(commands));
            pLoad[0] = (byte) pIdx;
            System.arraycopy(commands, 80 * pIdx +4, pLoad, 1, leftBytes-1);
            return pLoad;
        }
    }
}
