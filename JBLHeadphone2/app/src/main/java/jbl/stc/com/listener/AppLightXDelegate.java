package jbl.stc.com.listener;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;

/**
 * AppLightXDelegate
 * Created by intahmad on 8/3/2015.
 */
public interface AppLightXDelegate {
    void lightXAppReadResult(LightX var1, Command var2, boolean var3, byte[] var4);

    void lightXAppReceivedPush(LightX var1, Command var2, byte[] var3);

    void lightXAppWriteResult(LightX var1, Command var2, boolean var3);

    void lightXError(LightX var1, Exception var2);

    boolean lightXFirmwareReadStatus(LightX var1, LightX.FirmwareRegion var2, int var3, byte[] var4);

    boolean lightXFirmwareWriteStatus(LightX var1, LightX.FirmwareRegion var2, LightX.FirmwareWriteOperation var3, double var4, Exception var6);

    void lightXIsInBootloader(LightX var1, boolean var2);

    void lightXReadConfigResult(LightX var1, Command var2, boolean var3, String var4);

    boolean lightXWillRetransmit(LightX var1, Command var2);

    void isLightXInitialize();

    void headPhoneStatus(boolean isConnected);

    void lightXReadBootResult(LightX var1, Command command, boolean success, int var4, byte[] var5);

    void receivedAdminEvent(AdminEvent event, Object value);

    void receivedResponse(String command, ArrayList<responseResult> values, Status status);

    void receivedStatus(StatusEvent name, Object value);

    void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status);
}
