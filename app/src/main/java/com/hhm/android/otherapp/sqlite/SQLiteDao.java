package com.hhm.android.otherapp.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.Gson;
import com.hhm.android.otherapp.utils.AppUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author huangche
 * @date 2020/12/30
 * SQL 表数据获取、处理
 */
public class SQLiteDao {
    public static final String TAG = "SQLiteDao";

    private static int count;
    private static int maxCount = 10;
    private static List<LogObject> logObjectList = new ArrayList<>();

    private static MySQLiteHelper dbHelper;
    private static SQLiteDatabase db;

    public static void setMaxCount(int maxCount){
        SQLiteDao.maxCount = maxCount;
    }

    public SQLiteDao(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public static void setInstance(Context context){
        dbHelper = new MySQLiteHelper(context);
    }

    public static void addToList(String packageName, String windowContent){
        Date date = new Date();
        count += 1;
        LogObject logObject = new LogObject(date.getTime(),packageName,windowContent);
        logObjectList.add(logObject);
        if (count == maxCount){
            addLogOfDb();
        }
        System.out.println(count);
    }

    // 将浏览记录储存在 log 表中
    public static synchronized void addLogOfDb() {
        if (db==null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
        if (logObjectList.size() != 0){
            String sql = "insert into log(TIME,PACKAGE_NAME,WINDOW_CONTENT) values(?,?,?)";
            SQLiteStatement stat = db.compileStatement(sql);
            db.beginTransaction();
            for (LogObject logObject : logObjectList) {
                stat.bindLong(1, logObject.timeStamp);
                stat.bindString(2, logObject.packageName);
                stat.bindString(3, logObject.windowContent);
                stat.executeInsert();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            logObjectList.clear();
            count = 0;
        }
    }

    // 将剪贴板内容储存在 clip 表中
    public static synchronized void addClipOfDb(String clipText){
        if (db == null || !db.isOpen()){
            db = dbHelper.getWritableDatabase();
        }
        Date date = new Date();
        long timeStamp = date.getTime();
        String sql = "insert into clip(TIME,CLIP_BOARD_TEXT) values(?,?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        stat.bindLong(1, timeStamp);
        stat.bindString(2, clipText);
        stat.executeInsert();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    // 获取log表的数据
    public static synchronized ArrayList<LogObjects> searchAndDeleteLogOfDb(){
        Log.e(TAG,"searchAndDeleteLogOfDb():" + android.os.Process.myTid());
        addLogOfDb();
        if (db==null || !db.isOpen()){
            db = dbHelper.getWritableDatabase();
        }
        ArrayList<LogObjects> objectList = new ArrayList<>();
        Cursor cursor = db.query("log",null,null,null,null,null,null);

        /*if (cursor.moveToLast()){ //反向遍历对象
            do {
                int _id = cursor.getInt(0); // id
                long timeStamp = cursor.getLong(1); // 时间戳
                String packageName = cursor.getString(2); // 包名
                String windowContent = cursor.getString(3); // 文本内容
                LogObjects logObject = new LogObjects(_id,timeStamp,packageName,windowContent);
                objectList.add(logObject);
            } while (cursor.moveToPrevious());
        }*/

        while (cursor.moveToNext()){
            int _id = cursor.getInt(0); // id
            long timeStamp = cursor.getLong(1); // 时间戳
            String packageName = cursor.getString(2); // 包名
            String windowContent = cursor.getString(3); // 文本内容
            LogObjects logObject = new LogObjects(_id,timeStamp,packageName,windowContent);
            objectList.add(logObject);
        }
        cursor.close();

        db.close();
        delete_log();
        Log.e(TAG,"searchAndDeleteLogOfDb():Count"+ cursor.getCount());
        return objectList;
    }

    // 获取clip表数据
    public static synchronized ArrayList<ClipObject> searchAndDeleteClipOfDb(){
        Log.e(TAG,"searchAndDeleteClipOfDb():" + android.os.Process.myTid());
        if (db==null || !db.isOpen()){
            db = dbHelper.getWritableDatabase();
        }
        ArrayList<ClipObject> objectList = new ArrayList<>();
        Cursor cursor = db.query("clip",null,null,null,null,null,null);

        String Clip = null;
        /*if (cursor.moveToLast()){ // 反向遍历对象
            do {
//                int _id = cursor.getInt(0);  // id
//                long timeStamp = cursor.getLong(1); // 时间戳
//                String clipboardText = cursor.getString(2);// 剪贴板内容
//                ClipObject clipObject = new ClipObject(_id,timeStamp,clipboardText);
//                objectList.add(clipObject);
                int _id = cursor.getInt(0);
                long timeStamp = cursor.getLong(1);
                String clipboardText = cursor.getString(2);
                if (Clip == null){
                    Clip = clipboardText;
                    ClipObject clipObject = new ClipObject(_id,timeStamp,clipboardText);
                    objectList.add(clipObject);
                } else {
                    if (!Clip.equals(clipboardText)){
                        Clip = clipboardText;
                        ClipObject clipObject = new ClipObject(_id,timeStamp,clipboardText);
                        objectList.add(clipObject);
                    }
                }
            } while (cursor.moveToPrevious());
        }*/

        while (cursor.moveToNext()){
            int _id = cursor.getInt(0);
            long timeStamp = cursor.getLong(1);
            String clipboardText = cursor.getString(2);
            if (Clip == null){
                Clip = clipboardText;
                ClipObject clipObject = new ClipObject(_id,timeStamp,clipboardText);
                objectList.add(clipObject);
            } else {
                if (!Clip.equals(clipboardText)){
                    Clip = clipboardText;
                    ClipObject clipObject = new ClipObject(_id,timeStamp,clipboardText);
                    objectList.add(clipObject);
                }
            }
        }
        cursor.close();

        db.close();
        delete_clip();
        Log.e(TAG,"searchAndDeleteClipOfDb():Count"+ cursor.getCount());
        return objectList;
    }

    // 清空clip表数据
    public static void delete_clip() {
        if (db == null || !db.isOpen()){
            db = dbHelper.getWritableDatabase();
        }
        db.execSQL("delete from clip");
        db.close();
    }

    // 清空log表数据
    public static void delete_log() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
        db.execSQL("delete from log");
        db.close();
    }

}
