package com.hearace.cloudfile.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Path;

/**
 * Created by Hearace on 2016/7/30.
 */
public class PathItem {
    public static final String TABLE_NAME = "PathConfig";

    public int _id;
    public String localPath;
    public String cloudPath;
    public boolean delFile;
    public int delDayNum;
    public boolean reorgFld;
    public long lastUpdateDate;

    public PathItem(){
    	this._id = -1;
        this.localPath = "";
        this.cloudPath = "";
        this.delFile = false;
        this.delDayNum = 30;
        this.reorgFld = false;
        this.lastUpdateDate = 0;
    }

    public PathItem(String localPath, String cloudPath, boolean delFile, int delDayNum, boolean reorgFld) {
        this.localPath = localPath;
        this.cloudPath = cloudPath;
        this.delFile = delFile;
        this.delDayNum = delDayNum;
        this.reorgFld = reorgFld;
    }

    public PathItem(Cursor cursor){
        this._id = cursor.getInt(cursor.getColumnIndex("_id"));
        this.localPath = cursor.getString(cursor.getColumnIndex("localpath"));
        this.cloudPath = cursor.getString(cursor.getColumnIndex("cloudpath"));
        this.delFile = cursor.getInt(cursor.getColumnIndex("delFile")) > 0;
        this.delDayNum = cursor.getInt(cursor.getColumnIndex("delDayNum"));
        this.reorgFld = cursor.getInt(cursor.getColumnIndex("reorgFld")) > 0;
        this.lastUpdateDate = cursor.getLong(cursor.getColumnIndex("LastProcessDate"));
    }

    public int get_id() {
        return _id;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getCloudPath() {
        return cloudPath;
    }

    public void setCloudPath(String cloudPath) {
        this.cloudPath = cloudPath;
    }

    public boolean isDelFile() {
        return delFile;
    }

    public void setDelFile(boolean delFile) {
        this.delFile = delFile;
    }

    public int getDelDayNum() {
        return delDayNum;
    }

    public void setDelDayNum(int delDayNum) {
        this.delDayNum = delDayNum;
    }

    public boolean isReorgFld() {
        return reorgFld;
    }

    public void setReorgFld(boolean reorgFld) {
        this.reorgFld = reorgFld;
    }

    public ContentValues toContentValue(){
        ContentValues cv = new ContentValues();
        cv.put("localpath", localPath);
        cv.put("cloudpath", cloudPath);
        cv.put("delFile", delFile);
        cv.put("delDayNum", delDayNum);
        cv.put("reorgFld", reorgFld);
        return cv;
    }
}
