package com.hearace.cloudfile.baidu;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONUtil {

	public static String getProperty(String JSON, String propertyName) throws JSONException {
		JSONTokener jsonParser = new JSONTokener(JSON);
		JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
		try {
			return jsonObject.getString(propertyName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static AccessToken getAccessTokenFromJSON(String JSON) throws JSONException {
		JSONObject jsonObject = parseJSON(JSON);
		AccessToken token = new AccessToken();
		token.setAccess_token(jsonObject.getString("access_token"));
		token.setExpires_in(jsonObject.getLong("expires_in"));
		token.setRefresh_token(jsonObject.getString("refresh_token"));
		token.setScope(jsonObject.getString("scope"));
		token.setSession_key(jsonObject.getString("session_key"));
		token.setSession_secret(jsonObject.getString("session_secret"));
		return token;
	}

	public static ErrorMsg getErrorMsg(String JSON) throws JSONException {
		JSONObject jsonObject = parseJSON(JSON);
		ErrorMsg err = new ErrorMsg();
		err.setError(jsonObject.getString("error"));
		err.setError_desc(jsonObject.getString("error_description"));
		return err;
	}

	public static QuotaInfo parseQuotaInfo(String JSON) throws JSONException{
		JSONObject jsonObject = parseJSON(JSON);
		QuotaInfo quota = new QuotaInfo();
		quota.setQuota(jsonObject.getLong("quota"));
		quota.setUsed(jsonObject.getLong("used"));
		return quota;
	}

	public static FileInfo parseFileInfo(String JSON) throws JSONException{
		JSONObject jsonObject = parseJSON(JSON);
		FileInfo info = new FileInfo();
		info.setPath(jsonObject.getString("path"));
		info.setSize(jsonObject.getLong("size"));
		info.setCtime(jsonObject.getLong("ctime"));
		info.setMtime(jsonObject.getLong("mtime"));
		try {
			info.setMd5(jsonObject.getString("md5"));
			info.setIsDir(jsonObject.getInt("isdir") == 1);
			info.setIfHasSubDir((jsonObject.getInt("ifhassubdir"))==1);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return info;
	}

	private static JSONObject parseJSON(String jsonStr) throws JSONException {
		JSONTokener jsonParser = new JSONTokener(jsonStr);
		JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
		return jsonObject;
	}
}
