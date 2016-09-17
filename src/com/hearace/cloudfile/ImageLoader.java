package com.hearace.cloudfile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ListView;

public class ImageLoader {

	private ImageView mImageView;
	private String mUrl;
	private LruCache<String, Bitmap> mMemoryCaches;
	private Set<NewsAsyncTask> mTasks;
	private ListView mListView;

	public String mUrls[];
	
	private ContentResolver cr;

	public ImageLoader(ListView listView, ContentResolver cr) {

		this.mListView = listView;
		this.cr = cr;

		mTasks = new HashSet<NewsAsyncTask>();

		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSizes = maxMemory / 5;

		mMemoryCaches = new LruCache<String, Bitmap>(cacheSizes) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};

	}

	public Bitmap getBitmapFromLrucache(String url) {

		return mMemoryCaches.get(url);
	}

	public void addBitmapToLrucaches(String url, Bitmap bitmap) {

		if (getBitmapFromLrucache(url) == null) {
			mMemoryCaches.put(url, bitmap);
		}

	}

	public void loadImages(int start, int end) {

		for (int i = start; i < end; i++) {
			String loadUrl = mUrls[i];
			if (getBitmapFromLrucache(loadUrl) != null) {
				ImageView imageView = (ImageView) mListView
						.findViewWithTag(loadUrl);
				if(imageView == null){
					Log.d("ImageLoader", "get image view from"+start+"to"+end+" as null @"+i+",the url:"+loadUrl);
				}
				else{
					imageView.setImageBitmap(getBitmapFromLrucache(loadUrl));					
				}
			} else {
				NewsAsyncTask mNewsAsyncTask = new NewsAsyncTask(loadUrl);
				mTasks.add(mNewsAsyncTask);
				mNewsAsyncTask.execute(loadUrl);
			}
		}
	}

	public void showImage(ImageView imageView, String url) {

		Bitmap bitmap = getBitmapFromLrucache(url);
		if (bitmap == null) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	public void cancelAllAsyncTask() {
		if (mTasks != null) {
			for (NewsAsyncTask newsAsyncTask : mTasks) {
				newsAsyncTask.cancel(false);
			}
		}

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);
			}
		};
	};

	// 1.多线程的方法
	public void showImageByThead(ImageView iv, final String url) {
		mImageView = iv;
		mUrl = url;
		new Thread() {
			public void run() {
				Bitmap bitmap = getBitmapFromUrl(url);
				Message message = Message.obtain();
				message.obj = bitmap;
				mHandler.sendMessage(message);
			};
		}.start();
	}

//	public Bitmap getBitmapFromUrl(String urlString) {
//		Bitmap bitmap;
//		InputStream is = null;
//		try {
//			URL mUrl = new URL(urlString);
//			HttpURLConnection connection = (HttpURLConnection) mUrl
//					.openConnection();
//			is = new BufferedInputStream(connection.getInputStream());
//			bitmap = BitmapFactory.decodeStream(is);
//			connection.disconnect();
//			return bitmap;
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}
	
	public Bitmap getBitmapFromUrl(String urlString){
		String mimeType = getMimeType(urlString);
		Cursor cur1;
		if(mimeType.startsWith("image")){
			cur1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
					new String[]{
							MediaStore.Images.Media._ID
			}, MediaStore.Images.Media.DATA + "=?", new String[]{urlString}, null);
			if(cur1.moveToFirst()){
				Bitmap bm = Thumbnails.getThumbnail(cr, cur1.getInt(0), Thumbnails.MICRO_KIND, null);
				return bm;
			}
		}
		else if(mimeType.startsWith("video")){
			cur1 = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
					new String[]{
							MediaStore.Images.Media._ID
			}, MediaStore.Images.Media.DATA + "=?", new String[]{urlString}, null);
			if(cur1.moveToFirst()){
				Bitmap bm = MediaStore.Video.Thumbnails.getThumbnail(cr, cur1.getInt(0), Thumbnails.MICRO_KIND, null);
				return bm;
			}
		}else
			return null;
		
		return null;
	}
	
	public static String getMimeType(String url) {
	    String type = null;
	    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
	    if (extension != null) {
	        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	    }
	    return type;
	}

	public void showImageByAsyncTask(String url) {
		// 首先去LruCache中去找图片
		Bitmap bitmap = getBitmapFromLrucache(url);
		// 如果不为空，说明LruCache中已经缓存了该图片，则读取缓存直接显示，
		if (bitmap != null) {
			ImageView imageView = (ImageView) mListView.findViewWithTag(url);
			imageView.setImageBitmap(bitmap);
		} else {
			// 如果缓存中没有的话就开启异步任务去下载图片，
			new NewsAsyncTask(url).execute(url);
		}
	}

	class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

		private String mUrl;

		public NewsAsyncTask(String url) {
			mUrl = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			String url = params[0];
			Bitmap bitmap;

			bitmap = getBitmapFromUrl(url);
			// 下载完成之后将其加入到LruCache中这样下次加载的时候，就可以直接从LruCache中直接读取
			if (bitmap != null) {
				addBitmapToLrucaches(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			// if (myImageView.getTag().equals(mUrl)) {
			// myImageView.setImageBitmap(bitmap);
			// }

			ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);

			if (bitmap != null && imageView != null) {
				imageView.setImageBitmap(bitmap);
			}

			mTasks.remove(this);

		}

	}

}
