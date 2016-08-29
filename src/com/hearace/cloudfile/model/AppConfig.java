package com.hearace.cloudfile.model;

import android.database.Cursor;
import android.util.Log;

public class AppConfig {
	
	public static final String TABLE_NAME = "AppConfig";
	
	private static AppConfig config = null;
	private String userName = null;
	private String serverToken = null;
	private String deviceName = null;
	
	private DBManager dbMgr = null;

	private AppConfig(DBManager dbMgr) {
		super();
		this.dbMgr = dbMgr;
	}
	
	public static AppConfig getInstance(DBManager dbMgr){
		if(config == null){
			config = new AppConfig(dbMgr);
			config.init();
		}
		return config;
	}
	
	public void init(){
		Cursor cur = dbMgr.getAllConfig();
		int propertyIdx = cur.getColumnIndex("property");
		int valueIdx = cur.getColumnIndex("value");
		while(cur.moveToNext()){
			String property = cur.getString(propertyIdx);
			String value = cur.getString(valueIdx);
			if("username".equalsIgnoreCase(property)){
				this.userName = value;
			}else if("token".equalsIgnoreCase(property)){
				this.serverToken = value;
			}else if("devicename".equalsIgnoreCase(property)){
				this.deviceName = value;
			}
		}
		if(this.deviceName == null){
			setDeviceName(null);
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
		dbMgr.setConfigProperty("username", userName);
	}

	public String getServerToken() {
		return serverToken;
	}

	public void setServerToken(String serverToken) {
		this.serverToken = serverToken;
		dbMgr.setConfigProperty("token", serverToken);
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		if(deviceName == null){
			deviceName = android.os.Build.MODEL;
			Log.d("AppConfig", "Use the default device name:"+deviceName);
		}
		this.deviceName = deviceName;
		dbMgr.setConfigProperty("devicename", deviceName);
	}
	
	
}
