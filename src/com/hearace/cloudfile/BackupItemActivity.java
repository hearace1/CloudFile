package com.hearace.cloudfile;

import com.hearace.cloudfile.model.DBManager;
import com.hearace.cloudfile.model.UploadItemListAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class BackupItemActivity extends Activity {

	private ListView uploadFileList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_item);
		uploadFileList = (ListView) findViewById(R.id.uploadFileList);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		refreshList();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	private void refreshList(){
		DBManager dbMgr = DBManager.getInstance(this);
		Cursor cur = dbMgr.getUploadItems();
		
		UploadItemListAdapter listApapter = new UploadItemListAdapter(this, cur);
		uploadFileList.setAdapter(listApapter);
	}
}
