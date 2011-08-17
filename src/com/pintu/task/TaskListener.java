package com.pintu.task;

import java.util.List;

import org.json.JSONObject;

public interface TaskListener {
	String getName();
	
	void onPreExecute(GenericTask task);
	void onPostExecute(GenericTask task, TaskResult result);
	void onProgressUpdate(GenericTask task, Object param);
	void onCancelled(GenericTask task);
	//添加一个返回列表数据的接口
	//lwz7512 @ 2011/08/08
	void deliverRetreivedList(List<Object> results);
	//添加一个服务端返回字符串的接口，这个比较常用
	//lwz7512 @ 2011/08/10
	void deliverResponseString(String response);
	//添加一个服务端返回JSON对象的接口
	//lwz7512 @ 2011/08/10
	void deliveryResponseJson(JSONObject json);
	
}
