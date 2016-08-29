package com.hearace.cloudfile.baidu;

import java.io.IOException;
import java.io.Serializable;


public class AccessToken implements Serializable{
	private static final long serialVersionUID = 6299864292833750409L;
	private static final String client_id = "g16DokdUogYR2FCq2gh8k1Uj";
	private static final String SCOPE = "basic,netdisk";
	private static final String accessTokenURL = "https://openapi.baidu.com/oauth/2.0/token";
	private static final String grant_type = "refresh_token";
	private static final String client_secret = "AQeDYqsPxxT0elFY7zUdOxEjfvrHVUaf";

	
	private String access_token = null;
	private long expires_in = 0;
	private String refresh_token = null;
	private String scope = null;
	private String session_key = null;
	private String session_secret = null;
	
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public long getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(long expires_in) {
		this.expires_in = expires_in;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getSession_key() {
		return session_key;
	}
	public void setSession_key(String session_key) {
		this.session_key = session_key;
	}
	public String getSession_secret() {
		return session_secret;
	}
	public void setSession_secret(String session_secret) {
		this.session_secret = session_secret;
	}
	
	
	public void refreshToken() throws Exception{
		String requestURL = accessTokenURL+"?grant_type="+grant_type+
		"&refresh_token="+refresh_token+
		"&client_id="+client_id+
		"&client_secret="+client_secret+
		"&scope="+SCOPE;
		System.out.println("###DEBUG###:"+requestURL);
		HttpResponse response = null;
		try {
			response = HttpClientUtil.Get(requestURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new Exception("Failed to get device code from baidu", e);
		}
		AccessToken token;
		if(response.getCode() == 200) {
			token = JSONUtil.getAccessTokenFromJSON(response.getContent());
			this.access_token = token.access_token;
			this.refresh_token = token.refresh_token;
		} else {
			// TODO Auto-generated catch block
			ErrorMsg err = JSONUtil.getErrorMsg(response.getContent());
			throw new Exception("Failed to logon:\n"+err.getError() + "\n"+err.getError_desc());
		}
	}
	
}
