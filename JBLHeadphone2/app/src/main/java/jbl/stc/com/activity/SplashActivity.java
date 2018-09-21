package jbl.stc.com.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ViewSwitcher;

import com.avnera.smartdigitalheadset.Logger;

import jbl.stc.com.R;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.fragment.LegalFragment;
import jbl.stc.com.fragment.LegalLandingFragment;
import jbl.stc.com.listener.DismissListener;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;


public class SplashActivity extends FragmentActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder holder;
    private ViewSwitcher viewSwitcher;
    private SurfaceView mPreview;
    boolean isNeedToPlayed;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int REQUEST_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate");
        setContentView(R.layout.activity_splash);
        mPreview = findViewById(R.id.videoHolder);
        viewSwitcher = findViewById(R.id.viewswicther);
        boolean isShowJBLBrandManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME,SplashActivity.this);
        if (!isShowJBLBrandManyTimes) {
            findViewById(R.id.relative_layout_jbl).setVisibility(View.GONE);
            viewSwitcher.post(new Runnable() {
                @Override
                public void run() {
                    boolean isShowJBLBrandManyTimes = PreferenceUtils.getBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME,SplashActivity.this);
                    if (!isShowJBLBrandManyTimes){
                        //TODO: show JBL Brand related story.
                        PreferenceUtils.setBoolean(PreferenceKeys.SHOW_JBL_BRAND_FIRST_TIME, true, getApplicationContext());
                        viewSwitcher.showNext();
                        holder = mPreview.getHolder();
                        holder.addCallback(SplashActivity.this);
                        isNeedToPlayed = true;
                        playVideo(holder);
                    }
                }
            });
        }else{
            findViewById(R.id.relative_layout_jbl).setVisibility(View.VISIBLE);
            viewSwitcher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showLegalFragment();
                }
            },1500);
        }

    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedToPlayed) {
            playVideo(holder);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void showLegalFragment(){
        boolean legalPersist = PreferenceUtils.getBoolean(PreferenceKeys.LEGAL_PERSIST,getApplicationContext());
        if (!legalPersist){
            LegalLandingFragment legalLandingFragment = new LegalLandingFragment();
            legalLandingFragment.setOnDismissListener(new DismissListener(){

                @Override
                public void onDismiss(int reason) {
                    showDashBoard();
                }
            });
            Logger.d(TAG, "LegalLandingFragment");
            switchFragment(legalLandingFragment, JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT);
        }else{
            showDashBoard();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fr = getSupportFragmentManager().findFragmentById(R.id.relative_layout_splash);
        if (fr instanceof LegalLandingFragment) {
            return;
        }
        super.onBackPressed();
    }

    private void showDashBoard(){
        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
        finish();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    public void switchFragment(Fragment baseFragment, int type) {
        try {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down);
            }else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT){
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            }else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT){
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            }
            if (getSupportFragmentManager().findFragmentById(R.id.relative_layout_splash) == null) {
                ft.add(R.id.relative_layout_splash, baseFragment);
            } else {
                ft.replace(R.id.relative_layout_splash, baseFragment, baseFragment.getTag());
            }
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
            }
        }
    }

    int lastPosition = 0;
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                lastPosition = mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void startVideoPlayback() {
        Logger.d(TAG, "startVideoPlayback");
        if (mMediaPlayer == null){
            Logger.d(TAG,"mMediaPlayer is null");
            return;
        }
        if (holder == null){
            Logger.d(TAG,"holder is null");
            return;
        }
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
        try {
            if (lastPosition > 100) {
                lastPosition = 0;
                mMediaPlayer.seekTo(lastPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playVideo(SurfaceHolder holder) {
        doCleanUp();
        mMediaPlayer = new MediaPlayer();
        Logger.d(TAG, "playVideo holder=" + holder);
        try {
            AnalyticsManager.getInstance().reportDidSkipCoachMarks();
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.jbl_intro);
            mMediaPlayer.setDataSource(SplashActivity.this, video);
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    showLegalFragment();
                }
            });
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    showLegalFragment();
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "playVideo e=" + e);
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideo() {
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsVideoReadyToBePlayed = true;
        startVideo();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Logger.d(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Logger.d(TAG, "invalid video width(" + width + ") or height(" + height
                    + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        startVideo();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d(TAG, "surfaceCreated called");
        playVideo(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}