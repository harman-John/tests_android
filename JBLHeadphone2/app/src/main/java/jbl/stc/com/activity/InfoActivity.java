package jbl.stc.com.activity;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.legal.LegalApi;

public class InfoActivity extends BaseFragmentActivity implements View.OnClickListener {
    public static final String TAG = InfoActivity.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_info);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        findViewById(R.id.text_view_info_my_product).setOnClickListener(this);
        findViewById(R.id.text_view_open_source_license).setOnClickListener(this);
        findViewById(R.id.text_view_eula).setOnClickListener(this);
        findViewById(R.id.text_view_info_product_help).setOnClickListener(this);
        findViewById(R.id.text_view_harman_privacy_policy).setOnClickListener(this);
        findViewById(R.id.image_view_info_back).setOnClickListener(this);
        TextView textView = findViewById(R.id.text_view_info_app_version);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            String version = getString(R.string.app_version) + packageInfo.versionName +"("+ packageInfo.versionCode+")";
            textView.setText(version);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
            case R.id.text_view_info_product_help:{
//                switchFragment(new ProductHelpFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.text_view_info_my_product:{
//                getActivity().onBackPressed();
                finish();
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            }
        }

    }
}
