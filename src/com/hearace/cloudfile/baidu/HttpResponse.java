package com.hearace.cloudfile.baidu;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class HttpResponse {

	private int code = -1;
	private String content = null;
	private InputStream is = null;
	private Map<String, List<String>> headers = null;
	public HttpResponse(int code, Map<String, List<String>> headers, String content) {
		super();
		this.code = code;
		this.content = content;
		this.headers = headers;
	}
	public HttpResponse(int code, Map<String, List<String>> headers, InputStream is){
		super();
		this.code = code;
		this.is = is;
		this.headers = headers;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public InputStream getInputStream() {
		return is;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public List<String> getHeader(String headerName){
		return headers.get(headerName);
	}
	
}
