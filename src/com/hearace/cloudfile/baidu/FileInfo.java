package com.hearace.cloudfile.baidu;

import java.util.Date;

public class FileInfo {

	private String path = null;
	private long size = -1;
	private long ctime = -1;
	private Date ctimeDate = null;
	private long mtime = -1;
	private Date mtimeDate = null;
	private String md5 = null;
	private boolean isDir = false;
	private boolean ifHasSubDir = false;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
		this.ctimeDate = new Date(ctime*1000);
	}
	public long getMtime() {
		return mtime;
	}
	public void setMtime(long mtime) {
		this.mtime = mtime;
		this.mtimeDate = new Date(mtime*1000);
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public Date getCtimeDate() {
		return ctimeDate;
	}
	public Date getMtimeDate() {
		return mtimeDate;
	}
	public void setIsDir(boolean isDir){
		this.isDir = isDir;
	}
	public boolean getIsDir(){
		return isDir;
	}
	public boolean isIfHasSubDir() {
		return ifHasSubDir;
	}
	public void setIfHasSubDir(boolean ifHasSubDir) {
		this.ifHasSubDir = ifHasSubDir;
	}
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}
	
}
