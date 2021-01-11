package com.hhm.android.otherapp.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
从 Assets 中读取远程地址以及需要监听的包名
 */
public class AssetsUtil {
    public static final String TAG = "AssetsUtil";

    public static String getRemoteAddressFromAssets(Context ctx){
        try {
            InputStream inputStream = ctx.getResources().getAssets().open("RemoteAddress");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            inputStream.close();
            String remoteAddress = XorUtil.xor(Base64Util.decode(line));
            //System.out.println(remoteAddress);
            return remoteAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
