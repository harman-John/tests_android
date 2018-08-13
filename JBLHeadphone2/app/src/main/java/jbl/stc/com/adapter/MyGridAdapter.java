package jbl.stc.com.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.MyDragGridView;

public class MyGridAdapter extends BaseAdapter implements MyDragGridView.DragGridBaseAdapter {
    private static final String TAG = MyGridAdapter.class.getSimpleName();
    public List<MyDevice> mLists = new ArrayList<>();
    private Context mContext;
    public int mHidePosition = -1;
    private OnDeviceItemSelectedListener onDeviceItemSelectedListener;

    public void setMyAdapterList(List<MyDevice> lists) {
        Collections.sort(lists, new Comparator<MyDevice>() {

            @Override
            public int compare(MyDevice o1, MyDevice o2) {
                if (o1.connectStatus > o2.connectStatus) {
                    return -1;
                } else if (o1.connectStatus < o2.connectStatus) {
                    return 1;
                } else {
                    return 0;
                }
            }

        });
        this.mLists.clear();
        this.mLists.addAll(lists);
        notifyDataSetChanged();
    }

    public void removeAllMessage() {
        cbHandler.removeMessages(MSG_SHOW_FRAGMENT);
    }

    public void setOnDeviceSelectedListener(OnDeviceItemSelectedListener onItemSelectedListener) {
        this.onDeviceItemSelectedListener = onItemSelectedListener;
    }

    public interface OnDeviceItemSelectedListener{
        void onSelected(int position);
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
            viewHolder.linear_layout_item_connected_before_device = convertView.findViewById(R.id.linear_layout_item_connected_before_device);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        int marginTop = UiUtils.getDeviceNameMarginTop(mContext);
        int height = UiUtils.getDashboardDeviceImageHeight(mContext);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.relativeLayoutBreathingIcon.getLayoutParams();
        if (params != null) {
            params.height = height;
            params.width = height;
            viewHolder.relativeLayoutBreathingIcon.setLayoutParams(params);
        }

        LinearLayout.LayoutParams deviceNameParams = (LinearLayout.LayoutParams) viewHolder.textViewDeviceName.getLayoutParams();
        if (position == 0) {
            deviceNameParams.topMargin = marginTop;
            viewHolder.textViewDeviceName.setLayoutParams(deviceNameParams);
        }else{
            deviceNameParams.topMargin = UiUtils.dip2px(mContext,30);
            viewHolder.textViewDeviceName.setLayoutParams(deviceNameParams);
        }

        if (mLists.get(position).connectStatus == ConnectStatus.DEVICE_CONNECTED) {
            viewHolder.relativeLayoutBreathingIcon.getBackground().setAlpha(255);
            viewHolder.imageViewIcon.setImageAlpha(255);
        } else if (mLists.get(position).connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
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
                Logger.i(TAG, "v = " + v + ",position = " + position);
                Message msg = new Message();
                msg.what = MSG_SHOW_FRAGMENT;
                msg.arg1 = position;
                cbHandler.sendMessage(msg);
                /*if (onDeviceItemSelectedListener != null) {
                    onDeviceItemSelectedListener.onSelected(position);
                }*/
            }
        });
        if (position == mHidePosition) {
            viewHolder.relativeLayoutBreathingIcon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.relativeLayoutBreathingIcon.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        MyDevice temp = mLists.get(oldPosition);
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(mLists, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(mLists, i, i - 1);
            }
        }

        mLists.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    @Override
    public void deleteItem(int deletePosition) {
        if (null != mLists && deletePosition < mLists.size()) {
            MyDevice myDevice = mLists.get(deletePosition);
            if (myDevice.connectStatus == ConnectStatus.A2DP_UNCONNECTED) {
                String key = myDevice.deviceKey;
                mLists.remove(deletePosition);
                DashboardActivity.getDashboardActivity().removeDeviceList(key);
                notifyDataSetChanged();
            }
        }
    }

    private class ViewHolder {
        private TextView textViewDeviceName;
        private ImageView imageViewIcon;
        private RelativeLayout relativeLayoutBreathingIcon;
        private LinearLayout linear_layout_item_connected_before_device;
    }


    private CbaHandler cbHandler = new CbaHandler(Looper.getMainLooper());
    private final static int MSG_SHOW_FRAGMENT = 0;

    private class CbaHandler extends Handler {
        CbaHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_FRAGMENT: {
                    cbHandler.removeMessages(MSG_SHOW_FRAGMENT);
                    MyDevice myDevice = mLists.get(msg.arg1);
                    if (myDevice.connectStatus == ConnectStatus.DEVICE_CONNECTED
                            || myDevice.connectStatus == ConnectStatus.A2DP_HALF_CONNECTED) {
                        Logger.d(TAG, "Show home fragment");
                        DashboardActivity.getDashboardActivity().showHomeActivity(myDevice);
                    } else {
                        Fragment fr = DashboardActivity.getDashboardActivity().getSupportFragmentManager().findFragmentById(R.id.containerLayout);
                        if (fr instanceof UnableConnectFragment) {
                            Logger.d(TAG, "fr is already UnableConnectFragment");
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
