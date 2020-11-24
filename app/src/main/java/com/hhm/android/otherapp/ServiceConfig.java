package com.hhm.android.otherapp;

import java.util.ArrayList;

/*
用于设置 RemoteAddress 以及监听的 PackageNames
Assets 文件夹下文本文件包含经过异或编码后字符串
 */
public class ServiceConfig {
    public static final String TAG = "ServiceConfig";

    private static String[] packageList = new String[]{};
    /*{"com.android.chrome","com.android.browser","com.browser2345",
            "com.tencent.mtt","sogou.mobile.explorer","com.UCMobile","com.baidu.browser.apps",
            "com.qihoo.browser","com.ijinshan.browser_fast","org.mozilla.firefox","com.vivo.browser",
            "org.cyanogenmod.gello.browser"};*/

    private static String remoteAddress;

    public static final String MAIN_SERVICE_NAME = "com.hhm.android.rat.AccessService";

    public static final boolean DEBUG = false;

    public static void setPackages(String[] packageNames){
        packageList = packageNames;
        /*for (String name:packageNames){
            System.out.println(name);
        }
        System.out.println(packageNames.length);*/
    }

    public static void setPackages(ArrayList<String> packageNames){
        packageList = (String[])packageNames.toArray();
    }

    public static boolean isInPackage(String packageName){
        for (String name:packageList){
            if (name.equals(packageName)){
                return true;
            }
        }
        return false;
    }

    public static void setRemoteAddress(String address){
        remoteAddress = address;
    }

    public static String getRemoteAddress(){
        return remoteAddress;
    }
}
