package jbl.stc.com.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.Calibration;


/**
 * Created by intahmad on 7/6/2015.
 */
public class GlobalCalibration extends Fragment implements View.OnClickListener {

    public static final String TAG = GlobalCalibration.class.getSimpleName();
    TextView txtConnectMessage, txthelp, txtChangeMessage, txtCalibrating, txtExtraHelp;
    ProgressBar progressBar;
    int timing = 10 * 1000;
    private View informationLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.calibrationglobal, container, false);
        txtConnectMessage = (TextView) view.findViewById(R.id.txtConnectMessage);
        //txtConnectMessage.setText(Html.fromHtml(getResources().getString(R.string.personalizemessgae)));
        txtConnectMessage.setText(getResources().getString(R.string.personalizemessgae));

        txthelp = (TextView) view.findViewById(R.id.txthelp);
        txthelp.setText(getResources().getString(R.string.help));
        txtChangeMessage = (TextView) view.findViewById(R.id.txtChangeMessage);
        txtCalibrating = (TextView) view.findViewById(R.id.txtCalibrating);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        txtExtraHelp = (TextView) view.findViewById(R.id.txtExtraHelp);
        informationLayout = view.findViewById(R.id.informationLayout);
        informationLayout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.informationLayout)
            startCalibration();
        informationLayout.setOnClickListener(null);
    }


    String getStrings(int n) {
        return getActivity().getString(n);
    }

    public void startCalibration() {
        txtCalibrating.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txtChangeMessage.setText("");
        try {
            ((Calibration) getActivity()).startCalibration();
        } catch (Exception e) {
            e.printStackTrace();
            Calibration.setCalibration((Calibration) getActivity());
            if (Calibration.getCalibration() != null) {
                Calibration.getCalibration().startCalibration();
            }
        }
    }

    /**
     * Calibration start screen will be visible.
     */
    public void calibrationComplete() {
        txtConnectMessage.setVisibility(View.INVISIBLE);
        txtChangeMessage.setBackgroundResource(R.drawable.ic_donetick);
        progressBar.setVisibility(View.GONE);
        txthelp.setVisibility(View.VISIBLE);
        txtExtraHelp.setVisibility(View.INVISIBLE);
        txtCalibrating.setVisibility(View.GONE);

        txthelp.setText(Html.fromHtml(getStrings(R.string.autoComplete)));

        txthelp.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        }, 2000);
    }

    /**
     * Calibration failed screen will be visible.
     */
    public void calibrationFailed() {
        txtCalibrating.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        txtChangeMessage.setText(getStrings(R.string.start));
        txtCalibrating.setText(getStrings(R.string.failcalibration));
        txtCalibrating.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_alert), null, null, null);
        txtCalibrating.setCompoundDrawablePadding(5);
        informationLayout.setOnClickListener(this);
    }


}
