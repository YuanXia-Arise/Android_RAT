package com.hhm.android.otherapp.managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 通讯录获取 2020/11/12 by huangche
 */
public class ContactsManager {

    public static final String TAG = "ContactsManager";

    private static String[] projection = {ContactsContract.CommonDataKinds.Phone._ID,//Id
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,//通讯录姓名
            ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",//通讯录手机号
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,//通讯录Id
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};
    private static Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    public static JsonArray getContacts(Context context){
        try {
            JsonArray list = new JsonArray();
            Cursor cur = context.getContentResolver().query(contentUri, projection, null, null, "data1 DESC");
            while (cur.moveToNext()) {
                JsonObject contact = new JsonObject();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String num = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
                contact.addProperty("number_book_id", id);
                contact.addProperty("cont_remarks", name);
                contact.addProperty("cont_number", num);
                list.add(contact);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
