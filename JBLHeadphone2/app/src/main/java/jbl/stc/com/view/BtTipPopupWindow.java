package jbl.stc.com.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import jbl.stc.com.R;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 7/25/18.
 */
public class BtTipPopupWindow extends PopupWindow implements View.OnClickListener {

    private Button buttonGoToBluetooth;
    private Context mContext;

    public BtTipPopupWindow(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.popwindow_bt_tip, null,
                false);
        setContentView(popupWindow_view);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setAnimationStyle(R.style.style_down_to_top);
        buttonGoToBluetooth = (Button) popupWindow_view.findViewById(R.id.button_go_to_settings);
        buttonGoToBluetooth.setOnClickListener(this);
        popupWindow_view.findViewById(R.id.aa_popup_close_arrow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_go_to_settings:
                dismiss();
                mContext.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            case R.id.aa_popup_close_arrow:
                dismiss();
                break;
        }

    }

}
