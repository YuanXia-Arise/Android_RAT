package com.hhm.android.otherapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 获取通话记录 2020/11/12 by huangche
 */
public class CallsManager {

    public static final String TAG = "CallsManager";

    /**
     * 逆向读取，读最新
     */
    private static Uri callUri = CallLog.Calls.CONTENT_URI;  // 查询通话记录的URI
    private static String[] columns = {
            CallLog.Calls.CACHED_NAME, // 通话记录的联系人
            CallLog.Calls.NUMBER, // 通话记录的电话号码
            CallLog.Calls.DATE, // 通话记录的日期
            CallLog.Calls.DURATION, // 通话时长
            CallLog.Calls.TYPE, // 通话类型
            CallLog.Calls._ID};// 通话ID}
    public static JsonArray getContentCallLog(Context context, int num) { // 按照时间逆序排列，最近打的最先显示
        String sortOrder = (num == 0) ?  "date DESC" : "date DESC limit " + String.valueOf(num);
        try {
            JsonArray list = new JsonArray();
            Cursor cursor = context.getContentResolver().query(callUri, columns, null, null, sortOrder); // "date DESC limit num"
            Log.i(TAG, "cursor count:" + cursor.getCount());
            while (cursor.moveToNext()) {
                JsonObject call = new JsonObject();
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
                name = (name == null) ? number : name;
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
                int _id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID)); //ID
                call.addProperty("call_number", number);
                call.addProperty("call_remarks", name);
                call.addProperty("call_duration", duration);
                call.addProperty("call_date", date);
                call.addProperty("call_type", type);
                call.addProperty("call_record_id", _id);
                list.add(call);
            }
            return list;
        } catch (Exception e){}
        return null;
    }

}
