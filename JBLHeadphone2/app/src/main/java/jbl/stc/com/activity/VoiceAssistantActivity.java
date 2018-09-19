package jbl.stc.com.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import jbl.stc.com.R;
import jbl.stc.com.constant.ConnectStatus;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.ProductListManager;
import jbl.stc.com.view.BlurringView;
import jbl.stc.com.view.CustomFontTextView;
import jbl.stc.com.view.VoiceAssistantSettingPopWindow;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 09/12/18.
 */

public class VoiceAssistantActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = VoiceAssistantActivity.class.getSimpleName();
    private CustomFontTextView tv_goto_smart_assistant;
    private BlurringView mBlurView;
    private View bluredView;
    VoiceAssistantSettingPopWindow voiceAssistantSettingPopWindow;
    private MyDevice myDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_assistant);
        myDevice = ProductListManager.getInstance().getSelectDevice(ConnectStatus.DEVICE_CONNECTED);
        intView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSwipeBackEnable(false);
    }

    private void intView() {
        tv_goto_smart_assistant = (CustomFontTextView) findViewById(R.id.tv_goto_voice_assistant);
        tv_goto_smart_assistant.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_goto_smart_assistant.getPaint().setAntiAlias(true);
        tv_goto_smart_assistant.setOnClickListener(this);
        findViewById(R.id.tv_set_up_later).setOnClickListener(this);
        findViewById(R.id.image_view_back).setOnClickListener(this);
        bluredView = findViewById(R.id.relative_layout_vocie_assistant);
        mBlurView = (BlurringView) findViewById(R.id.view_blur);
        voiceAssistantSettingPopWindow = new VoiceAssistantSettingPopWindow(VoiceAssistantActivity.this);
        voiceAssistantSettingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                gotoHome();

            }
        });
    }

    private void gotoHome() {
        if (mBlurView != null) {
            mBlurView.setVisibility(View.GONE);
        }
        finish();
        Intent intent = new Intent(VoiceAssistantActivity.this, HomeActivity.class);
        intent.putExtra(JBLConstant.KEY_CONNECT_STATUS, myDevice.connectStatus);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_back: {
                gotoHome();
                break;
            }
            case R.id.tv_goto_voice_assistant: {
                try {
                    startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    /*Intent launchIntent = new Intent(Intent.ACTION_VIEW);
                    launchIntent.setPackage("com.google.android.googlequicksearchbox");
                    launchIntent.setClassName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.staticplugins.opa.OpaActivity");
                    startActivity(launchIntent);*/
                } catch (Exception e) {
                    Logger.d(TAG, "start vocie assistant error:" + e.toString());
                }
                break;
            }
            case R.id.tv_set_up_later: {
                showPopWindow();
                break;
            }


        }
    }

    private void showPopWindow() {

        mBlurView.setBlurredView(bluredView);
        mBlurView.invalidate();
        mBlurView.setVisibility(View.VISIBLE);
        mBlurView.setAlpha(0f);
        mBlurView.animate().alpha(0.5f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBlurView.setVisibility(View.VISIBLE);
                mBlurView.setAlpha(0.5f);
            }
        });
        voiceAssistantSettingPopWindow.showAtLocation(findViewById(R.id.relative_layout_voice_assistant_root), Gravity.NO_GRAVITY, 0, 0);
    }


    @Override
    public void onBackPressed() {
        if (voiceAssistantSettingPopWindow != null && voiceAssistantSettingPopWindow.isShowing()) {
            voiceAssistantSettingPopWindow.dismiss();
        } else {
            gotoHome();
        }

    }
}
