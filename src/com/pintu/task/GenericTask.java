package com.pintu.task;

import java.util.Observable;
import java.util.Observer;

import android.os.AsyncTask;
import android.util.Log;

public abstract class GenericTask extends
		AsyncTask<TaskParams, Object, TaskResult> implements Observer {
	private static final String TAG = "TaskManager";

	private TaskListener mListener = null;
	private Feedback mFeedback = null;
	private boolean isCancelable = true;

	//子类负责实现，一般来自执行请求
	abstract protected TaskResult _doInBackground(TaskParams... params);
	//子类专用来做返回数据解析，并传给TaskListener中的回调接口
	abstract protected void _onPostExecute(TaskResult result);

	public void setListener(TaskListener taskListener) {
		mListener = taskListener;
	}

	public TaskListener getListener() {
		return mListener;
	}

	public void doPublishProgress(Object... values) {
		super.publishProgress(values);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		if (mListener != null) {
			mListener.onCancelled(this);
		}
	}

	@Override
	protected void onPostExecute(TaskResult result) {
		super.onPostExecute(result);

		//该监听器不做数据方面的处理，只是操作状态和错误提示
		if (mListener != null) {
			mListener.onPostExecute(this, result);
		}
		
		//做数据解析，具体数据解析和更新视图
		//在该方法中操作相应的TaskListener接口方法
		//2011/08/25 added by lwz7512
		_onPostExecute(result);

		if (mFeedback != null) {
			mFeedback.success("");
		}

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mListener != null) {
			mListener.onPreExecute(this);
		}

		if (mFeedback != null) {
			mFeedback.start("");
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);

		if (mListener != null) {
			if (values != null && values.length > 0) {
				mListener.onProgressUpdate(this, values[0]);
			}
		}

		if (mFeedback != null) {
			mFeedback.update(values[0]);
		}
	}

	@Override
	protected TaskResult doInBackground(TaskParams... params) {
		TaskResult result = _doInBackground(params);
		if (mFeedback != null) {
			mFeedback.update(99);
		}
		return result;
	}

	public void update(Observable o, Object arg) {
		if (TaskManager.CANCEL_ALL == (Integer) arg && isCancelable) {
			if (getStatus() == GenericTask.Status.RUNNING) {
				cancel(true);
			}
		}
	}

	public void setCancelable(boolean flag) {
		isCancelable = flag;
	}

	public void setFeedback(Feedback feedback) {
		mFeedback = feedback;
	}
}
