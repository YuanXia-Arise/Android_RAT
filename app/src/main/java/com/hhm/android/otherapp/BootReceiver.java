package com.hhm.android.otherapp;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 实现开机启动
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = "BootReceiver";

    @SuppressLint("WakelockTimeout")
    @Override
    public void onReceive(Context context, Intent intent) {

        //屏幕唤醒
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //assert pm != null;
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "BootReceiver");
        wl.acquire();  // 获得锁，以保持屏幕唤醒
        //屏幕解锁
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("BootReceiver");
        kl.disableKeyguard();

        /*
        Intent sayHelloIntent = new Intent(context, MainActivity.class);
        sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sayHelloIntent);*/

        Intent mIntent = new Intent(context, myService.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(mIntent);
        Log.e(TAG,"onReceive():" + mIntent.getAction());
    }
}


