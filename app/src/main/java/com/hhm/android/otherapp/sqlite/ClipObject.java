package com.hhm.android.otherapp.sqlite;

/*
剪贴板对象
 */
public class ClipObject {
    public long timeStamp;
    public String clipBoardText;
    public int _id;

    public ClipObject(int _id, long timeStamp, String clipBoardText){
        this.timeStamp = timeStamp;
        this.clipBoardText = clipBoardText;
        this._id = _id;
    }
}
