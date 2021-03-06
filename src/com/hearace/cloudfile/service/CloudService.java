package com.hearace.cloudfile.service;

import java.util.Date;

import com.hearace.cloudfile.model.DBManager;

import android.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CloudService extends Service implements Task{
	private static final int UPLOAD_FILE_STEP = 2;
	private static final int RESET_LOCK = 3;
	
	private DBManager dbMgr = null;
	private int nextStep = -1;
	private Handler handler = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("CloudService", "onCreate executed at"+new Date());
		handler = new Handler();
		
//		NotificationCompat.Builder mBuilder =
//		        new NotificationCompat.Builder(this)
//		        .setSmallIcon(R.drawable.ic_notification_overlay)
//		        .setContentTitle("CloudFile")
//		        .setContentText("Start Backup");
//		// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = new Intent(this, CloudService.class);
//
//		// The stack builder object will contain an artificial back stack for the
//		// started Activity.
//		// This ensures that navigating backward from the Activity leads out of
//		// your application to the Home screen.
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//		// Adds the back stack for the Intent (but not the Intent itself)
//		stackBuilder.addParentStack(CloudService.class);
//		// Adds the Intent that starts the Activity to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(
//		            0,
//		            PendingIntent.FLAG_UPDATE_CURRENT
//		        );
//		mBuilder.setContentIntent(resultPendingIntent);
//		NotificationManager mNotificationManager =
//		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		// mId allows you to update the notification later on.
//		mNotificationManager.notify(1, mBuilder.build());
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.d("CloudService", "onStart executed at"+new Date());
		dbMgr = DBManager.getInstance(this);
		nextStep = UPLOAD_FILE_STEP;
		collectFiles();
	}

	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("CloudService", "onDestory the cloud service");
		dbMgr.closeDB();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void collectFiles(){
		Thread t = new Thread(new FileScanner(dbMgr, this));
		t.start();		
	}
	
	private void uploadFiles(){
		ThreadGroup tg = new ThreadGroup("FileUploaders");
		
		FileUploader uploader = new FileUploader(dbMgr, this, this, handler);
		for(int i=0; i<2; i++){
			new Thread(tg, uploader).start();
		}
	}

	@Override
	public void onComplete() {
		// TODO Auto-generated method stub
		Log.d("CloudService", "completed one task");
		switch (nextStep) {
		case UPLOAD_FILE_STEP:
			nextStep = RESET_LOCK;
			Log.d("CloudService", "start upload files");
			uploadFiles();
			break;
		case RESET_LOCK:
			Log.d("CloudService", "do reset lock");
			dbMgr.resetLock();
			break;
		default:
			break;
		}
	}

	@Override
	public void onFailed(String msg) {
		// TODO Auto-generated method stub
		Log.d("CloudService", msg);
		
	}

}
