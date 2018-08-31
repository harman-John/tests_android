package jbl.stc.com.listener;


import jbl.stc.com.entity.EQModel;


public interface OnCustomEqListener {
    void onCustomEqResult(EQModel eqModel, boolean isAdd);
}
