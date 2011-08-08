package com.pintu.task;

import java.util.List;

public abstract class TaskAdapter implements TaskListener {

    public void onPreExecute(GenericTask task) {};
    public void onPostExecute(GenericTask task, TaskResult result) {};
    public void onProgressUpdate(GenericTask task, Object param) {};
    public void onCancelled(GenericTask task) {};    
    //新加的返回泛型数据的回调方法，被onPostExecute执行，实例要重载来处理
    public  void deliverRetreivedList(List<Object> results){};

}
