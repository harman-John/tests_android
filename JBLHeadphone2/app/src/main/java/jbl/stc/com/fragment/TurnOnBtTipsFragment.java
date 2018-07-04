package jbl.stc.com.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import jbl.stc.com.R;
import jbl.stc.com.legal.LegalApi;

public class TurnOnBtTipsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = TurnOnBtTipsFragment.class.getSimpleName();
    private View view;
    private RelativeLayout relativeLayoutTurnOnBt;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_turn_on_bt,
                container, false);
        relativeLayoutTurnOnBt = view.findViewById(R.id.relative_layout_splash_turn_on_bt);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.relative_layout_splash_turn_on_bt:{
                break;
            }
        }

    }
}
