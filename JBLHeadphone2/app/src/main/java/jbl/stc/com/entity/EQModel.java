package jbl.stc.com.entity;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.avnera.smartdigitalheadset.GraphicEQPreset;

import java.io.Serializable;
import java.util.Arrays;

public class EQModel implements Serializable, Comparable<EQModel> {
    public static final String POINT_X_Y_SEP = ",";
    public int eqType;//0:off 1:Jazz 2:Vocal 3:Bass 4:USER(Custom)
    public String eqName = "";
    public int id;
    public int index;
    public float value_32;
    public float value_64;
    public float value_125;
    public float value_250;
    public float value_500;
    public float value_1000;
    public float value_2000;
    public float value_4000;
    public float value_8000;
    public float value_16000;
    private float[] pointX;
    private float[] pointY;
    public String deviceName;

    public boolean isSelected = false;
    public boolean isPlusItem = false;
    public boolean isCustomEq = false;

    public EQModel() {
        pointX = new float[]{32, 16000};
        pointY = new float[]{0, 0};
    }

    public EQModel(boolean isPlusItem) {
        this.isPlusItem = isPlusItem;
        this.isCustomEq = false;
        pointX = new float[]{32, 16000};
        pointY = new float[]{0, 0};
    }

    public EQModel(GraphicEQPreset eqType) {
        this.eqType = eqType.value();
        this.isCustomEq = false;
        pointX = new float[]{32, 16000};
        pointY = new float[]{0, 0};
    }

    public EQModel(GraphicEQPreset eqType, String noEqName) {
        this.eqType = eqType.value();
        this.isCustomEq = false;
        this.eqName = noEqName;
        pointX = new float[]{32, 16000};
        pointY = new float[]{0, 0};
    }

    public String getPointXString() {
        if (pointX == null) {
            return "";
        }
        String xStr = "";
        for (float x : pointX) {
            xStr += POINT_X_Y_SEP + x;
        }
        if (xStr.startsWith(POINT_X_Y_SEP)) {
            xStr = xStr.substring(1);
        }
        return xStr;
    }

    public String getPointYString() {
        if (pointY == null) {
            return "";
        }
        String yStr = "";
        for (float x : pointY) {
            yStr += POINT_X_Y_SEP + x;
        }
        if (yStr.startsWith(POINT_X_Y_SEP)) {
            yStr = yStr.substring(1);
        }
        return yStr;
    }

    public float[] getPointX() {
        return pointX;
    }

    public float[] getPointY() {
        return pointY;
    }

    public void setPointX(float[] pointX) {
        this.pointX = pointX;
    }

    public void setPointY(float[] pointY) {
        this.pointY = pointY;
    }

    public void setPointXFromStr(String pointXString) {
        if (TextUtils.isEmpty(pointXString)) {
            pointX = new float[]{32, 16000};
            return;
        }
        String[] points = pointXString.split(POINT_X_Y_SEP);
        if (points.length == 0) {
            pointX = new float[]{32, 16000};
        } else {
            try {
                pointX = new float[points.length];
                for (int i = 0; i < points.length; i++) {
                    pointX[i] = Float.parseFloat(points[i]);
                }
            } catch (Exception e) {
                pointX = new float[]{32, 16000};
                pointY = new float[]{0, 0};
            }
        }
    }

    public void setPointYFromStr(String pointYString) {
        if (TextUtils.isEmpty(pointYString)) {
            pointY = new float[]{0, 0};
            return;
        }
        String[] points = pointYString.split(POINT_X_Y_SEP);
        if (points.length == 0) {
            pointY = new float[]{0, 0};
        } else {
            try {
                pointY = new float[points.length];
                for (int i = 0; i < points.length; i++) {
                    pointY[i] = Float.parseFloat(points[i]);
                }
            } catch (Exception e) {
                pointX = new float[]{32, 16000};
                pointY = new float[]{0, 0};
            }
        }
    }

    public EQModel clone() {
        EQModel eqModel = new EQModel();
        eqModel.eqName = this.eqName;
        eqModel.eqType = this.eqType;
        eqModel.id = this.id;
        eqModel.index = this.index;
        eqModel.value_32 = this.value_32;
        eqModel.value_64 = this.value_64;
        eqModel.value_125 = this.value_125;
        eqModel.value_250 = this.value_250;
        eqModel.value_500 = this.value_500;
        eqModel.value_1000 = this.value_1000;
        eqModel.value_2000 = this.value_2000;
        eqModel.value_4000 = this.value_4000;
        eqModel.value_8000 = this.value_8000;
        eqModel.value_16000 = this.value_16000;
        eqModel.isSelected = this.isSelected;
        eqModel.isPlusItem = this.isPlusItem;
        eqModel.pointX = Arrays.copyOf(this.pointX, this.pointX.length);
        eqModel.pointY = Arrays.copyOf(this.pointY, this.pointY.length);
        eqModel.deviceName = this.deviceName;
        return eqModel;
    }

    @Override
    public String toString() {
        return "EQModel{" +
                "eqType=" + eqType +
                ", eqName='" + eqName + '\'' +
                ", id=" + id +
                ", index=" + index +
                ", value_32=" + value_32 +
                ", value_64=" + value_64 +
                ", value_125=" + value_125 +
                ", value_250=" + value_250 +
                ", value_500=" + value_500 +
                ", value_1000=" + value_1000 +
                ", value_2000=" + value_2000 +
                ", value_4000=" + value_4000 +
                ", value_8000=" + value_8000 +
                ", value_16000=" + value_16000 +
                ", pointX=" + Arrays.toString(pointX) +
                ", pointY=" + Arrays.toString(pointY) +
                ", isSelected=" + isSelected +
                ", isPlusItem=" + isPlusItem +
                ", isCustomEq=" + isCustomEq +
                ", deviceName=" + deviceName +
                '}';
    }

    @Override
    public int compareTo(@NonNull EQModel o) {
        return index - o.index;
    }
}
