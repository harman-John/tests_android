package jbl.stc.com.listener;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;
import java.util.Set;

import jbl.stc.com.entity.MyDevice;

/**
 * AppLightXDelegate
 * Created by intahmad on 8/3/2015.
 */
public interface ConnectListener {
    void connectDeviceStatus(boolean isConnected);
    void checkDevices(Set<MyDevice> deviceList);
}
