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

public class SaveSetUtil {

    private final static String FILENAME = "product_device_data";

    private final static String KEY = "my_device";
    private final static String CURRENTEQ_KEY = "current_eq_key";
    private static String TAG = SaveSetUtil.class.getSimpleName();

    public static void saveSet(Context context, Set<MyDevice> set){
        Set<MyDevice> setSaved = readSet(context);
        if (setSaved != null){
            setSaved.addAll(set);
        }else{
            setSaved = new HashSet<>(set);
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(setSaved);
        Logger.d(TAG, "json = "+json);
        editor.putString(KEY, json);
        editor.apply();
    }

    public static Set<MyDevice> readSet(Context context){

        SharedPreferences preferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE);
        String json = preferences.getString(KEY, null);
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

    public static void saveCurrentEqSet(Context context, Set<RetCurrentEQ> set){
        Set<RetCurrentEQ> setSaved = readCurrentEqSet(context);
        if (setSaved != null){
            setSaved.clear();
        }else{
            setSaved = new HashSet<>(set);
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(setSaved);
        Logger.d(TAG, "json = "+json);
        editor.putString(CURRENTEQ_KEY, json);
        editor.apply();
    }

    public static Set<RetCurrentEQ> readCurrentEqSet(Context context){
        SharedPreferences preferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE);
        String json = preferences.getString(CURRENTEQ_KEY, null);
        if (json != null)
        {
            Gson gson = new Gson();
            Type type = new TypeToken<Set<RetCurrentEQ>>(){}.getType();
            Set<RetCurrentEQ> set = gson.fromJson(json, type);
            if (set != null) {
                for (RetCurrentEQ retCurrentEQ: set) {
                    Logger.d(TAG, "retCurrentEQ = " + retCurrentEQ.enumEqCategory );
                }
                return set;
            }
        }
        return null;
    }

    public static void remove(Context context, MyDevice myDevice){
        Set<MyDevice> setSaved = readSet(context);
        if (setSaved != null){
            setSaved.remove(myDevice);
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(FILENAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(setSaved);
        Logger.d(TAG, "remove my device json = "+json);
        editor.putString(KEY, json);
        editor.apply();
    }
}
