package com.hearace.cloudfile.baidu;

public class QuotaInfo {
	
	private final String[] UNIT = {"B", "KB","MB","GB","TB"};
	private long used = -1;
	private long quota = -1;
	
	private String usedString = null;
	private String quotaString = null;
	public long getUsed() {
		return used;
	}
	public void setUsed(long used) {
		this.used = used;
		usedString = formatString(used);
	}
	public long getQuota() {
		return quota;
	}
	public void setQuota(long quota) {
		this.quota = quota;
		quotaString = formatString(quota);
	}
	
	private String formatString(long number) {
		double temp = number;
		int u = 0;
		while(temp > 1024 && u < 5){
			temp = temp/1024;
			u++;
		}
		return String.format("%1$.2f%2$s", temp, UNIT[u]);
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Used:"+usedString+"; Quota:"+quotaString+"; Usage:"+String.format("%1$.2f%%", used*100.0/quota);
	}
}
