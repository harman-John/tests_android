package jbl.stc.com.manager;

import android.content.Context;

import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

/**
 * Created by intahmad on 8/25/2015.
 * <b>CalibrationManager</b> is used to handle calibration related functions
 */
public class CalibrationManager {

    private static CalibrationManager calibrationManager;
    private final Context context;

    private CalibrationManager(Context context) {
        this.context = context;
    }

    private LightX lightX;

    public static CalibrationManager getCalibrationManager(Context context) {
        if (calibrationManager == null)
            calibrationManager = new CalibrationManager(context);
        return calibrationManager;
    }

    /**
     * Provide LightX Object
     *
     * @return Object
     */
    public LightX getLightX() {
        return lightX;
    }

    /**
     * LightX object is needed to perform library operation
     *
     * @param lightX
     */
    public void setLightX(LightX lightX) {
        this.lightX = lightX;
    }

    /**
     * @auther Tofeeq
     * start calibration
     */
    public void startCalibration() {
        if (lightX != null)
            lightX.writeAppWithUInt32Argument(Command.App_0xB2, 0);
    }

    /**
     * @auther Tofeeq
     * stop calibration
     */
    public void stopCalibration() {
        if (lightX != null)
            lightX.writeAppWithUInt32Argument(Command.App_0xB3, 0);
    }


    /**
     * @auther Tofeeq
     * stop calibration
     */
    public void getCalibrationStatus() {
        if (lightX != null)
            lightX.readApp(Command.App_0xB3);
    }

}
