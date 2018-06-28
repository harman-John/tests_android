package jbl.stc.com.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


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
    private static final String TAG = EqNameGridAdapter.class.getSimpleName();
    private List<EQModel> eqModels = new ArrayList<>();
    private OnEqItemSelectedListener onEqItemSelectedListener;

    public void setEqModels(List<EQModel> models) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        notifyDataSetChanged();
    }

    public void setOnEqSelectedListener(OnEqItemSelectedListener onEqItemSelectedListener) {
        this.onEqItemSelectedListener = onEqItemSelectedListener;
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
        private ImageView plusImageView;
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
            eqNameText = itemView.findViewById(R.id.eqNameText);
            plusImageView = itemView.findViewById(R.id.plusImageView);
            eqNameLayout = itemView.findViewById(R.id.eqNameLayout);
        }

        public void setDataToView(EQModel eqModel, int position) {
            currPosition = position;
            Context context = itemView.getContext();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (eqModel.isPlusItem) {
                eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_normal);
                eqNameText.setVisibility(View.GONE);
                plusImageView.setVisibility(View.VISIBLE);
            } else {
                if (position == 0) {
                    eqNameText.setText("OFF");
                } else if (position == 1) {
                    eqNameText.setText("JAZZ");
                } else if (position == 2) {
                    eqNameText.setText("VOCAL");
                } else if (position == 3) {
                    eqNameText.setText("BASS");
                }else{
                    eqNameText.setText(eqModel.eqName);
                }
                eqNameText.setVisibility(View.VISIBLE);
                plusImageView.setVisibility(View.GONE);
                if (eqModel.isSelected) {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_selected);
                    eqNameText.setTextColor(ContextCompat.getColor(context, R.color.eq_panel_name_text));
                } else {
                    eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_normal);
                    eqNameText.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
            }
            if (position < 2) {//0 or 1 ,row one
                layoutParams.topMargin = UiUtils.dip2px(context, 20);
            } else {
                layoutParams.topMargin = UiUtils.dip2px(context, 0);
            }
            eqNameLayout.setLayoutParams(layoutParams);
        }
    }

}
