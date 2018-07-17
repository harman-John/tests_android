package jbl.stc.com.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import jbl.stc.com.R;


public class SaPopupWindow extends PopupWindow implements View.OnClickListener{

    public SaPopupWindow(Context context) {
        super(context);
        init(context);

    }

    private void init(Context context){
        View popupWindow_view = LayoutInflater.from(context).inflate(R.layout.popup_window_sa, null,
                false);
        setContentView(popupWindow_view);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setAnimationStyle(R.style.style_down_to_top);
        popupWindow_view.findViewById(R.id.iv_ambient_aware).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.iv_talk_through).setOnClickListener(this);
        popupWindow_view.findViewById(R.id.aa_popup_close_arrow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
         switch (v.getId()){
             case R.id.aa_popup_close_arrow:
                 if (isShowing()) {
                     dismiss();
                 }
             break;
         }

    }
}
