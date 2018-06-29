package jbl.stc.com.ota;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.entity.FirmwareModel;
import jbl.stc.com.listener.OnDownloadedListener;
import jbl.stc.com.manager.AnalyticsManager;


public class DownloadProgrammingFile extends AsyncTask<String, Void, CopyOnWriteArrayList<FirmwareModel>> {
    private Context context;
    private static String DIR = "bin";
    private CopyOnWriteArrayList<FirmwareModel> firmwareModelArrayList;
    //ArrayList<FirmwareModel> firmwareModelArrayList;

    public DownloadProgrammingFile(Context context, OnDownloadedListener downloaded, CopyOnWriteArrayList<FirmwareModel> firmwareModelArrayList) {
        this.context = context;
        this.downloaded = downloaded;
        this.firmwareModelArrayList = firmwareModelArrayList;
    }

    //    ProgressDialog progressDialog;
    private OnDownloadedListener downloaded;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private String title;
    private String liveVersion = "";

    @Override
    protected CopyOnWriteArrayList<FirmwareModel> doInBackground(String... params) {
        File file = new File(context.getCacheDir(), DownloadProgrammingFile.DIR);
        if (!file.exists())
            file.mkdirs();

        if (firmwareModelArrayList.size() != 0) {
            liveVersion = firmwareModelArrayList.get(0).getVersion();
            for (FirmwareModel tempModel : firmwareModelArrayList) {
                publishProgress();
                title = tempModel.getName() + " downloading...";
                startDownload(file, tempModel);
            }
        }

        return firmwareModelArrayList;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        downloaded.onUpgradeUpdate(liveVersion, title);
    }

    private void startDownload(File file, FirmwareModel firmwareModel) {
        InputStream inputStream = null;
        AnalyticsManager.getInstance(context).reportFirmwareUpdateStarted(firmwareModel.getVersion());
        try {
            URL url = new URL(firmwareModel.getmURL());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            File tempFile = new File(file, getFileName(firmwareModel));
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            firmwareModel.setFile(tempFile);
            byte[] bytes = new byte[1024];
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            firmwareModel.setSuccess(true);
            AnalyticsManager.getInstance(context).reportFirmwareUpdateComplete(firmwareModel.getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            firmwareModel.setSuccess(false);
            AnalyticsManager.getInstance(context).reportFirmwareUpdateFailed(null);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFileName(FirmwareModel name) {
        String suffix = ".bin";
        switch (name.getFwtype()) {
            case APP:
                return "app" + suffix;
            case RSRC:
                return "rsrc" + suffix;
            case BOOT:
                return "boot" + suffix;
            case PARAM:
                return "param" +suffix;
            case FIRMWARE:
                return "firmware" +suffix;
            case DATA:
                return "data" +suffix;
        }
        return null;
    }

    @Override
    protected void onPostExecute(CopyOnWriteArrayList<FirmwareModel> bytes) {
        super.onPostExecute(bytes);
        try {
            if (downloaded != null) {
                if (bytes != null && bytes.size() != 0) {
                    downloaded.onDownloadedFirmware(bytes);
                } else {
                    downloaded.onFailedDownload();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
