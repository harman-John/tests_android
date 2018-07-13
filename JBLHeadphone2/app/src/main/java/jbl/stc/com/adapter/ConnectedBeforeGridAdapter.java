package jbl.stc.com.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.ConnectedBeforeDevice;
import jbl.stc.com.fragment.UnableConnectFragment;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.BreathLight;

public class ConnectedBeforeGridAdapter extends BaseAdapter {
    private static final String TAG = ConnectedBeforeGridAdapter.class.getSimpleName();
    private List<ConnectedBeforeDevice> mLists = new ArrayList<>();
    private Context mContext;
    public void setConnectedBeforeList(List<ConnectedBeforeDevice> lists){
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
            viewHolder.relativeLayoutBreathingIcon.setAlpha((float) 0.4);
        }
        viewHolder.textViewDeviceName.setText(mLists.get(position).deviceName);
        viewHolder.imageViewIcon.setImageDrawable(mLists.get(position).deviceIcon);
        viewHolder.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i(TAG,"v = "+ v+",position = "+position);

                if (!breathLightMap.containsKey(position)){
                    BreathLight breathLight = new BreathLight(mContext,
                            viewHolder.relativeLayoutBreathingIcon,
                            R.anim.breathing_lamp_fade_in,
                            R.anim.breathing_lamp_fade_out);
                    breathLight.startBreathing(position);
                    breathLightMap.put(position,breathLight);
                }


                for (Integer key: breathLightMap.keySet()){
                    if (key == position){
                        breathLightMap.get(key).startBreathing(position);
                        break;
                    }
                }

                Message msg = new Message();
                msg.what = MSG_SHOW_FRAGMENT;
                msg.arg1 = position;
                cbHandler.sendMessageDelayed(msg,2000);
            }
        });
        return convertView;
    }

    private Map<Integer,BreathLight> breathLightMap = new HashMap<>();
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
                    for (Integer key: breathLightMap.keySet()){
                        breathLightMap.get(key).stopBreathing();
                    }
                    ConnectedBeforeDevice connectedBeforeDevice = mLists.get(msg.arg1);
                    if (connectedBeforeDevice.a2dpConnected){
                        DashboardActivity.getDashboardActivity().goHomeFragment();
                    }else {
                        Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if (fr == null){
                            Logger.d(TAG,"fr is null");
                            return;
                        }
                        if ( fr instanceof  UnableConnectFragment){
                            Logger.d(TAG,"fr is already UnableConnectFragment");
                            return;
                        }
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
    }
}
