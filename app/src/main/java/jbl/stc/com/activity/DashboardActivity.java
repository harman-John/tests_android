package jbl.stc.com.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.dialog.CreateMyOwnEqDialog;
import jbl.stc.com.fragment.ANCFragment;
import jbl.stc.com.fragment.BaseFragment;
import jbl.stc.com.fragment.EqCustomFragment;
import jbl.stc.com.fragment.EqSettingFragment;
import jbl.stc.com.listener.OnDialogListener;
import jbl.stc.com.utils.FastClickHelper;
import jbl.stc.com.utils.LogUtil;

public class DashboardActivity extends BaseActivity implements View.OnClickListener {

    private ImageView logoImageView,
            settingImageView, smartAmbientImage,
            deviceImageView, autoOffImage,
            eqSwitchImageView;
    private LinearLayout eqSwitchLayout;
    private FrameLayout eqInfoLayout;
    private TextView eqNameText, titleEqText,
            batteryLevelText, eqTextView,
            autoOffTextView;
    private View eqDividerView;
    private ProgressBar batteryProgressBar;
    private CreateMyOwnEqDialog createMyOwnEqDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initView();
    }

    private void initView() {
        logoImageView = findViewById(R.id.logoImageView);
        settingImageView = findViewById(R.id.settingImageView);
        smartAmbientImage = findViewById(R.id.smartAmbientImage);
        deviceImageView = findViewById(R.id.deviceImageView);
        autoOffImage = findViewById(R.id.autoOffImage);
        eqSwitchLayout = findViewById(R.id.eqSwitchLayout);
        eqInfoLayout = findViewById(R.id.eqInfoLayout);
        eqSwitchImageView = findViewById(R.id.eqSwitchImageView);
        eqNameText = findViewById(R.id.eqNameText);
        titleEqText = findViewById(R.id.titleEqText);
        eqDividerView = findViewById(R.id.eqDividerView);
        batteryProgressBar = findViewById(R.id.batteryProgressBar);
        batteryLevelText = findViewById(R.id.batteryLevelText);
        eqTextView = findViewById(R.id.eqTextView);
        autoOffTextView = findViewById(R.id.autoOffTextView);

        eqInfoLayout.setOnClickListener(this);

        createMyOwnEqDialog = new CreateMyOwnEqDialog(this);
        createMyOwnEqDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm() {
                switchFragment(new EqCustomFragment());
            }

            @Override
            public void onCancel() {

            }
        });

    }

    public void switchFragment(BaseFragment baseFragment) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            if (getSupportFragmentManager().findFragmentById(R.id.containerLayout) == null) {
                ft.add(R.id.containerLayout, baseFragment);
            } else {
                ft.replace(R.id.containerLayout, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void  onBackPressed() {
        /*int backStackEntryCount = supportFragmentManager.backStackEntryCount;
        LogUtil.d(TAG, "onBackPressed BackStackEntryCount=$backStackEntryCount")
        if (application!!.mDeviceConnected.get() && backStackEntryCount > 0) {
            if (!application!!.isUpgradeFragment && !application!!.isSmartAmbientFragment && !application!!.isAddEqFragment) {
                supportFragmentManager.popBackStack()
                //runDeviceAnimation()

                currPageIsDashboard = (backStackEntryCount == 1)
                LogUtil.d(TAG, "currPageIsDashboard=" + currPageIsDashboard + ",needUpgrade="
                        + application!!.deviceInfo.needUpgrade + ",isUpgradeAvailableShow=" + isUpgradeAvailableShow)
                if (application!!.deviceInfo.needUpgrade && !isUpgradeAvailableShow) {
                    mHandler.postDelayed(showUpgradeAvailableDialog, 1500)
                }
            } else if (application!!.isAddEqFragment) {

            }
        } else {
            if (application!!.mDeviceConnected.get()) {
                AppUtils.hideFromForeground(this)
            } else {
                finish()
            }
        }*/
    }

    public void refreshPage() {
        LogUtil.d(TAG, "refreshPage()");
        //updateAutoOffImage(false);
        //updateEqSwitchImage(false);
        //updateSmartAmbientImage(false);
        //updateEqInfoLayout();
        //updateSettingImage();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eqInfoLayout: {
                if (FastClickHelper.isFastClick()) {
                    return;
                }
                //switchFragment(new EqSettingFragment());
                createMyOwnEqDialog.show();
                break;
            }
        }
    }
}
