package com.hearace.cloudfile.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class PollingUtils {
    //������ѯ����
    public static void startPollingService(Context context, long time, Class<?> cls,String action) {
        //��ȡAlarmManagerϵͳ����
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
                                                                                                                                                                                                                                       
        //��װ��Ҫִ��Service��Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                                                                                                                                                                                                       
        //�����������ʼʱ��
        long triggerAtTime = time;
                                                                                                                                                                                                                                       
        //ʹ��AlarmManger��setRepeating�������ö���ִ�е�ʱ������seconds�룩����Ҫִ�е�Service
        manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
                24* 3600 * 1000, pendingIntent);
    }
    //ֹͣ��ѯ����
    public static void stopPollingService(Context context, Class<?> cls,String action) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //ȡ������ִ�еķ���
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
