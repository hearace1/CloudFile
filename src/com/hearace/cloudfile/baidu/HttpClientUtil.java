package com.hearace.cloudfile.baidu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.http.entity.mime.MultipartEntity;

import android.util.Log;

public class HttpClientUtil {

	public static HttpResponse Get(String urlStr, Map<String, String> headers, boolean withContentStream) throws IOException{
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		if(headers != null && headers.size() > 0){
			for(String name: headers.keySet()){
				String value = headers.get(name);
				conn.setRequestProperty(name, value);
			}
		}
		conn.setDoInput(true);
		conn.setDoOutput(false);
		try {
			InputStream in = new BufferedInputStream(conn.getInputStream());
			if(withContentStream){
				return new HttpResponse(conn.getResponseCode(), conn.getHeaderFields(), conn.getInputStream());
			}else{
				return new HttpResponse(conn.getResponseCode(), conn.getHeaderFields(), readContent(conn.getInputStream()));				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("HttpClientUtil", e.getMessage());
		} finally {
			if(conn != null)
				conn.disconnect();
		}
		return null;
	}
	
	public static HttpResponse Get(String URL) throws IOException{
		return Get(URL, null, false);
	}
	
	public static HttpResponse multipost(String urlString, MultipartEntity reqEntity) throws Exception {
		HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.addRequestProperty("Content-length", ""+reqEntity.getContentLength());
            conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

            OutputStream os = conn.getOutputStream();
            reqEntity.writeTo(conn.getOutputStream());
            os.close();
            conn.connect();

            return new HttpResponse(conn.getResponseCode(), conn.getHeaderFields(), readContent(conn.getInputStream()));

        } catch (Exception e) {
        	e.printStackTrace();
            Log.e("HttpClientUtil", "multipart post error " + e + "(" + urlString + ")");
            throw e;
        } finally {
			if(conn != null)
				conn.disconnect();
		}
    }
	
	private static String readContent(InputStream in) throws IOException{
		StringBuffer strBuff = new StringBuffer();
		char[] buff = new char[1024];
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		int l = -1;
		while((l = br.read(buff)) > 0){
			strBuff.append(buff, 0, l);
		}
		return strBuff.toString();
	}
}
