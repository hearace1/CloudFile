package com.hearace.cloudfile.service;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import com.hearace.cloudfile.model.DBManager;
import com.hearace.cloudfile.model.PathItem;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

public class FileScanner implements Runnable {

	private DBManager dbMgr = null;
	private String basicPathStr = null;
	private Task task = null;

	public FileScanner(DBManager dbMgr, Task task) {
		super();
		this.dbMgr = dbMgr;
		this.basicPathStr = Environment.getExternalStorageDirectory().getPath();
		this.task = task;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		long currentRunTime = new Date().getTime();
		Cursor cur = dbMgr.getAllPath();
		while (cur.moveToNext()) {
			PathItem item = new PathItem(cur);
			File path = new File(basicPathStr + item.getLocalPath());
			Log.d("FileScanner", "start process path:" + path.getPath());
			File[] backupFile = path.listFiles(new BackupFileFilter(item.lastUpdateDate));
			Log.d("FileScanner", "find files num for backup" + backupFile.length);
			dbMgr.updatePathBak(item, backupFile, currentRunTime);
		}
		task.onComplete();
	}

}

class BackupFileFilter implements FileFilter {
	long lastUpdate;

	public BackupFileFilter(long lastUpdate) {
		super();
		this.lastUpdate = lastUpdate;
	}

	@Override
	public boolean accept(File pathname) {
		// TODO Auto-generated method stub
		Log.d("FileScanner.BackupFileFilter", "file modify time:"+pathname.lastModified()+";  check update time:"+lastUpdate);
		return pathname.isFile() && !pathname.isHidden() && pathname.lastModified() > lastUpdate;
	}
}