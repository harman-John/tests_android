package jbl.stc.com.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avnera.smartdigitalheadset.LightX;
import java.util.ArrayList;
import java.util.List;
import jbl.stc.com.R;
import jbl.stc.com.adapter.EqGridViewAdapter;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.manager.AnalyticsManager;
import jbl.stc.com.manager.AvneraManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.ToastUtil;
import jbl.stc.com.view.CustomFontTextView;
import jbl.stc.com.view.CustomScrollView;
import jbl.stc.com.view.EqArcView;
import jbl.stc.com.view.EqGridView;

/**
 * @name JBLHeadphone2
 * @class name：jbl.stc.com.view
 * @class describe
 * Created by Vicky on 7/20/18.
 */
public class NewEqMoreSettingFragment extends BaseFragment implements View.OnClickListener {

    private CustomScrollView mScrollView;
    private ImageView closeImageView;
    private EqGridView eqGridView;
    private EqGridViewAdapter adapter;
    private List<EQModel> eqModelList = new ArrayList<>();
    private EqArcView mEqArcView;
    private TextView tv_jazz, tv_vocal, tv_bass;
    private CustomFontTextView tv_dragImage;
    private LightX lightX;
    private static final String TAG = NewEqMoreSettingFragment.class.getSimpleName();
    private int screenWidth,screenHeight;
    private boolean clickormove=false;
    private int downX, downY;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_eq_more_setting_new, container, false);
        lightX = AvneraManager.getAvenraManager(getActivity()).getLightX();
        initView();
        initEvent();
        initValue();
        AnalyticsManager.getInstance(getActivity()).setScreenName(AnalyticsManager.SCREEN_EQ_MORE);
        return rootView;
    }

    private void initView() {
        String curEqName = PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, mContext, "");
        tv_jazz = (TextView) rootView.findViewById(R.id.tv_jazz);
        tv_vocal = (TextView) rootView.findViewById(R.id.tv_vocal);
        tv_bass = (TextView) rootView.findViewById(R.id.tv_bass);
        if (!TextUtils.isEmpty(curEqName) && curEqName.equals(getResources().getString(R.string.jazz))) {
            tv_jazz.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        } else if (!TextUtils.isEmpty(curEqName) && curEqName.equals(getResources().getString(R.string.vocal))) {
            tv_vocal.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        } else if (!TextUtils.isEmpty(curEqName) && curEqName.equals(getResources().getString(R.string.bass))) {
            tv_bass.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
        }
        tv_dragImage = (CustomFontTextView) rootView.findViewById(R.id.tv_dragImage);
        closeImageView = (ImageView) rootView.findViewById(R.id.closeImageView);
        eqGridView = (EqGridView) rootView.findViewById(R.id.eqGridView);
        mScrollView = rootView.findViewById(R.id.scrollview);
        mEqArcView = rootView.findViewById(R.id.eqArcView);
        tv_dragImage = rootView.findViewById(R.id.tv_dragImage);
        eqGridView.setmEqArcView(mEqArcView);
        eqGridView.setScrollView(mScrollView);
        eqGridView.setmTVDragImage(tv_dragImage);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        Logger.d(TAG,"height:"+String.valueOf(screenHeight));


        /*tv_dragImage.setOnTouchListener(new View.OnTouchListener() {//设置按钮被触摸的时间

            int lastX, lastY; // 记录移动的最后的位置

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int ea = event.getAction();//获取事件类型
                switch (ea) {
                    case MotionEvent.ACTION_DOWN: // 按下事件

                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        downX = lastX;
                        downY = lastY;
                        break;

                    case MotionEvent.ACTION_MOVE: // 拖动事件

     // 移动中动态设置位置
                        int dx = (int) event.getRawX() - lastX;//位移量X
                        int dy = (int) event.getRawY() - lastY;//位移量Y
                        int left = v.getLeft() + dx;
                        int top = v.getTop() + dy;
                        int right = v.getRight() + dx;
                        int bottom = v.getBottom() + dy;

     //++限定按钮被拖动的范围
                        if (left < 0) {

                            left = 0;
                            right = left + v.getWidth();

                        }
                        if (right > screenWidth) {

                            right = screenWidth;
                            left = right - v.getWidth();

                        }
                        if (top < 0) {

                            top = 0;
                            bottom = top + v.getHeight();

                        }
                        if (bottom > screenHeight) {

                            bottom = screenHeight;
                            top = bottom - v.getHeight();

                        }

      //--限定按钮被拖动的范围

                        v.layout(left, top, right, bottom);//按钮重画


      // 记录当前的位置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP: // 弹起事件

      //判断是单击事件或是拖动事件，位移量大于5则断定为拖动事件

                        if (Math.abs((int) (event.getRawX() - downX)) > 5
                                || Math.abs((int) (event.getRawY() - downY)) > 5)

                            clickormove = false;

                        else

                            clickormove = true;

                        break;

                }
                return false;

            }

        });*/

        /*tv_dragImage.setOnClickListener(new View.OnClickListener() {//设置按钮被点击的监听器

            @Override
            public void onClick(View arg0) {
                if (clickormove)

                    ToastUtil.ToastLong(getContext(),"single click");

            }

        });*/
    }




    private void initEvent() {
        closeImageView.setOnClickListener(this);

    }

    private void initValue() {
        eqModelList = EQSettingManager.get().getCompleteEQList(mContext);
        if (eqModelList != null && eqModelList.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                eqModelList.remove(0);
            }
        }
        Logger.d(TAG, "size:" + eqModelList.size());
        adapter = new EqGridViewAdapter();
        adapter.setEqModels(eqModelList, lightX);
        eqGridView.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageView:
                getActivity().onBackPressed();
                break;
        }

    }

}
