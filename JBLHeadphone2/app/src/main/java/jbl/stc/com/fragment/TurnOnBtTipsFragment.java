package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.view.BtTipPopupWindow;

public class TurnOnBtTipsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = TurnOnBtTipsFragment.class.getSimpleName();
    private View view;
    private RelativeLayout relativeLayoutTurnOnBt;
    private TextView tv_stillsee;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_turn_on_bt,
                container, false);
        tv_stillsee = view.findViewById(R.id.tv_stillsee);
        tv_stillsee.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_stillsee:{

                BtTipPopupWindow btTipPopupWindow=new BtTipPopupWindow(getActivity());
                btTipPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
                break;
            }
        }

    }
}
