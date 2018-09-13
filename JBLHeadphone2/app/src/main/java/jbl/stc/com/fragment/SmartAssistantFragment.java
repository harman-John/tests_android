package jbl.stc.com.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.utils.EnumCommands;

public class SmartAssistantFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = SmartAssistantFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_assistant,
                container, false);
        TextView textView = view.findViewById(R.id.text_view_go_to_google_assistant);
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        textView.setOnClickListener(this);
        view.findViewById(R.id.image_view_back).setOnClickListener(this);
        Switch switchGA = view.findViewById(R.id.switch_google_assistant);
        switchGA.setVisibility(View.GONE);
        switchGA.setOnClickListener(this);
        view.findViewById(R.id.text_view_not_set_up).setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance().setScreenName(AnalyticsManager.SCREEN_PROGRAMMABLE_SMART_BUTTON);
        ANCControlManager.getANCManager(getActivity()).getSmartButton();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_back:
                getActivity().onBackPressed();
                break;
            case R.id.text_view_go_to_google_assistant:
                break;
            case R.id.switch_google_assistant:
                break;
        }
    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);

    }
}