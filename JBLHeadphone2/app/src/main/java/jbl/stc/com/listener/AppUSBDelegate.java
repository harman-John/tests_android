package jbl.stc.com.listener;

import android.hardware.usb.UsbDevice;

/**
 * AppUSBDelegate
 * Created by intahmad on 1/6/2016.
 */
public interface AppUSBDelegate {

    void usbAttached(UsbDevice usbDevice);

    void usbDetached(UsbDevice usbDevice);
}
