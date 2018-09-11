package jbl.stc.com.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.avnera.smartdigitalheadset.GraphicEQPreset;
import com.harman.bluetooth.constants.Band;
import com.harman.bluetooth.constants.EnumEqCategory;
import com.harman.bluetooth.constants.EnumEqPresetIdx;
import com.harman.bluetooth.req.CmdEqSettingsSet;
import com.harman.bluetooth.ret.RetCurrentEQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.activity.HomeActivity;
import jbl.stc.com.constant.EqDbKey;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.logger.Logger;
import jbl.stc.com.storage.DatabaseHelper;
import jbl.stc.com.storage.PreferenceKeys;
import jbl.stc.com.storage.PreferenceUtils;
import jbl.stc.com.utils.AppUtils;
import jbl.stc.com.utils.SharePreferenceUtil;


public class EQSettingManager implements EqDbKey {
    private static final String TAG = EQSettingManager.class.getSimpleName();
    private static final EQSettingManager eqSettingManager = new EQSettingManager();
    public static float[] mEqFreqArray = new float[]{32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};

    public enum OperationStatus {
        INSERTED, UPDATED, FAILED, EXISTED, DELETED
    }

    private EQSettingManager() {
    }

    /**
     * Don't pass mContext object as null.
     *
     * @return EQSettingManager
     */
    public static EQSettingManager get() {
        return eqSettingManager;
    }

