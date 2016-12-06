package com.hearace.cloudfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowser extends ListActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
//	private File currentPath = null;
	private String basicPathStr = null;
	private String filePathStr = null;
    TextView pathText = null;
    private boolean needFile = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);
		pathText = (TextView)findViewById(R.id.path_content);
		basicPathStr = Environment.getExternalStorageDirectory().getPath();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        String comingPath = getIntent().getStringExtra("pathString");
        needFile = getIntent().getBooleanExtra("needFile", false);
        if(comingPath !=null){
            filePathStr = comingPath;
        }else {
            filePathStr = "";
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1234);
//            	fill();
            }else{
            	fill();
            }
        } else {
        	fill();
        }        
    }

    private void fill(){
        pathText.setText(filePathStr);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData());
        setListAdapter(adapter);
    }

    private List<String> getData(){
        List<String> fileList = null;
		try {
			fileList = new ArrayList<String>();
			File currentPath = new File(basicPathStr+filePathStr);
			Log.d("FileBrowser", "currentPath obj:"+currentPath);
			if(null != filePathStr && filePathStr.lastIndexOf(File.separator) >= 0){
			    fileList.add("..");
			}
			for(File f : currentPath.listFiles()){
			    if(f.isDirectory()){
				    fileList.add(File.separator+f.getName());
			    }else{
				    fileList.add(f.getName());
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
        return fileList;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String subfolder = (String) getListAdapter().getItem(position);
        String newPathStr = null;
        if("..".equals(subfolder)){
        	newPathStr = filePathStr.substring(0, filePathStr.lastIndexOf(File.separator));
        }else if(subfolder.startsWith(File.separator)){
        	newPathStr = filePathStr+subfolder;
        }
        else {
            newPathStr = filePathStr+ File.separator +subfolder;
        }
        File newPath = new File(basicPathStr+newPathStr);
        if (newPath.isDirectory()) {
            filePathStr = newPathStr;
            fill();
        } else {
        	if(needFile){
        		filePathStr = newPathStr;
        		okOnClick(null);
        	}else{
                Toast.makeText(this, newPath.getPath() + " is not a directory", Toast.LENGTH_LONG).show();        		
        	}
        }
    }

    public void okOnClick(View view){
        Intent intent = getIntent();
        intent.putExtra("pathString", filePathStr);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelOnClick(View view){
        finish();
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		//super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		fill();
	}
    
    
}
