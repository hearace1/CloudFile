package com.hearace.cloudfile.model;

import com.hearace.cloudfile.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadItemListAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private Cursor cur = null;
	
	public UploadItemListAdapter(Context context, Cursor cur) {
		this.mInflater = LayoutInflater.from(context);
		this.cur = cur;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cur.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(cur.moveToPosition(position)){
			return cur;
		}
		else{
			return null;
		}
		
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if(cur.moveToPosition(position)){
			return cur.getInt(cur.getColumnIndex("_id"));
		}else{
			return -1;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolderItem viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolderItem();
			convertView = mInflater.inflate(R.layout.upload_list_item, null);
			viewHolder.fileNameTextView = (TextView)convertView.findViewById(R.id.uploadFileNameTV);
			viewHolder.uploadProgress = (ProgressBar)convertView.findViewById(R.id.uploadProgress);
			viewHolder.uploadProgress.setMax(100);
			viewHolder.uploadStatusTextView = (TextView)convertView.findViewById(R.id.uploadStatus);
			viewHolder.errMsgTextView = (TextView)convertView.findViewById(R.id.errMsgTV);
			viewHolder.fileId = (TextView)convertView.findViewById(R.id.fileId);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		cur.moveToPosition(position);
		int status = cur.getInt(cur.getColumnIndex("status"));
		int fileId = cur.getInt(cur.getColumnIndex("_id"));
		String fileName = cur.getString(cur.getColumnIndex("file"));
		viewHolder.fileNameTextView.setText(fileName);
		viewHolder.fileId.setText(String.valueOf(fileId));
		int progressPercent = 0;
		long progress = cur.getLong(cur.getColumnIndex("progress"));
		long fileSZ = cur.getLong(cur.getColumnIndex("fileSZ"));
		switch(status){
		case 100:
			progressPercent = 100;
			viewHolder.uploadStatusTextView.setText("Completed");
			break;
		case -1:
			progressPercent = (int) (progress / fileSZ);
			viewHolder.uploadStatusTextView.setText("Failed");
			String errMsg = cur.getString(cur.getColumnIndex("errMsg"));
			viewHolder.errMsgTextView.setText(errMsg);
			viewHolder.errMsgTextView.setVisibility(TextView.VISIBLE);
			break;
		case 1:
			progressPercent = (int) (progress / fileSZ);
			viewHolder.uploadStatusTextView.setText("Uploading");
			break;
		}
		viewHolder.uploadProgress.setProgress(progressPercent);
		return convertView;
	}
	
	static class ViewHolderItem{
		TextView fileNameTextView;
		ProgressBar uploadProgress;
		TextView uploadStatusTextView;
		TextView errMsgTextView;
		TextView fileId;
	}
}

