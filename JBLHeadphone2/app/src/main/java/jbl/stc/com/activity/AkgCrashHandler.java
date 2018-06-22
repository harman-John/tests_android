package jbl.stc.com.activity;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import jbl.stc.com.utils.LogUtil;
import jbl.stc.com.utils.ToastUtil;


/**
 * AkgCrashHandler
 * Created by darren.lu on 2015/8/3.
 */
public class AkgCrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = AkgCrashHandler.class.getSimpleName();
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handlerException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(1000);//运行多2秒，保证文件可以上传成功
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(1);
        }
    }

    private boolean handlerException(Throwable ex) {
        if (ex == null)
            return false;

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtil.ToastLong(mContext, "Sorry，APP Handler Exception");
                Looper.loop();
            }
        }.start();

        saveCrashInfo2File(ex);
        return true;
    }


    private void saveCrashInfo2File(Throwable thr) {
        //thr.printStackTrace();
        String lineSeparator = System.getProperty("line.separator", "\r\n");
        StringBuffer stringBuffer = new StringBuffer("");
        stringBuffer.append("====== Handler Exception? ======" + lineSeparator);
        stringBuffer.append(lineSeparator);
        stringBuffer.append("message:");
        stringBuffer.append(thr.getMessage());
        stringBuffer.append(lineSeparator);
        stringBuffer.append(Log.getStackTraceString(thr));
        stringBuffer.append(lineSeparator);

        LogUtil.e(TAG, stringBuffer.toString());
    }
}
