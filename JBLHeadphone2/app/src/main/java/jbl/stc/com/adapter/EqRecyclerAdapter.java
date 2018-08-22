package jbl.stc.com.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.avnera.smartdigitalheadset.Logger;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnEqItemSelectedListener;
import jbl.stc.com.utils.UiUtils;

/**
 * EqRecyclerAdapter
 * Created by ludm1 on 2018/3/30.
 */
public class EqRecyclerAdapter extends RecyclerView.Adapter {
    private static final String TAG = EqRecyclerAdapter.class.getSimpleName();
    private List<EQModel> eqModels = new ArrayList<>();
    private OnEqItemSelectedListener onEqItemSelectedListener;
    private int lastAnimatedPosition = -1;
    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;
    private boolean mIsEnterAnimation = false;
    private boolean mIsExitAnimation = false;
    private RecyclerView mRecyclerView;
    private int screenHeight;

    public void setEqModels(List<EQModel> models, boolean mIsEnterAnimation, boolean mIsExitAnimation, RecyclerView recyclerView) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        this.mIsEnterAnimation = mIsEnterAnimation;
        if (mIsEnterAnimation) {
            lastAnimatedPosition = -1;
            animationsLocked = false;
            delayEnterAnimation = true;
        }
        this.mIsExitAnimation = mIsExitAnimation;
        this.mRecyclerView = recyclerView;
        notifyDataSetChanged();
    }

    public void setOnEqSelectedListener(OnEqItemSelectedListener onEqItemSelectedListener) {
        this.onEqItemSelectedListener = onEqItemSelectedListener;
    }

    public void setSelectedIndex(int index) {
        if (onEqItemSelectedListener != null) {
            onEqItemSelectedListener.onSelected(index);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_eq_name_grid, null);
        return new EqRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((EqRecyclerViewHolder) holder).setDataToView(eqModels.get(position), position);
    }

    @Override
    public int getItemCount() {
        return eqModels.size();
    }


    private class EqRecyclerViewHolder extends RecyclerView.ViewHolder {
        private ImageView eqNameBgImage;
        private TextView eqNameText;
        private View eqNameLayout;
        private int currPosition;

        public EqRecyclerViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onEqItemSelectedListener != null) {
                        onEqItemSelectedListener.onSelected(currPosition);
                    }
                }
            });
            eqNameBgImage = itemView.findViewById(R.id.eqNameBgImage);
            eqNameText = itemView.findViewById(R.id.text_view_grid_eq_name);
            eqNameLayout = itemView.findViewById(R.id.eqNameLayout);
        }

        public void setDataToView(EQModel eqModel, int position) {
            currPosition = position;
            Context context = itemView.getContext();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            DisplayMetrics dm = itemView.getContext().getResources().getDisplayMetrics();
            screenHeight = dm.heightPixels;


            eqNameText.setText(eqModel.eqName);
            eqNameText.setVisibility(View.VISIBLE);
            if (eqModel.isSelected) {
                if (eqModel.eqName.equals(context.getResources().getString(R.string.off))) {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_off_eq_name_bg_selected);
                } else {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_selected);
                }
                eqNameText.setTextColor(ContextCompat.getColor(context, R.color.white));

            } else {
                if (eqModel.eqName.equals(context.getResources().getString(R.string.off))) {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_off_eq_name_bg_normal);
                } else {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_normal);
                }
                eqNameText.setTextColor(ContextCompat.getColor(context, R.color.statusBarBackground));
            }

            if (position < 2) {//0 or 1 ,row one
                layoutParams.topMargin = UiUtils.dip2px(context, 20);
            } else {
                layoutParams.topMargin = UiUtils.dip2px(context, 0);
            }
            eqNameLayout.setLayoutParams(layoutParams);
            if (mIsEnterAnimation) {
                runEnterAnimation(itemView, position);
            }

            if (mIsExitAnimation) {
                runExitAnimation(itemView, position);
            }
        }
    }

    private void runEnterAnimation(View view, int position) {
        if (animationsLocked) return;
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(800);
            view.setAlpha(0.8f);
            view.animate()
                    .translationY(0).alpha(1.f)
                    .setStartDelay(delayEnterAnimation ? 40 * (position) : 0)
                    .setInterpolator(new DecelerateInterpolator(0.5f))
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true;
                        }
                    })
                    .start();
        }
    }

    private void runExitAnimation(View view, final int position) {
        view.setTranslationY(0);
        view.setAlpha(1f);
        view.animate()
                .translationY(800).alpha(0.8f)
                .setStartDelay(40 * (eqModels.size() - position))
                .setInterpolator(new DecelerateInterpolator(0.5f))
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (position == 0) {
                            mRecyclerView.setVisibility(View.GONE);
                        }
                    }
                })
                .start();

    }


}
