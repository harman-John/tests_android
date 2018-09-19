package jbl.stc.com.activity;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.legal.LegalApi;
import jbl.stc.com.utils.EnumCommands;

public class InfoActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = InfoActivity.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActivity(this);
        setContentView(R.layout.activity_info);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        findViewById(R.id.text_jbl_com).setOnClickListener(this);
        findViewById(R.id.text_view_open_source_license).setOnClickListener(this);
        findViewById(R.id.text_view_eula).setOnClickListener(this);
        findViewById(R.id.text_view_harman_privacy_policy).setOnClickListener(this);
        findViewById(R.id.image_view_info_back).setOnClickListener(this);
        TextView textView = (TextView) findViewById(R.id.text_view_info_app_version);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            String version = getString(R.string.app_version) + packageInfo.versionName +"."+ packageInfo.versionCode;
            textView.setText(version);
        }
    }

    @Override
    public void onConnectStatus(Object... objects) {
        super.onConnectStatus(objects);
        removeAllFragment();
        setResult(JBLConstant.REQUEST_CODE_INFO_ACTIVITY);
        finish();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onResume() {
        super.onResume();
        setSwipeBackEnable(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_open_source_license:{
                LegalApi.INSTANCE.showOpenSource(this,false);
                break;
            }
            case R.id.text_view_eula:{
                LegalApi.INSTANCE.showEula(this,false);
                break;
            }
            case R.id.text_view_harman_privacy_policy:{
                LegalApi.INSTANCE.showPrivacyPolicy(this,false);
                break;
            }
            case R.id.image_view_info_back:{
//                onBackPressed();
                finish();
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            }
            case R.id.text_jbl_com:{
                Uri uri = Uri.parse("http://www.jbl.com");
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
                break;
            }
        }

    }
}
