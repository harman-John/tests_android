package jbl.stc.com.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by intahmad on 8/11/2015.
 */
public class FirmwareUtil {
    public static String JBL_RSRCversion = "JBL_RSRCversion";
    public static AtomicBoolean isUpdatingFirmWare = new AtomicBoolean();
    public static int currentFirmware = -1;
    public static String disconnectHeadphoneText = null;
    static {
        isUpdatingFirmWare.set(false);
    }

    /**
     * @param rawResourceId
     * @param resources
     * @return
     * @deprecated
     */
    public static byte[] readRawResource(int rawResourceId, Resources resources) {
        byte[] buffer;
        InputStream inputStream;
        int read;
        ByteArrayOutputStream outputStream;

        buffer = new byte[4096];
        inputStream = resources.openRawResource(rawResourceId);
        outputStream = new ByteArrayOutputStream();

        try {
            for (; ; ) {
                if ((read = inputStream.read(buffer)) < 0) break;

                outputStream.write(buffer, 0, read);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * <p>Read data from Input stream</p>
     *
     * @param inputStream
     * @return
     */
    public static byte[] readInputStream(InputStream inputStream) {
        byte[] buffer;
        int read;
        ByteArrayOutputStream outputStream;

        buffer = new byte[4096];
        outputStream = new ByteArrayOutputStream();
        try {
            for (; ; ) {
                if ((read = inputStream.read(buffer)) < 0) break;

                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * <p>Checks whether firmware update is available by comparing the liveVersion with currentVersion</p>
     *
     * @param liveVersion
     * @param currentVersion
     * @return True is update is available else FALSE
     */
    public static boolean isUpdateAvailable(String liveVersion, String currentVersion) {
        String[] liveArray = new String[3];
        String[] currentArray = new String[3];
        int counter = 0;
        StringTokenizer st = new StringTokenizer(liveVersion, ".");

        while (st.hasMoreTokens()) {
            String x = st.nextToken();
            liveArray[counter++] = x;

        }
        counter = 0;
        st = new StringTokenizer(currentVersion, ".");
        while (st.hasMoreTokens()) {
            String x = st.nextToken();
            currentArray[counter++] = x;

        }
        try {
            if (Integer.parseInt(liveArray[0]) > Integer
                    .parseInt(currentArray[0])) {
                return true;
            } else if (Integer.parseInt(liveArray[0]) == Integer
                    .parseInt(currentArray[0])) {
                // Checking for second index
                if (Integer.parseInt(liveArray[1]) > Integer
                        .parseInt(currentArray[1])) {
                    return true;
                } else if (Integer.parseInt(liveArray[1]) == Integer.parseInt(currentArray[1])) {
                    // Checking for third Index
                    if (Integer.parseInt(liveArray[2]) >= Integer
                            .parseInt(currentArray[2])) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * <p>Checks if internet conneciton is available.</p>
     *
     * @param context
     * @return TRUE is available else FALSE
     */
    public static boolean isConnectionAvailable(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return
     * @deprecated
     */
    public static String getURL() {
        return "";
    }
}
