package com.hhm.android.otherapp;

import android.content.Context;
import android.support.v4.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * @author huangche
 * date: 2020/12/04
 * 设备权限判定
 * android.permission.CAMERA 相机
 * android.permission.ACCESS_FINE_LOCATION 位置
 * android.permission.READ_EXTERNAL_STORAGE android.permission.WRITE_EXTERNAL_STORAGE 存储
 * android.permission.READ_CALL_LOG 通话记录
 * android.permission.READ_SMS 短信
 * android.permission.READ_CONTACTS 通讯录
 * android.permission.RECORD_AUDIO 录音
 */
public class Permission {

    /**
     * @param context
     * @param permName
     * @return true or false
     */
    public static boolean getPermission(Context context, String permName){
        boolean boo = false;
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), permName) != PERMISSION_GRANTED) {
            boo = false;
        } else {
            boo = true;
        }
        return boo;
    }

}
