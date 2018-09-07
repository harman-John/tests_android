package com.harman.bluetooth.utils;

import java.util.zip.CRC32;

public class ArrayUtil {
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

    public static String toHex(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(String.format("%02x", data[i])).append(",");
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

    public static byte[] hexStr2Bytes(String src)
    {
        int m,n;
        int l=src.length()/2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
        }
        return ret;
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

}
