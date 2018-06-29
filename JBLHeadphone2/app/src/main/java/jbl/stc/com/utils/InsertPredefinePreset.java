package jbl.stc.com.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jbl.stc.com.R;
import jbl.stc.com.entity.EQModel;
import jbl.stc.com.manager.EQSettingManager;

/**
 * Created by Vicky on 29/06/2018.
 */
public class InsertPredefinePreset extends AsyncTask<Activity, Void, Void> {


    private static final String TAG = InsertPredefinePreset.class.getSimpleName();

    @Override
    protected Void doInBackground(Activity... params) {
        try {

            String jsonPresets = loadJSONFromAsset(params[0]);
            JSONObject jsonObject = new JSONObject(jsonPresets);
            JSONArray array = jsonObject.getJSONArray("defaults");
            for (int i = 0; i < array.length(); i++) {
                EQModel eqModel = new EQModel();
                JSONObject presetJsonObject = array.optJSONObject(i);
                eqModel.eqName = presetJsonObject.optString("name");
                if (!TextUtils.isEmpty(eqModel.eqName)) {
                    if (eqModel.eqName.equals(params[0].getResources().getString(R.string.off))) {
                        eqModel.eqType = 0;
                    } else if (eqModel.eqName.equals(params[0].getResources().getString(R.string.jazz))) {
                        eqModel.eqType = 1;
                    } else if (eqModel.eqName.equals(params[0].getResources().getString(R.string.vocal))) {
                        eqModel.eqType = 2;
                    } else if (eqModel.eqName.equals(params[0].getResources().getString(R.string.bass))) {
                        eqModel.eqType = 3;
                    }
                }
                float[] pointY = new float[10];
                float[] pointX = {32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
                JSONObject settingJsonObject = presetJsonObject.optJSONObject("settings");
                eqModel.id = settingJsonObject.optInt("presetIndex");
                eqModel.index = settingJsonObject.optInt("presetIndex");
                eqModel.value_32 = (float) settingJsonObject.optDouble("32Hz");
                pointY[0] = eqModel.value_32;
                eqModel.value_64 = (float) settingJsonObject.optDouble("64Hz");
                pointY[1] = eqModel.value_64;
                eqModel.value_125 = (float) settingJsonObject.optDouble("125Hz");
                pointY[2] = eqModel.value_125;
                eqModel.value_250 = (float) settingJsonObject.optDouble("250Hz");
                pointY[3] = eqModel.value_250;
                eqModel.value_500 = (float) settingJsonObject.optDouble("500Hz");
                pointY[4] = eqModel.value_500;
                eqModel.value_1000 = (float) settingJsonObject.optDouble("1000Hz");
                pointY[5] = eqModel.value_1000;
                eqModel.value_2000 = (float) settingJsonObject.optDouble("2000Hz");
                pointY[6] = eqModel.value_2000;
                eqModel.value_4000 = (float) settingJsonObject.optDouble("4000Hz");
                pointY[7] = eqModel.value_4000;
                eqModel.value_8000 = (float) settingJsonObject.optDouble("8000Hz");
                pointY[8] = eqModel.value_8000;
                eqModel.value_16000 = (float) settingJsonObject.optDouble("16000Hz");
                pointY[9] = eqModel.value_16000;
                eqModel.setPointX(pointX);
                eqModel.setPointY(pointY);
                EQSettingManager.get().addCustomEQ(eqModel, params[0]);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String loadJSONFromAsset(Activity activity) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open("eqDefaults.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
