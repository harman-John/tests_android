package jbl.stc.com.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.avnera.audiomanager.Status;
import com.avnera.audiomanager.responseResult;
import com.avnera.smartdigitalheadset.Command;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Utility;

import java.util.ArrayList;

import jbl.stc.com.R;
import jbl.stc.com.constant.AmCmds;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;

public class SmartButtonFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = SmartButtonFragment.class.getSimpleName();
    private RelativeLayout ambient, noise;
    private LightX lightX;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_smart_button,
                container, false);
        ambient = (RelativeLayout) view.findViewById(R.id.ambientLayout);
        noise = (RelativeLayout) view.findViewById(R.id.noiceCancelling);
        ambient.setOnClickListener(this);
        noise.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_PROGRAMMABLE_SMART_BUTTON);
        ANCControlManager.getANCManager(getActivity()).getSmartButton(lightX);
//        getActivity().setToolbarMenu(JBLConstant.SettingsSmartButton_FRAGMENT, null);
    }

    @Override
    public void lightXAppWriteResult(LightX var1, Command command, boolean success) {
        super.lightXAppWriteResult(var1, command, success);
    }

    @Override
    public void lightXAppReadResult(LightX var1, Command command, boolean success, byte[] buffer) {
        super.lightXAppReadResult(var1, command, success, buffer);
        if (success) {
            switch (command) {
                case AppSmartButtonFeatureIndex:
                    boolean boolValue = Utility.getBoolean(buffer, 0);
                    if (boolValue) {
//                        noiseImage.setVisibility(View.VISIBLE);
//                        ambientImage.setVisibility(View.INVISIBLE);
                    } else {
//                        noiseImage.setVisibility(View.INVISIBLE);
//                        ambientImage.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean lightXFirmwareReadStatus(LightX var1, LightX.FirmwareRegion var2, int var3, byte[] var4) {
        return super.lightXFirmwareReadStatus(var1, var2, var3, var4);

    }

    @Override
    public void isLightXintialize() {
        super.isLightXintialize();
    }

    @Override
    public void lightXIsInBootloader(LightX var1, boolean var2) {
        super.lightXIsInBootloader(var1, var2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ambientLayout:
//                noiseImage.setVisibility(View.INVISIBLE);
//                ambientImage.setVisibility(View.VISIBLE);
                writeProgrammableIndexButton(false);
                AnalyticsManager.getInstance(getActivity()).reportSmartButtonChange(getString(R.string.ambientAware));
                break;
            case R.id.noiceCancelling:
                writeProgrammableIndexButton(true);
//                noiseImage.setVisibility(View.VISIBLE);
//                ambientImage.setVisibility(View.INVISIBLE);
                AnalyticsManager.getInstance(getActivity()).reportSmartButtonChange(getString(R.string.noise_cancelling));
                break;
        }
    }

    /**
     * <p>Reads programmable smart button behaviour from headphone.</p>
     */
    public void readProgrammableIndexButton() {
        if (lightX != null) {
            lightX.readAppSmartButtonFeatureIndex();
        }
    }

    /**
     * Writes programmable smart button behaviour to headphone.
     *
     * @param noise boolean false for Ambient Awareness and true for ANC behaviour.
     */
    public void writeProgrammableIndexButton(boolean noise) {
        if (lightX != null) {
            lightX.writeAppSmartButtonFeatureIndex(noise);
        } else {
            ANCControlManager.getANCManager(getActivity()).setSmartButton(lightX, noise);
        }
    }

    @Override
    public void receivedResponse(String command, ArrayList<responseResult> values, Status status) {
        Logger.d(TAG, "receivedResponse command =" + command + ",values=" + values + ",status=" + status);
        switch (command) {
            case AmCmds.CMD_SmartButton: {
                boolean boolValue = false;
                if (values != null && values.size() > 0) {
                    boolValue = values.iterator().next().getValue().toString().equals("1");
                }
                if (boolValue) {
//                    noiseImage.setVisibility(View.VISIBLE);
//                    ambientImage.setVisibility(View.INVISIBLE);
                } else {
//                    noiseImage.setVisibility(View.INVISIBLE);
//                    ambientImage.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }
}