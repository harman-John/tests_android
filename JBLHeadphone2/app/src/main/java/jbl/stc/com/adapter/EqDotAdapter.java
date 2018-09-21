package jbl.stc.com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.utils.UiUtils;

public class EqDotAdapter extends RecyclerView.Adapter {
    private List<EQModel> eqModels = new ArrayList<>();
    private int curIndex;

    public void setEqModels(List<EQModel> models, int curIndex) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        this.curIndex = curIndex;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_eq_idot, null);
        return new EqDotAdapter.EqRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((EqDotAdapter.EqRecyclerViewHolder) holder).setDataToView(position);
    }

    @Override
    public int getItemCount() {
        return eqModels.size();
    }

    private class EqRecyclerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private LinearLayout ll_dotview;

        public EqRecyclerViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            ll_dotview = itemView.findViewById(R.id.ll_dotview);
        }

        public void setDataToView(int position) {
            Context context = itemView.getContext();
            if (Math.abs(position - curIndex) == 0) {
                imageView.setAlpha(1f);
            } else if (Math.abs(position - curIndex) == 1 || Math.abs(position - curIndex) == 2) {
                imageView.setAlpha(0.4f);
            } else if (Math.abs(position - curIndex) == 3) {
                imageView.setAlpha(0.2f);
            } else if (Math.abs(position - curIndex) == 4) {
                imageView.setAlpha(0.1f);
            } else {
                imageView.setAlpha(0f);
            }
            int padding = UiUtils.dip2px(context, 3);
            imageView.setImageResource(R.drawable.eq_choose_small_circle);
            if (position == 0) {
                ll_dotview.setPadding(0, 0, padding, 0);
            } else if (position == eqModels.size() - 1) {
                ll_dotview.setPadding(padding, 0, 0, 0);
            } else {
                ll_dotview.setPadding(padding, 0, padding, 0);
            }
        }
    }
}
