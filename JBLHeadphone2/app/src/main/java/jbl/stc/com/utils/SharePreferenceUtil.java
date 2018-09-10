package jbl.stc.com.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.harman.bluetooth.ret.RetCurrentEQ;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jbl.stc.com.entity.MyDevice;
import jbl.stc.com.logger.Logger;

import static android.content.Context.MODE_PRIVATE;

public class SharePreferenceUtil {

    public final static String FILENAME = "headphones_filename";

    public final static String PRODUCT_DEVICE_LIST_PER_KEY = "my_device";

    public final static String BLE_DESIGN_EQ = "ble_design_eq_key";
    public final static String BLE_GRAPHIC_EQ = "ble_graphic_eq_key";
    public final static String BLE_EQS = "ble_eqs";


    private static String TAG = SharePreferenceUtil.class.getSimpleName();

    public static void saveSet(Context context, String key, Set<MyDevice> set){
        Set<MyDevice> setSaved = readSet(context, key);
        if (setSaved != null){
            setSaved.addAll(set);
        }else{
            setSaved = new HashSet<>(set);
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(setSaved);
        Logger.d(TAG, "json = "+json);
        editor.putString(PRODUCT_DEVICE_LIST_PER_KEY, json);
        editor.apply();
    }

    public static Set<MyDevice> readSet(Context context,String key){

        SharedPreferences preferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE);
        String json = preferences.getString(key, null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<Set<MyDevice>>(){}.getType();
            Set<MyDevice> set = gson.fromJson(json, type);
            if (set != null) {
                for (MyDevice myDevice : set) {
                    Logger.d(TAG, "deviceKey = " + myDevice.deviceKey + ",name = " + myDevice.deviceName + ",pid = " + myDevice.pid);
                }
                return set;
            }
        }
        return null;
    }

    public static void remove(Context context, MyDevice myDevice){
        Set<MyDevice> setSaved = readSet(context,SharePreferenceUtil.PRODUCT_DEVICE_LIST_PER_KEY);
        if (setSaved != null){
            setSaved.remove(myDevice);
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(setSaved);
        Logger.d(TAG, "remove my device json = "+json);
        editor.putString(PRODUCT_DEVICE_LIST_PER_KEY, json);
        editor.apply();
    }

    public static void saveObjectToSharedPreference(Context context, String preferenceFileName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context, String preferenceFileName, String preferenceKey, Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    public static void saveCurrentEqSet(Context context, List<RetCurrentEQ> datalist , String key){
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        editor.putString(key,"").apply();
        Gson gson = new Gson();
        String json = gson.toJson(datalist);
        Logger.d(TAG, "json = "+json);
        editor.putString(key, json).apply();
    }

    public static List<RetCurrentEQ> readCurrentEqSet(Context context , String key){
        List<RetCurrentEQ> dataList=new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE);
        String json = preferences.getString(key, null);
        if (json == null) {
            return  dataList;
        }
        Gson gson = new Gson();
        dataList = gson.fromJson(json,new TypeToken<List<RetCurrentEQ>>(){}.getType());
        return  dataList;
    }
}
