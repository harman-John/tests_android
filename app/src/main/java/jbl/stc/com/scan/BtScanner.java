package jbl.stc.com.scan;

import com.harman.akg.headphone.interfaces.ScanListener;

public interface BtScanner {

    void startScan(ScanListener listener);

    void stopScan();

    void close();
}
