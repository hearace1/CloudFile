package com.hearace.cloudfile.model;

import java.io.File;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class BackupItem {
	public static final String TABLE_NAME = "FileBacklog";
	
	private int _id = -1;
	private String fileStr;
	private String cloudpath;
	private int status;
	private int retryNum;
	private String locker;
	private long progress = 0L;
	private long fileSZ = -1L;
	private String errMsg = null;
	private String md5s = "";
	
	public BackupItem(File file, String cloudpath, boolean isRegorFld) {
		this.fileStr = file.getPath();
		this.cloudpath = cloudpath;
		if(isRegorFld){
			if(!cloudpath.endsWith(File.separator))
				this.cloudpath += File.separator;
			this.cloudpath += String.format("%tF", new Date(file.lastModified()));
		}
		this.status = 0;
		this.retryNum = 0;
		this.locker = null;
		this.fileSZ = file.length();
		Log.d("BackupItem", "create new item:"+this.toString());
	}
	
	public BackupItem(Cursor c){
		this._id = c.getInt(c.getColumnIndex("_id"));
		this.fileStr = c.getString(c.getColumnIndex("file"));
		this.cloudpath = c.getString(c.getColumnIndex("cloudpath"));
		this.status = c.getInt(c.getColumnIndex("status"));
		this.retryNum = c.getInt(c.getColumnIndex("retryNum"));
		this.locker = c.getString(c.getColumnIndex("locker"));
		this.progress = c.getLong(c.getColumnIndex("progress"));
		this.md5s = c.getString(c.getColumnIndex("md5s"));
		this.fileSZ = c.getLong(c.getColumnIndex("fileSZ"));
		this.errMsg = c.getString(c.getColumnIndex("errMsg"));
	}
	
	
	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getLocker() {
		return locker;
	}

	public void setLocker(String locker) {
		this.locker = locker;
	}

	public String getFileStr() {
		return fileStr;
	}
	public void setFileStr(String fileStr) {
		this.fileStr = fileStr;
	}
	public String getCloudpath() {
		return cloudpath;
	}
	public void setCloudpath(String cloudpath) {
		this.cloudpath = cloudpath;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getRetryNum() {
		return retryNum;
	}
	public void setRetryNum(int retryNum) {
		this.retryNum = retryNum;
	}	
	public long getProgress() {
		return progress;
	}

	public void setProgress(long progress) {
		this.progress = progress;
	}

	public String getMd5s() {
		return md5s;
	}

	public void setMd5s(String md5s) {
		this.md5s = md5s;
	}
	public void addMd5(String md5){
		if(this.md5s != null && this.md5s.length() > 0){
			this.md5s+=",";
			this.md5s+=md5;
		}else{
			this.md5s = md5;
		}
	}

	@Override
	public String toString() {
		return "BackupItem [fileStr=" + fileStr + ", cloudpath=" + cloudpath + ", status=" + status + ", retryNum="
				+ retryNum + "]";
	}
	
	public ContentValues toContentValue(){
        ContentValues cv = new ContentValues();
        cv.put("file", this.fileStr);
        cv.put("cloudpath", this.cloudpath);
        cv.put("status", this.status);
        cv.put("retryNum", this.retryNum);
        cv.put("fileSZ", this.fileSZ);
        return cv;
	}
}
