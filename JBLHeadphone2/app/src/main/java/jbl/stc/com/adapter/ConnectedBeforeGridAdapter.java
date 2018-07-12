package jbl.stc.com.adapter;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.Bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.ConnectedBeforeDevice;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.fragment.UnableConnectFragment;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;

public class ConnectedBeforeGridAdapter extends BaseAdapter {
    private static final String TAG = ConnectedBeforeGridAdapter.class.getSimpleName();
    private List<ConnectedBeforeDevice> mLists = new ArrayList<>();
    private Context mContext;
    public void setConnectedBeforeList(List<ConnectedBeforeDevice> lists){
        this.mLists.clear();
        this.mLists.addAll(lists);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public ConnectedBeforeDevice getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        mContext = parent.getContext();
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_connected_before_device, null);
            viewHolder = new ViewHolder();
            viewHolder.relativeLayoutBreathingIcon = convertView.findViewById(R.id.relative_layout_item_connected_before_breathing_icon);
            viewHolder.textViewDeviceName = convertView.findViewById(R.id.text_view_item_connected_before_device_name);
            viewHolder.imageViewIcon = convertView.findViewById(R.id.image_view_item_connected_before_device_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mLists.get(position).a2dpConnected){
            viewHolder.relativeLayoutBreathingIcon.setAlpha(1);
        }else{
            viewHolder.relativeLayoutBreathingIcon.setAlpha((float) 0.3);
        }
        viewHolder.textViewDeviceName.setText(mLists.get(position).deviceName);
        viewHolder.imageViewIcon.setImageDrawable(mLists.get(position).deviceIcon);
        viewHolder.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i(TAG,"v = "+ v+",position = "+position);
                startTimer(viewHolder);
                Message msg = new Message();
                msg.what = MSG_START_BREATHING;
                msg.arg1 = position;
                cbHandler.sendMessageDelayed(msg,2000);
            }
        });
        return convertView;
    }


    private class ViewHolder {
        private TextView textViewDeviceName;
        private ImageView imageViewIcon;
        private RelativeLayout relativeLayoutBreathingIcon;
    }


    private CbaHandler cbHandler = new CbaHandler(Looper.getMainLooper());
    private final static int MSG_BREATHING_FADE_IN = 1;
    private final static int MSG_BREATHING_FADE_OUT = 2;
    private final static int MSG_BREATHING_STOP_AT_FADE_OUT = 3;
    private final static int MSG_BREATHING_STOP_AT_FADE_IN = 4;
    private final static int MSG_START_BREATHING = 5;
    private class CbaHandler extends Handler{
        public CbaHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BREATHING_FADE_IN:
                    ((ViewHolder)msg.obj).relativeLayoutBreathingIcon.clearAnimation();
                    ((ViewHolder)msg.obj).relativeLayoutBreathingIcon.setAnimation(getFadeIn());
                    break;
                case MSG_BREATHING_FADE_OUT:
                    ((ViewHolder)msg.obj).relativeLayoutBreathingIcon.clearAnimation();
                    ((ViewHolder)msg.obj).relativeLayoutBreathingIcon.setAnimation(getFadeOut());
                    break;
                case MSG_BREATHING_STOP_AT_FADE_OUT:
                    if (index == 2) {
                        isOpen = false;
                        if (timer != null) {
                            timer.cancel();
                        }
                        timer = null;
                    }else {
                        cbHandler.sendEmptyMessage(3);
                    }
                    break;
                case MSG_BREATHING_STOP_AT_FADE_IN:{
                    if (index == 1) {
                        isOpen = false;
                        if (timer != null) {
                            timer.cancel();
                        }
                        timer = null;
                    }else {
                        cbHandler.sendEmptyMessage(4);
                    }
                    break;
                }
                case MSG_START_BREATHING:{
                    stopTimerDisconnected();
                    ConnectedBeforeDevice connectedBeforeDevice = mLists.get(msg.arg1);
                    if (connectedBeforeDevice.a2dpConnected){
                        DashboardActivity.getDashboardActivity().goConnectedFragment();
                    }else {
                        Bundle bundle = new Bundle();
                        bundle.putString(JBLConstant.DEVICE_MODEL_NAME, connectedBeforeDevice.deviceName);
                        UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
                        unableConnectFragment.setArguments(bundle);
                        DashboardActivity.getDashboardActivity().switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

    private final int BREATH_INTERVAL_TIME = 1000;
    private Animation getFadeIn() {
        Animation fadeIn = AnimationUtils.loadAnimation(mContext,
                R.anim.breathing_lamp_fade_in);
        fadeIn.setDuration(BREATH_INTERVAL_TIME);
        fadeIn.setStartOffset(100);
        return fadeIn;
    }

    private Animation getFadeOut() {
        Animation fadeOut = AnimationUtils.loadAnimation(mContext,
                R.anim.breathing_lamp_fade_out);
        fadeOut.setDuration(BREATH_INTERVAL_TIME);
        fadeOut.setStartOffset(100);
        return fadeOut;
    }

    private Timer timer;
    private boolean isOpen = true;
    private int index = 0;
    private void startTimer(final ViewHolder viewHolder) {
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isOpen) {
                    if (index == 2) {
                        index = 0;
                    }
                    index++;
                    Message msg = new Message();
                    msg.what = index;
                    msg.obj = viewHolder;
                    cbHandler.sendMessage(msg);
                }
            }
        };
        timer.schedule(task, 0, BREATH_INTERVAL_TIME);
    }

    private void stopTimerDisconnected(){
        cbHandler.sendEmptyMessage(MSG_BREATHING_STOP_AT_FADE_OUT);
    }

    public void stopTimerConnected(){
        cbHandler.removeMessages(MSG_START_BREATHING);
        cbHandler.sendEmptyMessage(MSG_BREATHING_STOP_AT_FADE_IN);
    }
}
