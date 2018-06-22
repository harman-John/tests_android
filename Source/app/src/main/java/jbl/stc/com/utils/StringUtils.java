package jbl.stc.com.utils;

import java.text.DecimalFormat;

/**
 * StringUtils
 *
 * @author daman.lu 2017-9-29
 */
public class StringUtils {
    private static DecimalFormat format = new DecimalFormat("#0.0");
    private static DecimalFormat format_2 = new DecimalFormat("#0.00");

    public static String getStringFromFloat(float f) {
        if (0 == (f - (int) f)) {
            return (int) f + "";
        } else {
            return f + "";
        }
    }

    public static String getStringFromDouble(double d) {
        if (0 == (d - (long) d)) {
            return (long) d + "";
        } else {
            return format_2.format(d);
        }
    }

    public static double getDoubleFromString(String str) {
        return Double.valueOf(str);
    }

    public static String getOnePointFloat(float f) {
        return format.format(f);
    }

    public static String getTowPointFloat(float f) {
        return format_2.format(f);
    }

    public static String getTowPointDouble(double d) {
        return format_2.format(d);
    }
}
