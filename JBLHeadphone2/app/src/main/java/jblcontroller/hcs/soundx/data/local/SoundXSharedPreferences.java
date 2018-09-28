package jblcontroller.hcs.soundx.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SoundXSharedPreferences {
    private static final String KEY_IS_PREFERENCE_ENABLED = "isPreference_enabled";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_X_AUTH_TOKEN = "auth_token";
    private static final String KEY_LISTENING_EXP = "listening_experience";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DEVICE_TYPE = "device_type";
    private static final String KEY_YOB = "yearOfBirth";
    private static final String KEY_PREF_BASS_VALUE = "pref_bass";
    private static final String KEY_PREF_TREBLE = "pre_treble";
    private static final String KEY_PREF_CONFIGURATION_DONE = "is_configured";
    private static final String KEY_PREF_WORK_OFFLINE = "work_offline";


    public static boolean isPreferenceEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_IS_PREFERENCE_ENABLED, true);
    }

    public static void enablePreference(final boolean active, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_IS_PREFERENCE_ENABLED, active).commit();
    }
    //--------- Rest API preferences ----------------

    public static void setUserName(final Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_USER_NAME, name).commit();
    }

    public static void setPassword(final Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_PASSWORD, name).commit();
    }

    public static void setAuthToken(final Context context, String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_X_AUTH_TOKEN, name).commit();
    }

    public static void setListeningExp(final Context context, int name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_LISTENING_EXP, name).commit();
    }

    public static void setGender(final Context context, int name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_GENDER, name).commit();
    }

//    public static void setDeviceType(final Context context, String name) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        sp.edit().putString(KEY_DEVICE_TYPE, name).commit();
//    }

    public static void setYob(final Context context, int name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_YOB, name).commit();
    }

    public static String getUserName(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_USER_NAME, "");
    }

    public static String getPassword(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_PASSWORD, "");
    }

    public static String getAuthToken(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_X_AUTH_TOKEN, "");
    }

    public static int getListeningExp(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_LISTENING_EXP, 0);
    }

    public static int getGender(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_GENDER, 0);
    }

//    public static String getDeviceType(final Context context) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        return sp.getString(KEY_DEVICE_TYPE, "");
//    }

    public static int getYob(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_YOB, 0);
    }

    public static void setPreferredBass(final Context context, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_PREF_BASS_VALUE, value).commit();
    }

    public static void setPreferredTreble(final Context context, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_PREF_TREBLE, value).commit();
    }

    public static int getPrefBass(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_PREF_BASS_VALUE, 0);
    }

    public static int getPrefTreble(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_PREF_TREBLE, 0);
    }

    public static void setConfigurationDone(final boolean active, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_PREF_CONFIGURATION_DONE, active).commit();
    }

    public static boolean isConfiguration(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_PREF_CONFIGURATION_DONE, false);
    }

    public static void clearUserData(Context context) {
        setUserName(context,"");
        setPassword(context,"");
        setAuthToken(context,"");
        setConfigurationDone(false,context);
        setPreferredTreble(context,0);
        setPreferredTreble(context,0);
        setGender(context,0);
        setListeningExp(context,1);
        setYob(context,0);
    }

    public static void setWorkOffline(final boolean active, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_PREF_WORK_OFFLINE, active).commit();
    }


    public static boolean isOfflineEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_PREF_WORK_OFFLINE, false);
    }

}

