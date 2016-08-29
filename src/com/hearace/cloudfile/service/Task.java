package com.hearace.cloudfile.service;

public interface Task {

	public void onComplete();
	public void onFailed(String msg);
}
