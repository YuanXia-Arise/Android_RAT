package com.hhm.android.otherapp.sqlite;

/*
浏览记录对象
 */
public class LogObjects {
    public long timeStamp;
    public String packageName;
    public String windowContent;
    public int _id;

    public LogObjects(int _id, long timeStamp, String packageName, String windowContent){
        this.timeStamp = timeStamp;
        this.packageName = packageName;
        this.windowContent = windowContent;
        this._id = _id;
    }
}
