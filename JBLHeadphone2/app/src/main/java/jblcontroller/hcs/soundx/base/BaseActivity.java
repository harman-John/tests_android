package jblcontroller.hcs.soundx.base;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import jblcontroller.hcs.soundx.utils.FontManager;
import jblcontroller.hcs.soundx.utils.NetworkChangeListener;

import static jblcontroller.hcs.soundx.utils.AppConstants.ACTION_NETWORK_CHANGES;

public class BaseActivity extends AppCompatActivity {
    private BasePresenter mPresenter;
    private FontManager mFontManager;
    private NetworkChangeListener mNetworkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // -- Android N tracking issue ---------
        IntentFilter filter = new IntentFilter(ACTION_NETWORK_CHANGES);
        registerReceiver(mNetworkChangeListener, filter);

    }

    public void setPresenter(BasePresenter presenter) {
        mPresenter = presenter;
    }


    public Typeface getBoldFont() {

        return FontManager.getInstance(this).getBoldFont();
    }

    public Typeface getRegularFont() {

        return FontManager.getInstance(this).getRegularFont();
    }

    public Typeface getMediumFont() {

        return FontManager.getInstance(this).getMediumFont();
    }

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkChangeListener != null)
            unregisterReceiver(mNetworkChangeListener);
    }
}