    /**
     * Insert custom EQ setting to Database.
     *
     * @return Status
     */
    public OperationStatus addCustomEQ(EQModel eqModel, Context mContext) {
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        OperationStatus operationStatus = OperationStatus.FAILED;
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getWritableDatabase();
            Cursor cursor = db.query(DatabaseHelper.AKG_EQ, null, DatabaseHelper.EQ_NAME + "=? and " + DatabaseHelper.DEVICE_NAME + "=?", new String[]{eqModel.eqName, deviceName}, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                operationStatus = OperationStatus.EXISTED;
                cursor.close();
            } else {
                //long i = db.insert(DatabaseHelper.AKG_EQ, null, getContentValueFromModel(eqModel,mContext));
                long i = db.replace(DatabaseHelper.AKG_EQ, null, getContentValueFromModel(eqModel, mContext));
                if (i != -1) {
                    operationStatus = OperationStatus.INSERTED;
                } else {
                    operationStatus = OperationStatus.FAILED;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return operationStatus;
    }

    /**
     * Update Custom EQ setting in database.
     *
     * @param updateEqName String as base name to Update.
     * @return Status.
     */
    public OperationStatus updateCustomEQ(EQModel eqModel, String updateEqName, Context mContext) {
        OperationStatus operationStatus = OperationStatus.FAILED;
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getWritableDatabase();
            long i = db.update(DatabaseHelper.AKG_EQ, getContentValueFromModel(eqModel, mContext), DatabaseHelper.EQ_NAME + "=?", new String[]{updateEqName});
            if (i != -1) {
                operationStatus = OperationStatus.UPDATED;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return operationStatus;
    }

    public OperationStatus updateEqIndexByName(int updateIndex, String eqName, Context mContext) {
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        OperationStatus operationStatus = OperationStatus.FAILED;
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getWritableDatabase();
            long i = db.update(DatabaseHelper.AKG_EQ, getContentValueFromModelIndex(updateIndex), DatabaseHelper.EQ_NAME + "=? and " + DatabaseHelper.DEVICE_NAME + "=?", new String[]{eqName, deviceName});
            if (i != -1) {
                operationStatus = OperationStatus.UPDATED;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return operationStatus;
    }

    /**
     * Delete custom EQ setting from database.
     *
     * @param eqName String as base name to delete.
     * @return Status
     */
    public OperationStatus deleteEQ(String eqName, Context mContext) {
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        OperationStatus operationStatus = OperationStatus.FAILED;
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getWritableDatabase();
            long i = db.delete(DatabaseHelper.AKG_EQ, DatabaseHelper.EQ_NAME + "=? and " + DatabaseHelper.DEVICE_NAME + "=?", new String[]{eqName, deviceName});
            if (i != -1) {
                operationStatus = OperationStatus.DELETED;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return operationStatus;
    }


    /**
     * convert EQModel to ContentValues
     *
     * @return ContentValues
     */
    private ContentValues getContentValueFromModel(EQModel eqModel, Context mContext) {
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.EQ_NAME, eqModel.eqName);
        contentValues.put(DatabaseHelper.EQ_TYPE, eqModel.eqType);
        contentValues.put(DatabaseHelper.ID, eqModel.id);
        contentValues.put(DatabaseHelper.INDEX, eqModel.index);
        contentValues.put(DatabaseHelper.VALUE_32, eqModel.value_32);
        contentValues.put(DatabaseHelper.VALUE_64, eqModel.value_64);
        contentValues.put(DatabaseHelper.VALUE_125, eqModel.value_125);
        contentValues.put(DatabaseHelper.VALUE_250, eqModel.value_250);
        contentValues.put(DatabaseHelper.VALUE_500, eqModel.value_500);
        contentValues.put(DatabaseHelper.VALUE_1000, eqModel.value_1000);
        contentValues.put(DatabaseHelper.VALUE_2000, eqModel.value_2000);
        contentValues.put(DatabaseHelper.VALUE_4000, eqModel.value_4000);
        contentValues.put(DatabaseHelper.VALUE_8000, eqModel.value_8000);
        contentValues.put(DatabaseHelper.VALUE_16000, eqModel.value_16000);
        contentValues.put(DatabaseHelper.POINT_X, eqModel.getPointXString());
        contentValues.put(DatabaseHelper.POINT_Y, eqModel.getPointYString());
        contentValues.put(DatabaseHelper.DEVICE_NAME, deviceName);
        return contentValues;
    }

    private ContentValues getContentValueFromModelIndex(int eqIndex) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.INDEX, eqIndex);
        return contentValues;
    }

    /**
     * Returns complete EQ setting mSet from database.
     *
     * @return EQModel ArrayList
     */
    public ArrayList<EQModel> getCompleteEQList(Context mContext) {
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        ArrayList<EQModel> eQList = new ArrayList<>();
        String selectQuery = "SELECT * from AKG_EQ where " + DEVICE_NAME + "='" + deviceName + "'";
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    EQModel eqModel = getEqModelFromCursor(cursor);
                    eQList.add(eqModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        Collections.sort(eQList);
        return eQList;
    }

    /**
     * Returns EQ Model based on provided name
     *
     * @param name String as base name.
     * @return EQModel
     */
    public EQModel getEQModelByName(String name, Context mContext) {
        EQModel eqModel = null;
        String deviceName = PreferenceUtils.getString(PreferenceKeys.CONNECT_DEVICE_NAME, mContext, AppUtils.BASE_DEVICE_NAME);
        String selectQuery = "SELECT * from AKG_EQ where " + EQ_NAME + "='" + name + "' and " + DEVICE_NAME + "='" + deviceName + "'";
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    eqModel = getEqModelFromCursor(cursor);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return eqModel;
    }

    private EQModel getEqModelFromCursor(Cursor cursor) {
        EQModel eqModel = new EQModel();
        eqModel.eqName = cursor.getString(cursor.getColumnIndex(EQ_NAME));
        eqModel.eqType = cursor.getInt(cursor.getColumnIndex(EQ_TYPE));
        eqModel.id = cursor.getInt(cursor.getColumnIndex(ID));
        eqModel.index = cursor.getInt(cursor.getColumnIndex(INDEX));
        eqModel.value_32 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_32)));
        eqModel.value_64 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_64)));
        eqModel.value_125 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_125)));
        eqModel.value_250 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_250)));
        eqModel.value_500 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_500)));
        eqModel.value_1000 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_1000)));
        eqModel.value_2000 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_2000)));
        eqModel.value_4000 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_4000)));
        eqModel.value_8000 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_8000)));
        eqModel.value_16000 = getFloat(cursor.getString(cursor.getColumnIndex(VALUE_16000)));
        eqModel.setPointXFromStr(cursor.getString(cursor.getColumnIndex(POINT_X)));
        eqModel.setPointYFromStr(cursor.getString(cursor.getColumnIndex(POINT_Y)));
        eqModel.isCustomEq = eqModel.eqType == GraphicEQPreset.User.value();
        eqModel.deviceName = cursor.getString(cursor.getColumnIndex(DEVICE_NAME));
        return eqModel;
    }

    /**
     * Returns complete mSet based on where clause constraint.
     *
     * @param where String
     * @return EQModel ArrayList
     */
    public ArrayList<EQModel> getCompleteListByName(String where, Context mContext) {
        ArrayList<EQModel> eQList = new ArrayList<>();
        String selectQuery = "SELECT * from AKG_EQ where " + EQ_NAME + " like '%" + where + "%'";
        SQLiteDatabase db = null;
        try {
            db = new DatabaseHelper(mContext).getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    EQModel eqModel = getEqModelFromCursor(cursor);
                    eQList.add(eqModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return eQList;
    }

    /**
     * Returns integer parsed from String.
     *
     * @param num String parameter
     * @return int
     */
    private float getFloat(String num) {
        try {
            return Float.valueOf(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * Get EQ values from EQModel
     *
     * @return int array
     */
    public int[] getValuesFromEQModel(EQModel model) {
        int[] array = new int[10];
        if (model != null) {
            array[0] = (int) model.value_32;
            array[1] = (int) model.value_64;
            array[2] = (int) model.value_125;
            array[3] = (int) model.value_250;
            array[4] = (int) model.value_500;
            array[5] = (int) model.value_1000;
            array[6] = (int) model.value_2000;
            array[7] = (int) model.value_4000;
            array[8] = (int) model.value_8000;
            array[9] = (int) model.value_16000;
        }
        Logger.d(TAG, "getValuesFromEQModel:" + Arrays.toString(array));
        return array;
    }

    /**
     * Get EQ values from EQModel
     *
     * @return int array
     */
    public float[] getFloatValuesFromEQModel(EQModel model) {
        float[] array = new float[10];
        if (model != null) {
            array[0] = model.value_32;
            array[1] = model.value_64;
            array[2] = model.value_125;
            array[3] = model.value_250;
            array[4] = model.value_500;
            array[5] = model.value_1000;
            array[6] = model.value_2000;
            array[7] = model.value_4000;
            array[8] = model.value_8000;
            array[9] = model.value_16000;
        }
        return array;
    }

    /**
     * Get EQModel from values
     *
     * @return EQModel
     */
    public EQModel getEQModelFromValues(EQModel model, float[] values) {
        if (values.length == 10) {
            model.value_32 = values[0];
            model.value_64 = values[1];
            model.value_125 = values[2];
            model.value_250 = values[3];
            model.value_500 = values[4];
            model.value_1000 = values[5];
            model.value_2000 = values[6];
            model.value_4000 = values[7];
            model.value_8000 = values[8];
            model.value_16000 = values[9];
        }
        return model;
    }

    public EQModel getCustomeEQModelFromValues(int[] eqArray, String eqName) {
        float[] pointY = new float[10];
        float[] pointX = {32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
        EQModel eqModel = new EQModel();
        eqModel.eqName = eqName;
        eqModel.value_32 = eqArray[0];
        pointY[0] = eqModel.value_32;
        eqModel.value_64 = eqArray[1];
        pointY[1] = eqModel.value_64;
        eqModel.value_125 = eqArray[2];
        pointY[2] = eqModel.value_125;
        eqModel.value_250 = eqArray[3];
        pointY[3] = eqModel.value_250;
        eqModel.value_500 = eqArray[4];
        pointY[4] = eqModel.value_500;
        eqModel.value_1000 = eqArray[5];
        pointY[5] = eqModel.value_1000;
        eqModel.value_2000 = eqArray[6];
        pointY[6] = eqModel.value_2000;
        eqModel.value_4000 = eqArray[7];
        pointY[7] = eqModel.value_4000;
        eqModel.value_8000 = eqArray[8];
        pointY[8] = eqModel.value_8000;
        eqModel.value_16000 = eqArray[9];
        pointY[9] = eqModel.value_16000;
        eqModel.setPointY(pointY);
        eqModel.setPointX(pointX);
        eqModel.eqType = 4;
        return eqModel;
    }

    public boolean isTheSameEQ(EQModel eqModel, int[] eqArray) {
        boolean isSame = false;
        if (eqModel.value_32 == eqArray[0] && eqModel.value_64 == eqArray[1] && eqModel.value_125 == eqArray[2] && eqModel.value_250 == eqArray[3]
                && eqModel.value_500 == eqArray[4] && eqModel.value_1000 == eqArray[5] && eqModel.value_2000 == eqArray[6] && eqModel.value_4000 == eqArray[7]
                && eqModel.value_8000 == eqArray[8] && eqModel.value_16000 == eqArray[9]) {
            isSame = true;
        }
        return isSame;
    }

    public EQModel getEQModelFromValues(EQModel model, int[] values) {
        if (values.length == 10) {
            model.value_32 = values[0];
            model.value_64 = values[1];
            model.value_125 = values[2];
            model.value_250 = values[3];
            model.value_500 = values[4];
            model.value_1000 = values[5];
            model.value_2000 = values[6];
            model.value_4000 = values[7];
            model.value_8000 = values[8];
            model.value_16000 = values[9];
        } else if (values.length == 12) {
            model.value_32 = values[2];
            model.value_64 = values[3];
            model.value_125 = values[4];
            model.value_250 = values[5];
            model.value_500 = values[6];
            model.value_1000 = values[7];
            model.value_2000 = values[8];
            model.value_4000 = values[9];
            model.value_8000 = values[10];
            model.value_16000 = values[11];
        }
        return model;
    }

    public boolean isEqModelValuesEquals(EQModel model, int[] gainValues) {
        boolean equals = true;
        int[] eqValues = getValuesFromEQModel(model);
        if (gainValues.length != eqValues.length + 2) {
            equals = false;
        } else {
            for (int i = 0; i < eqValues.length; i++) {
                if (eqValues[i] != gainValues[i + 2]) {
                    equals = false;
                    break;
                }
            }
        }
        Logger.d(TAG, "isEqModelValuesEquals:equals=" + equals);
        return equals;
    }

    public CmdEqSettingsSet getBleEqSettingFromEqModel(EQModel eqModel, Context context) {
        CmdEqSettingsSet cmdEqSettingsSet = null;
        List<RetCurrentEQ> retCurrentEQList = SharePreferenceUtil.readCurrentEqSet(context, SharePreferenceUtil.BLE_DESIGN_EQ);
        if (retCurrentEQList != null && retCurrentEQList.size() > 0) {
            RetCurrentEQ bleDesignEq = retCurrentEQList.get(0);
            int packageIndex = 4;
            int presetIndex = EnumEqPresetIdx.USER.ordinal();
            EnumEqCategory eqCATEGORY = bleDesignEq.enumEqCategory;
            int sampleRate = bleDesignEq.sampleRate;
            float gain0 = bleDesignEq.gain0;
            float gain1 = bleDesignEq.gain1;
            Band[] bleDesignBands = bleDesignEq.bands;
            int bandType = bleDesignBands[0].type;
            float qValue = bleDesignBands[0].q;
            float[] pointX = eqModel.getPointX();
            float[] pointY = eqModel.getPointY();
            Band[] bands = new Band[pointX.length];
            for (int i = 0; i < pointX.length; i++) {
                bands[i] = new Band(bandType, pointY[i], pointX[i], qValue);
            }
            cmdEqSettingsSet = new CmdEqSettingsSet(packageIndex, presetIndex, eqCATEGORY, 0f, sampleRate, gain0, gain1, bands);
            float calib = DashboardActivity.getDashboardActivity().getCalib(cmdEqSettingsSet);
            cmdEqSettingsSet.setCalib(calib);
        }

        return cmdEqSettingsSet;
    }

    public static String getNewEqName(List<EQModel> eqModelList, String eqName, String updateEqName) {

        Logger.d(TAG, "getNewEqName eqName=" + eqName + "updateEqName:" + updateEqName);
        String newEqName;
        if (eqName.equals(updateEqName) || (eqModelList == null || eqModelList.isEmpty())) {
            return eqName;
        }

        boolean existed = false;
        for (EQModel eqModel : eqModelList) {
            if (eqName.equals(eqModel.eqName)) {
                existed = true;
                break;
            }
        }
        Logger.d(TAG, "getNewEqName existed=" + existed);
        if (!existed) {
            return eqName;
        }

        int lastEqNum = getLastEqNum(eqName);
        int lastEqNum2 = getLastEqNum(updateEqName);
        String tempName = getOriginalEqName(eqName, lastEqNum);
        Logger.d(TAG, "getNewEqName lastEqNum1=" + lastEqNum);
        List<Integer> eqNumList = new ArrayList<>();
        for (EQModel eqModel : eqModelList) {
            int eqNum = getLastEqNum(eqModel.eqName);
            String modelTempName = getOriginalEqName(eqModel.eqName, eqNum);
            if (!modelTempName.equals(tempName)) {
                continue;
            }
            if (eqNum == lastEqNum2) {
                continue;
            }
            eqNumList.add(eqNum);
            if (eqNum > lastEqNum) {
                lastEqNum = eqNum;
            }
        }
        Logger.d(TAG, "getNewEqName lastEqNum2=" + lastEqNum + ",tempName=" + tempName);
        if (lastEqNum == 0) {
            lastEqNum = 2;
            newEqName = tempName + AppUtils.EQ_NAME_AND_NUM_SEPARATE + lastEqNum;
        } else {
            int newEqNum = 0;
            for (int i = 2; i < lastEqNum; i++) {
                boolean numExisted = false;
                for (int j = 0; j < eqNumList.size(); j++) {
                    if (eqNumList.get(j) == i) {
                        numExisted = true;
                        break;
                    }
                }
                if (!numExisted) {
                    newEqNum = i;
                    break;
                }
            }

            if (newEqNum == 0) {
                newEqNum = lastEqNum + 1;
            }
            lastEqNum = newEqNum;
            newEqName = tempName + AppUtils.EQ_NAME_AND_NUM_SEPARATE + newEqNum;
        }
        Logger.d(TAG, "getNewEqName newEqName=" + newEqName + ",newEqNum=" + lastEqNum);
//        if (newEqName.equals(updateEqName)) {
//            return newEqName;
//        }
//        lastEqNum++;
//        newEqName = tempName + AppUtils.EQ_NAME_AND_NUM_SEPARATE + lastEqNum;
        Logger.d(TAG, "getNewEqName newEqName=" + newEqName + ",eqName=" + eqName + ",updateEqName=" + updateEqName + ",lastEqNum4=" + lastEqNum);
        return newEqName;
    }

    private static String getOriginalEqName(String eqName, int lastIndex) {
        String originalName = eqName;
        if (lastIndex > 0) {
            originalName = eqName.substring(0, eqName.lastIndexOf(AppUtils.EQ_NAME_AND_NUM_SEPARATE));
        }
        //Logger.d(TAG, "getOriginalEqName eqName=" + eqName + ",lastIndex=" + lastIndex + ",originalName=" + originalName);
        return originalName;
    }

    private static int getLastEqNum(String eqName) {
        try {
            String lastNumStr = eqName.substring(eqName.lastIndexOf(AppUtils.EQ_NAME_AND_NUM_SEPARATE) + 1);
            //Logger.d(TAG, "getLastIndex eqName=" + eqName + ",lastNumStr=" + lastNumStr);
            return Integer.valueOf(lastNumStr);
        } catch (Exception e) {
            return 0;
        }
    }

}
