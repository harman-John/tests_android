package jbl.stc.com.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.listener.OnDeleteEqListener;
import jbl.stc.com.utils.UiUtils;

public class EqNameGridAdapter extends BaseAdapter {
    private static final String TAG = EqNameGridAdapter.class.getSimpleName();
    private List<EQModel> eqModels = new ArrayList<>();
    private boolean mCanRemove = false;
    private OnDeleteEqListener onDeleteEqListener;
    private int hidePosition = AdapterView.INVALID_POSITION;

    public void setEqModels(List<EQModel> models) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        notifyDataSetChanged();
    }

    public void setCanRemove(boolean canRemove) {
        mCanRemove = canRemove;
    }

    public void setOnDeleteEqListener(OnDeleteEqListener onDeleteEqListener) {
        this.onDeleteEqListener = onDeleteEqListener;
    }

    @Override
    public int getCount() {
        return eqModels.size();
    }

    @Override
    public EQModel getItem(int position) {
        return eqModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Context context = parent.getContext();
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_eq_name_grid, null);
            viewHolder = new ViewHolder();
            viewHolder.eqNameBgImage = (ImageView) convertView.findViewById(R.id.eqNameBgImage);
            viewHolder.eqNameText = (TextView) convertView.findViewById(R.id.eqNameText);
            viewHolder.plusImageView = (ImageView) convertView.findViewById(R.id.plusImageView);
            viewHolder.removeImageView = (ImageView) convertView.findViewById(R.id.removeImageView);
            viewHolder.eqNameLayout = convertView.findViewById(R.id.eqNameLayout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        EQModel eqModel = getItem(position);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (eqModel.isPlusItem) {
            viewHolder.eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_normal);
            viewHolder.eqNameText.setVisibility(View.GONE);
            viewHolder.plusImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.eqNameText.setText(eqModel.eqName);
            viewHolder.eqNameText.setVisibility(View.VISIBLE);
            viewHolder.plusImageView.setVisibility(View.GONE);
            if (eqModel.isSelected) {
                viewHolder.eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_selected);
                viewHolder.eqNameText.setTextColor(ContextCompat.getColor(context, R.color.eq_panel_name_text));
            } else {
                viewHolder.eqNameBgImage.setImageResource(R.drawable.shape_circle_eq_name_bg_normal);
                viewHolder.eqNameText.setTextColor(ContextCompat.getColor(context, R.color.white));
            }
        }
        if (position < 2) {//0 or 1 ,row one
            layoutParams.topMargin = UiUtils.dip2px(context, 20);
        } else {
            layoutParams.topMargin = UiUtils.dip2px(context, 0);
        }
        viewHolder.eqNameLayout.setLayoutParams(layoutParams);

        if (mCanRemove && eqModel.isCustomEq) {
            viewHolder.removeImageView.setVisibility(View.VISIBLE);
            setViewOnClickListener(viewHolder.removeImageView, position);
        } else {
            viewHolder.removeImageView.setVisibility(View.GONE);
        }
        //Log.d(TAG, "getView position=" + position + "," + eqModel);
        if (position == hidePosition) {
            convertView.setVisibility(View.INVISIBLE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    private void setViewOnClickListener(final View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteEqListener != null) {
                    onDeleteEqListener.onDeleted(position);
                }
            }
        });
    }

    public void hideView(int pos) {
        hidePosition = pos;
        notifyDataSetChanged();
    }

    public void showHideView() {
        hidePosition = AdapterView.INVALID_POSITION;
        notifyDataSetChanged();
    }

    //更新拖动时的gridView
    public void swapView(int originalPosition, int nowPosition) {
        Log.d(TAG, "swapView originalPosition=" + originalPosition + ",nowPosition=" + nowPosition);
        //从前向后拖动，其他item依次前移
        if (originalPosition < nowPosition) {
            eqModels.add(nowPosition + 1, getItem(originalPosition));
            eqModels.remove(originalPosition);
        }
        //从后向前拖动，其他item依次后移
        else if (originalPosition > nowPosition) {
            eqModels.add(nowPosition, getItem(originalPosition));
            eqModels.remove(originalPosition + 1);
        }
        hidePosition = nowPosition;
        notifyDataSetChanged();
    }

    public void exchangePosition(int originalPosition, int nowPosition, boolean isMove) {
        Log.d(TAG, "exchangePosition originalPosition=" + originalPosition + ",nowPosition=" + nowPosition);
        EQModel t = eqModels.get(originalPosition);
        eqModels.remove(originalPosition);
        eqModels.add(nowPosition, t);
        hidePosition = nowPosition;
        for (int i = 0; i < eqModels.size(); i++) {
            Log.d(TAG, "i=" + i + "," + eqModels.get(i));
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private ImageView eqNameBgImage;
        private TextView eqNameText;
        private ImageView plusImageView;
        private View eqNameLayout;
        private ImageView removeImageView;
    }
}
