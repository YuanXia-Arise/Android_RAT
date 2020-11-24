package com.hhm.android.otherapp.sqlite;

/*
浏览记录对象
 */
public class LogObject {
    public long timeStamp;
    public String packageName;
    public String windowContent;

    public LogObject(long timeStamp, String packageName, String windowContent){
        this.timeStamp = timeStamp;
        this.packageName = packageName;
        this.windowContent = windowContent;
    }
}
