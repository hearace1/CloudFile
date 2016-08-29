package com.hearace.cloudfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import com.hearace.cloudfile.baidu.FileInfo;
import com.hearace.cloudfile.baidu.PCSAPI;
import com.hearace.cloudfile.baidu.QuotaInfo;
import com.hearace.cloudfile.model.AppConfig;
import com.hearace.cloudfile.model.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TestActivity extends Activity {
	DBManager dbMgr;
	TextView pathTV;
	String basicPath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		pathTV = (TextView)findViewById(R.id.FilePathTV);
		dbMgr = DBManager.getInstance(this);
		basicPath = Environment.getExternalStorageDirectory().getPath();
	}
	
	public void selectFileOnClick(View v){
		Log.d("TestActivity", "start upload files");
		
		try {
//			PCSAPI api = getAPI();
			Intent intent = new Intent(this, FileBrowser.class);
			intent.putExtra("needFile", true);
//	        intent.putExtra("pathString", null);
	        startActivityForResult(intent, 2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void uploadOnClick(View v){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					PCSAPI api = getAPI();
					String localPath = basicPath + pathTV.getText().toString();
					String remotePath = AppDefine.mbRootPath+"test"+localPath.substring(localPath.lastIndexOf("/"));
					Log.d("uploadOnClick", "localPath:"+localPath+"; remotePath"+remotePath);
					File f = new File(localPath);
					if(f.length() > 100*1024){
						Log.d("FileUploader", "upload item in pieces");
						try {
							RandomAccessFile accFile = new RandomAccessFile(f, "r");
							long pos = 0;
							String md5s = "";
							int bufferSize = 1 + (int)(f.length()/(1024*1024*1024));
							bufferSize = bufferSize *100 *1024;
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
								if(md5s.length() > 0){
									md5s += ",";
									md5s += md5;
								}else{
									md5s = md5;
								}
								Log.d("uploading", "uploaded "+pos);
//								dbMgr.updateProgress(item.get_id(), pos, item.getMd5s());
							}
							FileInfo info = api.createSuperFile(md5s, remotePath);
							Log.d("TestActivity", "cloud md5:"+info.getMd5()+"; local md5:"+getMd5ByFile(f));
							if(info.getMd5() == getMd5ByFile(f)){
								Log.d("uploadOnClick", "upload success");
							}
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}else{
						FileInfo info = api.uploadFile(remotePath, localPath);
						Log.d("uploadOnClick", info.toString());						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			private PCSAPI getAPI() throws Exception{
				String token = AppConfig.getInstance(dbMgr).getServerToken();
				if(token == null)
					throw new Exception("No server token found!");
				PCSAPI api = new PCSAPI(token);
				QuotaInfo info = api.getQuota();
				Log.d("getAPI", info.toString());
				return api;
					
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
		});
		t.start();
	}
	


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 2:
			if(resultCode == RESULT_OK){
				String newPath = data.getStringExtra("pathString");
				Log.d("TestActivity", newPath);
				pathTV.setText(newPath);
			}
			break;

		default:
			break;
		}
	}
	
}
