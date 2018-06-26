package jbl.stc.com.listener;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * AppLightXDelegate
 * Created by intahmad on 8/3/2015.
 */
public interface DeviceListener {
    void deviceConnected(Object value);
    void deviceDisConnected(Object value);

    void receivedResponse(String command, ArrayList<responseResult> values,Status status);
    void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status);

    void lightXAppReadResult(LightX lightX, Command command, boolean success, byte[] buffer);
    void lightXAppReceivedPush(LightX lightX, Command command, byte[] data);
    void lightXAppWriteResult(final LightX lightX, final Command command, final boolean success);
}
