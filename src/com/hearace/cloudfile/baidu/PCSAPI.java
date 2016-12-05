package com.hearace.cloudfile.baidu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;

import org.apache.http.entity.mime.content.*;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PCSAPI {
	private final String quotaURL = "https://pcs.baidu.com/rest/2.0/pcs/quota";
	private final String fileURL = "https://pcs.baidu.com/rest/2.0/pcs/file";

	private String token = null;

	public PCSAPI(String token) {
		this.token = token;
	}

	public QuotaInfo getQuota() throws Exception {
		String requestURL = quotaURL + "?method=info";
		requestURL = setAccessToken(requestURL);
		HttpResponse response = null;
		try {
			response = HttpClientUtil.Get(requestURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new Exception("Failed to get netdisk quota", e);
		}

		QuotaInfo quotaInfo = null;
		if (response.getCode() == 200)
			quotaInfo = JSONUtil.parseQuotaInfo(response.getContent());
		else {
			// TODO Auto-generated catch block
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to get netdisk quota:\n" + err.getError() + "\n" + err.getError_desc());
		}
		return quotaInfo;
	}

	public FileInfo uploadFile(String remotePath, String localPath) throws FileNotFoundException, Exception {
		String requestURL = fileURL + "?method=upload&ondup=overwrite";
		requestURL = setAccessToken(requestURL);
		requestURL += "&path=" + URLEncoder.encode(remotePath, "UTF-8");

		File localFile = new File(localPath);
		if (!localFile.exists() || localFile.isDirectory()) {
			throw new FileNotFoundException(localFile.getAbsolutePath());
		}
		MultipartEntity builder = new MultipartEntity();
		ContentBody bsData = new FileBody(localFile);
		builder.addPart("file", bsData);
		HttpResponse response = null;
		try {
			response = HttpClientUtil.multipost(requestURL, builder);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw e;
		}

		if (response == null)
			return null;
		FileInfo fileInfo = null;
		if (response.getCode() == 200)
			fileInfo = JSONUtil.parseFileInfo(response.getContent());
		else {
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to upload file:\n" + err.getError() + "\n" + err.getError_desc());
		}
		return fileInfo;
	}
	
	public boolean downloadFile(String remotePath, String localPath) throws Exception{
		String url = fileURL + "?method=download&path="+URLEncoder.encode(remotePath, "UTF-8");
		url = setAccessToken(url);
		HttpResponse response = HttpClientUtil.Get(url, null, true);
		if(response == null)
			return false;
		if(response.getCode() == 200){
			FileOutputStream fos = new FileOutputStream(localPath);
			InputStream is = response.getInputStream();
			if(fos != null && is != null){
				byte[] buffer = new byte[51200];
				int count = 0;
				try {
					while((count = is.read(buffer)) > 0){
						fos.write(buffer, 0, count);
					}
				} finally {
					fos.close();
				}
			}
		}else {
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to get meta file:\n" + err.getError() + "\n" + err.getError_desc());
		}
		return true;
	}
	
	public FileInfo getFileMeta(String remotePath) throws Exception{
		String url = fileURL + "?method=meta&path="+URLEncoder.encode(remotePath, "UTF-8");
		url = setAccessToken(url);
		HttpResponse response = HttpClientUtil.Get(url);
		if (response == null)
			return null;
		FileInfo fileInfo = null;
		if (response.getCode() == 200)
			fileInfo = JSONUtil.parseFileInfo(response.getContent());
		else {
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to get meta file:\n" + err.getError() + "\n" + err.getError_desc());
		}
		return fileInfo;
	}

	public String uploadFilePiece(byte[] bytes, String file) throws Exception {
		String url = fileURL + "?method=upload&ondup=overwrite";
		url = setAccessToken(url);
		url += "&type=tmpfile";

		MultipartEntity fileEntity = new MultipartEntity();
		ContentBody bsData = new ByteArrayBody(bytes, file);
		fileEntity.addPart("uploadedfile", bsData);

		HttpResponse response = HttpClientUtil.multipost(url, fileEntity);

		if (response.getCode() == 200) {
			return JSONUtil.getProperty(response.getContent(), "md5");
		} else {
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to upload file:\n" + err.getError() + "\n" + err.getError_desc());
		}
	}

	public FileInfo createSuperFile(String md5s, String target) throws Exception {

		String url = fileURL + "?method=createsuperfile&ondup=overwrite";
		url = setAccessToken(url);
		url += "&path=" + URLEncoder.encode(target, "UTF-8");
		if (md5s != null) {
			List<String> md5List = Arrays.asList(md5s.split(","));
			JSONArray json = new JSONArray(md5List);
			Map<String, JSONArray> map = new HashMap<String, JSONArray>();
			map.put("block_list", json);

			JSONObject md5list = new JSONObject(map);
			MultipartEntity fileEntity = new MultipartEntity();
			Log.d("PCSAPI", "the md5list json string:" + md5list.toString());
			ContentBody bsData = new StringBody(md5list.toString());

			fileEntity.addPart(new FormBodyPart("param", bsData));
			HttpResponse response = HttpClientUtil.multipost(url, fileEntity);

			if (response.getCode() == 200) {
				return JSONUtil.parseFileInfo(response.getContent());
			} else {
				ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
				throw new Exception("Failed to upload file:\n" + err.getError() + "\n" + err.getError_desc());
			}
		}
		return null;
	}

	private String setAccessToken(String URL) {
		if (URL.indexOf("?") > 0) {
			return URL + "&access_token=" + token;
		} else {
			return URL + "?access_token=" + token;
		}
	}
}
