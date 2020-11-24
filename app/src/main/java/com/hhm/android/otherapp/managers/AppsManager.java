package com.hhm.android.otherapp.managers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取设备应用列表 20201116
 * by huangche
 */
public class AppsManager {

    // 应用包名/名称集合
    public static JsonArray GetAppsList(Context context) {
        JsonArray list = new JsonArray();
        try {
            List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
            for (PackageInfo info : packageInfos) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("package",info.packageName);
                jsonObject.addProperty("name",getApplicationNameByPackageName(context,info.packageName));
                list.add(jsonObject);
            }
            return list;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    // 获取应用名
    public static String getApplicationNameByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String Name ;
        try {
            Name=pm.getApplicationLabel(pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "" ;
        }
        return Name;
    }
}
