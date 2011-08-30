package com.pintu.activity;

import java.util.List;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.Toast;

import com.pintu.task.GenericTask;
import com.pintu.task.TaskAdapter;
import com.pintu.task.TaskListener;
import com.pintu.task.TaskManager;
import com.pintu.task.TaskResult;
import com.pintu.util.DateTimeHelper;
import com.pintu.util.Preferences;

public abstract class TempletActivity extends FullScreenActivity {

	// Task
	protected GenericTask mSendTask;
	protected GenericTask mRetrieveTask;
	//管理当前视图内任务的销毁
	protected TaskManager taskManager = new TaskManager();

	//10分钟时间间隔
	protected long temMinutesMiliSeconds = 10*60*1000;
	
	//TODO, ----------------  模板Activity 生命周方法 -------------------------------

		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// 设置布局
			setContentView(getLayout());
			// 获得组件引用
			getViews();
			// 添加交互动作
			addEventListeners();
			// 初始化动作，取数据或者为当前视图填充内容
			justDoIt();
		}
		
		protected void onResume(){
			super.onResume();
			doItLater();
		}

		protected void onDestroy() {
			super.onDestroy();
			taskManager.cancelAll();
		}

	
//TODO, ------------ 子类必须实现的15个模板方法 ---------------------------------
	
	// 创建活动相关方法
	protected abstract int getLayout();
	//获得视图引用
	protected abstract void getViews();
	//添加交互
	protected abstract void addEventListeners();
	//初始化的动作
	protected abstract void justDoIt();
	//延后动作
	protected abstract void doItLater();
	// 发送请求
	protected abstract void doSend();
	// 请求开始
	protected abstract void onSendBegin();
	// 发送成功
	protected abstract void onSendSuccess();
	// 发送失败
	protected abstract void onSendFailure();	
	// 查询请求
	protected abstract void doRetrieve();
	//开始取数据
	protected abstract void onRetrieveBegin();
	//取数据成功
	protected abstract void onRetrieveSuccess();
	//取数据失败
	protected abstract void onRetrieveFailure();
	//解析JSON结果出错
	protected abstract void onParseJSONResultFailue();
	//根据列表数据更新列表视图
	protected abstract void refreshListView(List<Object> results);
	//根据JSON对象更新视图内容
	protected abstract void refreshMultView(JSONObject json);


	protected TaskListener mSendTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onSendBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onSendSuccess();
			} else if (result == TaskResult.FAILED) {
				onSendFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onSendFailure();
			}
		}

	};


	protected TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onRetrieveSuccess();
			} else if (result == TaskResult.FAILED) {
				onRetrieveFailure();
			} else if (result == TaskResult.IO_ERROR) {
				onRetrieveFailure();
			} else if (result == TaskResult.JSON_PARSE_ERROR) {
				onParseJSONResultFailue();
			}
		}

		public void deliverRetrievedList(List<Object> results) {
			refreshListView(results);
		}

		public void deliveryResponseJson(JSONObject json) {
			refreshMultView(json);
		}

	};

	protected void checkTaskStatus() {

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

	}
	
	protected void manageTask(GenericTask task){
		if(task!=null)
			taskManager.addTask(task);
	}

	protected void updateProgress(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	protected void updateProgress(int message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
    protected void rememberLastVisit(){
    	long existTime = DateTimeHelper.getNowTime();
    	this.getPreferences().edit().putLong(Preferences.LAST_VISIT_TIME, existTime).commit();
    }


}