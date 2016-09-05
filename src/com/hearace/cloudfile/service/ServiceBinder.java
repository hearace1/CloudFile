package com.hearace.cloudfile.service;

import android.os.Binder;
import android.util.Log;

public class ServiceBinder extends Binder{
	
	private CloudService service;
	
	public ServiceBinder(CloudService service) {
		super();
		this.service = service;
	}

	public void startDownload(){
		Log.d("CloudService", "startDownload() executed");
		service.uploadFiles();
	}
	
	public void stopDownload(){
		Log.d("CloudService", "stopDownload() executed");
		service.stopUpload();
	}
	
	public boolean isDownloading(){
		return service.isDownloading();
	}
}

