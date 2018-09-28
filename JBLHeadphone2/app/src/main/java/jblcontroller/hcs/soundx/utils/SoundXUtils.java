package jblcontroller.hcs.soundx.utils;

import android.app.Application;
import android.util.Log;

import com.google.common.io.BaseEncoding;

import java.io.File;
import java.io.UnsupportedEncodingException;

import jblcontroller.hcs.soundx.SoundXApplication;
import jblcontroller.hcs.soundx.data.local.SoundXSharedPreferences;

public class SoundXUtils {

    public static String getBase64(String username, String password){
        String base64="";
        try {
            String inputContent = username+":"+ password;
            base64 = BaseEncoding.base64().encode(inputContent.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e){
        }
        return base64;
    }

}
