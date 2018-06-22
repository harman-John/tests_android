package jbl.stc.com.scan;

public interface BtScanner {

    void startScan(ScanListener listener);

    void stopScan();

    void close();
}
