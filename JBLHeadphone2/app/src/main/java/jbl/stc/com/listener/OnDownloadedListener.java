package jbl.stc.com.listener;

import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;

import jbl.stc.com.entity.FirmwareModel;

public interface OnDownloadedListener {

    void onDownloadedFirmware(CopyOnWriteArrayList<FirmwareModel> fwlist) throws FileNotFoundException;

    void onFailedDownload();

    void onFailedToCheckUpdate();

    void onUpgradeUpdate(String liveVersion, String title);
}
