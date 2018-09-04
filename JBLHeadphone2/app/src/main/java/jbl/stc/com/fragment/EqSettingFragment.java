package jbl.stc.com.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;
import com.avnera.smartdigitalheadset.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jbl.stc.com.R;
import jbl.stc.com.activity.HomeActivity;
import jbl.stc.com.adapter.EqRecyclerAdapter;
import jbl.stc.com.constant.JBLConstant;
import jbl.stc.com.entity.CircleModel;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnCustomEqListener;
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.UiUtils;
import jbl.stc.com.view.CustomFontTextView;
import jbl.stc.com.view.EqualizerShowView;
import jbl.stc.com.view.EqualizerView;
import jbl.stc.com.view.MyGridLayoutManager;


public class EqSettingFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = EqSettingFragment.class.getSimpleName();
    private EqualizerShowView equalizerView;
    private EqualizerView equalizerLineView;
    private EqualizerView equalizerFinalView;
    private ImageView eqEditImage;
    private View titleBar;
    private TextView eqNameText;
    private TextView eqNameFinalText;
    private ImageView closeImageView;
    private ImageView moreImageView;
    private ImageView addImageView;
    private RecyclerView eqRecycleView;
    public static EqRecyclerAdapter eqAdapter;
    private LinearLayout frameLayout;
    public static List<EQModel> eqModelList = new ArrayList<>();
    private EQModel currSelectedEq;
    private int currSelectedEqIndex;
    private LightX lightX;
    private Handler mHandler = new Handler();
    private float mPosX = 0, mCurPosX;
    public static float mLastPosX = 0;
    private boolean isTranslationX = false;
    private float yDown;
    private float yMove;
    private int screenHeght;
    private int screenWidth;
    private boolean isDynamicDrawCurve = false;
    public static View rootView;
    public static int downNum;
    public static CustomFontTextView titleEq;
    public static LinearLayout ll_bottomeq;
    public static View eqDividerView;
    public static TextView eq;
    public static TextView appImageView;
    public static WindowManager mWindowManager;
    public static WindowManager.LayoutParams mWindowLayoutParams;
    public static WindowManager.LayoutParams mEqWindowLayoutParams;
    public static LinearLayout ll_eqtext;
    public static TextView tv_drageq;
    private float rawY = 0.0f;
    private boolean isShownFinal = false;
    private float dValue;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_eq_settings, container, false);
        lightX = AvneraManager.getAvenraManager().getLightX();
        mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeght = dm.heightPixels;
        screenWidth = dm.widthPixels;
        Bundle bundle = getArguments();
        if (bundle != null) {
            rawY = bundle.getFloat("rawY");
            Logger.d(TAG, "OnCreateView:" + String.valueOf(rawY));
            rootView.setTranslationY(rawY);
            bundle.remove("rawY");
        }
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_SETTINGS_PANEL);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    rawY = 0.0f;
                    HomeActivity.isEnter = false;
                    getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }

    private void initView() {
        titleBar = rootView.findViewById(R.id.titleBar);
        equalizerView = rootView.findViewById(R.id.equalizerView);
        equalizerLineView = rootView.findViewById(R.id.equalizerLineView);
        equalizerFinalView = rootView.findViewById(R.id.equalizerFinalView);
        eqEditImage = rootView.findViewById(R.id.eqEditImage);
        closeImageView = rootView.findViewById(R.id.closeImageView);
        moreImageView = rootView.findViewById(R.id.moreImageView);
        addImageView = rootView.findViewById(R.id.addImageView);
        eqNameText = rootView.findViewById(R.id.text_view_eq_settings_eq_name);
        eqNameFinalText = rootView.findViewById(R.id.text_view_eq_name_final);
        eqRecycleView = rootView.findViewById(R.id.eqRecycleView);
        eqRecycleView.setLayoutManager(new MyGridLayoutManager(getActivity(), 2));
        eqAdapter = new EqRecyclerAdapter();
        eqRecycleView.setAdapter(eqAdapter);
        if (rawY > 0.0) {
            eqRecycleView.setVisibility(View.GONE);
        }
        frameLayout = rootView.findViewById(R.id.frameLayout);
        frameLayout.setOnClickListener(this);
        rootView.findViewById(R.id.rl_eqRecycleView).setOnClickListener(this);
    }

    private void initEvent() {
        eqEditImage.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
        moreImageView.setOnClickListener(this);
        addImageView.setOnClickListener(this);
        eqAdapter.setOnEqSelectedListener(new OnEqItemSelectedListener() {
            @Override
            public void onSelected(int position) {
                onEqNameSelected(position, true);
                if (isDynamicDrawCurve) {
                    pos = 0;
                    myHandler.removeMessages(MSG_SHOW_LINE);
                    equalizerView.clearAllPointCircles();
                    myHandler.sendEmptyMessage(MSG_SHOW_LINE);
                    Logger.d(TAG, "aaaaa onEqNameSelected "); //dynamic draw curve
                }
            }
        });
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        yDown = event.getRawY();
                        Logger.d(TAG, "yDown" + String.valueOf(yDown));
                        downNum = eqModelList.size() - 1;
                        startRecyleViewGoneAnimation(downNum);
                        startBottomEqShownAnimation(getActivity(), screenHeght, screenWidth, 0.f);
                        int startX = (screenWidth) / 2 - UiUtils.dip2px(getActivity(), 35);
                        int startY = (int) (UiUtils.dip2px(getActivity(), 20) + UiUtils.getStatusHeight(getActivity()) - yDown / 2);
                        startEqTitleTextShown(getActivity(), startX, startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        yMove = event.getRawY();
                        if (yMove > UiUtils.getStatusHeight(getActivity())) {
                            if (yMove > screenHeght / 3) {
                                changeBottomEqAlpha(1);
                            }
                            if ((yMove - yDown) > 25) {
                                rootView.setTranslationY(yMove - UiUtils.getStatusHeight(getActivity()));
                                Logger.d(TAG, "yMove" + String.valueOf(yMove) + "screenHeight:" + screenHeght);
                                int height = (int) (yMove - yDown) / 2;
                                changeShadeViewHeight(height, getActivity());
                                int dragEqHeight = (int) ((yMove) / (screenHeght + UiUtils.getStatusHeight(getActivity()) - UiUtils.dip2px(getActivity(), 70)) * UiUtils.dip2px(getActivity(), 70));
                                changeDragEqTitleBarHeight(dragEqHeight, getActivity());
                                int fullWidth = (screenWidth - UiUtils.dip2px(getActivity(), 70)) / 2;
                                int x = (int) (fullWidth - yMove / (screenHeght + UiUtils.getStatusHeight(getActivity()) - UiUtils.dip2px(getActivity(), 70)) * fullWidth);
                                int y = 0;
                                if (x < fullWidth / 2) {
                                    y = (int) (yMove - dragEqHeight / 2 + UiUtils.getStatusHeight(getActivity()));
                                } else {
                                    y = (int) (yMove - dragEqHeight / 2);
                                }
                                updateEqTitleLocation(x, y);
                            }
                            if ((yMove + UiUtils.dip2px(getActivity(), 70)) > screenHeght) {
                                startBottomEqGonenAnimation();
                                startDragEqGoneAnimation();
                                HomeActivity.isEnter = false;
                                getActivity().onBackPressed();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        startBottomEqGonenAnimation();
                        startDragEqGoneAnimation();
                        if (yMove > screenHeght / 2) {
                            HomeActivity.isEnter = false;
                            getActivity().onBackPressed();
                        } else {
                            EqSettingFragment.rootView.setTranslationY(0);
                            setDragEqTitleBarGone();
                            int height = 0;
                            changeShadeViewHeight(height, getActivity());
                            eqRecycleView.setVisibility(View.VISIBLE);
                            eqAdapter.setEqModels(eqModelList, true, false, eqRecycleView);
                            if (currSelectedEqIndex == 0) {
                                frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq_off);
                                eqEditImage.setClickable(false);
                            } else {
                                frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq);
                                eqEditImage.setClickable(true);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        final int distance = screenWidth / 11;

        equalizerLineView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        isTranslationX = false;
                        break;
                    case MotionEvent.ACTION_MOVE:

                        //if (Math.abs(mCurPosX-mLastPosX)>UiUtils.dip2px(getActivity(),5)){
                            if (!isTranslationX) {
                                Logger.d(TAG, "fresh eq");
                                isTranslationX = true;
                                mCurPosX = event.getX();
                                dValue = mCurPosX - mPosX;
                                if (Math.abs(dValue) > distance && dValue > 0) {
                                    dValue = distance;
                                }
                                if (Math.abs(dValue) > distance && dValue < 0) {
                                    dValue = (-distance);
                                }

                                //equalizerLineView.setTranslationX(dValue);
                                equalizerLineView.setTranslateX(dValue);

                                /*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) equalizerLineView.getLayoutParams();
                                params.leftMargin = (int) dValue;
                                params.rightMargin = (int) (-dValue);
                                equalizerLineView.setLayoutParams(params);*/
                                equalizerLineView.setAlpha(1 - Math.abs(dValue) / distance + 0.2f);
                                isTranslationX = false;
                            }
                        //}
                        break;
                    case MotionEvent.ACTION_UP:
                        float alpha = equalizerLineView.getAlpha();
                        equalizerLineView.setTranslateX(0);
                        equalizerLineView.setTranslationX(0);
                        if (mCurPosX - mPosX > distance / 2) {
                            if (currSelectedEqIndex >= 1) {
                                isShownFinal = true;
                                List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(mContext);
                                EQModel eqModel = eqModels.get(currSelectedEqIndex - 1);
                                equalizerLineView.setCurveData(eqModel.getPointX(), eqModel.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
                                equalizerLineView.setAlpha(0.5f);
                                equalizerLineView.setTranslationX(-distance);
                                //equalizerLineView.setTranslateX(-distance);
                                /*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) equalizerLineView.getLayoutParams();
                                params.leftMargin = (-distance);
                                params.rightMargin = (distance);
                                equalizerLineView.setLayoutParams(params);*/

                                eqNameText.setText(eqModel.eqName);
                                eqNameText.setAlpha(0);
                                equalizerFinalView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
                                equalizerFinalView.setAlpha(alpha);
                                equalizerFinalView.setTranslationX(dValue);
                                //equalizerFinalView.setTranslateX(dValue);
                                /*FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) equalizerFinalView.getLayoutParams();
                                params1.leftMargin = (int) dValue;
                                params1.rightMargin = (int) (-dValue);
                                equalizerFinalView.setLayoutParams(params1);*/

                                eqNameFinalText.setText(currSelectedEq.eqName);
                                eqNameFinalText.setAlpha(1.0f);
                                eqViewLocationAnimation(equalizerLineView, -distance, 0);
                            }
                        }
                        if (mPosX - mCurPosX > distance / 2) {
                            if (currSelectedEqIndex < eqModelList.size() - 1) {
                                isShownFinal = true;
                                List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(mContext);
                                EQModel eqModel = eqModels.get(currSelectedEqIndex + 1);
                                equalizerLineView.setCurveData(eqModel.getPointX(), eqModel.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
                                equalizerLineView.setAlpha(0.5f);
                                equalizerLineView.setTranslationX(distance);
                                //equalizerLineView.setTranslateX(distance);
                                /*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) equalizerLineView.getLayoutParams();
                                params.leftMargin = (int) (distance);
                                params.rightMargin = (int) (-distance);
                                equalizerLineView.setLayoutParams(params);*/

                                eqNameText.setText(eqModel.eqName);
                                eqNameText.setAlpha(0);
                                equalizerFinalView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
                                equalizerFinalView.setAlpha(alpha);
                                equalizerFinalView.setTranslationX(dValue);
                                //equalizerFinalView.setTranslateX(dValue);
                                /*FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) equalizerFinalView.getLayoutParams();
                                params1.leftMargin = (int) dValue;
                                params1.rightMargin = (int) (-dValue);
                                equalizerFinalView.setLayoutParams(params1);*/

                                eqNameFinalText.setText(currSelectedEq.eqName);
                                eqNameFinalText.setAlpha(1.0f);
                                eqViewLocationAnimation(equalizerLineView, distance, 0);
                            }
                        }
                        if (isShownFinal) {
                            isShownFinal = false;
                            eqNameAlphaAnimation(eqNameText, 0f, 1.0f);
                            eqNameAlphaAnimation(eqNameFinalText, 1.0f, 0f);
                        } else {
                            equalizerLineView.setTranslationX(0);
                            //equalizerLineView.setTranslateX(0);
                            /*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) equalizerLineView.getLayoutParams();
                            params.leftMargin = 0;
                            params.rightMargin = 0;
                            equalizerLineView.setLayoutParams(params);*/
                            equalizerLineView.setAlpha(1);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void eqViewLocationAnimation(EqualizerView view, int transStartX, int transEndX) {
        view.clearAnimation();
        view.setTranslationX(transStartX);
        view.setAlpha(0.f);
        view.animate()
                .translationX(transEndX).alpha(1.f)
                //.setInterpolator(new DecelerateInterpolator(0.5f))
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mCurPosX - mPosX > 0) {
                            //scroll to right
                            eqAdapter.setSelectedIndex(currSelectedEqIndex - 1);
                        } else if (mPosX - mCurPosX > 0) {
                            //scroll to left
                            eqAdapter.setSelectedIndex(currSelectedEqIndex + 1);
                        }
                        //equalizerLineView.setTranslationX(0);
                        //equalizerLineView.setTranslateX(0);
                        equalizerLineView.setAlpha(1);
                        equalizerFinalView.setAlpha(0);
                    }
                })
                .start();

    }

    private void translationXAnimation(View view, float transStartX, float transEndX) {
        view.clearAnimation();
        view.setTranslationX(transStartX);
        view.animate()
                .translationX(transEndX)
                .setInterpolator(new DecelerateInterpolator(0.5f))
                .setDuration(10)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isTranslationX = false;
                    }
                })
                .start();

    }

    private void eqNameAlphaAnimation(View view, float startAlpha, float endAlpha) {
        view.clearAnimation();
        view.setAlpha(startAlpha);
        view.animate()
                .alpha(endAlpha)
                .setInterpolator(new DecelerateInterpolator(0.5f))
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isTranslationX = false;
                    }
                })
                .start();
    }

    public static void setDragTitleBarVisible() {
        RelativeLayout rl_dragtitlebar = rootView.findViewById(R.id.rl_dragtitlebar);
        rl_dragtitlebar.setVisibility(View.VISIBLE);
        LinearLayout frameLayout = rootView.findViewById(R.id.frameLayout);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
    }

    public static void startEqTitleTextShown(Context context, int x, int y) {

        CustomFontTextView eqTitleTextView = rootView.findViewById(R.id.eqTitleTextView);
        eqTitleTextView.setVisibility(View.GONE);
        mEqWindowLayoutParams = new WindowManager.LayoutParams();
        mEqWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mEqWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mEqWindowLayoutParams.x = x;
        mEqWindowLayoutParams.y = y;
        mEqWindowLayoutParams.alpha = 1.0f;
        mEqWindowLayoutParams.width = UiUtils.dip2px(context, 70);
        mEqWindowLayoutParams.height = UiUtils.dip2px(context, 30);
        mEqWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ll_eqtext = new LinearLayout(context);
        ll_eqtext.setGravity(Gravity.CENTER);
        tv_drageq = new TextView(context);
        WindowManager.LayoutParams dragEqParams = new WindowManager.LayoutParams();
        dragEqParams.gravity = Gravity.CENTER;
        tv_drageq.setTextSize(20);
        tv_drageq.setTextColor(Color.WHITE);
        tv_drageq.setGravity(Gravity.CENTER);
        tv_drageq.setTypeface(Typeface.createFromAsset(context.getAssets(), JBLConstant.OPEN_SANS_BOLD));
        tv_drageq.setText("EQ");
        ll_eqtext.addView(tv_drageq, dragEqParams);
        mWindowManager.addView(ll_eqtext, mEqWindowLayoutParams);
    }

    public static void startDragEqGoneAnimation() {
        CustomFontTextView eqTitleTextView = rootView.findViewById(R.id.eqTitleTextView);
        eqTitleTextView.setVisibility(View.VISIBLE);
        if (tv_drageq != null) {
            Logger.d(TAG, "tv_drageq is not null");
            mWindowManager.removeView(ll_eqtext);
            ll_eqtext = null;
            tv_drageq = null;
        } else {
            Logger.d(TAG, "tv_drageq is null");
        }
    }

    public static void updateEqTitleLocation(int x, int y) {
        mEqWindowLayoutParams.x = x;
        mEqWindowLayoutParams.y = y;
        mWindowManager.updateViewLayout(ll_eqtext, mEqWindowLayoutParams);
    }

    public static void changeDragEqTitleBarHeight(int height, Context context) {
        RelativeLayout rl_dragtitlebar = rootView.findViewById(R.id.rl_dragtitlebar);
        rl_dragtitlebar.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rl_dragtitlebar.getLayoutParams();
        params.height = height;
        rl_dragtitlebar.setLayoutParams(params);
    }

    public static void setDragEqTitleBarGone() {
        RelativeLayout rl_dragtitlebar = rootView.findViewById(R.id.rl_dragtitlebar);
        rl_dragtitlebar.setVisibility(View.GONE);
    }

    public static void changeBottomEqAlpha(float alpha) {
        mWindowLayoutParams.alpha = alpha;
        mWindowManager.updateViewLayout(ll_bottomeq, mWindowLayoutParams);
    }

    public static void startBottomEqShownAnimation(Context context, int screenHeight, int screenwidth, float alpha) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        mWindowLayoutParams.x = 0;
        mWindowLayoutParams.y = screenHeight - UiUtils.dip2px(context, 70);
        mWindowLayoutParams.alpha = alpha;
        mWindowLayoutParams.width = screenwidth;
        mWindowLayoutParams.height = UiUtils.dip2px(context, 70);
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ll_bottomeq = new LinearLayout(context);
        ll_bottomeq.setGravity(Gravity.CENTER_VERTICAL);
        ll_bottomeq.setOrientation(LinearLayout.HORIZONTAL);
        titleEq = new CustomFontTextView(context);
        WindowManager.LayoutParams titleEqParams = new WindowManager.LayoutParams();
        titleEqParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        titleEqParams.width = UiUtils.dip2px(context, 70);
        titleEq.setGravity(Gravity.CENTER);
        titleEq.setTextColor(Color.WHITE);
        titleEq.setTextSize(20);
        ll_bottomeq.addView(titleEq, titleEqParams);
        eqDividerView = new View(context);
        eqDividerView.setBackgroundColor(Color.WHITE);
        eqDividerView.setPadding(0, UiUtils.dip2px(context, 12), 0, UiUtils.dip2px(context, 12));
        WindowManager.LayoutParams eqDiveiderParams = new WindowManager.LayoutParams();
        eqDiveiderParams.width = UiUtils.dip2px(context, 1);
        eqDiveiderParams.height = UiUtils.dip2px(context, 45);
        eqDiveiderParams.gravity = Gravity.CENTER_VERTICAL;
        ll_bottomeq.addView(eqDividerView, eqDiveiderParams);
        eq = new CustomFontTextView(context);
        WindowManager.LayoutParams eqParams = new WindowManager.LayoutParams();
        eqParams.gravity = Gravity.CENTER_VERTICAL;
        eqParams.width = UiUtils.dip2px(context, 200);
        eq.setPadding(UiUtils.dip2px(context, 13), 0, 0, 0);
        EQModel currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, context, ""), context);
        eq.setText(currSelectedEq.eqName);
        eq.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        eq.setTextColor(Color.WHITE);
        eq.setTextSize(20);
        eq.setTypeface(Typeface.createFromAsset(context.getAssets(), JBLConstant.OPEN_SANS_REGULAR));
        eq.setSingleLine(true);
        ll_bottomeq.addView(eq, eqParams);
        appImageView = new TextView(context);
        appImageView.setBackgroundResource(R.mipmap.white_arrow_back);
        appImageView.setRotation(90);
        int paddingLeft = screenwidth - UiUtils.dip2px(context, 84) - UiUtils.dip2px(context, 187) - UiUtils.dip2px(context, 55);
        Logger.d("EqSettingFrament", "paddingLeft:" + paddingLeft);
        //appImageView.setPadding(100,0,UiUtils.dip2px(context,15),0);
        WindowManager.LayoutParams arrowImageParams = new WindowManager.LayoutParams();
        arrowImageParams.height = UiUtils.dip2px(context, 45);
        arrowImageParams.width = UiUtils.dip2px(context, 45);
        arrowImageParams.gravity = Gravity.CENTER_VERTICAL;
        ll_bottomeq.addView(appImageView, arrowImageParams);
        LinearLayout.LayoutParams arrowImageParams1 = (LinearLayout.LayoutParams) appImageView.getLayoutParams();
        arrowImageParams1.leftMargin = paddingLeft;
        appImageView.setLayoutParams(arrowImageParams1);

        mWindowManager.addView(ll_bottomeq, mWindowLayoutParams);
    }

    public static void startBottomEqGonenAnimation() {
        if (titleEq != null) {
            mWindowManager.removeView(ll_bottomeq);
            ll_bottomeq = null;
            titleEq = null;
            eqDividerView = null;
            eq = null;
            appImageView = null;
        }

    }

    public static void changeShadeViewHeight(int height, Context context) {
        View shade_view = rootView.findViewById(R.id.shade_view);
        shade_view.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) shade_view.getLayoutParams();
        params.height = height;
        shade_view.setLayoutParams(params);
    }

    public static void startRecyleViewGoneAnimation(int position) {
        final RecyclerView eqRecycleView = rootView.findViewById(R.id.eqRecycleView);
        eqAdapter.setEqModels(eqModelList, false, true, eqRecycleView);
    }

    public static void startRecycleViewShowAnimation() {
        RecyclerView eqRecycleView = rootView.findViewById(R.id.eqRecycleView);
        eqRecycleView.setVisibility(View.VISIBLE);
        eqAdapter.setEqModels(eqModelList, true, false, eqRecycleView);
    }

    private void initValue() {
        List<EQModel> eqModels = EQSettingManager.get().getCompleteEQList(mContext);
        eqModelList.clear();
        eqModelList.addAll(eqModels);
        currSelectedEq = EQSettingManager.get().getEQModelByName(PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, getResources().getString(R.string.off)), mContext);
        Logger.d(TAG, "initValue() currEqName=" + PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, getResources().getString(R.string.off)));
        if (currSelectedEq != null && currSelectedEq.eqName != null) {
            if (application.globalEqInfo.eqOn) {
                for (int i = 0; i < eqModelList.size(); i++) {
                    if (currSelectedEq.eqName.equals(eqModelList.get(i).eqName)) {
                        eqModelList.get(i).isSelected = true;
                        currSelectedEqIndex = i;
                    } else {
                        eqModelList.get(i).isSelected = false;
                    }
                }
            }
            if (application.globalEqInfo.eqOn) {
                eqNameText.setText(currSelectedEq.eqName);
                eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            } else {
                eqNameText.setText(R.string.off);
                eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
                eqModelList.get(0).isSelected = true;
            }
        } else {
            eqNameText.setText(R.string.off);
            eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.text_white_50));
            for (EQModel model : eqModelList) {
                model.isSelected = false;
            }
            eqModelList.get(0).isSelected = true;
        }
        for (int i = 0; i < eqModelList.size(); i++) {
            Logger.d(TAG, "i=" + i + "," + eqModelList.get(i));
        }
        if (rawY > 0.0) {
            eqRecycleView.setVisibility(View.GONE);
            rawY = 0.0f;
            setDragTitleBarVisible();
            startEqTitleTextShown(getActivity(), 0, screenHeght - UiUtils.dip2px(getActivity(), 50) - UiUtils.getStatusHeight(getActivity()));
            startBottomEqShownAnimation(getActivity(), screenHeght, screenWidth, 1.0f);
        } else {
            eqAdapter.setEqModels(eqModelList, true, false, eqRecycleView);
        }
        //float[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        if (currSelectedEq != null) {
            if (isDynamicDrawCurve) {
                pos = 0;
                equalizerView.clearAllPointCircles();
                myHandler.removeMessages(MSG_SHOW_LINE);
                myHandler.sendEmptyMessageDelayed(MSG_SHOW_LINE, 500);
                Logger.d(TAG, "aaaaa initValue");
            } else {
                equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
                equalizerLineView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
            }

        }
        if (currSelectedEqIndex == 0) {
            frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            eqEditImage.setClickable(false);
        } else {
            frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq);
            eqEditImage.setClickable(true);
        }
        smoothToPosition();

        //create the cicle view under the EqualizerShowView
        createCircleView();
    }

    private void createCircleView() {
        int circleNum = 7;
        LinearLayout ll_small_circle = rootView.findViewById(R.id.ll_small_circle);
        int width = UiUtils.dip2px(getActivity(), 6);
        ll_small_circle.removeAllViews();
        if (eqModelList.size() > 6) {
            int ll_width = (circleNum + circleNum - 1) * width;
            RelativeLayout.LayoutParams ll_params = new RelativeLayout.LayoutParams(ll_width, width);
            if (currSelectedEqIndex < 3) {
                ll_params.leftMargin = (int) (screenWidth - ll_width) / 2;
                ll_params.rightMargin = (int) (screenWidth - ll_width) / 2;
            } else {
                ll_params.leftMargin = (int) (screenWidth - ll_width) / 2 - (currSelectedEqIndex - 3) * width;
                ll_params.rightMargin = (int) (screenWidth - ll_width) / 2 + (currSelectedEqIndex - 3) * width;
            }
            ll_params.topMargin = (int) (UiUtils.dip2px(getActivity(), 45) - UiUtils.dip2px(getActivity(), 6)) / 2;
            ll_params.bottomMargin = (int) (UiUtils.dip2px(getActivity(), 45) - UiUtils.dip2px(getActivity(), 6)) / 2;
            ll_small_circle.setLayoutParams(ll_params);
            for (int i = 0; i < circleNum; i++) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setImageResource(R.drawable.eq_choose_small_circle);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
                imageView.setAlpha(0f);
                if (i < circleNum - 1) {
                    params.rightMargin = width;
                }
                int leftNum = currSelectedEqIndex;
                int rightNum = eqModelList.size() - 1 - currSelectedEqIndex;
                if (leftNum < 4) {
                    if (leftNum == 0) {
                        if (i == 0) {
                            imageView.setAlpha(1f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 4) {
                            imageView.setAlpha(0.1f);
                        }
                    } else if (leftNum == 1) {
                        if (i == 0) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 1) {
                            imageView.setAlpha(1.0f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 4) {
                            imageView.setAlpha(0.1f);
                        }
                    } else if (leftNum == 2) {
                        if (i == 0) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 2) {
                            imageView.setAlpha(1.0f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 4) {
                            imageView.setAlpha(0.1f);
                        }
                    } else if (leftNum == 3) {
                        if (i == 0) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(1.0f);
                        } else if (i == 4) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 5) {
                            imageView.setAlpha(0.1f);
                        }
                    }
                } else {
                    if (rightNum == 0) {
                        if (i == 0) {
                            imageView.setAlpha(0.1f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 4) {
                            imageView.setAlpha(1.0f);
                        }

                    }
                    if (rightNum == 1) {
                        if (i == 0) {
                            imageView.setAlpha(0.1f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 4) {
                            imageView.setAlpha(1.0f);
                        }
                        if (i == 5) {
                            imageView.setAlpha(0.4f);
                        }
                    }
                    if (rightNum > 1) {
                        if (i == 0) {
                            imageView.setAlpha(0.1f);
                        } else if (i == 1) {
                            imageView.setAlpha(0.2f);
                        } else if (i == 2) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 3) {
                            imageView.setAlpha(0.4f);
                        } else if (i == 4) {
                            imageView.setAlpha(1.0f);
                        }
                        if (i == 5 || i == 6) {
                            imageView.setAlpha(0.4f);
                        }
                    }
                }
                ll_small_circle.addView(imageView, params);
            }
        } else {
            circleNum = eqModelList.size();
            int ll_width = (circleNum + circleNum - 1) * width;
            RelativeLayout.LayoutParams ll_params = new RelativeLayout.LayoutParams(ll_width, width);
            ll_params.leftMargin = (int) (screenWidth - ll_width) / 2;
            ll_params.rightMargin = (int) (screenWidth - ll_width) / 2;
            ll_params.topMargin = (int) (UiUtils.dip2px(getActivity(), 45) - UiUtils.dip2px(getActivity(), 6)) / 2;
            ll_params.bottomMargin = (int) (UiUtils.dip2px(getActivity(), 45) - UiUtils.dip2px(getActivity(), 6)) / 2;
            ll_small_circle.setLayoutParams(ll_params);
            for (int i = 0; i < circleNum; i++) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setImageResource(R.drawable.eq_choose_small_circle);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
                imageView.setAlpha(0f);
                if (i < circleNum - 1) {
                    params.rightMargin = width;
                }
                if (i == currSelectedEqIndex) {
                    imageView.setAlpha(1f);
                } else if (Math.abs(i - currSelectedEqIndex) == 1 || (Math.abs(i - currSelectedEqIndex) == 2)) {
                    imageView.setAlpha(0.4f);
                } else if (Math.abs(i - currSelectedEqIndex) == 3) {
                    imageView.setAlpha(0.2f);
                } else if (Math.abs(i - currSelectedEqIndex) == 4) {
                    imageView.setAlpha(0.1f);
                }
                ll_small_circle.addView(imageView, params);
            }
        }
    }

    private static int pos = 0;
    private MyHandler myHandler = new MyHandler();
    private final static int MSG_SHOW_LINE = 0;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_LINE: {
                    List<CircleModel> listsBak = equalizerView.getAllPointCircles(currSelectedEq.getPointX(), currSelectedEq.getPointY());
                    if (pos >= listsBak.size()) {
                        pos = 0;
                        break;
                    }

                    if (pos < listsBak.size() && pos + 5 >= listsBak.size()) {
                        equalizerView.setCurveData(listsBak.subList(pos, listsBak.size() - 1), R.color.text_white_80, isDynamicDrawCurve);
                    } else {
                        equalizerView.setCurveData(listsBak.subList(pos, pos + 5), R.color.text_white_80, isDynamicDrawCurve);
                    }
                    myHandler.sendEmptyMessage(MSG_SHOW_LINE);
                    pos = pos + 5;
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void smoothToPosition() {
        if (currSelectedEqIndex > 1) {
            Logger.d(TAG, "smoothToPosition currSelectedEqIndex=" + currSelectedEqIndex);
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
    }

    private void onEqNameSelected(int eqIndex, boolean fromUser) {
        Logger.d(TAG, "onEqNameSelected eqIndex is " + eqIndex);
        currSelectedEq = eqModelList.get(eqIndex);
        currSelectedEqIndex = eqIndex;
        eqNameText.setText(currSelectedEq.eqName);
        eqNameText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        application.globalEqInfo.eqOn = true;
        for (EQModel model : eqModelList) {
            model.isSelected = false;
        }
        currSelectedEq.isSelected = true;
        //if (bundle == null) {
        eqAdapter.setEqModels(eqModelList, false, false, eqRecycleView);
        //}
        PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, mContext);
        if (!currSelectedEq.eqName.equals(getResources().getString(R.string.off))) {
            PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, currSelectedEq.eqName, mContext);
        }
        //int[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(currSelectedEq);
        if (!isDynamicDrawCurve) {
            equalizerView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
            equalizerLineView.setCurveData(currSelectedEq.getPointX(), currSelectedEq.getPointY(), R.color.text_white_80, isDynamicDrawCurve);
            equalizerLineView.setTranslationX(0);
            equalizerLineView.setAlpha(1);
        }
        AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
        if (fromUser) {
            eqRecycleView.smoothScrollToPosition(currSelectedEqIndex);
        }
        if (eqIndex == 0) {
            frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq_off);
            eqEditImage.setClickable(false);
        } else {
            frameLayout.setBackgroundResource(R.drawable.shape_gradient_eq);
            eqEditImage.setClickable(true);
        }
        mHandler.removeCallbacks(applyRunnable);
        mHandler.postDelayed(applyRunnable, 300);

        createCircleView();
    }

    /**
     * Stop user to abuse app and send all command only after 300 milliseconds
     */
    Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {
            switch (currSelectedEqIndex) {
                case 0:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Off);
                    break;
                case 1:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Jazz);
                    break;
                case 2:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Vocal);
                    break;
                case 3:
                    ANCControlManager.getANCManager(getContext()).applyPresetWithoutBand(GraphicEQPreset.Bass);
                    break;
                default:
                    ANCControlManager.getANCManager(getContext()).applyPresetsWithBand(GraphicEQPreset.User, EQSettingManager.get().getValuesFromEQModel(currSelectedEq));
                    break;
            }
            // AnalyticsManager.getInstance(getActivity()).reportSelectedNewEQ(currSelectedEq.eqName);
            PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME, currSelectedEq.eqName, getActivity());
            Logger.d(TAG, "select eq position is " + String.valueOf(currSelectedEqIndex));
        }
    };

    public void onAddCustomEq(boolean isAdd, boolean isPreset) {
        Logger.d(TAG, "onAddCustomEq()");
        EqCustomFragment fragment = new EqCustomFragment();
        fragment.setOnCustomEqListener(onCustomEqListener);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_ADD, isAdd);
        bundle.putBoolean(EqCustomFragment.EXTRA_IS_PRESET, isPreset);
        if (!isAdd || isPreset) {
            bundle.putSerializable(EqCustomFragment.EXTRA_EQ_MODEL, currSelectedEq);
        }
        fragment.setArguments(bundle);
        switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
        eqRecycleView.smoothScrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        if (rawY > 0.0) {
            rawY = 0.0f;
        }
        switch (v.getId()) {
            case R.id.eqEditImage:
                if (currSelectedEqIndex < 4) {
                    onAddCustomEq(true, true);
                } else {
                    onAddCustomEq(false, false);
                }
                break;
            case R.id.closeImageView:
                HomeActivity.isEnter = false;
                getActivity().onBackPressed();
                break;
            case R.id.moreImageView:
                /*EqMoreSettingFragment fragment = new EqMoreSettingFragment();
                switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);*/
                NewEqMoreSettingFragment fragment = new NewEqMoreSettingFragment();
                switchFragment(fragment, JBLConstant.SLIDE_FROM_DOWN_TO_TOP);
                break;
            case R.id.addImageView:
                onAddCustomEq(true, false);
                break;
        }
    }

    private OnCustomEqListener onCustomEqListener = new OnCustomEqListener() {
        @Override
        public void onCustomEqResult(EQModel model, boolean isAdd) {
            if (model != null) {
                if (!isAdd) {//modify
                    currSelectedEq = eqModelList.get(currSelectedEqIndex);
                    currSelectedEq.eqName = model.eqName;
                    currSelectedEq.value_32 = model.value_32;
                    currSelectedEq.value_64 = model.value_64;
                    currSelectedEq.value_125 = model.value_125;
                    currSelectedEq.value_250 = model.value_250;
                    currSelectedEq.value_500 = model.value_500;
                    currSelectedEq.value_1000 = model.value_1000;
                    currSelectedEq.value_2000 = model.value_2000;
                    currSelectedEq.value_4000 = model.value_4000;
                    currSelectedEq.value_8000 = model.value_8000;
                    currSelectedEq.value_16000 = model.value_16000;
                    onEqNameSelected(currSelectedEqIndex, false);
                } else {//add new
                    int addIndex = eqModelList.size() - 1;
                    eqModelList.add(addIndex, model);
                    onEqNameSelected(addIndex, false);
                }
                smoothToPosition();
            }
        }
    };
}
