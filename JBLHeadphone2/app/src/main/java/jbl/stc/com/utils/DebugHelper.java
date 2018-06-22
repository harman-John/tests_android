package jbl.stc.com.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;

/**
 * DebugHelper
 * <p>
 * Created by darren.lu on 8/15/2017.
 */
public class DebugHelper {
    private static String DEBUG_FILE_DIR = AppUtils.APP_PATH;
    private static String TODAY_FILE_PATH;
    private static String lineSeparator = System.getProperty("line.separator", "\r\n");
    private static boolean isCanWrite = false;

    public static void init() {
        LogUtil.d("DebugHelper", "create log file");
        FileUtils.deleteDirectory(Environment.getExternalStorageDirectory().getAbsolutePath() + DEBUG_FILE_DIR);
        String filename = DateUtils.getYearMonthDayTime(System.currentTimeMillis()) + ".txt";
        File file = new File(DEBUG_FILE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        File todayFile = new File(DEBUG_FILE_DIR, filename);
        try {
            if (todayFile.exists()) {
                todayFile.delete();
            }
            todayFile.createNewFile();
            TODAY_FILE_PATH = todayFile.getPath();
            isCanWrite = true;
            writeDebugContent(lineSeparator);
            writeDebugContent("===========================init DebugHelper==================================");
            LogUtil.d("DebugHelper", "create log file, today file path is " + TODAY_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            isCanWrite = false;
        }
    }

    public static void setIsCanWrite(boolean canWrite) {
        isCanWrite = canWrite;
    }

    public static void writeDebugContent(String content) {
        if (!isCanWrite) {
            return;
        }
        if (TextUtils.isEmpty(content)) {
            return;
        }
        try {
            FileWriter writer = new FileWriter(TODAY_FILE_PATH, true);
            String time = DateUtils.formatDateTime2(System.currentTimeMillis());
            writer.write(time + "：");
            writer.write(content);
            writer.write(lineSeparator);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            isCanWrite = false;
        }
    }

}
