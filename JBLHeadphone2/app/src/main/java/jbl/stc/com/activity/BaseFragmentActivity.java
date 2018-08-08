package jbl.stc.com.activity;

import android.hardware.usb.UsbDevice;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.audiomanager.Action;
import com.avnera.audiomanager.AdminEvent;
import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.StatusEvent;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.listener.AppLightXDelegate;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.manager.DeviceManager;


public class BaseFragmentActivity extends FragmentActivity implements View.OnTouchListener, AppLightXDelegate, AppUSBDelegate {
    private final static String TAG = BaseFragmentActivity.class.getSimpleName();


    @Override
    protected void onResume() {
        super.onResume();
        DeviceManager.getInstance(this).setAppLightXDelegate(this);
    }

    public void switchFragment(BaseFragment baseFragment, int type) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            } else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT) {
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            } else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT) {
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            }
            if (getSupportFragmentManager().findFragmentById(R.id.containerLayout) == null) {
                ft.add(R.id.containerLayout, baseFragment);
            } else {
                ft.replace(R.id.containerLayout, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDeviceNameAndImage(String deviceName, ImageView imageViewDevice, TextView textViewDeviceName) {
        if (TextUtils.isEmpty(deviceName)) {
            return;
        }
        //update device name
        textViewDeviceName.setText(deviceName);
        //update device image
        if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_REFLECT_AWARE).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.reflect_aware_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_100).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_100_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_150NC).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_150nc_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_300).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_300_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_700).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_700_icon);
        } else if (deviceName.toUpperCase().contains((JBLConstant.DEVICE_EVEREST_ELITE_750NC).toUpperCase())) {
            imageViewDevice.setImageResource(R.mipmap.everest_elite_750nc_icon);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command var2, boolean var3, byte[] var4) {

    }

    @Override
    public void lightXAppReceivedPush(LightX var1, Command var2, byte[] var3) {

    }

    @Override
    public void lightXAppWriteResult(LightX var1, Command var2, boolean var3) {

    }

    @Override
    public void lightXError(LightX var1, Exception var2) {

    }

    @Override
    public boolean lightXFirmwareReadStatus(LightX var1, LightX.FirmwareRegion var2, int var3, byte[] var4) {
        return false;
    }

    @Override
    public boolean lightXFirmwareWriteStatus(LightX var1, LightX.FirmwareRegion var2, LightX.FirmwareWriteOperation var3, double var4, Exception var6) {
        return false;
    }

    @Override
    public void lightXIsInBootloader(LightX var1, boolean var2) {

    }

    @Override
    public void lightXReadConfigResult(LightX var1, Command var2, boolean var3, String var4) {

    }

    @Override
    public boolean lightXWillRetransmit(LightX var1, Command var2) {
        return false;
    }

    @Override
    public void isLightXInitialize() {

    }

    @Override
    public void headPhoneStatus(boolean isConnected) {

    }

    @Override
    public void lightXReadBootResult(LightX var1, Command command, boolean success, int var4, byte[] var5) {

    }

    @Override
    public void receivedAdminEvent(AdminEvent event, Object value) {

    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {

    }

    @Override
    public void receivedStatus(StatusEvent name, Object value) {

    }

    @Override
    public void receivedPushNotification(Action action, String command, ArrayList<responseResult> values, Status status) {

    }

    @Override
    public void usbAttached(UsbDevice usbDevice) {

    }

    @Override
    public void usbDetached(UsbDevice usbDevice) {

    }
}