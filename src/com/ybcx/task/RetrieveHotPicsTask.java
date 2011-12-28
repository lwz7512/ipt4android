package com.ybcx.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.ybcx.PintuApp;
import com.ybcx.data.TPicDetails;
import com.ybcx.http.HttpException;

public class RetrieveHotPicsTask extends GenericTask {

	private static String TAG = "RetrieveHotPicsTask";	
	
	private List<Object> retrievedHotpics;
	
	@Override
	protected TaskResult _doInBackground(TaskParams... params) {
		JSONArray jsPics = null;
		TaskParams param = params[0];
		try {
			if(param.has("method")){
				//查询经典作品
				jsPics = PintuApp.mApi.getClassicPics();
			}else{
				//没有指定参数就是查询热图
				jsPics = PintuApp.mApi.getHotPicToday();
			}
		} catch (HttpException e) {
			e.printStackTrace();
			return TaskResult.FAILED;
		} catch (JSONException e) {
			e.printStackTrace();
			return TaskResult.JSON_PARSE_ERROR;
		}

		if(jsPics!=null){
			jsonPicToDetails(jsPics);			
		}else{
			return TaskResult.FAILED;
		}

		return TaskResult.OK;
	}

	private void jsonPicToDetails(JSONArray jsPics) {
		retrievedHotpics = new ArrayList<Object>();
		try {
			for (int i = 0; i < jsPics.length(); i++) {
				TPicDetails tpic = TPicDetails.parseJsonToObj(jsPics
						.getJSONObject(i));
				retrievedHotpics.add(tpic);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void _onPostExecute(TaskResult result) {
    	if(result==TaskResult.OK){
    		if(this.getListener()!=null && retrievedHotpics!=null){
    			//回调监听方法传结果
    			this.getListener().deliverRetrievedList(retrievedHotpics);
    		}else{
    			//listener is null or retrieved pics is null!
    			Log.d(TAG, "listener is null or retrieved hotpics is null!");
    		}
    	}else{
    		//ERROR!
    		Log.d(TAG, "Fetching  data ERROR!");
    	}
	}

}
