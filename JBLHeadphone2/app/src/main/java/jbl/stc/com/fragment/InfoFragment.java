package jbl.stc.com.fragment;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jbl.stc.com.R;
import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.legal.LegalApi;

public class InfoFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = InfoFragment.class.getSimpleName();
    private View view;
    private String typeFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info,
                container, false);
        Bundle bundle = getArguments();
        if (bundle != null){
            typeFragment = bundle.getString(JBLConstant.TYPE_FRAGMENT);
        }
        view.findViewById(R.id.text_view_info_my_product).setOnClickListener(this);
        view.findViewById(R.id.text_view_open_source_license).setOnClickListener(this);
        view.findViewById(R.id.text_view_eula).setOnClickListener(this);
        view.findViewById(R.id.text_view_info_product_help).setOnClickListener(this);
        view.findViewById(R.id.text_view_harman_privacy_policy).setOnClickListener(this);
        view.findViewById(R.id.image_view_info_back).setOnClickListener(this);
        TextView textView =view.findViewById(R.id.text_view_info_app_version);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            String version = getString(R.string.app_version) + packageInfo.versionName +"("+ packageInfo.versionCode+")";
            textView.setText(version);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_view_open_source_license:{
                LegalApi.INSTANCE.showOpenSource(getActivity(),false);
                break;
            }
            case R.id.text_view_eula:{
                LegalApi.INSTANCE.showEula(getActivity(),false);
                break;
            }
            case R.id.text_view_harman_privacy_policy:{
                LegalApi.INSTANCE.showPrivacyPolicy(getActivity(),false);
                break;
            }
            case R.id.image_view_info_back:{
                getActivity().onBackPressed();
                break;
            }
            case R.id.text_view_info_product_help:{
                switchFragment(new ProductHelpFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                break;
            }
            case R.id.text_view_info_my_product:{
                if (!DashboardActivity.getDashboardActivity().isConnected()){
                    getActivity().onBackPressed();
                }else{
                    switchFragment(new ConnectedBeforeFragment(),JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
                }
                break;
            }
        }

    }
}
