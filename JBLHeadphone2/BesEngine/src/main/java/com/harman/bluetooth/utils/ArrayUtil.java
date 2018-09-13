package com.harman.bluetooth.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;


public class ArrayUtil {

    public static final String TAG = "ArrayUtil";

    public static byte[] extractBytes(byte[] data, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(data, start, bytes, 0, length);
        return bytes;
    }

    public static boolean isEqual(byte[] array_1, byte[] array_2) {
        if (array_1 == null) {
            return array_2 == null;
        }
        if (array_2 == null) {
            return false;
        }
        if (array_1 == array_2) {
            return true;
        }
        if (array_1.length != array_2.length) {
            return false;
        }
        for (int i = 0; i < array_1.length; i++) {
            if (array_1[i] != array_2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(byte[] parent, byte[] child) {
        if (parent == null) {
            return child == null;
        }
        if (child == null || child.length == 0) {
            return true;
        }
        if (parent == child) {
            return true;
        }
        return new String(parent).contains(new String(child));
    }

    public static long crc32(byte[] data, int offset, int length) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, offset, length);
        return crc32.getValue();
    }

    public static byte checkSum(byte[] data, int len) {
        byte sum = (byte) 0;
        for (int i = 0; i < len; i++) {
            sum ^= data[i];
        }
        return sum;
    }

    public static String toHexAppendComma(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(String.format("%02x", data[i])).append(",");
        }
        return buffer.toString();
    }

    public static String toHexAppendCommaByByte(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            buffer.append(String.format("%02x", data[i]));
            if ((i+1) % 4 == 0) {
                buffer.append(" ");
            }
        }
        return buffer.toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStr2Bytes(String s) {
//        int m,n;
//        int l=src.length()/2;
//        byte[] ret = new byte[l];
//        for (int i = 0; i < l; i++)
//        {
//            m=i*2+1;
//            n=m+1;
//            ret[i] = (byte)Integer.valueOf(("0x" + src.substring(i*2, m) + src.substring(m,n)),16);
//        }
//
//        return ret;
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0)
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexToBin(s.charAt(i));
            int l = hexToBin(s.charAt(i + 1));
            if (h == -1 || l == -1)
                throw new IllegalArgumentException("contains illegal character for hexBinary: " + s);

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') return ch - '0';
        if ('A' <= ch && ch <= 'F') return ch - 'A' + 10;
        if ('a' <= ch && ch <= 'f') return ch - 'a' + 10;
        return -1;
    }


    public static boolean startsWith(byte[] data, byte[] param) {
        if (data == null) {
            return param == null;
        }
        if (param == null) {
            return true;
        }
        if (data.length < param.length) {
            return false;
        }
        for (int i = 0; i < param.length; i++) {
            if (data[i] != param[i]) {
                return false;
            }
        }
        return true;
    }

    public static int bytesToInt(byte[] src) {
        return (src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8)
                | ((src[2] & 0xFF) << 16)
                | ((src[3] & 0xFF) << 24);
    }

    public static int hexStrToInt(String bc) {
//        Logger.d(TAG, "hex string: " + bc);
        byte[] src = hexStr2Bytes(bc);
        return (src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8)
                | ((src[2] & 0xFF) << 16)
                | ((src[3] & 0xFF) << 24);
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static float hexStrToFloat(String bc) {
        byte[] bytes = hexStr2Bytes(bc);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static byte[] float2bytes(float f) {
        int fbit = Float.floatToIntBits(f);
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }
        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;

    }
}
