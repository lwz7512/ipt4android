package com.pintu.task;

import java.util.List;

import org.json.JSONObject;

public abstract class TaskAdapter implements TaskListener {

	/**
	 * 状态处理方法
	 */
    public void onPreExecute(GenericTask task) {};
    public void onPostExecute(GenericTask task, TaskResult result) {};
    public void onProgressUpdate(GenericTask task, Object param) {};
    public void onCancelled(GenericTask task) {};    
    
    /**
     * 数据处理方法
     */
    
    //新加的返回泛型数据的回调方法，被onPostExecute执行，实例要重载来处理
    public  void deliverRetrievedList(List<Object> results){};
    //新加的返回字符串的回调方法，以方便得到服务端返回字符串
    public void deliverResponseString(String response){};
    //处理服务端返回JSON对象的方法
    public void deliveryResponseJson(JSONObject json){};
}
