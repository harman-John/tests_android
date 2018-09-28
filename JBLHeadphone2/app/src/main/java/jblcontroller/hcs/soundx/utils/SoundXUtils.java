package jblcontroller.hcs.soundx.utils;

import com.google.common.io.BaseEncoding;

import java.io.UnsupportedEncodingException;

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
