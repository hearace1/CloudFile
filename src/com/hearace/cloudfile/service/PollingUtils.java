package com.hearace.cloudfile.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class PollingUtils {
    //开启轮询服务
    public static void startPollingService(Context context, long time, Class<?> cls,String action) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
                                                                                                                                                                                                                                       
        //包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                                                                                                                                                                                                       
        //触发服务的起始时间
        long triggerAtTime = time;
                                                                                                                                                                                                                                       
        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
                24* 3600 * 1000, pendingIntent);
    }
    //停止轮询服务
    public static void stopPollingService(Context context, Class<?> cls,String action) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }
    
    public static boolean isSetPollingService(Context context, Class<?> cls, String action){
    	Intent intent = new Intent(context, cls);
        intent.setAction(action);
    	boolean alarmUp = (PendingIntent.getService(context, 0, 
    	        intent, 
    	        PendingIntent.FLAG_NO_CREATE) != null);

    	if (alarmUp)
    	{
    	    Log.d("PollingUtils", "Alarm is already active");
    	}else{
    		Log.d("PollingUtils", "Alarm is not active");
    	}
    	return alarmUp;
    }
}
