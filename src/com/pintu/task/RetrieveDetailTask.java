package com.pintu.task;

import org.json.JSONException;
import org.json.JSONObject;

import com.pintu.PintuApp;
import com.pintu.api.PTApi;
import com.pintu.http.HttpException;

import android.util.Log;

public class RetrieveDetailTask extends GenericTask {
	static final String TAG = "RetrieveDetailTask";
	
	private JSONObject picDetails; 
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
    	TaskParams param = params[0];
        //根据参数查询缩略图
    	String tpId = param.get("tpId").toString();
    	try {
    		picDetails = PintuApp.mApi.getPictureDetailsById(tpId);
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}
    	return TaskResult.OK;
	}

    protected void _onPostExecute(TaskResult result){
    	
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && picDetails!=null){
    			//回调监听方法传结果
    			this.getListener().deliveryResponseJson(picDetails);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or pic details  is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
    }

	
}
