package jbl.stc.com.ota;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.data.FwTYPE;
import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.fragment.HomeFragment;
import jbl.stc.com.fragment.OTAFragment;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.utils.FirmwareUtil;

public class CheckUpdateAvailable extends AsyncTask<String, Void, CopyOnWriteArrayList<FirmwareModel>> {
    private static final String TAG = CheckUpdateAvailable.class.getSimpleName();
    private Context context;
    public static String DIR = "bin";
    private String currentRSRCVersion, currentAppVersion;
    private Object object;
    private OnDownloadedListener downloaded;

    public CheckUpdateAvailable(Context context, OnDownloadedListener downloaded) {
        this.context = context;
        this.downloaded = downloaded;
    }

    public static CheckUpdateAvailable start(Object fragmentObject, Context context, OnDownloadedListener downloaded, String mURL, String resourceVersion, String appVersion) {
        CheckUpdateAvailable checkUpdateAvailable = new CheckUpdateAvailable(context, downloaded);
        checkUpdateAvailable.currentRSRCVersion = resourceVersion;
        checkUpdateAvailable.currentAppVersion = appVersion;
        checkUpdateAvailable.object = fragmentObject;
        checkUpdateAvailable.executeOnExecutor(CheckUpdateAvailable.THREAD_POOL_EXECUTOR, mURL);
        return checkUpdateAvailable;
    }

    public boolean isRunnuning() {
        return getStatus() == Status.RUNNING;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected CopyOnWriteArrayList<FirmwareModel> doInBackground(String... params) {
        CopyOnWriteArrayList<FirmwareModel> mFirmwareList = new CopyOnWriteArrayList<>();
        File file = new File(context.getCacheDir(), CheckUpdateAvailable.DIR);
        if (!file.exists())
            file.mkdirs();
        try {
            URL url = new URL(params[0]);
            Logger.d(TAG, "URL:" + params[0]);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            try {
                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser myParser = xmlFactoryObject.newPullParser();
                myParser.setInput(inputStream, null);
                int event = myParser.getEventType();
                FirmwareModel firmwareModel = new FirmwareModel();
                StringBuilder builder = new StringBuilder();
                while (event != XmlPullParser.END_DOCUMENT) {
                    String name = myParser.getName();
                    switch (event) {
                        case XmlPullParser.START_TAG:
                            if (name.equalsIgnoreCase("Release")) {
                                firmwareModel = new FirmwareModel();
                                firmwareModel.setName(myParser.getAttributeValue(0));
                                firmwareModel.setVersion(myParser.getAttributeValue(1));
                            }
                            builder.setLength(0);
                            break;
                        case XmlPullParser.TEXT:
                            builder.append(myParser.getText().trim());
                            break;
                        case XmlPullParser.END_TAG:
                            if (name.equals("Release")) {
                                firmwareModel.setmURL(builder.toString());
                                if (!firmwareModel.getName().equalsIgnoreCase("boot")) {
                                    mFirmwareList.add(firmwareModel);
                                }
                            }
                            break;
                    }
                    event = myParser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mFirmwareList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mFirmwareList;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        if (downloaded != null) {
            downloaded.onUpgradeUpdate("", "");
        }
    }

    @Override
    protected void onPostExecute(CopyOnWriteArrayList<FirmwareModel> fwlist) {
        super.onPostExecute(fwlist);
        if (downloaded != null && fwlist != null && fwlist.size() != 0) {
            if (object instanceof OTAFragment) {
                ((OTAFragment) object).setIsUpdateAvailable(isUpdateAvailable(fwlist), fwlist);
            } else if (object instanceof DashboardActivity) {
                ((DashboardActivity) object).setIsUpdateAvailable(isUpdateAvailable(fwlist));
            } else {
                downloaded.onFailedToCheckUpdate();
            }
        }
    }

    private boolean isUpdateAvailable(CopyOnWriteArrayList<FirmwareModel> fwlist) {
        boolean isUpdateAvailable = false;
        String appVersion = "0.0.0", rsrcVersion = "0.0.0";
        for (FirmwareModel model : fwlist) {
            /**
             * reorder here so in case of any changed at server
             */
            switch (model.getFwtype()) {
                case APP:
                    appVersion = model.getVersion();
                    break;
                case RSRC:
                    rsrcVersion = model.getVersion();
                    break;
                case BOOT:
                    break;
                case PARAM:
                    break;
                case FIRMWARE:
                    appVersion = model.getVersion();
                    break;
                case DATA:
                    break;
            }

        }
        Logger.d(TAG, "appVersion=" + appVersion + ",currentAppVersion=" + currentAppVersion);
        if (FirmwareUtil.isUpdateAvailable(appVersion, currentAppVersion)) {
            isUpdateAvailable = true;
            Logger.d(TAG, "App will update");
            AnalyticsManager.getInstance(context).reportFirmwareUpdateAvailable(appVersion);
        } else {
            FirmwareModel modelTemp = null;
            for (FirmwareModel model : fwlist) {
                switch (model.getFwtype()) {
                    case APP:
                        modelTemp = model;
                        break;
                }
            }
            if (modelTemp != null)
                fwlist.remove(modelTemp);
            Logger.d(TAG, "App will not update");
        }

        Logger.d(TAG, "rsrcVersion=" + rsrcVersion + ",currentRSRCVersion=" + currentRSRCVersion);
        if (FirmwareUtil.isUpdateAvailable(rsrcVersion, currentRSRCVersion)) {
            currentRSRCVersion = rsrcVersion;
            isUpdateAvailable = true;
            AnalyticsManager.getInstance(context).reportFirmwareUpdateAvailable(currentRSRCVersion);
            Logger.d(TAG, "rsrc update on base of last saved");
        } else {
            AnalyticsManager.getInstance(context).reportFirmwareUpToDate(currentRSRCVersion);
            Logger.d(TAG, "rsrc will not update");
            FirmwareModel modelTemp = null;
            for (FirmwareModel model : fwlist) {
                if (model.getFwtype() == FwTYPE.RSRC) {
                    modelTemp = model;
                }
            }
            if (modelTemp != null) {
                fwlist.remove(modelTemp);
            }
        }
        return isUpdateAvailable;
    }
}
