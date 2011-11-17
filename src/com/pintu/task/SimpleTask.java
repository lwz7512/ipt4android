package com.pintu.task;

import android.util.Log;

import com.pintu.PintuApp;
import com.pintu.http.HttpException;

public class SimpleTask extends GenericTask {

	private static String TAG = "SimpleTask";
	private String response;
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		TaskParams param = params[0];		
		try {
			String method = param.getString("method");
			String name = param.getString("name");
			String value = param.getString("value");
			response = PintuApp.mApi.commonQuery(method, name, value);
		} catch (HttpException e) {			
			e.printStackTrace();
			return TaskResult.FAILED;
		}
		return TaskResult.OK;
	}

	@Override
	protected void _onPostExecute(TaskResult result) {
		if(result==TaskResult.OK){
			if(this.getListener()!=null && response!=null){
    			//回调监听方法传结果
    			this.getListener().deliverResponseString(response.trim());
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved stories is null!");
    		}
		}

	}

}
