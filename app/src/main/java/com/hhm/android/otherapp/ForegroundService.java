package com.hhm.android.otherapp;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// 前台服务，用于提高优先级和权限
// 无实际业务逻辑，仅开启一个前台服务后再停止（去除通知栏内通知）
// 使得真实业务服务 AccessService 能够使用该前台服务 ID ，以获取相应权限
public class ForegroundService extends Service {
    private static final int SERVICE_ID = 1113;

    @Override
    public void onCreate(){
        super.onCreate();
        startForeground(this); // 将自身作为前台服务启动，id 与 AccessService 一致
        stopSelf(); // 将自身停止，消除通知栏消息，但保留 AccessService 继续作为前台服务
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopForeground(true);
    }

    public static void startForeground(Service service){
        Notification.Builder builder = new Notification.Builder(service);
        builder.setContentTitle("The service is running").setAutoCancel(true);
        Notification notification = builder.build();
        service.startForeground(SERVICE_ID,notification);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
