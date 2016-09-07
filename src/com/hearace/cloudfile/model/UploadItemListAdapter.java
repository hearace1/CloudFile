package com.hearace.cloudfile.model;

import java.io.File;

import com.hearace.cloudfile.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadItemListAdapter extends BaseAdapter {

	private Context context = null;
	private LayoutInflater mInflater = null;
	private Cursor cur = null;
	private ContentResolver cr = null;
	public UploadItemListAdapter(Context context, Cursor cur) {
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		this.cur = cur;
		cr = context.getContentResolver();  
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
	
	public void updateDate(Cursor cur){
		this.cur = cur;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		cur.moveToPosition(position);
		ViewHolderItem viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolderItem();
			convertView = mInflater.inflate(R.layout.upload_list_item, null);
			viewHolder.fileNameTextView = (TextView)convertView.findViewById(R.id.uploadFileNameTV);
			viewHolder.uploadProgress = (ProgressBar)convertView.findViewById(R.id.uploadProgress);
			viewHolder.uploadProgress.setMax(100);
//			viewHolder.uploadStatusTextView = (TextView)convertView.findViewById(R.id.uploadStatus);
			viewHolder.errMsgTextView = (TextView)convertView.findViewById(R.id.errMsgTV);
			viewHolder.fileId = (TextView)convertView.findViewById(R.id.fileId);
			viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		int fileId = cur.getInt(cur.getColumnIndex("_id"));
		String fileName = cur.getString(cur.getColumnIndex("file"));
		viewHolder.fileNameTextView.setText(fileName);
		viewHolder.fileId.setText(String.valueOf(fileId));
		Bitmap thumbnail = getThumbnail(fileName);
		if(thumbnail != null)
			viewHolder.thumbnail.setImageBitmap(thumbnail);

		int status = cur.getInt(cur.getColumnIndex("status"));
		int progressPercent = 0;
		long progress = cur.getLong(cur.getColumnIndex("progress"));
		long fileSZ = cur.getLong(cur.getColumnIndex("fileSZ"));
		switch(status){
		case 100:
			progressPercent = 100;
			viewHolder.errMsgTextView.setVisibility(TextView.GONE);
//			viewHolder.uploadStatusTextView.setText("Completed");
			viewHolder.uploadProgress.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
			break;
		case -1:
			progressPercent = (int) (progress * 100 / fileSZ);
//			viewHolder.uploadStatusTextView.setText("Failed");
			String errMsg = cur.getString(cur.getColumnIndex("errMsg"));
			viewHolder.errMsgTextView.setText(errMsg);
			viewHolder.errMsgTextView.setVisibility(TextView.VISIBLE);
			viewHolder.uploadProgress.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
			break;
		case 1:
			progressPercent = (int) (progress * 100 / fileSZ);
			viewHolder.errMsgTextView.setVisibility(TextView.GONE);
//			viewHolder.uploadStatusTextView.setText("Uploading");
			viewHolder.uploadProgress.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
			break;
		case 0:
			progressPercent = 0;
//			viewHolder.uploadStatusTextView.setText("Wait");
			viewHolder.errMsgTextView.setVisibility(TextView.GONE);
			break;
		}
		viewHolder.uploadProgress.setProgress(progressPercent);
		return convertView;
	}
	
	private Bitmap getThumbnail(String filePath){
		Cursor cur1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
				new String[]{
						MediaStore.Images.Media._ID
		}, MediaStore.Images.Media.DATA + "=?", new String[]{filePath}, null);
		if(cur1.moveToFirst()){
			Bitmap bm = Thumbnails.getThumbnail(cr, cur1.getInt(0), Thumbnails.MICRO_KIND, null);
			return bm;
		}
		return null;
	}
	
	static class ViewHolderItem{
		TextView fileNameTextView;
		ProgressBar uploadProgress;
//		TextView uploadStatusTextView;
		TextView errMsgTextView;
		TextView fileId;
		ImageView thumbnail;
	}
}

