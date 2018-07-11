package jbl.stc.com.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;

public class EqGridViewAdapter extends BaseAdapter {
    private static final String TAG = EqNameGridAdapter.class.getSimpleName();
    private List<EQModel> eqModels = new ArrayList<>();
    private List<String> eqIndexs=new ArrayList<>();
    public void setEqModels(List<EQModel> models) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        notifyDataSetChanged();
    }
    public List<String> getEqIndexs(){
        return  eqIndexs;
    }
    @Override
    public int getCount() {
        return eqModels.size();
    }

    @Override
    public Object getItem(int position) {
        return eqModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        Context context = parent.getContext();
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_eq_more_setting_grid, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_eqname = (TextView) convertView.findViewById(R.id.tv_eqname);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final EQModel eqModel =eqModels.get(position);
        viewHolder.tv_eqname.setText(eqModel.eqName);
        viewHolder.tv_eqname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eqModel.isSelected){
                    eqModel.isSelected=false;
                    viewHolder.tv_eqname.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_normal);
                    if (eqIndexs.contains(String.valueOf(position))){
                        eqIndexs.remove(String.valueOf(position));
                    }
                }else{
                    eqModel.isSelected=true;
                    viewHolder.tv_eqname.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
                    if (!eqIndexs.contains(String.valueOf(position))){
                        eqIndexs.add(String.valueOf(position));
                    }
                }

            }
        });
        return convertView;
    }

    private class ViewHolder{
      private TextView  tv_eqname;
    }
}
