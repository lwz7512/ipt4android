package com.pintu.task;

import java.util.List;

public interface TaskListener {
	String getName();
	
	void onPreExecute(GenericTask task);
	void onPostExecute(GenericTask task, TaskResult result);
	void onProgressUpdate(GenericTask task, Object param);
	void onCancelled(GenericTask task);
	//添加一个返回列表数据的接口
	//lwz7512 @ 2011/08/08
	void deliverRetreivedList(List<Object> results);
}
