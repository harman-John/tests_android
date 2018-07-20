package jbl.stc.com.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.JBLApplication;


public class NotConnectedPopupWindow extends PopupWindow implements View.OnClickListener {

    private Button buttonGoToBluetooth;
    private TextView textViewKeepUsing;
    private Context mContext;

    public NotConnectedPopupWindow(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.popup_window_not_connected, null,
                false);
        setContentView(popupWindow_view);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setAnimationStyle(R.style.style_down_to_top);
        buttonGoToBluetooth = popupWindow_view.findViewById(R.id.button_not_connected_popup_go_to_bluetooth);
        textViewKeepUsing = popupWindow_view.findViewById(R.id.text_view_not_connected_popup_keep_using);

        buttonGoToBluetooth.setOnClickListener(this);
        textViewKeepUsing.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_not_connected_popup_go_to_bluetooth:
                mContext.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            case R.id.text_view_not_connected_popup_keep_using:
                dismiss();
                break;
        }

    }

}
