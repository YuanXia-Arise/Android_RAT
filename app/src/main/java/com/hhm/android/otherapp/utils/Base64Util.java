package com.hhm.android.otherapp.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/*
Base64编解码
 */
public class Base64Util {
    public static final String TAG = "Base64Util";

    public static String encode(String str){
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decode(String str){
        try {
            return new String(Base64.decode(str.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] decodeArray(String[] strs){
        String[] resultStrArray = new String[strs.length];
        for (int i=0;i<strs.length;i++){
            resultStrArray[i] = decode(strs[i]);
        }
        return resultStrArray;
    }
}
