package com.hhm.android.otherapp.sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{
    public static final String TAG = "MySQLiteHelper";

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "record.db";   // 默认存储位置 data/data/PACKAGE_NAME/databases
    @SuppressLint("SdCardPath")
    private static final String dbPath = "/sdcard/record.db";  // 自定义数据库存储位置

    public MySQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            /*db.execSQL("CREATE TABLE IF NOT EXISTS clip(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "TIME LONG,"+
                    "CLIP_BOARD_TEXT VARCHAR(8192))");
            db.execSQL("CREATE TABLE IF NOT EXISTS log(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "TIME LONG,"+
                    "PACKAGE_NAME VARCHAR(128),"+
                    "WINDOW_CONTENT VARCHAR(8192))");*/
            db.execSQL("CREATE TABLE IF NOT EXISTS clip(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "TIME LONG,"+
                    "CLIP_BOARD_TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS log(" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "TIME LONG,"+
                    "PACKAGE_NAME,"+
                    "WINDOW_CONTENT)");
            System.out.println("CREATE TABLE");
        } catch (SQLException e) {
            Log.e(TAG,"onCreate()"+e.getLocalizedMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    /*
    // 通过重载 getWritableDatabase()、getReadableDatabase() 实现自定义 SQLite 数据库位置
    // 若不重载，则数据库文件默认存储在 data/data/PACKAGE_NAME
    @Override
    public SQLiteDatabase getWritableDatabase(){
        return getDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase(){
        return getDatabase();
    }

    private SQLiteDatabase getDatabase(){
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(dbPath,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS clip(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "TIME LONG,"+
                "CLIP_BOARD_TEXT VARCHAR(8192))");
        database.execSQL("CREATE TABLE IF NOT EXISTS log(" +  // BEFORE : db.execSQL()
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "TIME LONG,"+
                "PACKAGE_NAME VARCHAR(128),"+
                "WINDOW_CONTENT VARCHAR(8192))");
        int oldVer = database.getVersion();
        if (VERSION>oldVer){
            this.onUpgrade(database,oldVer,VERSION);
        }else if(VERSION<oldVer){
            this.onDowngrade(database,oldVer,VERSION);
        }
        return database;
    }*/
}
