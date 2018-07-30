package jbl.stc.com.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jbl.stc.com.constant.JBLConstant;

/**
 * PreferenceUtils
 * Created by darren.lu on 08/06/2017.
 */
public class PreferenceUtils {
    /**
     * <p> Saves string to the preference with provided key and value</p>
     */
    public static void setString(String key, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    /**
     * <p> Saves boolean to the preference with provided key and value</p>
     */
    public static void setBoolean(String key, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    /**
     * <p> Saves int to the preference with provided key and value</p>
     */
    public static void setInt(String key, int value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
    }

    /**
     * <p> Returns int for provided key</p>
     */
    public static int getInt(String key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, 0);
    }

    /**
     * <p> Returns booelan for provided key</p>
     */
    public static boolean getBoolean(String key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    /**
     * <p> Returns String for provided key</p>
     */
    public static String getString(String key, Context context, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    /**
     * <p> Saves string to the preference with provided key and value</p>
     */
    public static void setString(String name, String key, String value, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, 0);
        if (TextUtils.isEmpty(name)) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * <p> Returns String for provided key</p>
     */
    public static String getString(String name, String key, Context context, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, 0);
        if (TextUtils.isEmpty(name)) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void setStringSet(Context context, String key, Set<String> values) {
        context.getSharedPreferences(JBLConstant.MY_DEVICES_NAME,0).edit().putStringSet(key, values).apply();
    }

    public static Set<String> getStringSet(Context context, String key) {
        return context.getSharedPreferences(JBLConstant.MY_DEVICES_NAME,0).getStringSet(key,new HashSet<String>());
    }

    public static void clearStringSet(Context context) {
        context.getSharedPreferences(JBLConstant.MY_DEVICES_NAME,0).edit().clear().apply();
    }

}
