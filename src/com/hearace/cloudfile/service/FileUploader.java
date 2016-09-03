package com.hearace.cloudfile.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import com.hearace.cloudfile.AppDefine;
import com.hearace.cloudfile.baidu.FileInfo;
import com.hearace.cloudfile.baidu.PCSAPI;
import com.hearace.cloudfile.baidu.QuotaInfo;
import com.hearace.cloudfile.model.AppConfig;
import com.hearace.cloudfile.model.BackupItem;
import com.hearace.cloudfile.model.DBManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class FileUploader implements Runnable {

	private DBManager dbMgr = null;
	private Task task = null;
	private Context context = null;
	Handler mbUiThreadHandler = null;
	PCSAPI api = null;
	
	public FileUploader(DBManager dbMgr, Task task, Context context, Handler handler) {
		super();
		this.dbMgr = dbMgr;
		this.task = task;
		this.context = context;
		this.mbUiThreadHandler = handler;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		String lockerId = String.valueOf(Thread.currentThread().getId());
		
		if(!isWifiConnected()){
			task.onFailed("no wifi connected");
			return;
		}
		try {
			api = getAPI();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			task.onFailed(e1.getMessage());
			return;
		}
		
		BackupItem bakItem = null;
		String cloudBase = AppDefine.mbRootPath+AppConfig.getInstance(dbMgr).getDeviceName()+File.pathSeparator;
		while((bakItem = dbMgr.lockBackItem(lockerId))!= null){
			Log.d("FileUploader", "lock and get the back item:"+bakItem);
			doUpload(bakItem);
		}
		Log.d("FileUploader", "Thread "+lockerId+" is completed. alive threads in group:"+Thread.currentThread().getThreadGroup().activeCount());
		if(Thread.currentThread().getThreadGroup().activeCount() <= 1)
			task.onComplete();
	}
	
	private PCSAPI getAPI() throws Exception{
		String token = AppConfig.getInstance(dbMgr).getServerToken();
		if(token == null)
			throw new Exception("No server token found!");
		PCSAPI api = new PCSAPI(token);
		QuotaInfo info = api.getQuota();
		return api;
			
	}
	
	private boolean isWifiConnected() {  
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);  
        if (cm != null) {  
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
            if (networkInfo != null  
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
                return true;  
            }  
        }  
        return false;  
    }  

	private int doUpload(BackupItem item){
		Log.d("FileUploader", "Start upload item:"+item.toString());
		File f = new File(item.getFileStr());
		String target = AppDefine.mbRootPath+AppConfig.getInstance(dbMgr).getDeviceName()+File.separator+item.getCloudpath();
		if(!target.endsWith(File.separator)){
			target = target+ File.separator;			
		}
		target = target+ f.getName();
		Log.d("FileUploader", "upload to:"+target);
		if(f.length() > 1024*1024){
			Log.d("FileUploader", "upload item in pieces");
			try {
				RandomAccessFile accFile = new RandomAccessFile(f, "r");
				long pos = item.getProgress();
				int bufferSize = 1 + (int)(f.length()/(1024*1024*1024));
				bufferSize = bufferSize *1024 *1024;
				byte[] buff = new byte[bufferSize];
				while(pos < f.length()){
					accFile.seek(pos);
					int i = accFile.read(buff);					
					pos += i;
					if(i < buff.length){
						byte[] newBuff = new byte[i];
						System.arraycopy(buff, 0, newBuff, 0, i);
						buff = newBuff;
					}
					String md5 = api.uploadFilePiece(buff, "temp");
					item.addMd5(md5);
					dbMgr.updateProgress(item.get_id(), pos, item.getMd5s());
				}
				FileInfo info = api.createSuperFile(item.getMd5s(), target);
//				dbMgr.delBackItem(item.get_id());
				dbMgr.uploadSuccess(item.get_id());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dbMgr.uploadFail(item.get_id(), e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dbMgr.uploadFail(item.get_id(), e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dbMgr.uploadFail(item.get_id(), e.getMessage());
			}
		}else{
			try {
				api.uploadFile(target, item.getFileStr());
//				dbMgr.delBackItem(item.get_id());
				dbMgr.uploadSuccess(item.get_id());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dbMgr.uploadFail(item.get_id(), e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dbMgr.uploadFail(item.get_id(), e.getMessage());
			}
		}
		return 1;
	}
	
	private String getMd5ByFile(File file) throws FileNotFoundException {  
        String value = null;  
        FileInputStream in = new FileInputStream(file);  
    try {  
        MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());  
        MessageDigest md5 = MessageDigest.getInstance("MD5");  
        md5.update(byteBuffer);  
        BigInteger bi = new BigInteger(1, md5.digest());  
        value = bi.toString(16);  
    } catch (Exception e) {  
        e.printStackTrace();  
    } finally {  
            if(null != in) {  
                try {  
                in.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
    return value;  
    }  
}
