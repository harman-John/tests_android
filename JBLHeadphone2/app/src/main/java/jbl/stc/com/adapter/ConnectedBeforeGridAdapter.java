package jbl.stc.com.adapter;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.Bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.entity.ConnectedBeforeDevice;
import jbl.stc.com.entity.EQModel;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        mContext = parent.getContext();
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_connected_before_device, null);
            viewHolder = new ViewHolder();
            viewHolder.textViewDeviceName = convertView.findViewById(R.id.text_view_item_connected_before_device_name);
            viewHolder.imageViewIcon = convertView.findViewById(R.id.image_view_item_connected_before_device_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewDeviceName.setText(mLists.get(position).deviceName);
        viewHolder.imageViewIcon.setImageDrawable(mLists.get(position).deviceIcon);
        viewHolder.imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }


    private class ViewHolder {
        private TextView textViewDeviceName;
        private ImageView imageViewIcon;
    }
}
