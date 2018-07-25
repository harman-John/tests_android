package jbl.stc.com.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.avnera.smartdigitalheadset.LightX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.manager.ANCControlManager;
import jbl.stc.com.manager.EQSettingManager;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.ToastUtil;
import jbl.stc.com.view.DragGridView;
import jbl.stc.com.view.EqGridView;

public class EqGridViewAdapter extends BaseAdapter implements EqGridView.DragGridBaseAdapter{
    private static final String TAG = EqGridViewAdapter.class.getSimpleName();
    private List<EQModel> eqModels = new ArrayList<>();
    public int mHidePosition = -1;
    private Context context;
    private LightX lightX;
    public void setEqModels(List<EQModel> models, LightX lightX) {
        this.eqModels.clear();
        this.eqModels.addAll(models);
        this.lightX=lightX;
        notifyDataSetChanged();
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
        context = parent.getContext();
        String curEqName= PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, context, "");
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_eq_more_setting_grid, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_eqname = (TextView) convertView.findViewById(R.id.tv_eqname);
            viewHolder.image_view=(ImageView) convertView.findViewById(R.id.image_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final EQModel eqModel =eqModels.get(position);
        viewHolder.tv_eqname.setText(eqModel.eqName);
        if (!TextUtils.isEmpty(curEqName)&&curEqName.equals(eqModel.eqName)){
            viewHolder.image_view.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_selected);
            viewHolder.tv_eqname.setTextColor(context.getResources().getColor(R.color.white));
        }else{
            viewHolder.image_view.setBackgroundResource(R.drawable.shape_circle_eq_name_bg_normal);
            viewHolder.tv_eqname.setTextColor(context.getResources().getColor(R.color.eq_panel_name_text));
        }
        //隐藏被拖动的
        if (position == mHidePosition) {
            convertView.setVisibility(View.INVISIBLE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public void reorderItems(int oldPosition, int newPosition) {
         if(oldPosition < 0) return;

        EQModel eqModel = eqModels.get(oldPosition);
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(eqModels, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(eqModels, i, i - 1);
            }
        }

        eqModels.set(newPosition, eqModel);
    }

    @Override
    public void setHideItem(int hidePosition) {
        mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    @Override
    public void deleteItem(int deletePosition) {
        String deleteEqName=eqModels.get(deletePosition).eqName;
        String curEqName= PreferenceUtils.getString(PreferenceKeys.CURR_EQ_NAME, context, "");
          if (null != eqModels &&eqModels.size()>0&& deletePosition < eqModels.size()) {
                EQSettingManager.OperationStatus operationStatus = EQSettingManager.get().deleteEQ(eqModels.get(deletePosition).eqName,context);
                eqModels.remove(deletePosition);
              if (!TextUtils.isEmpty(curEqName)&&!(curEqName.equals(context.getResources().getString(R.string.off)))&&
                      !(curEqName.equals(context.getResources().getString(R.string.jazz)))
                      &&!(curEqName.equals(context.getResources().getString(R.string.vocal)))&&
                      !(curEqName.equals(context.getResources().getString(R.string.bass)))){
                  if (operationStatus == EQSettingManager.OperationStatus.DELETED) {
                          if (eqModels!=null&&eqModels.size()>0){
                              if (curEqName.equals(deleteEqName)){
                              int[] eqValueArray = EQSettingManager.get().getValuesFromEQModel(eqModels.get(0));
                              ANCControlManager.getANCManager(context).applyPresetsWithBand(GraphicEQPreset.User, eqValueArray, lightX);
                              PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME,eqModels.get(0).eqName,context);
                              PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF, eqModels.get(0).eqName, context);
                             }
                          }else{
                              ANCControlManager.getANCManager(context).applyPresetWithoutBand(GraphicEQPreset.Off, lightX);
                              PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME,context.getResources().getString(R.string.off),context);
                              PreferenceUtils.setString(PreferenceKeys.CURR_EQ_NAME_EXCLUSIVE_OFF,context.getResources().getString(R.string.jazz),context);

                      }
                  }
              }

              notifyDataSetChanged();
            }


    }

    private class ViewHolder{
      private TextView  tv_eqname;
      private ImageView image_view;
    }



}
