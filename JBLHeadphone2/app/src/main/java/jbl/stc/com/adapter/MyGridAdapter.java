package jbl.stc.com.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.fragment.UnableConnectFragment;
import jbl.stc.com.logger.Logger;

public class MyGridAdapter extends BaseAdapter {
    private static final String TAG = MyGridAdapter.class.getSimpleName();
    private List<MyDevice> mLists = new ArrayList<>();
    private Context mContext;
    public void setMyAdapterList(List<MyDevice> lists){
        Collections.sort(lists, new Comparator<MyDevice>() {

            @Override
            public int compare(MyDevice o1, MyDevice o2) {
                if ( o1.connectStatus > o2.connectStatus){
                    return -1;
                }else if (o1.connectStatus < o2.connectStatus){
                    return 1;
                }else{
                    return 0;
                }
            }

        });
        this.mLists.clear();
        this.mLists.addAll(lists);
        notifyDataSetChanged();
    }

    public void removeAllMessage(){
        cbHandler.removeMessages(MSG_SHOW_FRAGMENT);
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public MyDevice getItem(int position) {
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
        if (mLists.get(position).connectStatus == ConnectStatus.A2DP_CONNECTED){
            viewHolder.relativeLayoutBreathingIcon.getBackground().setAlpha(255);
            viewHolder.imageViewIcon.setImageAlpha(255);
        } else if (mLists.get(position).connectStatus == ConnectStatus.A2DP_HALF_CONNECTED){
            viewHolder.relativeLayoutBreathingIcon.getBackground().setAlpha(128);
            viewHolder.imageViewIcon.setImageAlpha(255);
        } else {
            viewHolder.relativeLayoutBreathingIcon.getBackground().setAlpha(128);
            viewHolder.imageViewIcon.setImageAlpha(128);
        }
        viewHolder.textViewDeviceName.setText(mLists.get(position).deviceName);

        viewHolder.imageViewIcon.setImageDrawable(ContextCompat.getDrawable(mContext, mLists.get(position).deviceIcon));
        viewHolder.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i(TAG,"v = "+ v+",position = "+position);
                Message msg = new Message();
                msg.what = MSG_SHOW_FRAGMENT;
                msg.arg1 = position;
                cbHandler.sendMessage(msg);
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
    private final static int MSG_SHOW_FRAGMENT = 0;
    private class CbaHandler extends Handler{
        CbaHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_FRAGMENT:{
                    cbHandler.removeMessages(MSG_SHOW_FRAGMENT);
//                    for (Integer key: breathLightMap.keySet()){
//                        breathLightMap.get(key).stopBreathing();
//                    }
                    MyDevice myDevice = mLists.get(msg.arg1);
                    //                            && DashboardActivity.getDashboardActivity().isConnected()
                    if (myDevice.connectStatus == ConnectStatus.A2DP_CONNECTED
                            || myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                        Logger.d(TAG, "Show home fragment");
                        DashboardActivity.getDashboardActivity().goHomeFragment(myDevice);
                    }else {
                        Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if ( fr instanceof  UnableConnectFragment){
                            Logger.d(TAG,"fr is already UnableConnectFragment");
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString(JBLConstant.DEVICE_MODEL_NAME, myDevice.deviceName);
                        UnableConnectFragment unableConnectFragment = new UnableConnectFragment();
                        unableConnectFragment.setArguments(bundle);
                        DashboardActivity.getDashboardActivity().switchFragment(unableConnectFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                    }
                    break;
                }
            }
            super.handleMessage(msg);
        }
    }
}
