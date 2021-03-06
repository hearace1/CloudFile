package com.hearace.cloudfile.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hearace on 2016/7/30.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cloudfile.db";
    private static final int DATABASE_VERSION = 5;

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认�
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS PathConfig" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, localpath VARCHAR, cloudpath VARCHAR, delFile BOOLEAN, delDayNum INTEGER, reorgFld BOOLEAN," +
                "isEnable BOOLEAN, LastProcessDate TIMESTAMP)");
        db.execSQL("CREATE TABLE IF NOT EXISTS FileBacklog" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, file VARCHAR, cloudpath VARCHAR, status INTEGER, retryNum INTEGER, locker VARCHAR, "
                + "progress BIGINT, md5s VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS AppConfig" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, property VARCHAR, value VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    	db.execSQL("ALTER TABLE FileBacklog " +
                "ADD COLUMN progress BIGINT");
    	db.execSQL("ALTER TABLE FileBacklog " +
                "ADD COLUMN md5s VARCHAR");
    }
}
