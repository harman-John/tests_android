package jbl.stc.com.listener;


import jbl.stc.com.entity.EQModel;

/**
 * OnCustomEqListener
 * Created by darren.lu on 2017/8/24.
 */

public interface OnCustomEqListener {
    void onCustomEqResult(EQModel eqModel, boolean isAdd);
}
