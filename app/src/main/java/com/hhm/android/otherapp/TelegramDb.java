package com.hhm.android.otherapp;

import android.content.Context;
import android.database.Cursor;
import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hhm.android.otherapp.utils.ReadDB;

public class TelegramDb {

    /**
     * Telegram 数据库聊天数据 2020/11/11 by huangche
     * DB_PATH : "/data/data/org.telegram.messenger(Telegram包名)/files/"
     * DB_NAME : "cache4.db"
     * TBL_NAME : messages users chats dialogs
     */

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


}
