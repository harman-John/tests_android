package jbl.stc.com.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * DateUtils
 * Created by darren.lu on 8/15/2017.
 */
public class DateUtils {

    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;

    private static SimpleDateFormat sdf = new SimpleDateFormat();//线程不安全

    public static String formatDateTime(long milliseconds) {
        return formatDateTime(milliseconds, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDateTime2(long milliseconds) {
        return formatDateTime(milliseconds, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String formatDateTime(long milliseconds, float timeZoneOffset) {
        if (timeZoneOffset > 13 || timeZoneOffset < -12) {
            timeZoneOffset = 0;
        }

        int newTime = (int) (timeZoneOffset * 60 * 60 * 1000);
        TimeZone timeZone;
        String[] ids = TimeZone.getAvailableIDs(newTime);
        if (ids.length == 0) {
            timeZone = TimeZone.getDefault();
        } else {
            timeZone = new SimpleTimeZone(newTime, ids[0]);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(timeZone);
        return sdf.format(new Date(milliseconds));
    }

    public static String formatDateTime(long milliseconds, String pattern) {
        sdf.applyPattern(pattern);
        return sdf.format(new Date(milliseconds));
    }

    public static boolean isTheSameDay(Date date) {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        c1.setTime(date);
        c2.setTime(new Date());
        return c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2
                .get(Calendar.DAY_OF_MONTH)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isTheSameYear(Date date) {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        c1.setTime(date);
        c2.setTime(new Date());
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static String getTimeTextHM(Date date) {
        sdf.applyPattern("HH:mm");
        return sdf.format(date);
    }

    public static String getStandardTimeText(long milliseconds) {
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String getTimeTextYMD(Date date) {
        sdf.applyPattern("yyyy年MM月dd日");
        return sdf.format(date);
    }

    public static String getYearMonthDayTime(long milliseconds) {
        sdf.applyPattern("yyyy-MM-dd");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String getTimeTextYMD(long milliseconds) {
        sdf.applyPattern("yyyy.MM.dd");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String getTimeTextMD(long milliseconds) {
        sdf.applyPattern("MM-dd");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String getTimeTextMD2(long milliseconds) {
        sdf.applyPattern("MM月dd日");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

    public static String getTimeTextMD3(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return month + "月" + day + "日";
    }


    /**
     * 取月日时  mm月dd日hh时
     */
    public static String getTimeStrMDH(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return month + "月" + day + "日" + hour + "时";
    }

    public static String getTimeStrMDH2(long milliseconds) {
        sdf.applyPattern("MM月dd日HH时");
        Date date = new Date(milliseconds);
        return sdf.format(date);
    }

}