package jbl.stc.com.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jbl.stc.com.R;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.utils.EnumCommands;

public class SmartButtonFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = SmartButtonFragment.class.getSimpleName();
    private ImageView iv_check_ambient, ivCheckNoiseCancelling;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_button,
                container, false);
        RelativeLayout ambient = view.findViewById(R.id.relative_layout_smart_button_ambient_aware);
        RelativeLayout noise = view.findViewById(R.id.relative_layout_smart_button_noise_cancelling);
        iv_check_ambient = view.findViewById(R.id.iv_check_ambient);
        ivCheckNoiseCancelling = view.findViewById(R.id.iv_check_noiseCancelling);
        view.findViewById(R.id.image_view_back).setOnClickListener(this);
        ambient.setOnClickListener(this);
        noise.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance().setScreenName(AnalyticsManager.SCREEN_PROGRAMMABLE_SMART_BUTTON);
        ANCControlManager.getANCManager(getActivity()).getSmartButton();
    }

//    @Override
//    public void lightXAppWriteResult(LightX var1, Command command, boolean success) {
//        super.lightXAppWriteResult(var1, command, success);
//    }
//
//    @Override
//    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
//        super.lightXAppReadResult(var1, command, success, buffer);
//        if (success) {
//            switch (command) {
//                case AppSmartButtonFeatureIndex:
//                    boolean boolValue = Utility.getBoolean(buffer, 0);
//                    if (boolValue) {
//                        ivCheckNoiseCancelling.setVisibility(View.VISIBLE);
//                        ivCheckNoiseCancelling.setImageResource(R.mipmap.button_selected);
//                        iv_check_ambient.setVisibility(View.GONE);
//                    } else {
//                        ivCheckNoiseCancelling.setVisibility(View.GONE);
//                        iv_check_ambient.setVisibility(View.VISIBLE);
//                        iv_check_ambient.setImageResource(R.mipmap.button_selected);
//                    }
//                    break;
//            }
//        }
//    }

//    @Override
//    public boolean lightXFirmwareReadStatus(LightX var1, LightX.FirmwareRegion var2, int var3, byte[] var4) {
//        return super.lightXFirmwareReadStatus(var1, var2, var3, var4);
//
//    }
//
//    @Override
//    public void isLightXInitialize() {
//        super.isLightXInitialize();
//    }
//
//    @Override
//    public void lightXIsInBootloader(LightX var1, boolean var2) {
//        super.lightXIsInBootloader(var1, var2);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_back:
                getActivity().onBackPressed();
                break;
            case R.id.relative_layout_smart_button_ambient_aware:
                ivCheckNoiseCancelling.setVisibility(View.GONE);
                iv_check_ambient.setVisibility(View.VISIBLE);
                iv_check_ambient.setImageResource(R.mipmap.button_selected);
                ANCControlManager.getANCManager(getActivity()).setSmartButton(false);
                AnalyticsManager.getInstance().reportSmartButtonChange(getString(R.string.ambientAware));
                break;
            case R.id.relative_layout_smart_button_noise_cancelling:
                ivCheckNoiseCancelling.setVisibility(View.VISIBLE);
                ivCheckNoiseCancelling.setImageResource(R.mipmap.button_selected);
                iv_check_ambient.setVisibility(View.GONE);
                ANCControlManager.getANCManager(getActivity()).setSmartButton(true);
                AnalyticsManager.getInstance().reportSmartButtonChange(getString(R.string.noise_cancelling));
                break;
        }
    }

//    @Override
//    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
//        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
//        if (values == null || values.size() == 0) {
//            return;
//        }
//        switch (command) {
//            case AmCmds.CMD_SmartButton: {
//                boolean boolValue = values.iterator().next().getValue().toString().equals("1");
//                if (boolValue) {
//                    ivCheckNoiseCancelling.setVisibility(View.VISIBLE);
//                    ivCheckNoiseCancelling.setImageResource(R.mipmap.button_selected);
//                    iv_check_ambient.setVisibility(View.GONE);
//                } else {
//                    ivCheckNoiseCancelling.setVisibility(View.GONE);
//                    iv_check_ambient.setVisibility(View.VISIBLE);
//                    iv_check_ambient.setImageResource(R.mipmap.button_selected);
//                }
//                break;
//            }
//        }
//    }

    @Override
    public void onReceive(EnumCommands enumCommands, Object... objects) {
        super.onReceive(enumCommands, objects);
        switch (enumCommands) {
            case CMD_SMART_BUTTON: {
                boolean smartType = (boolean) objects[0];
                if (smartType) {
                    ivCheckNoiseCancelling.setVisibility(View.VISIBLE);
                    ivCheckNoiseCancelling.setImageResource(R.mipmap.button_selected);
                    iv_check_ambient.setVisibility(View.GONE);
                } else {
                    ivCheckNoiseCancelling.setVisibility(View.GONE);
                    iv_check_ambient.setVisibility(View.VISIBLE);
                    iv_check_ambient.setImageResource(R.mipmap.button_selected);
                }
                break;
            }
        }
    }
}