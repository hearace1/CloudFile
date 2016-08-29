package com.hearace.cloudfile;

import java.io.File;

import com.hearace.cloudfile.model.DBManager;
import com.hearace.cloudfile.model.PathItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class PathConfigure extends Activity implements CheckBox.OnCheckedChangeListener  {
	private  final int GET_PATH_CODE=100;
	
	private PathItem item = new PathItem();
	
	EditText pathText, dayText, cloudPathText;
    CheckBox deleteChkBox, reorgFld;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path_configure);
		
		pathText = (EditText)findViewById(R.id.path_edit);
        deleteChkBox = (CheckBox)findViewById(R.id.delFileChk);
        deleteChkBox.setOnCheckedChangeListener(this);
        dayText = (EditText)findViewById(R.id.dayNumEdit);
        cloudPathText = (EditText)findViewById(R.id.cloud_path_edit);
        reorgFld = (CheckBox)findViewById(R.id.reorgFldChk);
        Intent i = getIntent();
        String pathIDStr = i.getStringExtra("pathID");
        int pathId = -1;
        try{
            pathId = Integer.parseInt(pathIDStr);
            Log.d("PathConfigure", "receive the input pathId:"+pathId);
            DBManager dbMgr = DBManager.getInstance(this);
            item = dbMgr.getPathById(String.valueOf(pathId));
            Log.d("PathConfigure", "get item from db:"+item);
        }catch (Exception e){
        }
        fillPage();
	}
	
	private void fillPage(){
        if(item != null){
            pathText.setText(item.getLocalPath());
            cloudPathText.setText(item.getCloudPath());
           	dayText.setText(String.valueOf(item.getDelDayNum()));
            deleteChkBox.setChecked(item.isDelFile());
            reorgFld.setChecked(item.isReorgFld());

        }
    }
	
	public void startChoosePath(View view){
		Intent intent = new Intent(this, FileBrowser.class);
//        intent.putExtra("pathString", null);
        startActivityForResult(intent, GET_PATH_CODE);
	}
	
	public void okOnClick(View view){
		String localPathStr = pathText.getText().toString();
        String cloudPathString = cloudPathText.getText().toString();
        boolean delFile = deleteChkBox.isChecked();
        int delDayNum = 30;
        try{
        	delDayNum = Integer.parseInt(dayText.getText().toString());
        }catch(Exception e){
        	
        }
        boolean reorgFldB = reorgFld.isChecked();
        item.setLocalPath(localPathStr);
        item.setCloudPath(cloudPathString);
        item.setDelFile(delFile);
        item.setDelDayNum(delDayNum);
        item.setReorgFld(reorgFldB);
        DBManager dbMgr = DBManager.getInstance(this);
        if(item._id == -1)
        	dbMgr.addPath(item);
        else
        	dbMgr.updatePathConf(item);
		
		Intent intent = getIntent();
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void cancelOnClick(View vew){
		finish();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == GET_PATH_CODE){
            if(resultCode == RESULT_OK){
                String newPath = intent.getStringExtra("pathString");
                if(newPath != null){
                    this.item.setLocalPath(newPath);
                    if(item.cloudPath == null || "".equals(item.cloudPath)){
                        String[] folders = newPath.split(File.separator);
                        this.item.setCloudPath(folders[folders.length-1]);                    	
                    }
                    fillPage();
                }
            }
        }
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		Log.d("PathConfigure", buttonView.getId()+":"+isChecked);
		switch (buttonView.getId()){
        case R.id.delFileChk:
            dayText.setEnabled(isChecked);
            Log.d("EnableText", "Set the text enable:"+isChecked);
            break;
    }
	}
}
