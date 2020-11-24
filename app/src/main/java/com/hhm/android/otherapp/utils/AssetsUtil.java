package com.hhm.android.otherapp.utils;

import android.content.Context;
import android.util.Log;

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

    public static String getLine(Context ctx, String fileName) {
        String line = "";
        try {
            InputStream inputStream = ctx.getResources().getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            line = bufferedReader.readLine();
            inputStream.close();
            System.out.println(line);
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

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

    public static String[] getPackageNamesFromAssets(Context ctx){
        try {
            InputStream inputStream = ctx.getResources().getAssets().open("PackageNames");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            List<String> packageNames = new ArrayList<>();
            while((line = bufferedReader.readLine())!=null){
                if (!line.equals("")){
                    String packageName = XorUtil.xor(Base64Util.decode(line));
                    packageNames.add(packageName);
                }
            }
            inputStream.close();
            Log.e(TAG,"getPackageNamesFromAssets():"+packageNames.toString());
            return packageNames.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{};
    }
}
