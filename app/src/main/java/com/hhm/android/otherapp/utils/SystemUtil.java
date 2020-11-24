package com.hhm.android.otherapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/*
判断服务是否运行，读取设备内所有包名
 */
public class SystemUtil {
    public static final String TAG = "SystemUtil";

    public static boolean isServiceRunning(Context context, final String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = null;
        if (activityManager != null) {
            info = activityManager.getRunningServices(Integer.MAX_VALUE);
        }
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    //读取系统所有包名
    public static ArrayList<String> getAllPackageName(Context context) {
        ArrayList<String> packageList = new ArrayList<>();
        //获取PackageManager
        PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageList.add(packName);
                //Log.e(TAG,"getAllPackageName():"+packName);
            }
        }
        return packageList;
    }

    public static boolean hasAutoFocus(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }
}
