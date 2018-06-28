package jbl.stc.com.activity;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.avnera.audiomanager.audioManager;
import com.avnera.smartdigitalheadset.Bluetooth;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Logger;
import com.avnera.smartdigitalheadset.USB;

import java.util.HashMap;
import java.util.Iterator;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.dialog.AlertsDialog;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.listener.AppUSBDelegate;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.StatusBarUtil;

/**
 * BaseActivity
 * Created by darren.lu on 08/06/2017.
 */
public class SplashActivity extends FragmentActivity  {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate");
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}