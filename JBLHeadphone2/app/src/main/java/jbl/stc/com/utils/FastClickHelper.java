package jbl.stc.com.utils;

public class FastClickHelper {

    private static long lastClickTime;

    public static boolean isFastClick() {
        long now = System.currentTimeMillis();

        boolean tooFast = (now - lastClickTime) < 500;
        if (!tooFast)
            lastClickTime = now;

        return tooFast;
    }

    public static boolean isFastClick(long timeMillis) {
        long now = System.currentTimeMillis();

        boolean tooFast = (now - lastClickTime) < timeMillis;
        if (!tooFast)
            lastClickTime = now;

        return tooFast;
    }

}
