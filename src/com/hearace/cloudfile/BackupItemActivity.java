package com.hearace.cloudfile;

import com.hearace.cloudfile.model.DBManager;
import com.hearace.cloudfile.model.UploadItemListAdapter;
import com.hearace.cloudfile.service.CloudService;
import com.hearace.cloudfile.service.ServiceBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class BackupItemActivity extends Activity {

	private ListView uploadFileList;
	private ServiceBinder sBinder = null;
	private Button startBtn = null;
	private Button stopBtn = null;
	private ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.d("BackupItemActivity", "do onServiceConnected");
			sBinder = (ServiceBinder) service;
		}
	};
	
	private final Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_item);
		uploadFileList = (ListView) findViewById(R.id.uploadFileList);
		startBtn = (Button)findViewById(R.id.uploadStartBtn);
		stopBtn = (Button)findViewById(R.id.stopUploadBtn);
		bindService();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.d("BackupItemActivity", "onStart()");
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
		if(sBinder != null && sBinder.isDownloading()){
			startBtn.setEnabled(false);
			stopBtn.setEnabled(true);
			
			handler.post(new RefreshThread());
		}else{
			startBtn.setEnabled(true);
			stopBtn.setEnabled(false);
		}
	}
	
	private void bindService(){
		Log.d("BackupItemActivity", "bindService()");
		Intent bindIntent = new Intent(this, CloudService.class);		
		bindService(bindIntent, connection, BIND_AUTO_CREATE );
	}
	
	public void startBtnOnClick(View view){
		sBinder.startDownload();
	}
	
	public void stopBtnOnClick(View view){
		sBinder.stopDownload();
	}
	
	public void resetOnClick(View view){
		DBManager dbMgr = DBManager.getInstance(this);
		dbMgr.resetFailedItems();
		refreshList();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(sBinder != null)
			unbindService(connection);
	}
	
	class RefreshThread implements Runnable {
		public void run() {
			if(sBinder != null && sBinder.isDownloading()){
					refreshList();
					handler.postDelayed(this, 5000);
			}
		}
	}
}

