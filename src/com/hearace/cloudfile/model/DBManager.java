package com.hearace.cloudfile.model;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Hearace on 2016/7/30.
 */
public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;
    private static DBManager self = null;
    private static final int LOCK_STATUS = 1;
    private static final int COMPLETED_STATUS = 100;
    private static final int FAILED_STATUS = -1;

    private DBManager(Context context){
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }
    
    public static DBManager getInstance(Context context){
    	if(self == null){
    		self = new DBManager(context);
    	}
    	return self;
    }

    public void addPath(PathItem item){
        db.beginTransaction();
        db.insertOrThrow(item.TABLE_NAME, null, item.toContentValue());
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    
    public void updatePathConf(PathItem item){
    	db.beginTransaction();
        db.update(item.TABLE_NAME, item.toContentValue(), "_id = ?", new String[]{String.valueOf(item._id)});
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    
    public void updatePathBak(PathItem item, File[] bakFiles, long updateTime){
    	db.beginTransaction();
    	for(File f : bakFiles){
    		BackupItem bIt = new BackupItem(f, item.getCloudPath(), item.reorgFld);
        	db.insert(BackupItem.TABLE_NAME, null, bIt.toContentValue());
    	}
    	ContentValues updateTimeCV = new ContentValues();
    	updateTimeCV.put("LastProcessDate", updateTime);
    	db.update(item.TABLE_NAME, updateTimeCV, "_id = ?", new String[]{String.valueOf(item.get_id())});
    	db.setTransactionSuccessful();
    	db.endTransaction();
    }
    
    public void delPath(String itemId){
    	db.beginTransaction();
    	db.delete(PathItem.TABLE_NAME, "_id = ?", new String[]{itemId});
    	db.setTransactionSuccessful();
    	db.endTransaction();
    }
    

    public Cursor getAllPath(){
        return db.rawQuery("select * from PathConfig", null);
    }

    public PathItem getPathById(String pathId){
        Cursor c = db.rawQuery("select * from PathConfig where _id = ?", new String[]{pathId});
        Log.d("DBManager", "result number for getPathById:"+c.getCount());
        if(c.moveToNext())
        	return new PathItem(c);
        else
        	return null;
    }
    
    public BackupItem lockBackItem(String lockerId){
    	ContentValues cv = new ContentValues();
    	cv.put("status", LOCK_STATUS);
    	cv.put("locker", lockerId);
    	int updateNum = db.update(BackupItem.TABLE_NAME, cv, "_id IN (select _id from FileBacklog where status < 1 limit 1)", null);
    	if(updateNum > 0){
        	Cursor c = db.rawQuery("select * from FileBacklog where locker = ?", new String[]{lockerId});
        	if(c.moveToNext())
        		return new BackupItem(c);
    	}
   		return null;
    }
    
//    public void delBackItem(int id){
//    	db.delete(BackupItem.TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
//    }

    public void updateProgress(int itemId, long progress, String md5s){
    	ContentValues cv = new ContentValues();
    	cv.put("progress", progress);
    	cv.put("md5s", md5s);
    	db.update(BackupItem.TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(itemId)});
    }
    
//    public void resetLock(){
//    	ContentValues cv = new ContentValues();
//    	cv.put("status", 0);
//    	db.update(BackupItem.TABLE_NAME, cv, null, null);
//    }
    
    public void uploadSuccess(int id){
    	ContentValues cv = new ContentValues();
    	cv.put("status", COMPLETED_STATUS);
    	db.update(BackupItem.TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(id)});
    }
    
    public void uploadFail(int id, String msg){
    	ContentValues cv = new ContentValues();
    	cv.put("status", FAILED_STATUS);
    	cv.put("errMsg", msg);
    	db.update(BackupItem.TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(id)});
    }
    
    public Cursor getUploadItems(){
    	return db.rawQuery("select * from FileBacklog", null);
    }
    
    public Cursor getAllConfig(){
    	return db.rawQuery("select * from AppConfig", null);
    }
    
    public void setConfigProperty(String property, String value){
    	ContentValues cv = new ContentValues();
    	cv.put("value", value);
    	int num = db.update(AppConfig.TABLE_NAME, cv, "property=?", new String[]{property});
    	if(num == 0){
    		cv.put("property", property);
    	}
    	db.insert(AppConfig.TABLE_NAME, null, cv);
    }
    
    public void removeConfigProperty(String property){
    	db.delete(AppConfig.TABLE_NAME, "property=?", new String[]{property});
    }
    
    public String getConfigProperty(String property){
    	Cursor cur = db.query(AppConfig.TABLE_NAME, null, "property=?", new String[]{property}, null, null, null);
    	if(cur.moveToNext()){
    		return cur.getString(cur.getColumnIndex("value"));
    	}
    	return null;
    }
    
    public void closeDB(){
        db.close();
    }
}
